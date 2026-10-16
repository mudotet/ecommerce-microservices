package com.mudotet.ecommerce.product.service.impl;

import com.mudotet.ecommerce.product.common.ErrorCode;
import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.dto.response.ProductResponse;
import com.mudotet.ecommerce.product.dto.response.ProductItemResponse;
import com.mudotet.ecommerce.product.entity.Category;
import com.mudotet.ecommerce.product.entity.Inventory;
import com.mudotet.ecommerce.product.entity.Product;
import com.mudotet.ecommerce.product.entity.ProductVariant;
import com.mudotet.ecommerce.product.mapper.CategoryMapper;
import com.mudotet.ecommerce.product.mapper.InventoryMapper;
import com.mudotet.ecommerce.product.mapper.ProductMapper;
import com.mudotet.ecommerce.product.mapper.ProductVariantMapper;
import com.mudotet.ecommerce.product.repository.CategoryRepository;
import com.mudotet.ecommerce.product.repository.InventoryRepository;
import com.mudotet.ecommerce.product.repository.ProductRepository;
import com.mudotet.ecommerce.product.repository.ProductVariantRepository;
import com.mudotet.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;
  private final ProductVariantRepository productVariantRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;
  private final InventoryMapper inventoryMapper;
  private final ProductVariantMapper productVariantMapper;
  private final CategoryMapper categoryMapper;

  @Override
  @Transactional(readOnly = true)
  public List<ProductItemResponse> getProductItems(List<UUID> variantIds) {
    var products = productVariantRepository.findAllById(variantIds);
    if (products.size() != variantIds.size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found");
    }
    return products.stream().map(product -> {
      var price = product.getPrice();
      if (price == null || !Double.isFinite(price) || price < 0) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid product price");
      }
      return new ProductItemResponse(product.getId(), BigDecimal.valueOf(price));
    }).toList();
  }

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
