package com.research.management.stakeholder;

import jakarta.validation.constraints.NotBlank;

public record StakeholderUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String stakeholderName,
        @NotBlank String relatedWorkType,
        @NotBlank String relatedWorkValue,
        @NotBlank String mainContent
) {
}
