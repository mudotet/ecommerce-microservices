package com.mudotet.productservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddProductRequest {
  @NotNull String productName;
  @NotNull UUID categoryId;
  @NotNull String variantName;
  @NotNull Double price;
  @NotNull String description;
  @NotNull Integer quantity;
  @NotNull String inventoryName;
}
