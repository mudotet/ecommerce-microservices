package com.mudotet.productservice;

import com.mudotet.productservice.config.AuditingConfig;
import com.mudotet.productservice.dto.request.LockProductItem;
import com.mudotet.productservice.dto.request.LockProductReq;
import com.mudotet.productservice.entity.*;
import com.mudotet.productservice.repository.*;
import com.mudotet.productservice.service.InventoryLockService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = InventoryLockIntegrationTests.Config.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:inventory-tests;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
    })
class InventoryLockIntegrationTests {
  @Configuration
  @EnableAutoConfiguration
  @EntityScan(basePackageClasses = Inventory.class)
  @EnableJpaRepositories(basePackageClasses = InventoryRepository.class,
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
          classes = {CategoryRepository.class, ProductRepository.class, ProductVariantRepository.class}))
  @Import({InventoryLockService.class, AuditingConfig.class})
  static class Config {}

  @Autowired InventoryLockService service;
  @Autowired InventoryRepository inventories;
  @Autowired ProcessedOrderRepository results;
  @Autowired EntityManager entityManager;
  @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

  @Test
  void concurrentOrdersCannotOversell() throws Exception {
    var variantId = stock(5);
    var start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(() -> { start.await(); return service.lockProducts(request(variantId, 4)); });
      var second = executor.submit(() -> { start.await(); return service.lockProducts(request(variantId, 4)); });
      start.countDown();
      var a = first.get(15, TimeUnit.SECONDS);
      var b = second.get(15, TimeUnit.SECONDS);
      assertNotEquals(a.isSuccess(), b.isSuccess());
      assertEquals(1, quantity(variantId));
      assertTrue(results.existsById(a.getOrderId()));
      assertTrue(results.existsById(b.getOrderId()));
    }
  }

  @Test
  void concurrentDuplicateDeductsOnceAfterRetry() throws Exception {
    var variantId = stock(5);
    var request = request(variantId, 2);
    var start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(() -> { start.await(); return consumeWithRetry(request); });
      var second = executor.submit(() -> { start.await(); return consumeWithRetry(request); });
      start.countDown();
      assertTrue(first.get(15, TimeUnit.SECONDS).isSuccess());
      assertTrue(second.get(15, TimeUnit.SECONDS).isSuccess());
      assertEquals(3, quantity(variantId));
      assertTrue(results.findById(request.getOrderId()).orElseThrow().isSuccess());
    }
  }

  @Test
  void transactionFailureRollsBackStockAndProcessingRecord() {
    var variantId = stock(5);
    var request = request(variantId, 2);
    assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager).execute(status -> {
      service.lockProducts(request);
      throw new IllegalStateException("Simulated failure before commit");
    }));
    assertEquals(5, quantity(variantId));
    assertFalse(results.existsById(request.getOrderId()));
    assertTrue(service.lockProducts(request).isSuccess());
    assertEquals(3, quantity(variantId));
  }

  @Test
  void missingItemCommitsRejectionWithoutDeductingAvailableStock() {
    var variantId = stock(5);
    var orderId = UUID.randomUUID();
    var request = new LockProductReq(orderId, List.of(
        new LockProductItem(variantId, 2), new LockProductItem(UUID.randomUUID(), 1)));
    assertFalse(service.lockProducts(request).isSuccess());
    assertEquals(5, quantity(variantId));
    assertFalse(results.findById(orderId).orElseThrow().isSuccess());
    assertFalse(service.lockProducts(request).isSuccess());
    assertEquals(5, quantity(variantId));
  }

  private ProcessedOrder consumeWithRetry(LockProductReq request) {
    try {
      return service.lockProducts(request);
    } catch (DataIntegrityViolationException duplicateClaim) {
      // The Kafka listener retries after the competing transaction has committed.
      return service.lockProducts(request);
    }
  }

  private LockProductReq request(UUID variantId, int quantity) {
    return new LockProductReq(UUID.randomUUID(), List.of(new LockProductItem(variantId, quantity)));
  }

  private UUID stock(int quantity) {
    return new TransactionTemplate(transactionManager).execute(status -> {
      var category = new Category();
      category.setName("Category");
      entityManager.persist(category);
      var product = new Product();
      product.setName("Product");
      product.setCategory(category);
      entityManager.persist(product);
      var variant = new ProductVariant();
      variant.setProduct(product);
      variant.setVariantName("Variant");
      variant.setPrice(10.0);
      variant.setDescription("Test stock");
      entityManager.persist(variant);
      entityManager.persist(Inventory.builder().name("Stock").quantity(quantity).productVariant(variant).build());
      return variant.getId();
    });
  }

  private int quantity(UUID variantId) {
    return new TransactionTemplate(transactionManager).execute(status ->
        inventories.findAllForUpdate(List.of(variantId)).getFirst().getQuantity());
  }
}
