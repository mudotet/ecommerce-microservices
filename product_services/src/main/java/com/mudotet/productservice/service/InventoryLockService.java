package com.mudotet.productservice.service;

import com.mudotet.productservice.dto.request.LockProductReq;
import com.mudotet.productservice.entity.ProcessedOrder;

public interface InventoryLockService {
  ProcessedOrder lockProducts(LockProductReq request);
}
