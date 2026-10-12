package com.mudotet.productservice.consumer;

import com.mudotet.productservice.consumer.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedConsumer {
    @KafkaListener(topics = "order_created")
    public void HandleOrderCreatedEvent(Order order){
        log.info(order.toString());
    }

}
