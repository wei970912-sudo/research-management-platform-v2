package com.research.management.calendar;

public record CalendarEventResponse(
        String id,
        String title,
        String date,
        String time,
        String type,
        String projectName,
        Integer remindBefore,
        String note,
        String projectId
) {
}
