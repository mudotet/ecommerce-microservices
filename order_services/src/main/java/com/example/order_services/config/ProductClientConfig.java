package com.example.order_services.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ProductClientConfig {
  @Bean
  @LoadBalanced
  WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}
