package edu.ntnu.idatt2105.backend.temperature.mapper;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementSummaryResponse;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import org.springframework.stereotype.Component;

/**
 * Maps temperature-related entities to their response DTOs.
 */
@Component
public class TemperatureMapper {

  /**
   * Maps a {@link TemperatureZoneModel} to a {@link TemperatureZoneResponse}.
   *
   * @param zone the temperature zone entity
   * @return the zone response DTO
   */
  public TemperatureZoneResponse toZoneResponse(TemperatureZoneModel zone) {
    return new TemperatureZoneResponse(
        zone.getId(),
        IcModule.fromComplianceArea(zone.getComplianceArea()),
        zone.getName(),
        zone.getZoneType(),
        zone.getTargetMin(),
        zone.getTargetMax()
    );
  }
  /**
   * Maps a {@link TemperatureMeasurementModel} to a {@link TemperatureMeasurementResponse}.
   *
   * @param model  the measurement entity
   * @param module the IC module the measurement belongs to
   * @return the measurement response DTO with a computed {@code deviation} flag
   */
  public TemperatureMeasurementResponse toMeasurementResponse(TemperatureMeasurementModel model, IcModule module) {
    return new TemperatureMeasurementResponse(
        model.getId(),
        module,
        model.getChecklist().getId(),
        model.getTask().getId(),
        model.getValueC(),
        model.getMeasuredAt(),
        model.getPeriodKey(),
        isDeviation(model)
    );
  }

  /**
   * Maps a {@link TemperatureMeasurementModel} to a compact {@link TemperatureMeasurementSummaryResponse}
   * used in inspection report sections.
   *
   * @param model the measurement entity
   * @return the summary response DTO with a computed {@code deviation} flag
   */
  public TemperatureMeasurementSummaryResponse toSummaryResponse(TemperatureMeasurementModel model) {
    return new TemperatureMeasurementSummaryResponse(
        model.getId(),
        model.getValueC(),
        model.getMeasuredAt(),
        model.getPeriodKey(),
        isDeviation(model)
    );
  }

  /**
   * Returns {@code true} if the measured value falls outside the task template's target range.
   *
   * <p>A reading is a deviation when {@code valueC < targetMin} or {@code valueC > targetMax}.
   * If either bound is {@code null} that constraint is ignored.
   *
   * @param m the measurement to evaluate
   * @return {@code true} if the value is out of range, {@code false} otherwise
   */
  public boolean isDeviation(TemperatureMeasurementModel m) {
    if (m == null || m.getTask() == null || m.getTask().getTaskTemplate() == null) return false;
    TaskTemplate t = m.getTask().getTaskTemplate();
    if (t.getTargetMin() != null && m.getValueC().compareTo(t.getTargetMin()) < 0) return true;
    return t.getTargetMax() != null && m.getValueC().compareTo(t.getTargetMax()) > 0;
  }
}
