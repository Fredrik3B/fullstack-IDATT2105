package edu.ntnu.idatt2105.backend.task.validation;

import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.task.dto.CreateTaskRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TemperatureZoneRequiredValidator implements
    ConstraintValidator<TemperatureZoneRequiredIfTemperature, CreateTaskRequest> {

  @Override
  public boolean isValid(CreateTaskRequest req, ConstraintValidatorContext ctx) {
    if (req.sectionType() == SectionTypes.TEMPERATURE_CONTROL) {
      return req.temperatureZoneId() != null;
    }
    return true;
  }
}
