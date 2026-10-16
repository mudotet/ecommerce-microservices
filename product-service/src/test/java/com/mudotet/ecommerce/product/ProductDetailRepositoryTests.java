package com.mudotet.ecommerce.product;

import com.mudotet.ecommerce.product.config.AuditingConfig;
import com.mudotet.ecommerce.product.entity.Category;
import com.mudotet.ecommerce.product.entity.Product;
import com.mudotet.ecommerce.product.entity.ProductVariant;
import com.mudotet.ecommerce.product.entity.Inventory;
import com.mudotet.ecommerce.product.repository.InventoryRepository;
import com.mudotet.ecommerce.product.repository.ProcessedOrderRepository;
import com.mudotet.ecommerce.product.repository.ProductRepository;
import com.mudotet.ecommerce.product.repository.ProductVariantRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.redisson.spring.starter.RedissonAutoConfigurationV4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ProductDetailRepositoryTests.Config.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:product-detail-tests;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false",
        "eureka.client.enabled=false"
    })
class ProductDetailRepositoryTests {
  @Configuration
  @EnableAutoConfiguration(exclude = RedissonAutoConfigurationV4.class)
  @EntityScan(basePackageClasses = Product.class)
  @EnableJpaRepositories(basePackageClasses = ProductRepository.class,
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
          classes = ProcessedOrderRepository.class))
  @Import(AuditingConfig.class)
  static class Config {
  }

  @Autowired EntityManager entityManager;
  @Autowired ProductVariantRepository variants;
  @Autowired InventoryRepository inventory;
  @Autowired ProductRepository products;

  @Test
  @Transactional
  void deletesProductChildrenWithoutDeletingAnotherProductsData() {
    var category = Category.builder().name("Demo").build();
    entityManager.persist(category);
    var shirt = Product.builder().name("T-Shirt").category(category).build();
    var mug = Product.builder().name("Mug").category(category).build();
    entityManager.persist(shirt);
    entityManager.persist(mug);
    var shirtVariant = variant(shirt, "Black M");
    var mugVariant = variant(mug, "White");
    entityManager.persist(Inventory.builder().name("Shirt stock").quantity(10)
        .productVariant(shirtVariant).build());
    entityManager.persist(Inventory.builder().name("Mug stock").quantity(5)
        .productVariant(mugVariant).build());
    entityManager.flush();

    inventory.deleteAllByProductVariantProductId(shirt.getId());
    variants.deleteAllByProductId(shirt.getId());
    products.delete(shirt);
    entityManager.flush();
    entityManager.clear();

    assertEquals(false, products.existsById(shirt.getId()));
    assertEquals(true, products.existsById(mug.getId()));
    assertEquals(List.of(mugVariant.getId()), variants.findAll().stream().map(ProductVariant::getId).toList());
    assertEquals(1, inventory.count());
  }

  @Test
  @Transactional
  void findsOnlyVariantsBelongingToTheRequestedProduct() {
    var category = Category.builder().name("Demo").build();
    entityManager.persist(category);
    var shirt = Product.builder().name("T-Shirt").category(category).build();
    var mug = Product.builder().name("Mug").category(category).build();
    entityManager.persist(shirt);
    entityManager.persist(mug);
    var black = variant(shirt, "Black M");
    var white = variant(shirt, "White L");
    variant(mug, "White");
    entityManager.flush();
    entityManager.clear();

    var result = variants.findAllByProductId(shirt.getId());

    assertEquals(2, result.size());
    assertEquals(List.of(black.getId(), white.getId()).stream().sorted().toList(),
        result.stream().map(ProductVariant::getId).sorted().toList());
    assertEquals("T-Shirt", result.getFirst().getProduct().getName());
    assertEquals("Demo", result.getFirst().getProduct().getCategory().getName());
  }

  private ProductVariant variant(Product product, String name) {
    var variant = ProductVariant.builder().product(product).variantName(name)
        .price(100000.0).description("Demo variant").build();
    entityManager.persist(variant);
    return variant;
  }
}
