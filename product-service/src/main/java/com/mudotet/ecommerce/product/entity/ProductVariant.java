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
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {
  @Column(name = "variant_name", nullable = false)
  String variantName;

  @Column(name = "price", nullable = false)
  Double price;

  @Column(name = "description", nullable = false)
  String description;

  @JoinColumn(name = "product_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  Product product;
}
