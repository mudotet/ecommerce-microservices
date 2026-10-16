package com.mudotet.ecommerce.order.dto.response;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.mudotet.ecommerce.order.entity.OrderStatus;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponse {
    private UUID id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String failureReason;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
