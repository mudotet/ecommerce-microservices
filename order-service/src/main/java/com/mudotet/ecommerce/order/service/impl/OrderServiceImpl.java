package com.mudotet.ecommerce.order.service.impl;

import com.mudotet.ecommerce.order.clients.ProductClient;
import com.mudotet.ecommerce.order.dto.request.AddOrderRequest;
import com.mudotet.ecommerce.order.dto.request.OrderItemRequest;
import com.mudotet.ecommerce.order.entity.Order;
import com.mudotet.ecommerce.order.entity.OrderItem;
import com.mudotet.ecommerce.order.common.ErrorCode;
import com.mudotet.ecommerce.order.common.dto.BaseResponse;
import com.mudotet.ecommerce.order.dto.response.OrderResponse;
import com.mudotet.ecommerce.order.mapper.OrderMapper;
import com.mudotet.ecommerce.order.repository.OrderRepository;
import com.mudotet.ecommerce.order.service.OrderService;

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
public class OrderServiceImpl implements OrderService {
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
