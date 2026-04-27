package com.research.management.schedule;

import jakarta.validation.constraints.NotBlank;

public record PhaseUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String name,
        @NotBlank String owner,
        @NotBlank String startDate,
        @NotBlank String endDate,
        @NotBlank String status
) {
}
