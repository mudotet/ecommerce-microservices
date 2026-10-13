package com.mudotet.productservice.service;

import com.mudotet.productservice.common.exception.BusinessException;
import com.mudotet.productservice.dto.request.LockProductReq;
import com.mudotet.productservice.entity.ProcessedOrder;
import com.mudotet.productservice.repository.InventoryRepository;
import com.mudotet.productservice.repository.ProcessedOrderRepository;
import java.util.HashMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryLockService {
  private final InventoryRepository inventoryRepository;
  private final ProcessedOrderRepository processedOrderRepository;

  @Transactional
  public ProcessedOrder lockProducts(LockProductReq request) {
    if (request == null || request.getOrderId() == null || request.getItems() == null || request.getItems().isEmpty()) {
      throw new BusinessException("Order ID and items are required");
    }
    var quantities = new HashMap<UUID, Integer>();
    for (var item : request.getItems()) {
      if (item == null || item.getProductVariantId() == null || item.getQuantity() == null || item.getQuantity() <= 0
          || quantities.putIfAbsent(item.getProductVariantId(), item.getQuantity()) != null) {
        throw new BusinessException("Each variant must appear once with a positive quantity");
      }
    }

    var previous = processedOrderRepository.findById(request.getOrderId());
    if (previous.isPresent()) {
      return previous.get();
    }
    var result = new ProcessedOrder(request.getOrderId());
    // The unique order ID claims this event before any stock can change.
    // A concurrent duplicate rolls back here and succeeds on Kafka's retry.
    processedOrderRepository.saveAndFlush(result);

    var inventories = inventoryRepository.findAllForUpdate(quantities.keySet().stream().sorted().toList());
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
      inventory.setQuantity(inventory.getQuantity() - quantities.get(inventory.getProductVariant().getId()));
    }
    result.succeed();
    return result;
  }
}
