package edu.ntnu.idatt2105.backend.task.mapper;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.task.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import org.springframework.stereotype.Component;

/**
 * Maps {@link TaskTemplate} and {@link edu.ntnu.idatt2105.backend.task.model.TasksModel}
 * entities to their response DTOs.
 */
@Component
public class TaskMapper {

  /**
   * Maps a {@link TaskTemplate} to a {@link TaskResponse}.
   *
   * @param template the task template entity
   * @return the task response DTO
   */
  public TaskResponse toResponse(TaskTemplate template) {
    TemperatureZoneModel zone = template.getTemperatureZone();
    return new TaskResponse(
        template.getId(),
        IcModule.fromComplianceArea(template.getComplianceArea()),
        template.getTitle(),
        template.getMeta(),
        template.getSectionType(),
        zone != null ? zone.getId() : null,
        zone != null ? zone.getName() : null,
        zone != null ? zone.getZoneType() : null,
        template.getUnit(),
        template.getTargetMin(),
        template.getTargetMax()
    );
  }

  /**
   * Maps a deviated {@link edu.ntnu.idatt2105.backend.task.model.TasksModel} to an
   * {@link UnresolvedItemDto} for use in inspection reports.
   *
   * @param task the task instance that was not completed
   * @return the unresolved item DTO
   */
  public UnresolvedItemDto toUnresolvedDto(TasksModel task) {
    return new UnresolvedItemDto(task.getTaskTemplate().getTitle(), task.getEndedAt());
  }
}
