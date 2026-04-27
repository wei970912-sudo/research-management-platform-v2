package com.research.management.meeting;

public record MeetingSummary(
        String id,
        String topic,
        String projectName,
        String date,
        String host,
        String type,
        String location,
        String attendees,
        String summary,
        String attachmentNames
) {
}
