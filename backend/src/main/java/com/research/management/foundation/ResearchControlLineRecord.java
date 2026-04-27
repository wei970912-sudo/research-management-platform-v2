package com.research.management.foundation;

public record ResearchControlLineRecord(
        String id,
        String projectId,
        String name,
        String targetDate,
        String owner,
        String note
) {
}
