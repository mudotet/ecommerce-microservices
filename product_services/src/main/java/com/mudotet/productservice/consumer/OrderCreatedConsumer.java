package com.mudotet.productservice.consumer;

import com.mudotet.productservice.consumer.dto.OrderCreatedEvent;
import com.mudotet.productservice.event.ProductLockedEvent;
import com.mudotet.productservice.event.ProductLockFailedEvent;
import com.mudotet.productservice.mapper.OrderCreatedEventMapper;
import com.mudotet.productservice.service.InventoryLockService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import com.mudotet.productservice.common.exception.BusinessException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {
  private final OrderCreatedEventMapper eventMapper;
  private final InventoryLockService inventoryLockService;
  private final KafkaTemplate<String, Object> kafkaTemplate;


  @RetryableTopic(
          attempts = "3",
          backOff = @BackOff(delay = 1000, multiplier = 2.0),
          exclude = {BusinessException.class}
  )
  @KafkaListener(topics = "order_created")
  public void onOrderCreated(OrderCreatedEvent event) throws Exception {
    // This service call commits inventory and the processing result before publishing.
    var result = inventoryLockService.lockProducts(eventMapper.toRequest(event));
    if (result.isSuccess()) {
      kafkaTemplate.send("product_locked",
          new ProductLockedEvent(event.getOrderId())).get(10, TimeUnit.SECONDS);
    } else {
      kafkaTemplate.send("product_lock_failed",
          new ProductLockFailedEvent(event.getOrderId(), result.getFailureReason())).get(10, TimeUnit.SECONDS);
    }
  }
}
