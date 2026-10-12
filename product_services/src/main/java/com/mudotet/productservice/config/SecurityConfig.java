package com.mudotet.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.ignoringRequestMatchers("/products/order-items"))
        .authorizeHttpRequests(requests -> requests.requestMatchers("/products/order-items").authenticated().anyRequest().permitAll())
        .httpBasic(Customizer.withDefaults())
        .build();
  }
}
