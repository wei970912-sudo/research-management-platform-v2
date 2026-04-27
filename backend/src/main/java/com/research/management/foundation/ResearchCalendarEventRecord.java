package com.research.management.foundation;

public record ResearchCalendarEventRecord(
        String id,
        String projectId,
        String title,
        String date,
        String time,
        String type,
        Integer remindBefore,
        String note
) {
}
