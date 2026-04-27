package com.research.management.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String nodeName,
        @NotNull Integer attachmentCount,
        @NotBlank String status,
        @NotBlank String owner,
        @NotBlank String targetDate,
        @NotBlank String description,
        @NotBlank String notes,
        String attachmentNames
) {
}
