package com.example.order_services.controller;

import com.example.order_services.dto.request.AddOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import com.example.order_services.common.dto.BaseResponse;
import com.example.order_services.dto.response.OrderResponse;
import com.example.order_services.service.OrderServices;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderServices orderServices;

    @PostMapping
    public BaseResponse<OrderResponse> createOrder(
            @Valid @RequestBody AddOrderRequest request) {
        return orderServices.createOrder(request);
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Order Service is up and running!";
    }

    @GetMapping("/{orderId}")
    public BaseResponse<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        return orderServices.getOrderById(orderId);
    }
}
