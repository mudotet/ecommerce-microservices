package com.example.order_services.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {
    private UUID id;
    private UUID productVariantId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
