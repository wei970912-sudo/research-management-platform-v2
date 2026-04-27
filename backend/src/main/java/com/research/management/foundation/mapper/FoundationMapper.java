package com.research.management.foundation.mapper;

import com.research.management.foundation.*;
import com.research.management.foundation.entity.*;
import org.springframework.stereotype.Component;

@Component
public class FoundationMapper {

    public ResearchProjectRecord toRecord(ProjectEntity entity) {
        return new ResearchProjectRecord(
                entity.getId(),
                entity.getName(),
                entity.getOwner(),
                entity.getStatus(),
                entity.getCurrentStage(),
                entity.getMilestoneCompletionRate(),
                entity.getDescription()
        );
    }

    public ResearchPhaseRecord toRecord(PhaseEntity entity) {
        return new ResearchPhaseRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getName(),
                entity.getOwner(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus()
        );
    }

    public ResearchMilestoneRecord toRecord(MilestoneEntity entity) {
        return new ResearchMilestoneRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getPhaseId(),
                entity.getName(),
                entity.getDate(),
                entity.getStatus(),
                Boolean.TRUE.equals(entity.getCritical())
        );
    }

    public ResearchMeetingRecord toRecord(MeetingEntity entity) {
        return new ResearchMeetingRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getTopic(),
                entity.getDate(),
                entity.getHost(),
                entity.getType(),
                entity.getLocation(),
                entity.getAttendees(),
                entity.getSummary(),
                entity.getAttachmentNames()
        );
    }

    public ResearchContractRecord toRecord(ContractEntity entity) {
        return new ResearchContractRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getTaskBookItemId(),
                entity.getName(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getTaskName(),
                entity.getTaskBudget(),
                entity.getSigningUnit(),
                entity.getSigningDate(),
                entity.getDeliveryDate(),
                entity.getContent(),
                entity.getDeliveryTerms(),
                entity.getPaymentTerms()
        );
    }

    public ResearchDocumentRecord toRecord(DocumentEntity entity) {
        return new ResearchDocumentRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getNodeName(),
                entity.getAttachmentCount(),
                entity.getStatus(),
                entity.getOwner(),
                entity.getTargetDate(),
                entity.getDescription(),
                entity.getNotes(),
                entity.getAttachmentNames()
        );
    }

    public ResearchCalendarEventRecord toRecord(CalendarEventEntity entity) {
        return new ResearchCalendarEventRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getTitle(),
                entity.getDate(),
                entity.getTime(),
                entity.getType(),
                entity.getRemindBefore(),
                entity.getNote()
        );
    }

    public ResearchControlLineRecord toRecord(ControlLineEntity entity) {
        return new ResearchControlLineRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getName(),
                entity.getTargetDate(),
                entity.getOwner(),
                entity.getNote()
        );
    }

    public ResearchControlPointRecord toRecord(ControlPointEntity entity) {
        return new ResearchControlPointRecord(
                entity.getId(),
                entity.getControlLineId(),
                entity.getProjectId(),
                entity.getName(),
                entity.getPlannedDate(),
                entity.getStatus(),
                entity.getSortOrder()
        );
    }

    public ResearchControlPointItemRecord toRecord(ControlPointItemEntity entity) {
        return new ResearchControlPointItemRecord(
                entity.getId(),
                entity.getControlPointId(),
                entity.getName(),
                entity.getCompleted(),
                entity.getSortOrder()
        );
    }

    public ResearchStakeholderRecord toRecord(StakeholderEntity entity) {
        return new ResearchStakeholderRecord(
                entity.getId(),
                entity.getProjectId(),
                entity.getStakeholderName(),
                entity.getRelatedWorkType(),
                entity.getRelatedWorkValue(),
                entity.getMainContent()
        );
    }
}
