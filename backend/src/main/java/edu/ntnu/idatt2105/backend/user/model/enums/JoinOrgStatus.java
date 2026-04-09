package edu.ntnu.idatt2105.backend.user.model.enums;

/**
 * Lifecycle states of a {@link edu.ntnu.idatt2105.backend.user.model.JoinRequestModel}.
 */
public enum JoinOrgStatus {
  /** The request has been submitted and is awaiting review by an admin or manager. */
  PENDING,
  /** An admin or manager accepted the request; the user is now a member. */
  ACCEPTED,
  /** An admin or manager declined the request. */
  DECLINED
}
