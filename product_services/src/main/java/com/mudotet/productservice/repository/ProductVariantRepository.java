package com.mudotet.productservice.repository;

import com.mudotet.productservice.common.dto.BaseResponse;
import com.mudotet.productservice.dto.response.ProductResponse;
import com.mudotet.productservice.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
  @Query(
      """
    select new com.mudotet.productservice.dto.response.ProductResponse(
        pv.variantName, c.name, pv.price, pv.description)
    from ProductVariant pv
    join pv.product p
    join p.category c
    where pv.id = :productVariantId
    """)
  ProductResponse findProductVariantByIdToResponse(String productVariantId);

  @Query(
      """
          select new com.mudotet.productservice.dto.response.ProductResponse(
              pv.variantName, c.name, pv.price, pv.description)
          from ProductVariant pv
          join pv.product p
          join p.category c
      """)
  BaseResponse<List<ProductResponse>> findAllToResponse();
}
