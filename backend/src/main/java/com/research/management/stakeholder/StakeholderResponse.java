package com.research.management.stakeholder;

public record StakeholderResponse(
        String id,
        String projectId,
        String projectName,
        String stakeholderName,
        String relatedWorkType,
        String relatedWorkValue,
        String mainContent
) {
}
