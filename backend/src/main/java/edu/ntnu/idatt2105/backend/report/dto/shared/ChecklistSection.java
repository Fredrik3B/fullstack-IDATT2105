package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Report section summarising checklist activity within the report period.
 */
@Data
@Builder
public class ChecklistSection {
  private int totalChecklists;
  private int activeChecklists;
  private List<ChecklistRecord> checklists;
}
