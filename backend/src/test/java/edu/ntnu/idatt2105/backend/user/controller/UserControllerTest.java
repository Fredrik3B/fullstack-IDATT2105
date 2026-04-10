package edu.ntnu.idatt2105.backend.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ntnu.idatt2105.backend.exception.UserAlreadyExistsException;
import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import edu.ntnu.idatt2105.backend.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DisplayName("UserController (/api/auth)")
class UserControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private UserService userService;

  private MockMvc mockMvc;

  private LoginResponse sampleResponse;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    sampleResponse = new LoginResponse(
        "access-token",
        new LoginResponse.UserInfo("user@test.com", "Test User"),
        new LoginResponse.RestaurantInfo(UUID.randomUUID(), "My Restaurant", "MYR-1234"),
        "refresh-token"
    );
  }

  @Test
  @DisplayName("POST /api/auth/register - success returns 200 with body and refresh cookie")
  void register_success_returns200WithCookie() throws Exception {
    when(userService.register(any())).thenReturn(sampleResponse);

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"user@test.com","password":"pass1234","firstName":"Test","lastName":"User"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.user.email").value("user@test.com"))
        .andExpect(header().exists(HttpHeaders.SET_COOKIE));
  }

  @Test
  @DisplayName("POST /api/auth/register - duplicate email returns 409")
  void register_duplicateEmail_returns409() throws Exception {
    when(userService.register(any()))
        .thenThrow(new UserAlreadyExistsException("user@test.com"));

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"user@test.com","password":"pass1234","firstName":"A","lastName":"B"}
                """))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /api/auth/login - success returns 200 with body and refresh cookie")
  void login_success_returns200WithCookie() throws Exception {
    when(userService.login(any())).thenReturn(sampleResponse);

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"user@test.com","password":"pass123"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(header().exists(HttpHeaders.SET_COOKIE));
  }

  @Test
  @DisplayName("POST /api/auth/login - wrong credentials returns 401")
  void login_badCredentials_returns401() throws Exception {
    when(userService.login(any()))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"wrong@test.com","password":"bad"}
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/auth/refresh - valid cookie returns 200 with new tokens")
  void refresh_withCookie_returns200() throws Exception {
    when(userService.refreshToken("refresh-token")).thenReturn(sampleResponse);

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new MockCookie("refreshToken", "refresh-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"));
  }

  @Test
  @DisplayName("POST /api/auth/refresh - no cookie returns 401")
  void refresh_noCookie_returns401() throws Exception {
    mockMvc.perform(post("/api/auth/refresh"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/auth/refresh - expired token returns 401")
  void refresh_expiredToken_returns401() throws Exception {
    when(userService.refreshToken("expired-token"))
        .thenThrow(new ExpiredJwtException(null, null, "expired"));

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new MockCookie("refreshToken", "expired-token")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/auth/logout - returns 200 and clears cookie")
  void logout_returns200WithClearedCookie() throws Exception {
    mockMvc.perform(post("/api/auth/logout"))
        .andExpect(status().isOk())
        .andExpect(header().exists(HttpHeaders.SET_COOKIE));
  }
}
