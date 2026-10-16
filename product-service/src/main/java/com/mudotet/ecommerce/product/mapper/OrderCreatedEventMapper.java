package com.mudotet.ecommerce.product.mapper;

import com.mudotet.ecommerce.product.consumer.dto.OrderCreatedEvent;
import com.mudotet.ecommerce.product.dto.request.LockProductItem;
import com.mudotet.ecommerce.product.dto.request.LockProductRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventMapper {
  public LockProductRequest toRequest(OrderCreatedEvent event) {
    var items = event.getItems().stream()
        .map(item -> new LockProductItem(item.getProductVariantId(), item.getQuantity()))
        .toList();
    return new LockProductRequest(event.getOrderId(), items);
  }
}
