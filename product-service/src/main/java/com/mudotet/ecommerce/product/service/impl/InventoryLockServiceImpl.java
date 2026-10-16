package com.mudotet.ecommerce.product.service.impl;

import com.mudotet.ecommerce.product.common.exception.BusinessException;
import com.mudotet.ecommerce.product.dto.request.LockProductRequest;
import com.mudotet.ecommerce.product.dto.request.LockProductItem;
import com.mudotet.ecommerce.product.entity.ProcessedOrder;
import com.mudotet.ecommerce.product.repository.InventoryRepository;
import com.mudotet.ecommerce.product.repository.ProcessedOrderRepository;
import com.mudotet.ecommerce.product.service.InventoryLockService;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryLockServiceImpl implements InventoryLockService {
  private final InventoryRepository inventoryRepository;
  private final ProcessedOrderRepository processedOrderRepository;
  private final RedissonClient redissonClient;

  @Override
  @Transactional
  public ProcessedOrder lockProducts(LockProductRequest request) {
    if (request == null
        || request.getOrderId() == null
        || request.getItems() == null
        || request.getItems().isEmpty()) {
      throw new BusinessException("Order ID and items are required");
    }
    var items = request.getItems();
    var quantities = new HashMap<UUID, Integer>();
    for (var item : items) {
      if (item == null
          || item.getProductVariantId() == null
          || item.getQuantity() == null
          || item.getQuantity() <= 0
          || quantities.putIfAbsent(item.getProductVariantId(), item.getQuantity()) != null) {
        throw new BusinessException("Each variant must appear once with a positive quantity");
      }
    }

    var sortedIds = items.stream().map(LockProductItem::getProductVariantId).sorted().toList();
    String lockKey =
        "lock:products:" + sortedIds.stream().map(UUID::toString).collect(Collectors.joining(","));
    RLock lock = redissonClient.getLock(lockKey);

    try {
      if (!lock.tryLock(10, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Timed out waiting for product lock");
      }

      var previous = processedOrderRepository.findById(request.getOrderId());
      if (previous.isPresent()) {
        return previous.get();
      }
      var result = new ProcessedOrder(request.getOrderId());

      processedOrderRepository.save(result);

      var inventories = inventoryRepository.findAllForUpdate(sortedIds);
      if (inventories.size() != quantities.size()) {
        result.reject("Inventory not found for one or more variants");
        return result;
      }
      // Check every item before changing any quantity.
      for (var inventory : inventories) {
        if (inventory.getQuantity() < quantities.get(inventory.getProductVariant().getId())) {
          result.reject("Insufficient stock");
          return result;
        }
      }
      for (var inventory : inventories) {
        inventory.setQuantity(
            inventory.getQuantity() - quantities.get(inventory.getProductVariant().getId()));
      }
      result.succeed();
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for inventory locks", e);
    } catch (RuntimeException e) {
      log.error("Failed to lock products for order {}", request.getOrderId(), e);
      throw e;
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}
