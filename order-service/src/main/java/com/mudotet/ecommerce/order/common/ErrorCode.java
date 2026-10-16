package com.mudotet.ecommerce.order.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ORDER_NOT_FOUND("Order not found");

    private final String message;
}
