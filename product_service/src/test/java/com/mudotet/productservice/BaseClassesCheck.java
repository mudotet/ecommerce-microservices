package com.mudotet.productservice;

import com.mudotet.productservice.common.dto.BaseResponse;
import com.mudotet.productservice.common.entity.BaseEntity;
import java.time.LocalDateTime;

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
        entity.delete();
        LocalDateTime deletedAt = entity.getDeletedAt();
        assert entity.isDeleted();
        assert deletedAt != null;
        entity.delete();
        assert entity.getDeletedAt().equals(deletedAt);
    }
}
