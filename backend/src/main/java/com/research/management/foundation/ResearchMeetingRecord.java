package com.research.management.foundation;

public record ResearchMeetingRecord(
        String id,
        String projectId,
        String topic,
        String date,
        String host,
        String type,
        String location,
        String attendees,
        String summary,
        String attachmentNames
) {
}
