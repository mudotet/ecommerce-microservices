package com.mudotet.ecommerce.product.entity;

import com.mudotet.ecommerce.product.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {
  @Column(name = "name", nullable = false)
  String name;

  @Column(name = "quantity", nullable = false)
  Integer quantity;

  @JoinColumn(name = "product_variant_id", nullable = false)
  @OneToOne(fetch = FetchType.LAZY)
  ProductVariant productVariant;
}
