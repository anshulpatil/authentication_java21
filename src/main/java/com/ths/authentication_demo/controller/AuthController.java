package com.ths.authentication_demo.controller;

import com.ths.authentication_demo.dto.*;
import com.ths.authentication_demo.entity.RefreshToken;
import com.ths.authentication_demo.entity.User;
import com.ths.authentication_demo.jwt.JwtHelper;
import com.ths.authentication_demo.service.RefreshTokenService;
import com.ths.authentication_demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest requestDto) {
    log.info("User Logging in {}", requestDto.email());
    userService.signup(requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping(value = "/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    try {
      log.info("User logging in {}", request.email());
      authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
      );
    } catch (BadCredentialsException e) {
      System.out.println("Bad Credentials");
      log.error("Bad Credentials");
      return ResponseEntity.ok(LoginResponse.builder().status("Bad Credentials").build());
    } catch (Exception e) {
      log.error("Error logging in : {}",e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginResponse.builder().status("Bad Credentials").build());
    }

    String token = JwtHelper.generateToken(request.email());
    User user = userService.findByEmail(request.email());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
    log.info("Token {}", token);
    return ResponseEntity.ok(
      LoginResponse
        .builder()
        .email(request.email())
        .status("Success")
        .token(token)
        .refreshToken(refreshToken.getToken())
        .build()
    );
  }

  @PostMapping("/forget-password")
  public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordDto request) {
    log.info("Reset password for : {}", request.getEmail());
    ResetPasswordResponse.ResetPasswordResponseBuilder response = ResetPasswordResponse.builder();

    User user = null;
    try {
      user = userService.findByEmail(request.getEmail());
    } catch (UsernameNotFoundException e) {
      response.status("Error");
      response.message("User not found by email");
      return ResponseEntity.ok(response.build());
    }
    if (user != null) {
      if(request.getNewPassword().equals(request.getConfirmNewPassword())) {
        userService.resetPassword(user, request.getNewPassword());
        response.status("Success");
        response.message("Password reset successfully");
      } else {
        response.status("Error");
        response.message("New password and confirm password do not match");
      }
    }
    return ResponseEntity.ok(response.build());
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
    log.info("refresh token : {}", request.getRefreshToken());
    return refreshTokenService
      .findByToken(request)
      .map(refreshTokenService::verifyExpiration)
      .map(RefreshToken::getUser)
      .map(
        user -> {
          String token = JwtHelper.generateToken(user.getEmail());
          return ResponseEntity.ok(
            TokenRefreshResponse
              .builder()
              .token(token)
              .refreshToken(request.getRefreshToken())
              .build()
          );
        }
      )
      .orElseThrow(() -> new RuntimeException("Refresh token not in database"));
  }

  @GetMapping("/log/{content}")
  public ResponseEntity<String> log(@PathVariable("content") String content) {
    log.info("This is the log : {}", content);
    return ResponseEntity.ok("Hello " + content);
  }
}
