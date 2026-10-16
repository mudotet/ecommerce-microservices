package com.mudotet.ecommerce.product.consumer.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedItem {
  private UUID productVariantId;
  private Integer quantity;
}
