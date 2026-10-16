package com.mudotet.ecommerce.product.dto.response;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDetailResponse implements Serializable {
  private UUID id;
  private String variantName;
  private Double price;
  private String description;
}
