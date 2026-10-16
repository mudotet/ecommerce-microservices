package com.mudotet.ecommerce.order.common.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BaseResponse<T> {

    private final T data;
    private final String message;
    private final Instant timestamp;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data, "Success", Instant.now());
    }
}
