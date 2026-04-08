package edu.ntnu.idatt2105.backend.temperature.mapper;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import org.springframework.stereotype.Component;

@Component
public class TemperatureMapper {
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

  public boolean isDeviation(TemperatureMeasurementModel m) {
    if (m == null || m.getTask() == null || m.getTask().getTaskTemplate() == null) return false;
    TaskTemplate t = m.getTask().getTaskTemplate();
    if (t.getTargetMin() != null && m.getValueC().compareTo(t.getTargetMin()) < 0) return true;
    return t.getTargetMax() != null && m.getValueC().compareTo(t.getTargetMax()) > 0;
  }
}
