package com.mudotet.ecommerce.order.controller;

import com.mudotet.ecommerce.order.dto.request.AddOrderRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import com.mudotet.ecommerce.order.common.dto.BaseResponse;
import com.mudotet.ecommerce.order.dto.response.OrderResponse;
import com.mudotet.ecommerce.order.service.OrderService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  public BaseResponse<OrderResponse> createOrder(@Valid @RequestBody AddOrderRequest request) {
    log.info("Received request to create order: {}", request);
    return orderService.createOrder(request);
  }

  @GetMapping("/health")
  public String healthCheck() {
    return "Order Service is up and running!";
  }

  @GetMapping("/{orderId}")
  public BaseResponse<OrderResponse> getOrderById(@PathVariable UUID orderId) {
    return orderService.getOrderById(orderId);
  }
}
