package com.mudotet.productservice.service.impl;

import com.mudotet.productservice.common.ErrorCode;
import com.mudotet.productservice.common.dto.BaseResponse;
import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.dto.response.ProductResponse;
import com.mudotet.productservice.entity.Category;
import com.mudotet.productservice.entity.Inventory;
import com.mudotet.productservice.entity.Product;
import com.mudotet.productservice.entity.ProductVariant;
import com.mudotet.productservice.mapper.CategoryMapper;
import com.mudotet.productservice.mapper.InventoryMapper;
import com.mudotet.productservice.mapper.ProductMapper;
import com.mudotet.productservice.mapper.ProductVariantMapper;
import com.mudotet.productservice.repository.CategoryRepository;
import com.mudotet.productservice.repository.InventoryRepository;
import com.mudotet.productservice.repository.ProductRepository;
import com.mudotet.productservice.repository.ProductVariantRepository;
import com.mudotet.productservice.service.ProductServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServicesImpl implements ProductServices {
  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;
  private final ProductVariantRepository productVariantRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;
  private final InventoryMapper inventoryMapper;
  private final ProductVariantMapper productVariantMapper;
  private final CategoryMapper categoryMapper;

  @Override
  @Transactional
  public BaseResponse<Boolean> addProduct(AddProductRequest request) {
    if (productRepository.existsByName(request.getProductName())) {
      return BaseResponse.<Boolean>builder()
          .data(false)
          .message(ErrorCode.PRODUCT_ALREADY_EXISTS.getMessage())
          .timestamp(Instant.now())
          .build();
    }

    if (!categoryRepository.existsById(request.getCategoryId())) {
      return BaseResponse.<Boolean>builder()
          .data(false)
          .message(ErrorCode.CATEGORY_NOT_FOUND.getMessage())
          .timestamp(Instant.now())
          .build();
    }

    Product product = productMapper.toEntity(request);
    productRepository.save(product);

    ProductVariant productVariant = productVariantMapper.toEntity(request);
    productVariantRepository.save(productVariant);

    Inventory inventory = inventoryMapper.toEntity(request);
    inventoryRepository.save(inventory);

    return BaseResponse.<Boolean>builder()
        .data(true)
        .message("Product added successfully")
        .timestamp(Instant.now())
        .build();
  }

  @Override
  public BaseResponse<ProductResponse> getProductById(String productVariantId) {
    ProductResponse productVariant =
        productVariantRepository.findProductVariantByIdToResponse(productVariantId);
    return BaseResponse.<ProductResponse>builder()
        .data(productVariant)
        .message("Product found successfully")
        .timestamp(Instant.now())
        .build();
  }

  @Override
  public BaseResponse<List<ProductResponse>> getAllProducts() {
    return productVariantRepository.findAllToResponse();
  }
}
