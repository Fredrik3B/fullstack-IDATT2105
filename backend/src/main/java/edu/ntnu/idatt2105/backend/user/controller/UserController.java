package edu.ntnu.idatt2105.backend.user.controller;

import edu.ntnu.idatt2105.backend.security.JwtService;
import edu.ntnu.idatt2105.backend.user.dto.AuthDto;
import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.service.UserService;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@RequestBody @Valid CreateUserRequest request) {
    AuthDto result = userService.register(request);
    return buildLoginResponse(result);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    AuthDto result = userService.login(request);
    return buildLoginResponse(result);
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
      @CookieValue(name = "refreshToken", required = false) String refreshToken
  ) {
    if (refreshToken != null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    AuthDto result = userService.refreshToken(refreshToken);
    return buildLoginResponse(result);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
        .httpOnly(true)
        .secure(false)
        .path("/api/auth/refresh")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .build();
  }

  private ResponseEntity<LoginResponse> buildLoginResponse(AuthDto result) {
    ResponseCookie cookie = createRefreshTokenCookie(result.getRefreshToken());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new LoginResponse( result.getEmail(), result.getAccessToken()));
  }

  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // should be changed
        .path("/api/users/refresh")
        .maxAge(Duration.ofDays(7))
        .sameSite("Lax")
        .build();
  }
}
