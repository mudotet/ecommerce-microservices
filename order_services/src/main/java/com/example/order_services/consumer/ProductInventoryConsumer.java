package com.example.order_services.consumer;

import com.example.order_services.consumer.dto.ProductLockedEvent;
import com.example.order_services.consumer.dto.ProductLockFailedEvent;
import com.example.order_services.service.OrderInventoryService;
import lombok.RequiredArgsConstructor;
import com.example.order_services.common.exception.BusinessException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductInventoryConsumer {
    private final OrderInventoryService orderInventoryService;


    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            exclude = {BusinessException.class}
    )
    @KafkaListener(topics = "product_locked")
    public void onProductLocked(ProductLockedEvent event) {
        orderInventoryService.prepare(event.getOrderId());
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            exclude = {BusinessException.class}
    )
    @KafkaListener(topics = "product_lock_failed")
    public void onProductLockFailed(ProductLockFailedEvent event) {
        orderInventoryService.reject(event.getOrderId(), event.getFailureReason());
    }
}
