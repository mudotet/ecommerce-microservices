package com.example.order_services.service;

import com.example.order_services.dto.request.AddOrderRequest;

import com.example.order_services.common.dto.BaseResponse;
import com.example.order_services.dto.response.OrderResponse;
import java.util.UUID;

public interface OrderServices {
    BaseResponse<OrderResponse> createOrder(AddOrderRequest request);
    BaseResponse<OrderResponse> getOrderById(UUID orderId);
}
