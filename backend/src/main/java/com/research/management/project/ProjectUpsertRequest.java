package com.research.management.project;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProjectUpsertRequest(
        @NotBlank String name,
        @NotBlank String owner,
        @NotBlank String status,
        @NotBlank String currentStage,
        @Min(0) @Max(100) Integer milestoneCompletionRate,
        @NotBlank String description
) {
}

