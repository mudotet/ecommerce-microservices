package com.mudotet.ecommerce.order.mapper;

import com.mudotet.ecommerce.order.dto.response.OrderResponse;
import com.mudotet.ecommerce.order.dto.response.OrderItemResponse;
import com.mudotet.ecommerce.order.entity.Order;
import com.mudotet.ecommerce.order.entity.OrderItem;
import com.mudotet.ecommerce.order.event.OrderCreatedEvent;
import com.mudotet.ecommerce.order.event.OrderCreatedItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {
    OrderResponse toResponse(Order order);

    OrderItemResponse toResponse(OrderItem item);

    @Mapping(source = "id", target = "orderId")
    OrderCreatedEvent toEvent(Order order);

    OrderCreatedItem toEventItem(OrderItem item);
}
