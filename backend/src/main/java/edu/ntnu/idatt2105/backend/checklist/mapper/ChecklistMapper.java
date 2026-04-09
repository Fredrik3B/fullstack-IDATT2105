package edu.ntnu.idatt2105.backend.checklist.mapper;


import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistSectionResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementSummaryResponse;
import edu.ntnu.idatt2105.backend.temperature.mapper.TemperatureMapper;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Maps checklist and task entities to their response DTOs.
 *
 * <p>Groups task items into labelled sections based on their
 * {@link edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes}, and resolves
 * the human-readable status label and tone shown on each checklist card.
 */
@Component
@RequiredArgsConstructor
public class ChecklistMapper {

  private final TemperatureMapper temperatureMapper;

  /**
   * Builds the full card response for a checklist, including progress and sections.
   */
  public ChecklistCardResponse toCardResponse(
      ChecklistModel checklist,
      String activePeriodKey,
      List<TasksModel> tasks,
      Map<Long, TemperatureMeasurementModel> measurementsByTaskId,
      boolean shouldLoadActiveTasks
  ) {
    int completedCount = (int) tasks.stream().filter(TasksModel::isCompleted).count();
    int total = tasks.size();
    int progress = total == 0 ? 0 : Math.round((completedCount * 100f) / total);
    boolean allCompleted = total > 0 && completedCount == total;

    List<ChecklistSectionResponse> sections = shouldLoadActiveTasks
        ? toSectionResponses(tasks, measurementsByTaskId)
        : toTemplateSectionResponses(checklist);

    String statusLabel = resolveStatusLabel(checklist, shouldLoadActiveTasks, allCompleted);
    String statusTone = resolveStatusTone(checklist, shouldLoadActiveTasks, allCompleted);

    return new ChecklistCardResponse(
        checklist.getId(),
        PeriodKeyUtil.frequencyToPeriod(checklist.getFrequency()),
        activePeriodKey,
        checklist.isRecurring(),
        checklist.isDisplayedOnWorkbench(),
        checklist.getName(),
        checklist.getDescription() != null ? checklist.getDescription() : "",
        null,
        Boolean.FALSE,
        statusLabel,
        statusTone,
        progress,
        sections
    );
  }

  /**
   * Maps a persisted {@link edu.ntnu.idatt2105.backend.task.model.TasksModel} to its response DTO,
   * including any associated temperature measurement summary.
   *
   * @param task        the task entity
   * @param measurement the most recent measurement for this task, or {@code null}
   * @return the task item response
   */
  public ChecklistTaskItemResponse toTaskItemResponse(TasksModel task, TemperatureMeasurementModel measurement) {
    TaskTemplate template = task.getTaskTemplate();
    String state = "todo";
    String completedForPeriodKey = null;
    LocalDateTime completedAt = null;
    String pendingForPeriodKey = null;
    Boolean highlighted = null;

    if (task.isCompleted()) {
      state = "completed";
      completedForPeriodKey = task.getPeriodKey();
      completedAt = task.getEndedAt();
    } else if (task.isFlagged()) {
      state = "pending";
      pendingForPeriodKey = task.getPeriodKey();
      highlighted = Boolean.TRUE;
    }

    return new ChecklistTaskItemResponse(
        task.getId(),
        template.getId(),
        template.getTitle(),
        task.getMeta(),
        isTemperatureTemplate(template) ? "temperature" : null,
        template.getUnit(),
        template.getTargetMin(),
        template.getTargetMax(),
        state,
        highlighted,
        completedForPeriodKey,
        completedAt,
        pendingForPeriodKey,
        toMeasurementSummary(measurement)
    );
  }

  /**
   * Maps a {@link edu.ntnu.idatt2105.backend.task.model.TaskTemplate} to a placeholder task item
   * response with {@code state = "todo"} — used when the checklist is not on the workbench.
   *
   * @param template the task template
   * @return the placeholder task item response
   */
  public ChecklistTaskItemResponse toTemplateTaskItemResponse(TaskTemplate template) {
    return new ChecklistTaskItemResponse(
        null,
        template.getId(),
        template.getTitle(),
        template.getMeta(),
        isTemperatureTemplate(template) ? "temperature" : null,
        template.getUnit(),
        template.getTargetMin(),
        template.getTargetMax(),
        "todo",
        Boolean.FALSE,
        null, null, null, null
    );
  }


  /**
   * Groups active task instances into labelled {@link ChecklistSectionResponse}s.
   *
   * @param tasks                  the active task instances for the current period
   * @param measurementsByTaskId   map of the latest temperature measurement keyed by task ID
   * @return the ordered list of section responses
   */
  public List<ChecklistSectionResponse> toSectionResponses(
      List<TasksModel> tasks,
      Map<Long, TemperatureMeasurementModel> measurementsByTaskId
  ) {
    Map<String, List<TasksModel>> grouped = new LinkedHashMap<>();
    for (TasksModel task : tasks) {
      grouped.computeIfAbsent(sectionLabel(task.getTaskTemplate().getSectionType()), k -> new ArrayList<>()).add(task);
    }

    return grouped.entrySet().stream()
        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
        .map(entry -> {
          entry.getValue().sort(Comparator.comparing(t -> t.getTaskTemplate().getTitle(), String.CASE_INSENSITIVE_ORDER));
          return new ChecklistSectionResponse(
              entry.getKey(),
              entry.getValue().stream()
                  .map(t -> toTaskItemResponse(t, measurementsByTaskId.get(t.getId())))
                  .toList()
          );
        })
        .toList();
  }

  /**
   * Groups the checklist's task templates into labelled sections for library view
   * (no active task instances — all states default to {@code "todo"}).
   *
   * @param checklist the checklist whose templates to render
   * @return the ordered list of section responses
   */
  public List<ChecklistSectionResponse> toTemplateSectionResponses(ChecklistModel checklist) {
    Map<String, List<TaskTemplate>> grouped = new LinkedHashMap<>();
    for (TaskTemplate template : checklist.getTaskTemplates()) {
      grouped.computeIfAbsent(sectionLabel(template.getSectionType()), k -> new ArrayList<>()).add(template);
    }

    return grouped.entrySet().stream()
        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
        .map(entry -> {
          entry.getValue().sort(Comparator.comparing(TaskTemplate::getTitle, String.CASE_INSENSITIVE_ORDER));
          return new ChecklistSectionResponse(
              entry.getKey(),
              entry.getValue().stream().map(this::toTemplateTaskItemResponse).toList()
          );
        })
        .toList();
  }

  /**
   * Returns a comparator that sorts task templates by section label then by title,
   * both case-insensitive.
   *
   * @return the task template comparator
   */
  public Comparator<TaskTemplate> taskTemplateComparator() {
    return Comparator
        .comparing((TaskTemplate t) -> sectionLabel(t.getSectionType()), String.CASE_INSENSITIVE_ORDER)
        .thenComparing(TaskTemplate::getTitle, String.CASE_INSENSITIVE_ORDER);
  }

  private TemperatureMeasurementSummaryResponse toMeasurementSummary(TemperatureMeasurementModel m) {
    return m == null ? null : temperatureMapper.toSummaryResponse(m);
  }

  private boolean isTemperatureTemplate(TaskTemplate t) {
    return t.getUnit() != null || t.getTargetMin() != null || t.getTargetMax() != null;
  }

  /**
   * Converts a {@link SectionTypes} constant to a human-readable title-case label.
   * Returns {@code "Tasks"} for {@code null}.
   *
   * @param sectionType the section type, may be {@code null}
   * @return the formatted section label
   */
  public String sectionLabel(SectionTypes sectionType) {
    if (sectionType == null) return "Tasks";
    String[] parts = sectionType.name().split("_");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) sb.append(' ');
      String p = parts[i].toLowerCase();
      sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
    }
    return sb.toString();
  }

  private String resolveStatusLabel(ChecklistModel cl, boolean active, boolean completed) {
    if (!cl.isDisplayedOnWorkbench()) return "In library";
    if (!active) return "Waiting for next period";
    return completed ? "Ready to submit" : "In progress";
  }

  private String resolveStatusTone(ChecklistModel cl, boolean active, boolean completed) {
    if (!cl.isDisplayedOnWorkbench() || !active) return "muted";
    return completed ? "success" : "muted";
  }
}