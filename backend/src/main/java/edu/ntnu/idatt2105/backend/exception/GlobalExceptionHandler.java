package edu.ntnu.idatt2105.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, e.getMessage()
    );
    problem.setTitle("User already exists");
    return problem;
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED, e.getMessage()
    );
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, e.getMessage()
    );
    problem.setTitle("Bad request");
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .orElse("Request validation failed.");
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    problem.setTitle("Validation failed");
    return problem;
  }
}
