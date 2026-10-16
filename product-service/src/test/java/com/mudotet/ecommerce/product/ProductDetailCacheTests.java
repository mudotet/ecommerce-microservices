package com.mudotet.ecommerce.product;

import com.mudotet.ecommerce.product.dto.request.UpdateProductRequest;
import com.mudotet.ecommerce.product.entity.Category;
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
import com.mudotet.ecommerce.product.service.impl.ProductServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(ProductDetailCacheTests.Config.class)
class ProductDetailCacheTests {
  @Configuration
  @EnableCaching
  static class Config {
    @Bean
    CacheManager cacheManager() {
      var manager = new CaffeineCacheManager("product-details");
      manager.setCacheSpecification("maximumSize=1000,expireAfterWrite=5m");
      return manager;
    }

    @Bean
    ProductRepository products() {
      return mock(ProductRepository.class);
    }

    @Bean
    ProductVariantRepository variants() {
      return mock(ProductVariantRepository.class);
    }

    @Bean
    CategoryRepository categories() {
      return mock(CategoryRepository.class);
    }

    @Bean
    InventoryRepository inventory() {
      return mock(InventoryRepository.class);
    }

    @Bean
    ProductService productService(ProductRepository products, ProductVariantRepository variants,
        CategoryRepository categories, InventoryRepository inventory) {
      return new ProductServiceImpl(products, inventory, variants,
          categories, mock(ProductMapper.class), mock(InventoryMapper.class),
          mock(ProductVariantMapper.class), mock(CategoryMapper.class));
    }
  }

  @Autowired ProductService service;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired CacheManager caches;
  @Autowired CategoryRepository categories;
  @Autowired InventoryRepository inventory;
  private final List<UUID> productIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    reset(products, variants, categories, inventory);
  }

  @AfterEach
  void removeTestEntries() {
    var cache = caches.getCache("product-details");
    productIds.forEach(cache::evict);
  }

  @Test
  void updatingProductEvictsOnlyItsDetails() {
    var product = product("T-Shirt");
    var other = product("Mug");
    service.getProductDetail(product.getId());
    var otherDetails = service.getProductDetail(other.getId());
    when(categories.findById(product.getCategory().getId()))
        .thenReturn(Optional.of(product.getCategory()));

    service.updateProduct(product.getId(),
        new UpdateProductRequest("Updated shirt", product.getCategory().getId()));

    assertNull(caches.getCache("product-details").get(product.getId()));
    assertEquals("Updated shirt", service.getProductDetail(product.getId()).getName());
    assertSame(otherDetails, service.getProductDetail(other.getId()));
    verify(products, times(3)).findById(product.getId());
    verify(products, times(1)).findById(other.getId());
  }

  @Test
  void failedUpdateKeepsCachedDetails() {
    var product = product("T-Shirt");
    var cached = service.getProductDetail(product.getId());

    assertThrows(ResponseStatusException.class, () -> service.updateProduct(product.getId(),
        new UpdateProductRequest("Updated shirt", UUID.randomUUID())));

    assertSame(cached, service.getProductDetail(product.getId()));
    verify(products, never()).save(any());
  }

  @Test
  void deletingProductEvictsOnlyItsDetailsAndRemovesChildrenFirst() {
    var product = product("T-Shirt");
    var other = product("Mug");
    service.getProductDetail(product.getId());
    var otherDetails = service.getProductDetail(other.getId());

    service.deleteProduct(product.getId());
    when(products.findById(product.getId())).thenReturn(Optional.empty());

    assertNull(caches.getCache("product-details").get(product.getId()));
    assertThrows(ResponseStatusException.class, () -> service.getProductDetail(product.getId()));
    assertSame(otherDetails, service.getProductDetail(other.getId()));
    var order = inOrder(inventory, variants, products);
    order.verify(inventory).deleteAllByProductVariantProductId(product.getId());
    order.verify(variants).deleteAllByProductId(product.getId());
    order.verify(products).delete(product);
  }

  @Test
  void failedDeleteKeepsCachedDetails() {
    var product = product("T-Shirt");
    var cached = service.getProductDetail(product.getId());
    doThrow(new IllegalStateException("Database write failed")).when(products).delete(product);

    assertThrows(IllegalStateException.class, () -> service.deleteProduct(product.getId()));

    assertSame(cached, service.getProductDetail(product.getId()));
  }

  @Test
  void repeatedRequestReturnsCachedDetailsWithoutReadingRepositoriesAgain() {
    var product = product("T-Shirt");
    var variant = variant(product);
    when(variants.findAllByProductId(product.getId())).thenReturn(List.of(variant));

    var first = service.getProductDetail(product.getId());
    product.setName("Changed in database");
    variant.setPrice(999.0);
    var second = service.getProductDetail(product.getId());

    assertEquals("T-Shirt", first.getName());
    assertEquals("T-Shirt", second.getName());
    assertEquals("Clothing", second.getCategoryName());
    assertEquals(product.getCategory().getId(), second.getCategoryId());
    assertEquals(product.getCreatedAt(), second.getCreatedAt());
    assertEquals(variant.getId(), second.getVariants().getFirst().getId());
    assertEquals("Black M", second.getVariants().getFirst().getVariantName());
    assertEquals(100000.0, second.getVariants().getFirst().getPrice());
    assertEquals("Cotton", second.getVariants().getFirst().getDescription());
    verify(products, times(1)).findById(product.getId());
    verify(variants, times(1)).findAllByProductId(product.getId());
  }

  @Test
  void differentProductIdsHaveSeparateCacheEntries() {
    var first = product("T-Shirt");
    var second = product("Mug");

    assertEquals("T-Shirt", service.getProductDetail(first.getId()).getName());
    assertEquals("Mug", service.getProductDetail(second.getId()).getName());
    assertEquals(first.getId(), service.getProductDetail(first.getId()).getId());
    verify(products, times(1)).findById(first.getId());
    verify(products, times(1)).findById(second.getId());
  }

  @Test
  void missingProductIsNotCachedAndCanBeFoundOnNextRequest() {
    var product = product("T-Shirt");
    when(products.findById(product.getId())).thenReturn(Optional.empty(), Optional.of(product));

    var error = assertThrows(ResponseStatusException.class,
        () -> service.getProductDetail(product.getId()));
    assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    verifyNoInteractions(variants);

    assertEquals("T-Shirt", service.getProductDetail(product.getId()).getName());
    assertEquals("T-Shirt", service.getProductDetail(product.getId()).getName());
    verify(products, times(2)).findById(product.getId());
    verify(variants, times(1)).findAllByProductId(product.getId());
  }

  @Test
  void productWithoutVariantsReturnsAnEmptyList() {
    var product = product("New product");

    assertTrue(service.getProductDetail(product.getId()).getVariants().isEmpty());
    assertTrue(service.getProductDetail(product.getId()).getVariants().isEmpty());
    verify(products, times(1)).findById(product.getId());
  }

  private Product product(String name) {
    var category = Category.builder().name("Clothing").build();
    ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
    var product = Product.builder().name(name).category(category).build();
    var id = UUID.randomUUID();
    ReflectionTestUtils.setField(product, "id", id);
    ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.of(2026, 1, 2, 10, 0));
    productIds.add(id);
    when(products.findById(id)).thenReturn(Optional.of(product));
    when(variants.findAllByProductId(id)).thenReturn(List.of());
    return product;
  }

  private ProductVariant variant(Product product) {
    var variant = ProductVariant.builder().product(product).variantName("Black M")
        .price(100000.0).description("Cotton").build();
    ReflectionTestUtils.setField(variant, "id", UUID.randomUUID());
    return variant;
  }
}
