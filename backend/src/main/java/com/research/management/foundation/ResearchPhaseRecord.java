package com.research.management.foundation;

public record ResearchPhaseRecord(
        String id,
        String projectId,
        String name,
        String owner,
        String startDate,
        String endDate,
        String status
) {
}

