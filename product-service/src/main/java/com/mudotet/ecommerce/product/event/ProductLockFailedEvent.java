package com.mudotet.ecommerce.product.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductLockFailedEvent {
  @JsonProperty("order_id")
  private UUID orderId;
  private String failureReason;
}
