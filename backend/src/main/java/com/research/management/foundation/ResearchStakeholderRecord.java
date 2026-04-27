package com.research.management.foundation;

public record ResearchStakeholderRecord(
        String id,
        String projectId,
        String stakeholderName,
        String relatedWorkType,
        String relatedWorkValue,
        String mainContent
) {
}
