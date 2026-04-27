package com.research.management.project;

public record ProjectDetailResponse(
        String id,
        String name,
        String owner,
        String status,
        String currentStage,
        int milestoneCompletionRate,
        String description
) {
}

