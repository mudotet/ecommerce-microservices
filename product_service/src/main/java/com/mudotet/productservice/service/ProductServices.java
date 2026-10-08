package com.mudotet.productservice.service;

import com.mudotet.productservice.common.dto.BaseResponse;
import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.dto.response.ProductResponse;

import java.util.List;

public interface ProductServices {
  BaseResponse<Boolean> addProduct(AddProductRequest request);

  BaseResponse<ProductResponse> getProductById(String productVariantId);

  BaseResponse<List<ProductResponse>> getAllProducts();
}
