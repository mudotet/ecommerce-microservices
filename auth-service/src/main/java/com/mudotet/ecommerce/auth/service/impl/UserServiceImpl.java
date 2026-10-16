package com.mudotet.ecommerce.auth.service.impl;

import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.mudotet.ecommerce.auth.dto.request.UserLoginRequest;
import com.mudotet.ecommerce.auth.dto.request.UserRegistrationRequest;
import com.mudotet.ecommerce.auth.dto.response.UserLoginResponse;
import com.mudotet.ecommerce.auth.mapper.UserMapper;
import com.mudotet.ecommerce.auth.service.UserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final Keycloak keycloak;
  private final UserMapper userMapper;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${keycloak.auth-server-url}")
  private String authServerUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.resource}")
  private String clientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  @Override
  public void createUser(UserRegistrationRequest userRegistrationRequest) {
    UserRepresentation user = userMapper.toUserRepresentation(userRegistrationRequest);
    user.setEnabled(true);
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(userRegistrationRequest.getPassword());
    credential.setTemporary(false);
    user.setCredentials(List.of(credential));

    try (Response response = keycloak.realm(realm).users().create(user)) {
      if (response.getStatus() != 201) {
        String errorMessage = response.readEntity(String.class);
        throw new RuntimeException("Failed to create user: " + errorMessage);
      }
    }
  }

  @Override
  public UserLoginResponse loginUser(UserLoginRequest userLoginRequest) {
    if (userLoginRequest == null || !StringUtils.hasText(userLoginRequest.getUsername())
        || !StringUtils.hasText(userLoginRequest.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username và password không được để trống");
    }
    String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("username", userLoginRequest.getUsername());
    formData.add("password", userLoginRequest.getPassword());
    formData.add("client_id", clientId);
    formData.add("grant_type", "password");
    formData.add("client_secret", clientSecret);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    try {
      return restTemplate.postForObject(
          url, new HttpEntity<>(formData, headers), UserLoginResponse.class);
    } catch (HttpStatusCodeException e) {
      throw new ResponseStatusException(e.getStatusCode(), "Đăng nhập thất bại");
    }
  }
}
