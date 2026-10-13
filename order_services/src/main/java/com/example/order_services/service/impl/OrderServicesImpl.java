package com.example.order_services.service.impl;

import com.example.order_services.clients.ProductClient;
import com.example.order_services.dto.request.AddOrderRequest;
import com.example.order_services.dto.request.OrderItemRequest;
import com.example.order_services.entity.Order;
import com.example.order_services.entity.OrderItem;
import com.example.order_services.common.ErrorCode;
import com.example.order_services.common.dto.BaseResponse;
import com.example.order_services.dto.response.OrderResponse;
import com.example.order_services.mapper.OrderMapper;
import com.example.order_services.repository.OrderRepository;
import com.example.order_services.service.OrderServices;

import java.util.HashMap;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderServicesImpl implements OrderServices {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public BaseResponse<OrderResponse> createOrder(AddOrderRequest request) {
        var quantities = new HashMap<UUID, Integer>();
        for (var item : request.getItems()) {
            if (quantities.putIfAbsent(item.getProductVariantId(), item.getQuantity()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate product variant");
            }
        }
        var variantIds = request.getItems().stream().map(OrderItemRequest::getProductVariantId).toList();
        var products = productClient.getProductItems(variantIds);
        var order = new Order();
        for (var product : products) {
            if (product == null || !quantities.containsKey(product.getProductVariantId())
                    || product.getUnitPrice() == null || product.getUnitPrice().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid product response");
            }
            var item = new OrderItem();
            item.setProductVariantId(product.getProductVariantId());
            item.setQuantity(quantities.remove(product.getProductVariantId()));
            item.setUnitPrice(product.getUnitPrice());
            order.addItem(item);
        }
        if (!quantities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Missing product response");
        }
        Order orderCreated = orderRepository.save(order);
        var event = orderMapper.toEvent(orderCreated);
        kafkaTemplate.send("order_created", event);
        OrderResponse orderResponse = orderMapper.toResponse(orderCreated);
        return BaseResponse.success(orderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<OrderResponse> getOrderById(UUID orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND.getMessage()));
        return BaseResponse.success(orderMapper.toResponse(order));
    }
}
