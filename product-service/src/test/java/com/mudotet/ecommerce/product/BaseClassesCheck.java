package com.mudotet.ecommerce.product;

import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.common.entity.BaseEntity;

public class BaseClassesCheck {

    public static void main(String[] args) {
        BaseResponse<String> response = BaseResponse.success("ok");
        assert response.getData().equals("ok");
        assert response.getTimestamp() != null;

        BaseResponse<String> built = BaseResponse.<String>builder()
                .data("built")
                .message("Success")
                .timestamp(response.getTimestamp())
                .build();
        assert built.getData().equals("built");

        BaseEntity entity = new BaseEntity() { };
        assert entity.getId() == null;
        assert entity.getCreatedAt() == null;
        assert entity.getUpdatedAt() == null;
        assert entity.getDeletedAt() == null;
    }
}
