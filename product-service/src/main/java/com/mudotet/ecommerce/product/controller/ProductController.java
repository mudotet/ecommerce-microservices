package com.mudotet.ecommerce.product.controller;

import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.dto.response.ProductResponse;
import com.mudotet.ecommerce.product.dto.response.ProductItemResponse;
import com.mudotet.ecommerce.product.service.impl.ProductServiceImpl;
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
  private final ProductServiceImpl productService;

  @PostMapping("/order-items")
  public List<ProductItemResponse> getProductItems(@RequestBody @NotEmpty List<@NotNull UUID> variantIds) {
    return productService.getProductItems(variantIds);
  }

  @GetMapping("/health")
  public String healthCheck() {
    return "Product Service is up and running!";
  }

  // Add product
  @PutMapping()
  public BaseResponse<Boolean> addProduct(@RequestBody AddProductRequest request) {
    return productService.addProduct(request);
  }

  // Get product by id
  @GetMapping("/{productVariantId}")
  public BaseResponse<ProductResponse> getProductById(@PathVariable String productVariantId) {
    return productService.getProductById(productVariantId);
  }

  // Get products
  @GetMapping()
  public BaseResponse<List<ProductResponse>> getAllProducts() {
    return productService.getAllProducts();
  }
}
