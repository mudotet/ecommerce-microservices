package com.mudotet.ecommerce.product.mapper;

import com.mudotet.ecommerce.product.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  Category toEntity(String categoryName);
}
