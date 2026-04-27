package com.research.management.foundation;

public record ResearchControlPointItemRecord(
        String id,
        String controlPointId,
        String name,
        Boolean completed,
        Integer sortOrder
) {
}
