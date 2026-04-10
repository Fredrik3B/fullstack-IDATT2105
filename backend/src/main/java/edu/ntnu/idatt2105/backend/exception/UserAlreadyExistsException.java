package edu.ntnu.idatt2105.backend.exception;

/**
 * Thrown during registration when a user with the given email already exists.
 *
 * <p>Handled by {@link GlobalExceptionHandler} and mapped to a 409 Conflict response.
 */
public class UserAlreadyExistsException extends RuntimeException {

  /**
   * Creates the exception identifying the conflicting email address.
   *
   * @param email the email that is already registered
   */
  public UserAlreadyExistsException(String email) {
    super("User already exists with email: " + email);
  }
}
