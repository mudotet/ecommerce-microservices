package org.example.auth_server.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyCloakConfig {
  @Value("${keycloak.auth-server-url}")
  private String authServerUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.resource}")
  private String clientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  @Bean
  public Keycloak keyCloak() {
    return KeycloakBuilder.builder()
        .serverUrl(authServerUrl)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .realm(realm)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();
  }
}
