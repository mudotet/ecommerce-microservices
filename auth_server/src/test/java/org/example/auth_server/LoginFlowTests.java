package org.example.auth_server;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.auth_server.controller.AuthController;
import org.example.auth_server.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

class LoginFlowTests {
  private MockRestServiceServer keycloakServer;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    UserServiceImpl service = new UserServiceImpl(null, null);
    RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
    keycloakServer = MockRestServiceServer.bindTo(restTemplate).build();
    ReflectionTestUtils.setField(service, "authServerUrl", "http://keycloak.test");
    ReflectionTestUtils.setField(service, "realm", "learning");
    ReflectionTestUtils.setField(service, "clientId", "backend");
    ReflectionTestUtils.setField(service, "clientSecret", "server-secret");
    mvc = MockMvcBuilders.standaloneSetup(new AuthController(service)).build();
  }

  @Test
  void returnsTokensAndUsesServerClientCredentials() throws Exception {
    keycloakServer.expect(requestTo("http://keycloak.test/realms/learning/protocol/openid-connect/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(content().string("username=tu&password=p%2Ba%26ss&client_id=backend&grant_type=password&client_secret=server-secret"))
        .andRespond(withSuccess("""
            {"access_token":"access-value","refresh_token":"refresh-value",
             "expires_in":300,"refresh_expires_in":1800,"token_type":"Bearer",
             "scope":"profile email","session_state":"session-1"}
            """, MediaType.APPLICATION_JSON));

    mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
            {"username":"tu","password":"p+a&ss"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("access-value"))
        .andExpect(jsonPath("$.refresh_token").value("refresh-value"))
        .andExpect(jsonPath("$.expires_in").value(300))
        .andExpect(jsonPath("$.token_type").value("Bearer"))
        .andExpect(jsonPath("$.session_state").value("session-1"))
        .andExpect(header().string("Cache-Control", "no-store"));
    keycloakServer.verify();
  }

  @Test
  void preservesKeycloakStatusForInvalidCredentials() throws Exception {
    expectError(HttpStatus.BAD_REQUEST, "invalid_grant");
    login().andExpect(status().isBadRequest());
    keycloakServer.verify();
  }

  @Test
  void preservesKeycloakStatusForInvalidClient() throws Exception {
    expectError(HttpStatus.UNAUTHORIZED, "invalid_client");
    login().andExpect(status().isUnauthorized());
    keycloakServer.verify();
  }

  @Test
  void preservesKeycloakStatusForDisabledDirectGrant() throws Exception {
    expectError(HttpStatus.BAD_REQUEST, "unauthorized_client");
    login().andExpect(status().isBadRequest());
    keycloakServer.verify();
  }

  @Test
  void missingPasswordReturns400WithoutCallingKeycloak() throws Exception {
    mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"tu\"}"))
        .andExpect(status().isBadRequest());
    keycloakServer.verify();
  }

  @Test
  void blankUsernameReturns400WithoutCallingKeycloak() throws Exception {
    mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"   \",\"password\":\"pass\"}"))
        .andExpect(status().isBadRequest());
    keycloakServer.verify();
  }

  private void expectError(HttpStatus status, String error) {
    keycloakServer.expect(anything()).andRespond(withStatus(status)
        .contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"" + error + "\"}"));
  }

  private org.springframework.test.web.servlet.ResultActions login() throws Exception {
    return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"tu\",\"password\":\"pass\"}"));
  }
}
