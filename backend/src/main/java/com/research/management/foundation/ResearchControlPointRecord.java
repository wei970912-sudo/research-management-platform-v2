package com.research.management.foundation;

public record ResearchControlPointRecord(
        String id,
        String controlLineId,
        String projectId,
        String name,
        String plannedDate,
        String status,
        Integer sortOrder
) {
}
