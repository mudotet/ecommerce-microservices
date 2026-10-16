package com.mudotet.ecommerce.product.controller;

import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.AddProductRequest;
import com.mudotet.ecommerce.product.dto.request.UpdateProductRequest;
import jakarta.validation.Valid;
import com.mudotet.ecommerce.product.dto.response.ProductResponse;
import com.mudotet.ecommerce.product.dto.response.ProductItemResponse;
import com.mudotet.ecommerce.product.dto.response.ProductDetailResponse;
import com.mudotet.ecommerce.product.service.ProductService;
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
  private final ProductService productService;

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

  @PutMapping("/{productId}")
  public BaseResponse<Boolean> updateProduct(@PathVariable UUID productId,
      @Valid @RequestBody UpdateProductRequest request) {
    return productService.updateProduct(productId, request);
  }

  @DeleteMapping("/{productId}")
  public BaseResponse<Boolean> deleteProduct(@PathVariable UUID productId) {
    return productService.deleteProduct(productId);
  }

  // Get product variant by id
  @GetMapping("/{productVariantId}")
  public BaseResponse<ProductResponse> getProductById(@PathVariable String productVariantId) {
    return productService.getProductById(productVariantId);
  }

  @GetMapping("/{productId}/detail")
  public BaseResponse<ProductDetailResponse> getProductDetail(@PathVariable UUID productId) {
    return BaseResponse.success(productService.getProductDetail(productId));
  }

  // Get products
  @GetMapping()
  public BaseResponse<List<ProductResponse>> getAllProducts() {
    return productService.getAllProducts();
  }
}
