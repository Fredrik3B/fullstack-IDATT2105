package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChecklistSection {
  private int totalChecklists;
  private int activeChecklists;
  private List<ChecklistRecord> checklists;
}
