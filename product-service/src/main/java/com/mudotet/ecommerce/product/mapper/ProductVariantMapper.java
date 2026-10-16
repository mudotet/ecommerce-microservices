package com.mudotet.ecommerce.product.mapper;

import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.entity.ProductVariant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
  ProductVariant toEntity(AddProductRequest request);
}
