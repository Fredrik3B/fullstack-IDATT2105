package edu.ntnu.idatt2105.backend.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ntnu.idatt2105.backend.shared.enums.DeviationSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * DTO representing a report from the Norwegian food administration.
 *
 * @see <a href="https://mattilsynet-xp7prod.enonic.cloud/_/attachment/inline/5578f5d7-af3b-4089-8f9e-57140277918a:24c68bef096002490e3d7b40947240eaa1ed629a/Skjema%20for%20%C3%A5%20registrere%20avvik.pdf">
 *   Deviantion report pdf</a>
 */
@Schema(description = "Request to create a new deviation report")
@Data
public class DeviationReport {

  @NotBlank
  @Schema(description = "Deviation for")
  private String deviationName;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Schema(description = "Severity of the deviation", example = "MINOR")
  private DeviationSeverity severity;

  @NotNull
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  @Schema(description = "Time of when the deviation happened", example = "2026-04-09T21:00")
  private LocalDateTime occurredAt;

  @NotBlank
  @Schema(description = "Deviation noticed by")
  private String noticedBy;

  @NotBlank
  @Schema(description = "Deviation was reported to")
  private String reportedTo;

  @NotBlank
  @Schema(description = "Deviation was handled by")
  private String processedBy;

  @NotBlank
  @Schema(description = "Description of what went wrong")
  private String description;

  @NotBlank
  @Schema(description = "Actions immediately taken")
  private String immediateAction;

  @NotBlank
  @Schema(description = "Believed cause for deviation to happen")
  private String believedCause;

  @NotBlank
  @Schema(description = "Measures implemented for non-repeating of deviations")
  private String correctiveMeasures;

  @NotBlank
  @Schema(description = "Measures already done")
  private String correctiveMeasuresDone;
}
