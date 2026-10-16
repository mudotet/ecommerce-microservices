package com.mudotet.ecommerce.product;

import com.mudotet.ecommerce.product.common.exception.BusinessException;
import com.mudotet.ecommerce.product.dto.request.LockProductItem;
import com.mudotet.ecommerce.product.dto.request.LockProductRequest;
import com.mudotet.ecommerce.product.entity.Inventory;
import com.mudotet.ecommerce.product.entity.ProcessedOrder;
import com.mudotet.ecommerce.product.entity.ProductVariant;
import com.mudotet.ecommerce.product.repository.InventoryRepository;
import com.mudotet.ecommerce.product.repository.ProcessedOrderRepository;
import com.mudotet.ecommerce.product.service.InventoryLockService;
import com.mudotet.ecommerce.product.service.impl.InventoryLockServiceImpl;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import java.util.concurrent.TimeUnit;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLockTests {
  private final InventoryRepository inventories = mock(InventoryRepository.class);
  private final ProcessedOrderRepository results = mock(ProcessedOrderRepository.class);
  private final RedissonClient redisson = InventoryTestLocks.client();
  private final InventoryLockService service = new InventoryLockServiceImpl(inventories, results, redisson);

  @Test
  void deductsAllRequestedStock() {
    var a = inventory(5);
    var b = inventory(3);
    when(inventories.findAllForUpdate(anyList())).thenReturn(List.of(a, b));
    var result = service.lockProducts(request(a, 2, b, 3));
    assertTrue(result.isSuccess());
    assertEquals(3, a.getQuantity());
    assertEquals(0, b.getQuantity());
  }

  @Test
  void insufficientItemLeavesAllQuantitiesUnchanged() {
    var a = inventory(5);
    var b = inventory(1);
    when(inventories.findAllForUpdate(anyList())).thenReturn(List.of(a, b));
    var result = service.lockProducts(request(a, 2, b, 3));
    assertFalse(result.isSuccess());
    assertNotNull(result.getFailureReason());
    assertEquals(5, a.getQuantity());
    assertEquals(1, b.getQuantity());
  }

  @Test
  void missingInventoryRejectsWithoutDeductingOtherItems() {
    var a = inventory(5);
    var b = inventory(3);
    when(inventories.findAllForUpdate(anyList())).thenReturn(List.of(a));
    assertFalse(service.lockProducts(request(a, 2, b, 3)).isSuccess());
    assertEquals(5, a.getQuantity());
  }

  @Test
  void duplicateEventReturnsStoredResultWithoutDeduction() {
    var a = inventory(5);
    var b = inventory(3);
    var request = request(a, 2, b, 3);
    var previous = new ProcessedOrder(request.getOrderId());
    previous.succeed();
    when(results.findById(request.getOrderId())).thenReturn(Optional.of(previous));
    assertSame(previous, service.lockProducts(request));
    verifyNoInteractions(inventories);
    verify(results, never()).saveAndFlush(any());
  }

  @Test
  void rejectsDuplicateVariantsBeforeChangingStock() {
    var a = inventory(5);
    assertThrows(BusinessException.class, () -> service.lockProducts(request(a, 2, a, 2)));
    assertEquals(5, a.getQuantity());
    verifyNoInteractions(inventories, results);
  }

  @Test
  void missingOrderIdIsBusinessErrorBeforeDatabaseAccess() {
    assertThrows(BusinessException.class,
        () -> service.lockProducts(new LockProductRequest(null, List.of())));
    verifyNoInteractions(inventories, results);
  }

  @Test
  void zeroQuantityIsBusinessErrorBeforeDatabaseAccess() {
    var a = inventory(5);
    var b = inventory(3);
    assertThrows(BusinessException.class, () -> service.lockProducts(request(a, 0, b, 1)));
    verifyNoInteractions(inventories, results);
  }

  @Test
  void nullRequestIsRejectedBeforeAccessingRedis() {
    assertThrows(BusinessException.class, () -> service.lockProducts(null));
    verifyNoInteractions(redisson, inventories, results);
  }

  @Test
  void lockTimeoutLeavesStockUntouchedAndDoesNotUnlockAnotherOwner() throws Exception {
    var a = inventory(5);
    var b = inventory(3);
    var request = request(a, 2, b, 1);
    RLock blocked = mock(RLock.class);
    when(redisson.getLock(lockKey(request))).thenReturn(blocked);
    assertThrows(IllegalStateException.class, () -> service.lockProducts(request));
    assertEquals(5, a.getQuantity());
    assertEquals(3, b.getQuantity());
    verifyNoInteractions(inventories, results);
    verify(blocked, never()).unlock();
  }

  @Test
  void interruptionRestoresInterruptFlagAndDoesNotChangeStock() throws Exception {
    var a = inventory(5);
    var request = request(a, 2, inventory(3), 1);
    RLock interrupted = mock(RLock.class);
    when(redisson.getLock(lockKey(request))).thenReturn(interrupted);
    when(interrupted.tryLock(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
    try {
      assertThrows(IllegalStateException.class, () -> service.lockProducts(request));
      assertTrue(Thread.currentThread().isInterrupted());
      assertEquals(5, a.getQuantity());
      verifyNoInteractions(inventories, results);
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void databaseFailureReleasesLock() {
    var a = inventory(5);
    var b = inventory(3);
    var request = request(a, 2, b, 1);
    when(results.findById(request.getOrderId())).thenThrow(new IllegalStateException("Database unavailable"));
    assertThrows(IllegalStateException.class, () -> service.lockProducts(request));
    assertEquals(5, a.getQuantity());
    assertFalse(redisson.getLock(lockKey(request)).isHeldByCurrentThread());
  }

  @Test
  void successfulRequestReleasesLockInFinally() {
    var a = inventory(5);
    var b = inventory(3);
    var request = request(a, 2, b, 1);
    when(inventories.findAllForUpdate(anyList())).thenReturn(List.of(a, b));
    assertTrue(service.lockProducts(request).isSuccess());
    assertEquals(3, a.getQuantity());
    assertFalse(redisson.getLock(lockKey(request)).isHeldByCurrentThread());
  }

  private String lockKey(LockProductRequest request) {
    return "lock:products:" + request.getItems().stream()
        .map(LockProductItem::getProductVariantId).sorted().map(UUID::toString)
        .collect(java.util.stream.Collectors.joining(","));
  }

  private Inventory inventory(int quantity) {
    var variant = new ProductVariant();
    ReflectionTestUtils.setField(variant, "id", UUID.randomUUID());
    return Inventory.builder().productVariant(variant).quantity(quantity).name("Stock").build();
  }

  private LockProductRequest request(Inventory a, int qa, Inventory b, int qb) {
    return new LockProductRequest(UUID.randomUUID(), List.of(
        new LockProductItem(a.getProductVariant().getId(), qa),
        new LockProductItem(b.getProductVariant().getId(), qb)));
  }
}
