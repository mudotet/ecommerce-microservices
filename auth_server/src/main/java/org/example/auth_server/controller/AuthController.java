package org.example.auth_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.auth_server.dto.request.UserLoginDto;
import org.example.auth_server.dto.request.UserRegistrationDto;
import org.example.auth_server.service.UserService;
import org.example.auth_server.dto.response.UserLoginResponseDto;
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
  public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDto userRegistrationDto) {
    userService.createUser(userRegistrationDto);
    return ResponseEntity.ok("User registered successfully");
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDto> loginUser(@RequestBody UserLoginDto userLoginDto) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
        .body(userService.loginUser(userLoginDto));
  }
}
