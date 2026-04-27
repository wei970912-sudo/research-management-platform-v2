package com.research.management.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ControlPointItemUpsertRequest(
        @NotBlank String controlPointId,
        @NotBlank String name,
        @NotNull Boolean completed,
        @NotNull Integer sortOrder
) {
}
