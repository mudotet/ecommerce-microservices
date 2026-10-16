package com.mudotet.ecommerce.order.dto.request;

import com.mudotet.ecommerce.order.common.dto.BaseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddOrderRequest extends BaseRequest {
    @NotEmpty
    private List<@NotNull @Valid OrderItemRequest> items;
}
