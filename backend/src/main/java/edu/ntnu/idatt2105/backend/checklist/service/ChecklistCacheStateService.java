package edu.ntnu.idatt2105.backend.checklist.service;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import java.time.Instant;
import java.util.UUID;

public interface ChecklistCacheStateService {

	Instant getLastModified(UUID organizationId, ComplianceArea complianceArea);

	void touch(UUID organizationId, ComplianceArea complianceArea);
}
