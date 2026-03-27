package edu.ntnu.idatt2105.backend.exception;

public class UserAlreadyExistsException extends RuntimeException {

  public UserAlreadyExistsException(String email) {
    super("User already exists with email: " + email);
  }
}
