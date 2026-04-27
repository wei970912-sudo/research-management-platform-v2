package com.research.management.schedule;

import jakarta.validation.constraints.NotBlank;

public record ControlLineUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String name,
        @NotBlank String targetDate,
        @NotBlank String owner,
        @NotBlank String note
) {
}
