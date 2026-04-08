package edu.ntnu.idatt2105.backend.task.mapper;

import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

  public UnresolvedItemDto toUnresolvedDto(TasksModel task) {
    return new UnresolvedItemDto(task.getTaskTemplate().getTitle(), task.getEndedAt());
  }
}
