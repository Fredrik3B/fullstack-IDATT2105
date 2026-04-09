package edu.ntnu.idatt2105.backend.exception;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>Maps domain and framework exceptions to RFC 7807 {@link ProblemDetail} responses,
 * providing consistent JSON error bodies across the entire API without
 * requiring try/catch blocks in individual controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles {@link ResourceNotFoundException} — returns 404.
   *
   * @param e the exception
   * @return a 404 problem detail
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleResourceNotFound(ResourceNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /**
   * Handles {@link ExpiredJwtException} — returns 401.
   *
   * @param e the exception
   * @return a 401 problem detail
   */
  @ExceptionHandler(ExpiredJwtException.class)
  public ProblemDetail handleExpiredJwt(ExpiredJwtException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Token expired");
  }

  /**
   * Handles {@link UserAlreadyExistsException} — returns 409.
   *
   * @param e the exception
   * @return a 409 conflict problem detail
   */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, e.getMessage()
    );
    problem.setTitle("User already exists");
    return problem;
  }

  /**
   * Handles {@link BadCredentialsException} — returns 401.
   *
   * @param e the exception
   * @return a 401 problem detail
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED, e.getMessage()
    );
  }

  /**
   * Handles {@link IllegalArgumentException} — returns 400.
   *
   * @param e the exception
   * @return a 400 bad request problem detail
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, e.getMessage()
    );
    problem.setTitle("Bad request");
    return problem;
  }

  /**
   * Handles Bean Validation failures — returns 400 with the first failing field message.
   *
   * @param e the exception containing field error details
   * @return a 400 problem detail with the first validation error message
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
            (existing, duplicate) -> existing
        ));

    List<String> globalErrors = e.getBindingResult().getGlobalErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .toList();

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "One or more fields failed validation.");
    problem.setTitle("Validation failed");

    if (!fieldErrors.isEmpty()) problem.setProperty("errors", fieldErrors);
    if (!globalErrors.isEmpty()) problem.setProperty("globalErrors", globalErrors);

    return problem;
  }

  /**
   * Handles {@link IllegalStateException} — returns 409 to signal a conflicting state.
   *
   * @param e the exception
   * @return a 409 conflict problem detail
   */
  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalState(IllegalStateException e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, e.getMessage()
    );
  }

  /**
   * Handles {@link OrganizationRequiredException} — returns 403 when a user
   * attempts an organization-scoped action without belonging to one.
   *
   * @param e the exception
   * @return a 403 forbidden problem detail
   */
  @ExceptionHandler(OrganizationRequiredException.class)
  public ProblemDetail handleOrganizationRequired(OrganizationRequiredException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleUnreadableMessage(HttpMessageNotReadableException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, e.getMessage());
    problem.setTitle("Malformed request");
    return problem;
  }
}
