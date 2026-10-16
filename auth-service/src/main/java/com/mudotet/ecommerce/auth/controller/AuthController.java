package com.mudotet.ecommerce.auth.controller;

import lombok.RequiredArgsConstructor;
import com.mudotet.ecommerce.auth.dto.request.UserLoginRequest;
import com.mudotet.ecommerce.auth.dto.request.UserRegistrationRequest;
import com.mudotet.ecommerce.auth.service.UserService;
import com.mudotet.ecommerce.auth.dto.response.UserLoginResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final UserService userService;

  @GetMapping("/health")
  public String healthCheck() {
    return "Auth Service is up and running!";
  }

  @PostMapping("/register")
  public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
    userService.createUser(userRegistrationRequest);
    return ResponseEntity.ok("User registered successfully");
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
        .body(userService.loginUser(userLoginRequest));
  }
}
