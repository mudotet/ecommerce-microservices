package com.example.order_services.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    @NotNull
    private UUID productVariantId;

    @NotNull
    @Positive
    private Integer quantity;
}
