package com.example.order_services.mapper;

import com.example.order_services.dto.response.OrderResponse;
import com.example.order_services.dto.response.OrderItemResponse;
import com.example.order_services.entity.Order;
import com.example.order_services.entity.OrderItem;
import com.example.order_services.event.OrderCreatedEvent;
import com.example.order_services.event.OrderCreatedItem;
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
