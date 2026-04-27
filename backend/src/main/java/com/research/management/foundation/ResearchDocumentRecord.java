package com.research.management.foundation;

public record ResearchDocumentRecord(
        String id,
        String projectId,
        String nodeName,
        int attachmentCount,
        String status,
        String owner,
        String targetDate,
        String description,
        String notes,
        String attachmentNames
) {
}
