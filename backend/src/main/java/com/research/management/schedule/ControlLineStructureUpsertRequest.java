package com.research.management.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ControlLineStructureUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String name,
        @NotBlank String targetDate,
        @NotBlank String owner,
        @NotBlank String note,
        @Valid @NotNull List<ControlPointDraft> points
) {

    public record ControlPointDraft(
            String id,
            @NotBlank String name,
            @NotBlank String plannedDate,
            @NotBlank String status,
            @Valid @NotNull List<ControlPointItemDraft> items
    ) {
    }

    public record ControlPointItemDraft(
            String id,
            @NotBlank String name,
            @NotNull Boolean completed
    ) {
    }
}
