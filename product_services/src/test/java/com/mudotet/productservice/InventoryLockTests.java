package com.mudotet.productservice;

import com.mudotet.productservice.common.exception.BusinessException;
import com.mudotet.productservice.dto.request.LockProductItem;
import com.mudotet.productservice.dto.request.LockProductReq;
import com.mudotet.productservice.entity.Inventory;
import com.mudotet.productservice.entity.ProcessedOrder;
import com.mudotet.productservice.entity.ProductVariant;
import com.mudotet.productservice.repository.InventoryRepository;
import com.mudotet.productservice.repository.ProcessedOrderRepository;
import com.mudotet.productservice.service.InventoryLockService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLockTests {
  private final InventoryRepository inventories = mock(InventoryRepository.class);
  private final ProcessedOrderRepository results = mock(ProcessedOrderRepository.class);
  private final InventoryLockService service = new InventoryLockService(inventories, results);

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
        () -> service.lockProducts(new LockProductReq(null, List.of())));
    verifyNoInteractions(inventories, results);
  }

  @Test
  void zeroQuantityIsBusinessErrorBeforeDatabaseAccess() {
    var a = inventory(5);
    var b = inventory(3);
    assertThrows(BusinessException.class, () -> service.lockProducts(request(a, 0, b, 1)));
    verifyNoInteractions(inventories, results);
  }

  private Inventory inventory(int quantity) {
    var variant = new ProductVariant();
    ReflectionTestUtils.setField(variant, "id", UUID.randomUUID());
    return Inventory.builder().productVariant(variant).quantity(quantity).name("Stock").build();
  }

  private LockProductReq request(Inventory a, int qa, Inventory b, int qb) {
    return new LockProductReq(UUID.randomUUID(), List.of(
        new LockProductItem(a.getProductVariant().getId(), qa),
        new LockProductItem(b.getProductVariant().getId(), qb)));
  }
}
