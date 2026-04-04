package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgSection {
  private String name;
  private List<String> adminNames;
  private List<String> managerNames;
  private int totalStaff;
}
