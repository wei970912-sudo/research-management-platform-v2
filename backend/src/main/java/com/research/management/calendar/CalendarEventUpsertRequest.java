package com.research.management.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalendarEventUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String title,
        @NotBlank String date,
        @NotBlank String time,
        @NotBlank String type,
        @NotNull Integer remindBefore,
        @NotBlank String note
) {
}
