package com.example.order_services.service;

import java.util.UUID;

public interface OrderInventoryService {
    void prepare(UUID orderId);
    void reject(UUID orderId, String reason);
}
