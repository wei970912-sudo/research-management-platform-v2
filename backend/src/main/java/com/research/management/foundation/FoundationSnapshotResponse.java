package com.research.management.foundation;

import java.util.List;

public record FoundationSnapshotResponse(
        List<ResearchProjectRecord> projects,
        List<ResearchPhaseRecord> phases,
        List<ResearchMilestoneRecord> milestones,
        List<ResearchMeetingRecord> meetings,
        List<ResearchContractRecord> contracts,
        List<ResearchDocumentRecord> documents,
        List<ResearchCalendarEventRecord> calendarEvents
) {
}

