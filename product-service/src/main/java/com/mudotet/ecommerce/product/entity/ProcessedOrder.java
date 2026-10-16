package com.mudotet.ecommerce.product.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "processed_orders")
public class ProcessedOrder {
  @Id
  @Column(name = "order_id", nullable = false, updatable = false)
  private UUID orderId;

  // A null version makes Spring Data insert this assigned ID instead of merging it.
  @Version
  private Long version;

  @Column(nullable = false)
  private boolean success;

  @Column(name = "failure_reason")
  private String failureReason;

  public ProcessedOrder(UUID orderId) {
    this.orderId = orderId;
  }

  public void succeed() {
    success = true;
  }

  public void reject(String reason) {
    failureReason = reason;
  }
}
