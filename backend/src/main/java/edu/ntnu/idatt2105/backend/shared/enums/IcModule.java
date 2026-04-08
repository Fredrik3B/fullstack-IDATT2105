package edu.ntnu.idatt2105.backend.shared.enums;

public enum IcModule {
	IC_FOOD,
	IC_ALCOHOL;

	public static IcModule fromComplianceArea(ComplianceArea complianceArea) {
		return complianceArea == ComplianceArea.IK_ALKOHOL ? IC_ALCOHOL : IC_FOOD;	}

	public ComplianceArea toComplianceArea() {
		return switch (this) {
			case IC_FOOD -> ComplianceArea.IK_MAT;
			case IC_ALCOHOL -> ComplianceArea.IK_ALKOHOL;
		};
	}
}

