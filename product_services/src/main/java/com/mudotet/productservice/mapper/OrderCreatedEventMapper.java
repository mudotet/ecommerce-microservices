package com.mudotet.productservice.mapper;

import com.mudotet.productservice.consumer.dto.OrderCreatedEvent;
import com.mudotet.productservice.dto.request.LockProductItem;
import com.mudotet.productservice.dto.request.LockProductReq;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventMapper {
  public LockProductReq toRequest(OrderCreatedEvent event) {
    var items = event.getItems().stream()
        .map(item -> new LockProductItem(item.getProductVariantId(), item.getQuantity()))
        .toList();
    return new LockProductReq(event.getOrderId(), items);
  }
}
