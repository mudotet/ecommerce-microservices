package org.example.auth_server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
  private String username;
  private String email;
  private String password;
  private String firstName;
  private String lastName;
}
