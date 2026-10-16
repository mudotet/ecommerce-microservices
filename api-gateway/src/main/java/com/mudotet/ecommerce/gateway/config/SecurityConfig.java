package com.mudotet.ecommerce.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.csrf(csrfSpec -> csrfSpec.disable())
        .authorizeExchange(
            authorizeExchangeSpec ->
                authorizeExchangeSpec
                    .pathMatchers("/products/**")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                    .pathMatchers("/orders/**")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2ResourceServerSpec ->
                oauth2ResourceServerSpec.jwt(
                    jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          Object resourceAccess = jwt.getClaim("resource_access");
          if (!(resourceAccess instanceof Map<?, ?> clients)) return List.of();

          Object backendAccess = clients.get("backend");
          if (!(backendAccess instanceof Map<?, ?> backend)) return List.of();

          Object roles = backend.get("roles");
          if (!(roles instanceof List<?> roleList)) return List.of();

          return roleList.stream()
              .filter(role -> role instanceof String)
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
              .collect(Collectors.toList());
        });
    return new ReactiveJwtAuthenticationConverterAdapter(converter);
  }
}
