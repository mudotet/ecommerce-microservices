package com.mudotet.ecommerce.product.service.impl;

import com.mudotet.ecommerce.product.common.ErrorCode;
import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.dto.request.UpdateProductRequest;
import com.mudotet.ecommerce.product.dto.response.ProductResponse;
import com.mudotet.ecommerce.product.dto.response.ProductItemResponse;
import com.mudotet.ecommerce.product.dto.response.ProductDetailResponse;
import com.mudotet.ecommerce.product.dto.response.ProductVariantDetailResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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
  @CacheEvict(cacheNames = "product-details", key = "#productId")
  @Transactional
  public BaseResponse<Boolean> updateProduct(UUID productId, UpdateProductRequest request) {
    log.info("[PRODUCT UPDATE] Reading product and category from database: productId={}, categoryId={}",
        productId, request.getCategoryId());
    var product = productRepository.findById(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    var category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    if (!product.getName().equals(request.getProductName())
        && productRepository.existsByName(request.getProductName())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists");
    }
    product.setName(request.getProductName());
    product.setCategory(category);
    productRepository.save(product);
    log.info("[PRODUCT UPDATE] Saved changes: productId={}; cache eviction follows successful execution",
        productId);
    return BaseResponse.success(true);
  }

  @Override
  @CacheEvict(cacheNames = "product-details", key = "#productId")
  @Transactional
  public BaseResponse<Boolean> deleteProduct(UUID productId) {
    log.info("[PRODUCT DELETE] Reading product from database: productId={}", productId);
    var product = productRepository.findById(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    inventoryRepository.deleteAllByProductVariantProductId(productId);
    productVariantRepository.deleteAllByProductId(productId);
    productRepository.delete(product);
    log.info("[PRODUCT DELETE] Scheduled deletion of product, variants and inventory: productId={}; "
        + "cache eviction follows successful execution", productId);
    return BaseResponse.success(true);
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
  @Cacheable(cacheNames = "product-details", key = "#productId")
  @Transactional(readOnly = true)
  public ProductDetailResponse getProductDetail(UUID productId) {
    log.info("[PRODUCT DETAIL] Cache miss; reading product from database: productId={}", productId);
    var product = productRepository.findById(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    log.info("[PRODUCT DETAIL] Reading variants from database: productId={}", productId);
    var variants = productVariantRepository.findAllByProductId(productId).stream()
        .map(variant -> ProductVariantDetailResponse.builder()
            .id(variant.getId())
            .variantName(variant.getVariantName())
            .price(variant.getPrice())
            .description(variant.getDescription())
            .build())
        .toList();

    var detail = ProductDetailResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .categoryId(product.getCategory().getId())
        .categoryName(product.getCategory().getName())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .variants(variants)
        .build();
    log.info("[PRODUCT DETAIL] Loaded database details: productId={}, variantCount={}; "
        + "result ready for caching", productId, variants.size());
    return detail;
  }

  @Override
  public BaseResponse<List<ProductResponse>> getAllProducts() {
    return productVariantRepository.findAllToResponse();
  }
}
