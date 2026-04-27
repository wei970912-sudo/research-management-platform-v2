package com.research.management.schedule;

import java.util.List;

public record ScheduleOverviewResponse(
        int phaseCount,
        int criticalMilestoneCount,
        int completionRate,
        List<MilestoneItem> milestones
) {
    public record MilestoneItem(
            String id,
            String projectName,
            String name,
            String date,
            String status
    ) {
    }
}

