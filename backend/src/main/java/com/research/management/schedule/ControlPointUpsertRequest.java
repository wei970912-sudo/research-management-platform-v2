package com.research.management.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ControlPointUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String controlLineId,
        @NotBlank String name,
        @NotBlank String plannedDate,
        @NotBlank String status,
        @NotNull Integer sortOrder
) {
}
