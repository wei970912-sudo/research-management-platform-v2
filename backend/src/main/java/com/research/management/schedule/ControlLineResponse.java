package com.research.management.schedule;

import java.util.List;

public record ControlLineResponse(
        String id,
        String projectId,
        String projectName,
        String name,
        String targetDate,
        String owner,
        String note,
        int totalControlPoints,
        int completedControlPoints,
        int progressPercent,
        List<ControlPointResponse> controlPoints
) {

    public record ControlPointResponse(
            String id,
            String controlLineId,
            String projectId,
            String name,
            String plannedDate,
            String status,
            Integer sortOrder,
            boolean completed,
            List<ControlPointItemResponse> items
    ) {
    }

    public record ControlPointItemResponse(
            String id,
            String controlPointId,
            String name,
            Boolean completed,
            Integer sortOrder
    ) {
    }
}
