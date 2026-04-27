package com.research.management.document;

public record DocumentSummary(
        String id,
        String nodeName,
        String projectName,
        int attachmentCount,
        String status,
        String owner,
        String targetDate,
        String description,
        String notes,
        String attachmentNames,
        String projectId
) {
}
