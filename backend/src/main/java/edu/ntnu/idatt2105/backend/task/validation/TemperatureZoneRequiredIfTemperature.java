package edu.ntnu.idatt2105.backend.task.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TemperatureZoneRequiredValidator.class)
public @interface TemperatureZoneRequiredIfTemperature {

  String message() default "temperatureZoneId is required when sectionType is TEMPERATURE_CONTROL";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
