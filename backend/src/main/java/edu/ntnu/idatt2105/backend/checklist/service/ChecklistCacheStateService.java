package edu.ntnu.idatt2105.backend.checklist.service;

import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModuleState;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistModuleStateRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tracks the last-modified timestamp for each organisation's checklist module.
 *
 * <p>Used by the checklist controller to support HTTP conditional requests
 * ({@code Last-Modified} / {@code If-Modified-Since}). Every mutating checklist
 * operation calls {@link #touch} to advance the timestamp so clients know to
 * re-fetch.
 *
 * <p>Timestamps are kept at second precision to comply with the HTTP date header format.
 * If two mutations arrive within the same second the timestamp is advanced by one second
 * to guarantee strict monotonic increase.
 */
@Service
@AllArgsConstructor
public class ChecklistCacheStateService {

	private final ChecklistModuleStateRepository checklistModuleStateRepository;

	/**
	 * Returns the last-modified {@link Instant} for the given organisation's module.
	 *
	 * <p>Creates a new state row with the current second if none exists yet.
	 *
	 * @param organizationId the organisation
	 * @param complianceArea the compliance module
	 * @return the last-modified instant (second precision)
	 */
	@Transactional
	public Instant getLastModified(UUID organizationId, ComplianceArea complianceArea) {
		UUID safeOrganizationId = Objects.requireNonNull(organizationId, "organizationId is required.");
		ComplianceArea safeComplianceArea = Objects.requireNonNull(complianceArea, "complianceArea is required.");

		ChecklistModuleState state = checklistModuleStateRepository
			.findByOrganizationIdAndComplianceArea(safeOrganizationId, safeComplianceArea)
			.orElseGet(() -> createState(safeOrganizationId, safeComplianceArea, currentHttpSecond()));

		if (state.getModifiedAt() == null) {
			state.setModifiedAt(currentHttpSecond());
			state = checklistModuleStateRepository.save(state);
		}

		return state.getModifiedAt().atZone(ZoneId.systemDefault()).toInstant();
	}

	/**
	 * Advances the last-modified timestamp for the given organisation's module.
	 *
	 * <p>Called after every mutating checklist or task operation so that
	 * subsequent conditional GET requests return fresh data.
	 *
	 * @param organizationId the organisation whose state to touch
	 * @param complianceArea the module whose state to touch
	 */
	@Transactional
	public void touch(UUID organizationId, ComplianceArea complianceArea) {
		UUID safeOrganizationId = Objects.requireNonNull(organizationId, "organizationId is required.");
		ComplianceArea safeComplianceArea = Objects.requireNonNull(complianceArea, "complianceArea is required.");

		ChecklistModuleState state = checklistModuleStateRepository
			.findByOrganizationIdAndComplianceArea(safeOrganizationId, safeComplianceArea)
			.orElseGet(() -> createState(safeOrganizationId, safeComplianceArea, currentHttpSecond()));

		state.setModifiedAt(nextModifiedAt(state.getModifiedAt()));
		checklistModuleStateRepository.save(state);
	}

	/**
	 * Persists a new {@link ChecklistModuleState} row.
	 *
	 * @param organizationId the organisation
	 * @param complianceArea the module
	 * @param modifiedAt     the initial modified timestamp
	 * @return the saved state entity
	 */
	private ChecklistModuleState createState(UUID organizationId, ComplianceArea complianceArea, LocalDateTime modifiedAt) {
		ChecklistModuleState created = new ChecklistModuleState();
		created.setOrganizationId(organizationId);
		created.setComplianceArea(complianceArea);
		created.setModifiedAt(modifiedAt);
		return checklistModuleStateRepository.save(created);
	}

	/**
	 * Returns the next monotonically increasing modified timestamp.
	 *
	 * <p>If the current second is strictly after the previous modified second,
	 * the current second is used. Otherwise the previous second is incremented
	 * by one to guarantee strict ordering.
	 *
	 * @param previousModifiedAt the current stored timestamp, may be {@code null}
	 * @return the next modified timestamp
	 */
	private LocalDateTime nextModifiedAt(LocalDateTime previousModifiedAt) {
		LocalDateTime currentSecond = currentHttpSecond();
		if (previousModifiedAt == null) {
			return currentSecond;
		}

		LocalDateTime previousSecond = previousModifiedAt.truncatedTo(ChronoUnit.SECONDS);
		if (currentSecond.isAfter(previousSecond)) {
			return currentSecond;
		}
		return previousSecond.plusSeconds(1);
	}

	/**
	 * Returns the current local time truncated to seconds, matching HTTP date header precision.
	 *
	 * @return the current second
	 */
	private LocalDateTime currentHttpSecond() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}
}
