package com.research.management.foundation;

public record ResearchMilestoneRecord(
        String id,
        String projectId,
        String phaseId,
        String name,
        String date,
        String status,
        boolean critical
) {
}

