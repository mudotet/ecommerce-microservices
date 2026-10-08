package com.mudotet.productservice.mapper;

import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.entity.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
  Inventory toEntity(AddProductRequest request);
}
