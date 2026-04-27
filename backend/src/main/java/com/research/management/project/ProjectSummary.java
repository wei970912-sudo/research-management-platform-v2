package com.research.management.project;

public record ProjectSummary(
        String id,
        String name,
        String owner,
        String status,
        String currentStage
) {
}

