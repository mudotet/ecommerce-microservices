package com.mudotet.ecommerce.product.service;

import com.mudotet.ecommerce.product.dto.request.LockProductRequest;
import com.mudotet.ecommerce.product.entity.ProcessedOrder;

public interface InventoryLockService {
  ProcessedOrder lockProducts(LockProductRequest request);
}
