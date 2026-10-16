package com.mudotet.ecommerce.product.config;

import java.util.HashSet;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {
  @Bean
  @Primary
  public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
      CacheProperties properties) {
    var configuration = RedisCacheConfiguration.defaultCacheConfig();
    if (properties.getRedis().getTimeToLive() != null) {
      configuration = configuration.entryTtl(properties.getRedis().getTimeToLive());
    }
    if (!properties.getRedis().isCacheNullValues()) {
      configuration = configuration.disableCachingNullValues();
    }
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(configuration)
        .initialCacheNames(new HashSet<>(properties.getCacheNames()))
        .transactionAware()
        .build();
  }

  @Bean
  public CaffeineCacheManager caffeineCacheManager(CacheProperties properties) {
    var manager = new CaffeineCacheManager();
    manager.setCacheSpecification(properties.getCaffeine().getSpec());
    manager.setCacheNames(properties.getCacheNames());
    manager.setAllowNullValues(false);
    return manager;
  }
}
