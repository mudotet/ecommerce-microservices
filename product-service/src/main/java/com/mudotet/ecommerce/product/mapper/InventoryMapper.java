package com.mudotet.ecommerce.product.mapper;

import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.entity.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
  Inventory toEntity(AddProductRequest request);
}
