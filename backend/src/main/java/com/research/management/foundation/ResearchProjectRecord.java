package com.research.management.foundation;

public record ResearchProjectRecord(
        String id,
        String name,
        String owner,
        String status,
        String currentStage,
        int milestoneCompletionRate,
        String description
) {
}

