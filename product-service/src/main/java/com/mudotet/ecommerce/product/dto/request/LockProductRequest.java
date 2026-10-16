package com.mudotet.ecommerce.product.dto.request;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LockProductRequest {
  private UUID orderId;
  private List<LockProductItem> items;
}
