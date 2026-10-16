package com.mudotet.ecommerce.product;

import com.mudotet.ecommerce.product.config.CacheConfig;
import com.mudotet.ecommerce.product.dto.response.ProductDetailResponse;
import com.mudotet.ecommerce.product.dto.response.ProductVariantDetailResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(CacheConfigTests.Config.class)
@TestPropertySource(properties = {
    "spring.cache.cache-names=product-details",
    "spring.cache.redis.time-to-live=5m",
    "spring.cache.redis.cache-null-values=false",
    "spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=5m"
})
class CacheConfigTests {
  @Configuration
  @Import(CacheConfig.class)
  static class Config {
    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
      return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    CachedCatalog catalog() {
      return new CachedCatalog();
    }
  }

  static class CachedCatalog {
    int reads;

    public int readCount() {
      return reads;
    }

    @Cacheable(cacheNames = "product-details", key = "#id")
    public ProductDetailResponse detail(UUID id) {
      reads++;
      return ProductDetailResponse.builder().id(id).name("Demo")
          .variants(List.of(new ProductVariantDetailResponse(UUID.randomUUID(), "Black M", 100.0, "Demo")))
          .build();
    }

    @CacheEvict(cacheNames = "product-details", key = "#id")
    public void evict(UUID id) {
    }
  }

  @Autowired CacheManager primary;
  @Autowired RedisCacheManager redis;
  @Autowired @Qualifier("caffeineCacheManager") CaffeineCacheManager caffeine;
  @Autowired LettuceConnectionFactory connectionFactory;
  @Autowired CachedCatalog catalog;

  @Test
  void redisIsPrimaryAndBothManagersUseConfiguredLimits() {
    assertSame(redis, primary);
    var decorated = (TransactionAwareCacheDecorator) redis.getCache("product-details");
    var config = ((RedisCache) decorated.getTargetCache()).getCacheConfiguration();
    assertEquals(Duration.ofMinutes(5), config.getTtlFunction().getTimeToLive("key", "value"));
    assertFalse(config.getAllowCacheNullValues());
    var nativeCache = ((CaffeineCache) caffeine.getCache("product-details")).getNativeCache();
    assertEquals(1000, nativeCache.policy().eviction().orElseThrow().getMaximum());
    assertEquals(Duration.ofMinutes(5), nativeCache.policy().expireAfterWrite().orElseThrow().getExpiresAfter());
  }

  @Test
  @EnabledIfSystemProperty(named = "product.tests.use-redis", matches = "true")
  void defaultAnnotationsUseRedisWithSerializationTtlAndEviction() {
    var id = UUID.randomUUID();
    var cache = redis.getCache("product-details");
    try {
      var first = catalog.detail(id);
      var second = catalog.detail(id);
      assertEquals(1, catalog.readCount());
      assertNotSame(first, second);
      assertEquals(first.getVariants().getFirst().getId(), second.getVariants().getFirst().getId());
      var ttl = new StringRedisTemplate(connectionFactory).getExpire("product-details::" + id);
      assertTrue(ttl > 0 && ttl <= 300);
      assertNull(caffeine.getCache("product-details").get(id));

      catalog.evict(id);

      assertNull(cache.get(id));
      catalog.detail(id);
      assertEquals(2, catalog.readCount());
    } finally {
      cache.evict(id);
    }
  }
}
