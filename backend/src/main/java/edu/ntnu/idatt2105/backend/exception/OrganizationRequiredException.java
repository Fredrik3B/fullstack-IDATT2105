package edu.ntnu.idatt2105.backend.exception;

public class OrganizationRequiredException extends RuntimeException {
  public OrganizationRequiredException() {
    super("Organization membership required");
  }
}
