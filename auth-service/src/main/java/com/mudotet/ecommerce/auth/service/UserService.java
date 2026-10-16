package com.mudotet.ecommerce.auth.service;

import com.mudotet.ecommerce.auth.dto.request.UserLoginRequest;
import com.mudotet.ecommerce.auth.dto.request.UserRegistrationRequest;
import com.mudotet.ecommerce.auth.dto.response.UserLoginResponse;

public interface UserService {
  void createUser(UserRegistrationRequest userRegistrationRequest);

  UserLoginResponse loginUser(UserLoginRequest userLoginRequest);
}
