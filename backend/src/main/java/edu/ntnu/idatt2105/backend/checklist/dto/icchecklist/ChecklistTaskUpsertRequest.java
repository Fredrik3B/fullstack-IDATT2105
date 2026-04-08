package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChecklistTaskUpsertRequest(
	String id,
	String label,
	String meta,
	String type,
	String unit,
	BigDecimal targetMin,
	BigDecimal targetMax,
	String state
) {
}

