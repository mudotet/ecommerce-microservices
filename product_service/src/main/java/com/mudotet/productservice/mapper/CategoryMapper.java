package com.mudotet.productservice.mapper;

import com.mudotet.productservice.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  Category toEntity(String categoryName);
}
