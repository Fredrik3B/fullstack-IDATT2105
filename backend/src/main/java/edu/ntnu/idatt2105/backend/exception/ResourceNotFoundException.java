package edu.ntnu.idatt2105.backend.exception;

/**
 * Thrown when a requested resource cannot be found in the database.
 *
 * <p>Handled by {@link GlobalExceptionHandler} and mapped to a 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Creates the exception with a descriptive message.
   *
   * @param message a human-readable description of which resource was not found
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
