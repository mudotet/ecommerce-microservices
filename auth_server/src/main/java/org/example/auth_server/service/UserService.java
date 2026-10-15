package org.example.auth_server.service;

import org.example.auth_server.dto.request.UserLoginDto;
import org.example.auth_server.dto.request.UserRegistrationDto;
import org.example.auth_server.dto.response.UserLoginResponseDto;

public interface UserService {
  void createUser(UserRegistrationDto userRegistrationDto);

  UserLoginResponseDto loginUser(UserLoginDto userLoginDto);
}
