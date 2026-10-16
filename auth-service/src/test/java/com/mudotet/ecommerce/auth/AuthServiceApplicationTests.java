package com.mudotet.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "keycloak.credentials.secret=test-client-secret")
class AuthServiceApplicationTests {

  @Test
  void contextLoads() {}
}
