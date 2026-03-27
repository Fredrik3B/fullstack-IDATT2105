package no.ntnu.resturant_manager.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank
        @Size(max = 120)
        String title,

        @Size(max = 1000)
        String description,

        @Min(0)
        int orderIndex,

        Boolean requiredTask,

        Boolean active,

        @NotNull
        Long checklistId
) {}