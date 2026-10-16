package com.mudotet.ecommerce.gateway.config;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(SecurityConfigTests.TestConfig.class)
class SecurityConfigTests {
  @Configuration
  @EnableWebFlux
  @EnableWebFluxSecurity
  @Import({SecurityConfig.class, TestEndpoints.class})
  static class TestConfig {
    @Bean
    ReactiveJwtDecoder jwtDecoder() {
      return mock(ReactiveJwtDecoder.class);
    }
  }

  @RestController
  static class TestEndpoints {
    @GetMapping({"/products/test", "/orders/test"})
    String get() {
      return "allowed";
    }

    @PostMapping("/orders/test")
    String post() {
      return "allowed";
    }
  }

  @Autowired ApplicationContext context;
  @Autowired ReactiveJwtDecoder decoder;
  private WebTestClient client;

  @BeforeEach
  void setUp() {
    reset(decoder);
    client = WebTestClient.bindToApplicationContext(context).build();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/products/test", "/orders/test"})
  void missingTokenReturns401(String path) {
    client.get().uri(path).exchange().expectStatus().isUnauthorized();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/products/test", "/orders/test"})
  void backendUserCanAccessProtectedRoutes(String path) {
    tokenWithClaims(Map.of("resource_access", Map.of("backend", Map.of("roles", List.of("USER")))));

    client.get().uri(path).headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("allowed");
  }

  @Test
  void backendUserCanCreateOrderWithoutCsrfToken() {
    tokenWithClaims(Map.of("resource_access", Map.of("backend", Map.of("roles", List.of("USER")))));

    client.post().uri("/orders/test").headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isOk();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/products/test", "/orders/test"})
  void validTokenWithoutRolesReturns403(String path) {
    tokenWithClaims(Map.of());

    client.get().uri(path).headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isForbidden();
  }

  @Test
  void roleFromAnotherClientDoesNotGrantAccess() {
    tokenWithClaims(Map.of("resource_access", Map.of("another-client", Map.of("roles", List.of("USER")))));

    client.get().uri("/orders/test").headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isForbidden();
  }

  @Test
  void backendAdminCanAccessProtectedRoutes() {
    tokenWithClaims(Map.of("resource_access", Map.of("backend", Map.of("roles", List.of("ADMIN")))));

    client.get().uri("/orders/test").headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isOk();
  }

  @Test
  void unrelatedBackendRoleDoesNotGrantAccess() {
    tokenWithClaims(Map.of("resource_access", Map.of("backend", Map.of("roles", List.of("VIEWER")))));

    client.get().uri("/orders/test").headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isForbidden();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "resource_access", "backend", "roles"
  })
  void malformedRoleClaimReturns403InsteadOfServerError(String malformedPart) {
    Map<String, Object> claims = switch (malformedPart) {
      case "resource_access" -> Map.of("resource_access", "invalid");
      case "backend" -> Map.of("resource_access", Map.of("backend", "invalid"));
      default -> Map.of("resource_access", Map.of("backend", Map.of("roles", "USER")));
    };
    tokenWithClaims(claims);

    client.get().uri("/orders/test").headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange().expectStatus().isForbidden();
  }

  @Test
  void invalidTokenReturns401() {
    when(decoder.decode("invalid-token")).thenReturn(Mono.error(new BadJwtException("Invalid token")));

    client.get().uri("/orders/test").header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
        .exchange().expectStatus().isUnauthorized();
  }

  private void tokenWithClaims(Map<String, Object> claims) {
    Jwt jwt = Jwt.withTokenValue("valid-token").header("alg", "RS256").subject("test-user")
        .claims(values -> values.putAll(claims)).build();
    when(decoder.decode("valid-token")).thenReturn(Mono.just(jwt));
  }
}
