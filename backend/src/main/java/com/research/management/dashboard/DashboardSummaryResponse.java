package com.research.management.dashboard;

import java.util.List;

public record DashboardSummaryResponse(
        int projectCount,
        int milestoneCompletionRate,
        int pendingReminderCount,
        List<ImportantItem> importantItems
) {
    public record ImportantItem(String title, String dueDate, String source) {
    }
}

