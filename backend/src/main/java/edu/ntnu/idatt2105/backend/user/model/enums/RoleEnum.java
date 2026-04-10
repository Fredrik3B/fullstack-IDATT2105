package edu.ntnu.idatt2105.backend.user.model.enums;

/**
 * Application roles that determine a user's permissions within an organisation.
 *
 * <p>Roles are stored as strings in the database and converted to Spring Security
 * authorities with a {@code "ROLE_"} prefix by
 * {@link edu.ntnu.idatt2105.backend.security.UserPrincipal}.
 */
public enum RoleEnum {
  /**
   * Full administrative access; can manage members, roles and organisation settings.
   */
  ADMIN,
  /**
   * Can manage checklists, tasks, and review join requests.
   */
  MANAGER,
  /**
   * Human-resources role with access to staff management features.
   */
  HR,
  /**
   * Default role assigned to every new user on registration.
   */
  STAFF
}
