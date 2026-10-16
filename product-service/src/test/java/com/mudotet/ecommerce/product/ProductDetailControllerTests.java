package com.mudotet.ecommerce.product;

import com.mudotet.ecommerce.product.controller.ProductController;
import com.mudotet.ecommerce.product.common.dto.BaseResponse;
import com.mudotet.ecommerce.product.dto.request.UpdateProductRequest;
import com.mudotet.ecommerce.product.dto.response.ProductDetailResponse;
import com.mudotet.ecommerce.product.dto.response.ProductVariantDetailResponse;
import com.mudotet.ecommerce.product.service.ProductService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ProductDetailControllerTests {
  @Test
  void updateAndDeleteUseProductId() throws Exception {
    var id = UUID.randomUUID();
    var categoryId = UUID.randomUUID();
    var service = mock(ProductService.class);
    when(service.updateProduct(eq(id), any(UpdateProductRequest.class)))
        .thenReturn(BaseResponse.success(true));
    when(service.deleteProduct(id)).thenReturn(BaseResponse.success(true));
    var mvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();

    mvc.perform(put("/products/{id}", id).contentType("application/json")
        .content("""
            {"productName":"Updated shirt","categoryId":"%s"}
            """.formatted(categoryId)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
    mvc.perform(delete("/products/{id}", id))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

    verify(service).deleteProduct(id);
  }

  @Test
  void invalidUpdateBodyReturns400WithoutCallingService() throws Exception {
    var service = mock(ProductService.class);
    var mvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();

    mvc.perform(put("/products/{id}", UUID.randomUUID()).contentType("application/json")
        .content("{\"productName\":\" \"}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void invalidProductIdReturns400() throws Exception {
    var service = mock(ProductService.class);
    var mvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();

    mvc.perform(get("/products/not-a-uuid/detail")).andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void returnsProductAndVariantsInResponseEnvelope() throws Exception {
    var id = UUID.randomUUID();
    var variantId = UUID.randomUUID();
    var service = mock(ProductService.class);
    when(service.getProductDetail(id)).thenReturn(ProductDetailResponse.builder()
        .id(id).name("T-Shirt").categoryName("Clothing")
        .variants(List.of(new ProductVariantDetailResponse(variantId, "Black M", 100000.0, "Cotton")))
        .build());
    var mvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();

    mvc.perform(get("/products/{id}/detail", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(id.toString()))
        .andExpect(jsonPath("$.data.name").value("T-Shirt"))
        .andExpect(jsonPath("$.data.categoryName").value("Clothing"))
        .andExpect(jsonPath("$.data.variants[0].id").value(variantId.toString()))
        .andExpect(jsonPath("$.data.variants[0].price").value(100000.0))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void missingProductReturns404() throws Exception {
    var id = UUID.randomUUID();
    var service = mock(ProductService.class);
    when(service.getProductDetail(id))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    var mvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();

    mvc.perform(get("/products/{id}/detail", id)).andExpect(status().isNotFound());
  }
}
