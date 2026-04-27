package com.research.management.meeting;

import jakarta.validation.constraints.NotBlank;

public record MeetingUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String topic,
        @NotBlank String date,
        @NotBlank String host,
        @NotBlank String type,
        @NotBlank String location,
        @NotBlank String attendees,
        @NotBlank String summary,
        String attachmentNames
) {
}
