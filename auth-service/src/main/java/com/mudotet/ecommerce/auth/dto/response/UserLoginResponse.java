package com.mudotet.ecommerce.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLoginResponse {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("expires_in")
  private long expiresIn;

  @JsonProperty("refresh_expires_in")
  private long refreshExpiresIn;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("session_state")
  private String sessionState;

  private String scope;
}
