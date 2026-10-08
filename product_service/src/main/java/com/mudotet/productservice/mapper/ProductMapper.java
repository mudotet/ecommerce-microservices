package com.mudotet.productservice.mapper;

import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  Product toEntity(AddProductRequest request);
}
