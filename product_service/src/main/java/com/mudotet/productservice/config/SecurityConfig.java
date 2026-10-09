package com.mudotet.productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
      @Value("${APP_USERNAME:}") String username,
      @Value("${APP_PASSWORD:}") String password) {
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      throw new IllegalStateException(
          "Set APP_USERNAME and APP_PASSWORD before starting the service");
    }
    return new InMemoryUserDetailsManager(
        User.withUsername(username)
            .password(passwordEncoder.encode(password))
            .roles("USER")
            .build());
  }
}
