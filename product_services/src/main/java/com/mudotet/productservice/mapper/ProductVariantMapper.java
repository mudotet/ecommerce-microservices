package com.mudotet.productservice.mapper;

import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.entity.ProductVariant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
  ProductVariant toEntity(AddProductRequest request);
}
