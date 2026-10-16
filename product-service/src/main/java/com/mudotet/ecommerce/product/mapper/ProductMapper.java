package com.mudotet.ecommerce.product.mapper;

import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  Product toEntity(AddProductRequest request);
}
