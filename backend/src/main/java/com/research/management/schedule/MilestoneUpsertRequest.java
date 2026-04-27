package com.research.management.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MilestoneUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String phaseId,
        @NotBlank String name,
        @NotBlank String date,
        @NotBlank String status,
        @NotNull Boolean critical
) {
}

