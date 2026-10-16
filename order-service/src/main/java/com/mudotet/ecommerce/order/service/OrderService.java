package com.mudotet.ecommerce.order.service;

import com.mudotet.ecommerce.order.dto.request.AddOrderRequest;

import com.mudotet.ecommerce.order.common.dto.BaseResponse;
import com.mudotet.ecommerce.order.dto.response.OrderResponse;
import java.util.UUID;

public interface OrderService {
    BaseResponse<OrderResponse> createOrder(AddOrderRequest request);
    BaseResponse<OrderResponse> getOrderById(UUID orderId);
}
