package com.example.order_services.service.impl;

import com.example.order_services.repository.OrderRepository;
import com.example.order_services.service.OrderInventoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderInventoryImpl implements OrderInventoryService {
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void prepare(UUID orderId) {
        var order = orderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        order.prepare();
    }

    @Override
    @Transactional
    public void reject(UUID orderId, String reason) {
        var order = orderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        order.reject(reason);
    }
}
