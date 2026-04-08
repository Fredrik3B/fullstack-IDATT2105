package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;

public enum IcModule {
	IC_FOOD,
	IC_ALCOHOL;

	public ComplianceArea toComplianceArea() {
		return switch (this) {
			case IC_FOOD -> ComplianceArea.IK_MAT;
			case IC_ALCOHOL -> ComplianceArea.IK_ALKOHOL;
		};
	}
}

