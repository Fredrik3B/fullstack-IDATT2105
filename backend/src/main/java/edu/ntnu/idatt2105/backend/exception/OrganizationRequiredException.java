package edu.ntnu.idatt2105.backend.exception;

/**
 * Thrown when a user performs an action that requires organization membership,
 * but the user has not yet joined any organization.
 *
 * <p>Handled by {@link GlobalExceptionHandler} and mapped to a 403 Forbidden response.
 */
public class OrganizationRequiredException extends RuntimeException {

  /** Creates the exception with a fixed human-readable message. */
  public OrganizationRequiredException() {
    super("Organization membership required");
  }
}
