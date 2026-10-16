package com.mudotet.ecommerce.product.service;

import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.dto.request.UpdateProductRequest;
import com.mudotet.ecommerce.product.dto.response.ProductResponse;
import com.mudotet.ecommerce.product.dto.response.ProductItemResponse;
import com.mudotet.ecommerce.product.dto.response.ProductDetailResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
  List<ProductItemResponse> getProductItems(List<UUID> variantIds);

  BaseResponse<Boolean> addProduct(AddProductRequest request);

  BaseResponse<Boolean> updateProduct(UUID productId, UpdateProductRequest request);

  BaseResponse<Boolean> deleteProduct(UUID productId);

  BaseResponse<ProductResponse> getProductById(String productVariantId);

  ProductDetailResponse getProductDetail(UUID productId);

  BaseResponse<List<ProductResponse>> getAllProducts();
}
