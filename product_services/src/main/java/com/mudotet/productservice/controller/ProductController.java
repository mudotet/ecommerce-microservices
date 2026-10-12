package com.mudotet.productservice.controller;

import com.mudotet.productservice.common.dto.BaseResponse;
import com.mudotet.productservice.dto.request.AddProductRequest;
import com.mudotet.productservice.dto.response.ProductResponse;
import com.mudotet.productservice.dto.response.ProductItemResponse;
import com.mudotet.productservice.service.impl.ProductServicesImpl;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
  private final ProductServicesImpl productServices;

  @PostMapping("/order-items")
  public List<ProductItemResponse> getProductItems(@RequestBody @NotEmpty List<@NotNull UUID> variantIds) {
    return productServices.getProductItems(variantIds);
  }

  @GetMapping("/health")
  public String healthCheck() {
    return "Product Service is up and running!";
  }

  // Add product
  @PutMapping()
  public BaseResponse<Boolean> addProduct(@RequestBody AddProductRequest request) {
    return productServices.addProduct(request);
  }

  // Get product by id
  @GetMapping("/{productVariantId}")
  public BaseResponse<ProductResponse> getProductById(@PathVariable String productVariantId) {
    return productServices.getProductById(productVariantId);
  }

  // Get products
  @GetMapping()
  public BaseResponse<List<ProductResponse>> getAllProducts() {
    return productServices.getAllProducts();
  }
}
