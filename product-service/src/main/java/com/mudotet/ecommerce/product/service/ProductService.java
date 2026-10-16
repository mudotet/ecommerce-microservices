package com.mudotet.ecommerce.product.service;

import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.dto.response.ProductResponse;
import com.mudotet.ecommerce.product.dto.response.ProductItemResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
  List<ProductItemResponse> getProductItems(List<UUID> variantIds);

  BaseResponse<Boolean> addProduct(AddProductRequest request);

  BaseResponse<ProductResponse> getProductById(String productVariantId);

  BaseResponse<List<ProductResponse>> getAllProducts();
}
