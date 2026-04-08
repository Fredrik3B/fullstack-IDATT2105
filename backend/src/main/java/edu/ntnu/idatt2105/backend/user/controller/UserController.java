package edu.ntnu.idatt2105.backend.user.controller;

import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import edu.ntnu.idatt2105.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@Tag(name = "Authentication", description = "Register, login, refresh token, and logout")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

  @Value("${app.cookie-secure:true}")
  private boolean cookieSecure;

  private final UserService userService;


  @Operation(summary = "Register a new user",
      description = "Creates a user with STAFF role. Returns access token, refresh token in HttpOnly cookie.")
  @ApiResponse(responseCode = "200", description = "Registration successful")
  @ApiResponse(responseCode = "409", description = "Email already in use")
  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@RequestBody @Valid CreateUserRequest request) {
    return sendWithCookie(userService.register(request));
  }

  @Operation(summary = "Login with email and password")
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    return sendWithCookie(userService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
      @CookieValue(name = "refreshToken", required = false) String refreshToken
  ) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return sendWithCookie(userService.refreshToken(refreshToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/api/auth/refresh")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .build();
  }

  private ResponseEntity<LoginResponse> sendWithCookie(LoginResponse response) {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
        .httpOnly(true).secure(cookieSecure)
        .path("/api/auth/refresh").maxAge(Duration.ofDays(7)).sameSite("Lax")
        .build();
    response.setRefreshToken(null);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(response);
  }

  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/api/auth/refresh")
        .maxAge(Duration.ofDays(7))
        .sameSite("Lax")
        .build();
  }
}
