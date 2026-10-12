package com.mudotet.productservice.service;

import com.mudotet.productservice.common.dto.BaseResponse;
import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.dto.response.ProductResponse;
import com.mudotet.productservice.dto.response.ProductItemResponse;

import java.util.List;
import java.util.UUID;

public interface ProductServices {
  List<ProductItemResponse> getProductItems(List<UUID> variantIds);

  BaseResponse<Boolean> addProduct(AddProductRequest request);

  BaseResponse<ProductResponse> getProductById(String productVariantId);

  BaseResponse<List<ProductResponse>> getAllProducts();
}
