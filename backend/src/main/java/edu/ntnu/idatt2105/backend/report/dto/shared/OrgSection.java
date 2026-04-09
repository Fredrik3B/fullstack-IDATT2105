package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Schema(description = "Organization details included in the report")
public record OrgSection(

    @Schema(description = "Organization name")
    String name,

    @Schema(description = "Names of all admins")
    List<String> adminNames,

    @Schema(description = "Names of all managers")
    List<String> managerNames,

    @Schema(description = "Total number of staff members")
    int totalStaff
) {}
