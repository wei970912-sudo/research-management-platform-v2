package com.research.management.foundation;

import com.research.management.calendar.CalendarEventResponse;
import com.research.management.calendar.CalendarEventUpsertRequest;
import com.research.management.contract.ContractSummary;
import com.research.management.contract.ContractUpsertRequest;
import com.research.management.contract.TaskBookItemSummary;
import com.research.management.contract.TaskBookItemUpsertRequest;
import com.research.management.dashboard.DashboardSummaryResponse;
import com.research.management.document.DocumentSummary;
import com.research.management.document.DocumentUpsertRequest;
import com.research.management.foundation.entity.ProjectEntity;
import com.research.management.foundation.entity.TaskBookItemEntity;
import com.research.management.foundation.mapper.FoundationMapper;
import com.research.management.foundation.repository.ControlLineRepository;
import com.research.management.foundation.repository.ControlPointItemRepository;
import com.research.management.foundation.repository.ControlPointRepository;
import com.research.management.foundation.repository.CalendarEventRepository;
import com.research.management.foundation.repository.ContractRepository;
import com.research.management.foundation.repository.DocumentRepository;
import com.research.management.foundation.repository.MeetingRepository;
import com.research.management.foundation.repository.MilestoneRepository;
import com.research.management.foundation.repository.PhaseRepository;
import com.research.management.foundation.repository.ProjectRepository;
import com.research.management.foundation.repository.StakeholderRepository;
import com.research.management.foundation.repository.TaskBookItemRepository;
import com.research.management.meeting.MeetingSummary;
import com.research.management.meeting.MeetingUpsertRequest;
import com.research.management.project.ProjectDetailResponse;
import com.research.management.project.ProjectSummary;
import com.research.management.project.ProjectUpsertRequest;
import com.research.management.schedule.ControlLineResponse;
import com.research.management.schedule.ControlLineStructureUpsertRequest;
import com.research.management.schedule.ControlLineUpsertRequest;
import com.research.management.schedule.ControlPointItemUpsertRequest;
import com.research.management.schedule.ControlPointUpsertRequest;
import com.research.management.schedule.MilestoneUpsertRequest;
import com.research.management.schedule.PhaseUpsertRequest;
import com.research.management.schedule.ScheduleOverviewResponse;
import com.research.management.stakeholder.StakeholderResponse;
import com.research.management.stakeholder.StakeholderUpsertRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
public class UnifiedDataStore {

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final MilestoneRepository milestoneRepository;
    private final MeetingRepository meetingRepository;
    private final ContractRepository contractRepository;
    private final DocumentRepository documentRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final ControlLineRepository controlLineRepository;
    private final ControlPointRepository controlPointRepository;
    private final ControlPointItemRepository controlPointItemRepository;
    private final StakeholderRepository stakeholderRepository;
    private final TaskBookItemRepository taskBookItemRepository;
    private final FoundationMapper foundationMapper;

    public UnifiedDataStore(
            ProjectRepository projectRepository,
            PhaseRepository phaseRepository,
            MilestoneRepository milestoneRepository,
            MeetingRepository meetingRepository,
            ContractRepository contractRepository,
            DocumentRepository documentRepository,
            CalendarEventRepository calendarEventRepository,
            ControlLineRepository controlLineRepository,
            ControlPointRepository controlPointRepository,
            ControlPointItemRepository controlPointItemRepository,
            StakeholderRepository stakeholderRepository,
            TaskBookItemRepository taskBookItemRepository,
            FoundationMapper foundationMapper
    ) {
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
        this.milestoneRepository = milestoneRepository;
        this.meetingRepository = meetingRepository;
        this.contractRepository = contractRepository;
        this.documentRepository = documentRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.controlLineRepository = controlLineRepository;
        this.controlPointRepository = controlPointRepository;
        this.controlPointItemRepository = controlPointItemRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.taskBookItemRepository = taskBookItemRepository;
        this.foundationMapper = foundationMapper;
    }

    public List<ProjectSummary> listProjectSummaries() {
        return projectRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .map(item -> new ProjectSummary(item.id(), item.name(), item.owner(), item.status(), item.currentStage()))
                .toList();
    }

    public ProjectDetailResponse getProjectDetail(String projectId) {
        ResearchProjectRecord project = getProjectRecord(projectId);
        return new ProjectDetailResponse(
                project.id(),
                project.name(),
                project.owner(),
                project.status(),
                project.currentStage(),
                project.milestoneCompletionRate(),
                project.description()
        );
    }

    public ScheduleOverviewResponse buildScheduleOverview() {
        List<ScheduleOverviewResponse.MilestoneItem> items = listMilestones();
        List<ResearchMilestoneRecord> milestones = listMilestoneRecords();
        return new ScheduleOverviewResponse(
                phaseRepository.count() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) phaseRepository.count(),
                (int) milestones.stream().filter(ResearchMilestoneRecord::critical).count(),
                controlPointCompletionRate(),
                items
        );
    }

    public List<ScheduleOverviewResponse.MilestoneItem> listMilestones() {
        return listMilestoneRecords().stream()
                .map(this::toMilestoneItem)
                .sorted(Comparator.comparing(ScheduleOverviewResponse.MilestoneItem::date))
                .toList();
    }

    public List<ResearchPhaseRecord> listPhases() {
        return phaseRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .sorted(Comparator.comparing(ResearchPhaseRecord::startDate))
                .toList();
    }

    public List<MeetingSummary> listMeetings() {
        return meetingRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .sorted(Comparator.comparing(ResearchMeetingRecord::date).reversed()
                        .thenComparing(ResearchMeetingRecord::topic))
                .map(this::toMeetingSummary)
                .toList();
    }

    public List<ContractSummary> listContracts() {
        ensureTaskBookItemsFromContracts();
        Map<String, TaskBookItemSummary> taskBookMap = listTaskBookItems().stream()
                .collect(Collectors.toMap(TaskBookItemSummary::id, item -> item));
        return contractRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .map(item -> toContractSummary(item, taskBookMap))
                .toList();
    }

    public List<TaskBookItemSummary> listTaskBookItems() {
        ensureTaskBookItemsFromContracts();
        List<ResearchContractRecord> contracts = contractRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .toList();
        return taskBookItemRepository.findAll().stream()
                .sorted(Comparator.comparing(TaskBookItemEntity::getProjectId).thenComparing(TaskBookItemEntity::getName))
                .map(item -> toTaskBookItemSummary(item, contracts))
                .toList();
    }

    public List<DocumentSummary> listDocuments() {
        return documentRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .map(this::toDocumentSummary)
                .toList();
    }

    public List<CalendarEventResponse> listCalendarEvents() {
        return calendarEventRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .map(item -> new CalendarEventResponse(
                        item.id(),
                        item.title(),
                        item.date(),
                        item.time(),
                        item.type(),
                        getProjectName(item.projectId()),
                        item.remindBefore(),
                        item.note(),
                        item.projectId()
                ))
                .toList();
    }

    public List<ControlLineResponse> listControlLines() {
        List<ResearchControlPointRecord> points = listControlPointRecords();
        List<ResearchControlPointItemRecord> items = listControlPointItemRecords();
        return controlLineRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .sorted(Comparator.comparing(ResearchControlLineRecord::targetDate).thenComparing(ResearchControlLineRecord::name))
                .map(line -> toControlLineResponse(line, points, items))
                .toList();
    }

    public List<StakeholderResponse> listStakeholders() {
        return stakeholderRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .sorted(Comparator.comparing(ResearchStakeholderRecord::projectId).thenComparing(ResearchStakeholderRecord::stakeholderName))
                .map(item -> new StakeholderResponse(
                        item.id(),
                        item.projectId(),
                        getProjectName(item.projectId()),
                        item.stakeholderName(),
                        item.relatedWorkType(),
                        item.relatedWorkValue(),
                        item.mainContent()
                ))
                .toList();
    }

    public DashboardSummaryResponse buildDashboardSummary() {
        List<DashboardSummaryResponse.ImportantItem> importantItems = listMilestoneRecords().stream()
                .filter(ResearchMilestoneRecord::critical)
                .sorted(Comparator.comparing(ResearchMilestoneRecord::date))
                .limit(5)
                .map(item -> new DashboardSummaryResponse.ImportantItem(item.name(), item.date(), getProjectName(item.projectId())))
                .toList();

        return new DashboardSummaryResponse(
                (int) projectRepository.count(),
                controlPointRepository.count() > 0 ? controlPointCompletionRate() : averageCompletionRate(),
                (int) calendarEventRepository.count(),
                importantItems
        );
    }

    public FoundationSnapshotResponse snapshot() {
        return new FoundationSnapshotResponse(
                listProjectRecords(),
                phaseRepository.findAll().stream().map(foundationMapper::toRecord).toList(),
                listMilestoneRecords(),
                meetingRepository.findAll().stream().map(foundationMapper::toRecord).toList(),
                contractRepository.findAll().stream().map(foundationMapper::toRecord).toList(),
                documentRepository.findAll().stream().map(foundationMapper::toRecord).toList(),
                calendarEventRepository.findAll().stream().map(foundationMapper::toRecord).toList()
        );
    }

    public String buildAssistantContext(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            String projectText = listProjectRecords().stream()
                    .map(item -> item.name() + "(" + item.status() + "," + item.currentStage() + "," + item.owner() + ")")
                    .collect(Collectors.joining("; "));
            String phaseText = listPhases().stream()
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.name() + "(" + item.status() + "," + item.startDate() + "-" + item.endDate() + ")")
                    .collect(Collectors.joining("; "));
            String milestoneText = listMilestoneRecords().stream()
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.name() + "(" + item.date() + "," + item.status() + ",critical=" + item.critical() + ")")
                    .collect(Collectors.joining("; "));
            String meetingText = meetingRepository.findAll().stream()
                    .map(foundationMapper::toRecord)
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.topic() + "(" + item.date() + "," + item.type() + "," + item.summary() + ")")
                    .collect(Collectors.joining("; "));
            String contractText = contractRepository.findAll().stream()
                    .map(foundationMapper::toRecord)
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.name() + "(" + item.status() + "," + item.amount() + "," + item.taskName() + ")")
                    .collect(Collectors.joining("; "));
            String documentText = documentRepository.findAll().stream()
                    .map(foundationMapper::toRecord)
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.nodeName() + "(" + item.status() + "," + item.targetDate() + "," + item.description() + ")")
                    .collect(Collectors.joining("; "));
            String calendarText = calendarEventRepository.findAll().stream()
                    .map(foundationMapper::toRecord)
                    .sorted(Comparator.comparing(ResearchCalendarEventRecord::date))
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.title() + "(" + item.date() + " " + item.time() + "," + item.type() + "," + item.note() + ")")
                    .collect(Collectors.joining("; "));
            String controlLineText = controlLineRepository.findAll().stream()
                    .map(foundationMapper::toRecord)
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.name() + "(" + item.targetDate() + "," + item.owner() + ")")
                    .collect(Collectors.joining("; "));
            String stakeholderText = stakeholderRepository.findAll().stream()
                    .map(foundationMapper::toRecord)
                    .limit(20)
                    .map(item -> getProjectName(item.projectId()) + ":" + item.stakeholderName() + "(" + item.relatedWorkType() + "," + item.relatedWorkValue() + ")")
                    .collect(Collectors.joining("; "));

            return """
                    Current platform context:
                    Project count: %s
                    Phase count: %s
                    Milestone count: %s
                    Meeting count: %s
                    Contract count: %s
                    Document node count: %s
                    Reminder count: %s
                    Control line count: %s
                    Stakeholder count: %s
                    Projects: %s
                    Phases: %s
                    Milestones: %s
                    Meetings: %s
                    Contracts: %s
                    Documents: %s
                    Calendar reminders: %s
                    Control lines: %s
                    Stakeholders: %s
                    """.formatted(
                    projectRepository.count(),
                    phaseRepository.count(),
                    milestoneRepository.count(),
                    meetingRepository.count(),
                    contractRepository.count(),
                    documentRepository.count(),
                    calendarEventRepository.count(),
                    controlLineRepository.count(),
                    stakeholderRepository.count(),
                    projectText.isBlank() ? "None" : projectText,
                    phaseText.isBlank() ? "None" : phaseText,
                    milestoneText.isBlank() ? "None" : milestoneText,
                    meetingText.isBlank() ? "None" : meetingText,
                    contractText.isBlank() ? "None" : contractText,
                    documentText.isBlank() ? "None" : documentText,
                    calendarText.isBlank() ? "None" : calendarText,
                    controlLineText.isBlank() ? "None" : controlLineText,
                    stakeholderText.isBlank() ? "None" : stakeholderText
            );
        }

        ProjectContext context = projectContext(projectId);
        if (context == null) {
            return "No project context found.";
        }

        String phaseText = context.phases().stream()
                .map(item -> item.name() + "(" + item.status() + "," + item.startDate() + "-" + item.endDate() + ")")
                .collect(Collectors.joining("; "));
        String milestoneText = context.milestones().stream()
                .map(item -> item.name() + "(" + item.date() + "," + item.status() + ")")
                .collect(Collectors.joining("; "));
        String meetingText = context.meetings().stream()
                .map(item -> item.topic() + "(" + item.date() + ")")
                .collect(Collectors.joining("; "));
        String contractText = context.contracts().stream()
                .map(item -> item.name() + "(" + item.status() + "," + item.amount() + ")")
                .collect(Collectors.joining("; "));
        String documentText = context.documents().stream()
                .map(item -> item.nodeName() + "(" + item.status() + "," + item.targetDate() + "," + item.description() + ")")
                .collect(Collectors.joining("; "));
        String calendarText = context.calendarEvents().stream()
                .map(item -> item.title() + "(" + item.date() + " " + item.time() + "," + item.type() + "," + item.note() + ")")
                .collect(Collectors.joining("; "));
        String controlLineText = context.controlLines().stream()
                .map(item -> item.name() + "(" + item.targetDate() + "," + item.owner() + ")")
                .collect(Collectors.joining("; "));
        String stakeholderText = context.stakeholders().stream()
                .map(item -> item.stakeholderName() + "(" + item.relatedWorkType() + "," + item.relatedWorkValue() + ")")
                .collect(Collectors.joining("; "));

        return """
                Current project context:
                Project: %s
                Owner: %s
                Status: %s
                Current stage: %s
                Description: %s
                Phases: %s
                Milestones: %s
                Meetings: %s
                Contracts: %s
                Documents: %s
                Calendar reminders: %s
                Control lines: %s
                Stakeholders: %s
                """.formatted(
                context.project().name(),
                context.project().owner(),
                context.project().status(),
                context.project().currentStage(),
                context.project().description(),
                phaseText.isBlank() ? "None" : phaseText,
                milestoneText.isBlank() ? "None" : milestoneText,
                meetingText.isBlank() ? "None" : meetingText,
                contractText.isBlank() ? "None" : contractText,
                documentText.isBlank() ? "None" : documentText,
                calendarText.isBlank() ? "None" : calendarText,
                controlLineText.isBlank() ? "None" : controlLineText,
                stakeholderText.isBlank() ? "None" : stakeholderText
        );
    }

    public List<Map<String, String>> buildAssistantReferences(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return List.of(
                    Map.of("type", "project-count", "id", "all-projects", "title", String.valueOf(projectRepository.count())),
                    Map.of("type", "phase-count", "id", "all-phases", "title", String.valueOf(phaseRepository.count())),
                    Map.of("type", "milestone-count", "id", "all-milestones", "title", String.valueOf(milestoneRepository.count())),
                Map.of("type", "meeting-count", "id", "all-meetings", "title", String.valueOf(meetingRepository.count())),
                Map.of("type", "contract-count", "id", "all-contracts", "title", String.valueOf(contractRepository.count())),
                Map.of("type", "document-count", "id", "all-documents", "title", String.valueOf(documentRepository.count())),
                Map.of("type", "calendar-count", "id", "all-calendar-events", "title", String.valueOf(calendarEventRepository.count())),
                Map.of("type", "control-line-count", "id", "all-control-lines", "title", String.valueOf(controlLineRepository.count())),
                Map.of("type", "stakeholder-count", "id", "all-stakeholders", "title", String.valueOf(stakeholderRepository.count()))
        );
        }

        ProjectContext context = projectContext(projectId);
        if (context == null) {
            return List.of();
        }

        return List.of(
                Map.of("type", "project", "id", context.project().id(), "title", context.project().name()),
                Map.of("type", "phase-count", "id", context.project().id(), "title", String.valueOf(context.phases().size())),
                Map.of("type", "milestone-count", "id", context.project().id(), "title", String.valueOf(context.milestones().size())),
                Map.of("type", "meeting-count", "id", context.project().id(), "title", String.valueOf(context.meetings().size())),
                Map.of("type", "contract-count", "id", context.project().id(), "title", String.valueOf(context.contracts().size())),
                Map.of("type", "document-count", "id", context.project().id(), "title", String.valueOf(context.documents().size())),
                Map.of("type", "calendar-count", "id", context.project().id(), "title", String.valueOf(context.calendarEvents().size())),
                Map.of("type", "control-line-count", "id", context.project().id(), "title", String.valueOf(context.controlLines().size())),
                Map.of("type", "stakeholder-count", "id", context.project().id(), "title", String.valueOf(context.stakeholders().size()))
        );
    }

    public ProjectDetailResponse createProject(ProjectUpsertRequest request) {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(nextId("P"));
        applyProject(entity, request);
        projectRepository.save(entity);
        return getProjectDetail(entity.getId());
    }

    public ProjectDetailResponse updateProject(String projectId, ProjectUpsertRequest request) {
        ProjectEntity entity = projectRepository.findById(projectId).orElseThrow();
        applyProject(entity, request);
        projectRepository.save(entity);
        return getProjectDetail(entity.getId());
    }

    public void deleteProject(String projectId) {
        calendarEventRepository.findByProjectId(projectId).forEach(calendarEventRepository::delete);
        stakeholderRepository.findByProjectId(projectId).forEach(stakeholderRepository::delete);
        controlLineRepository.findByProjectId(projectId).forEach(controlLineRepository::delete);
        controlPointRepository.findByProjectId(projectId).forEach(point -> {
            controlPointItemRepository.findByControlPointId(point.getId()).forEach(controlPointItemRepository::delete);
            controlPointRepository.delete(point);
        });
        documentRepository.findByProjectId(projectId).forEach(documentRepository::delete);
        contractRepository.findByProjectId(projectId).forEach(contractRepository::delete);
        meetingRepository.findByProjectId(projectId).forEach(meetingRepository::delete);
        milestoneRepository.findByProjectId(projectId).forEach(milestoneRepository::delete);
        phaseRepository.findByProjectId(projectId).forEach(phaseRepository::delete);
        projectRepository.deleteById(projectId);
    }

    public MeetingSummary createMeeting(MeetingUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.MeetingEntity();
        entity.setId(nextId("MT"));
        applyMeeting(entity, request);
        meetingRepository.save(entity);
        return toMeetingSummary(foundationMapper.toRecord(entity));
    }

    public MeetingSummary updateMeeting(String meetingId, MeetingUpsertRequest request) {
        var entity = meetingRepository.findById(meetingId).orElseThrow();
        applyMeeting(entity, request);
        meetingRepository.save(entity);
        return toMeetingSummary(foundationMapper.toRecord(entity));
    }

    public void deleteMeeting(String meetingId) {
        meetingRepository.deleteById(meetingId);
    }

    public MeetingSummary appendMeetingAttachments(String meetingId, List<String> attachmentNames) {
        var entity = meetingRepository.findById(meetingId).orElseThrow();
        LinkedHashSet<String> merged = Stream.concat(
                        splitAttachmentNames(entity.getAttachmentNames()).stream(),
                        attachmentNames.stream()
                )
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        entity.setAttachmentNames(String.join(", ", merged));
        meetingRepository.save(entity);
        return toMeetingSummary(foundationMapper.toRecord(entity));
    }

    public ContractSummary createContract(ContractUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.ContractEntity();
        entity.setId(nextId("CT"));
        applyContract(entity, request);
        contractRepository.save(entity);
        return listContracts().stream().filter(item -> item.id().equals(entity.getId())).findFirst().orElseThrow();
    }

    public ContractSummary updateContract(String contractId, ContractUpsertRequest request) {
        var entity = contractRepository.findById(contractId).orElseThrow();
        applyContract(entity, request);
        contractRepository.save(entity);
        return listContracts().stream().filter(item -> item.id().equals(entity.getId())).findFirst().orElseThrow();
    }

    public void deleteContract(String contractId) {
        contractRepository.deleteById(contractId);
    }

    public TaskBookItemSummary createTaskBookItem(TaskBookItemUpsertRequest request) {
        TaskBookItemEntity entity = new TaskBookItemEntity();
        entity.setId(nextId("TB"));
        applyTaskBookItem(entity, request);
        taskBookItemRepository.save(entity);
        return listTaskBookItems().stream().filter(item -> item.id().equals(entity.getId())).findFirst().orElseThrow();
    }

    public TaskBookItemSummary updateTaskBookItem(String taskBookItemId, TaskBookItemUpsertRequest request) {
        TaskBookItemEntity entity = taskBookItemRepository.findById(taskBookItemId).orElseThrow();
        applyTaskBookItem(entity, request);
        taskBookItemRepository.save(entity);
        contractRepository.findAll().stream()
                .filter(contract -> taskBookItemId.equals(contract.getTaskBookItemId()))
                .forEach(contract -> {
                    contract.setProjectId(entity.getProjectId());
                    contract.setTaskName(entity.getName());
                    contract.setTaskBudget(entity.getBudget());
                    contractRepository.save(contract);
                });
        return listTaskBookItems().stream().filter(item -> item.id().equals(entity.getId())).findFirst().orElseThrow();
    }

    public void deleteTaskBookItem(String taskBookItemId) {
        boolean used = contractRepository.findAll().stream().anyMatch(item -> taskBookItemId.equals(item.getTaskBookItemId()));
        if (used) {
            throw new IllegalStateException("该任务书项已被合同引用，不能删除。");
        }
        taskBookItemRepository.deleteById(taskBookItemId);
    }

    public CalendarEventResponse createCalendarEvent(CalendarEventUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.CalendarEventEntity();
        entity.setId(nextId("EV"));
        applyCalendarEvent(entity, request);
        calendarEventRepository.save(entity);
        return toCalendarEventResponse(foundationMapper.toRecord(entity));
    }

    public CalendarEventResponse updateCalendarEvent(String eventId, CalendarEventUpsertRequest request) {
        var entity = calendarEventRepository.findById(eventId).orElseThrow();
        applyCalendarEvent(entity, request);
        calendarEventRepository.save(entity);
        return toCalendarEventResponse(foundationMapper.toRecord(entity));
    }

    public void deleteCalendarEvent(String eventId) {
        calendarEventRepository.deleteById(eventId);
    }

    public ControlLineResponse createControlLine(ControlLineUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.ControlLineEntity();
        entity.setId(nextId("CL"));
        applyControlLine(entity, request);
        controlLineRepository.save(entity);
        return listControlLines().stream().filter(item -> item.id().equals(entity.getId())).findFirst().orElseThrow();
    }

    @Transactional
    public ControlLineResponse createControlLineStructured(ControlLineStructureUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.ControlLineEntity();
        entity.setId(nextId("CL"));
        applyControlLine(entity, request);
        controlLineRepository.save(entity);
        syncControlLineStructure(entity.getId(), entity.getProjectId(), request.points());
        return findControlLineResponse(entity.getId());
    }

    public ControlLineResponse updateControlLine(String controlLineId, ControlLineUpsertRequest request) {
        var entity = controlLineRepository.findById(controlLineId).orElseThrow();
        applyControlLine(entity, request);
        controlLineRepository.save(entity);
        return listControlLines().stream().filter(item -> item.id().equals(entity.getId())).findFirst().orElseThrow();
    }

    @Transactional
    public ControlLineResponse updateControlLineStructured(String controlLineId, ControlLineStructureUpsertRequest request) {
        var entity = controlLineRepository.findById(controlLineId).orElseThrow();
        applyControlLine(entity, request);
        controlLineRepository.save(entity);
        syncControlLineStructure(entity.getId(), entity.getProjectId(), request.points());
        return findControlLineResponse(entity.getId());
    }

    public void deleteControlLine(String controlLineId) {
        controlPointRepository.findByControlLineId(controlLineId).forEach(point -> {
            controlPointItemRepository.findByControlPointId(point.getId()).forEach(controlPointItemRepository::delete);
            controlPointRepository.delete(point);
        });
        controlLineRepository.deleteById(controlLineId);
    }

    public ControlLineResponse.ControlPointResponse createControlPoint(ControlPointUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.ControlPointEntity();
        entity.setId(nextId("CP"));
        applyControlPoint(entity, request);
        controlPointRepository.save(entity);
        return listControlLines().stream()
                .flatMap(item -> item.controlPoints().stream())
                .filter(item -> item.id().equals(entity.getId()))
                .findFirst()
                .orElseThrow();
    }

    public ControlLineResponse.ControlPointResponse updateControlPoint(String controlPointId, ControlPointUpsertRequest request) {
        var entity = controlPointRepository.findById(controlPointId).orElseThrow();
        applyControlPoint(entity, request);
        controlPointRepository.save(entity);
        return listControlLines().stream()
                .flatMap(item -> item.controlPoints().stream())
                .filter(item -> item.id().equals(entity.getId()))
                .findFirst()
                .orElseThrow();
    }

    public void deleteControlPoint(String controlPointId) {
        controlPointItemRepository.findByControlPointId(controlPointId).forEach(controlPointItemRepository::delete);
        controlPointRepository.deleteById(controlPointId);
    }

    public ControlLineResponse.ControlPointItemResponse createControlPointItem(ControlPointItemUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.ControlPointItemEntity();
        entity.setId(nextId("CI"));
        applyControlPointItem(entity, request);
        controlPointItemRepository.save(entity);
        return listControlLines().stream()
                .flatMap(line -> line.controlPoints().stream())
                .flatMap(point -> point.items().stream())
                .filter(item -> item.id().equals(entity.getId()))
                .findFirst()
                .orElseThrow();
    }

    public ControlLineResponse.ControlPointItemResponse updateControlPointItem(String itemId, ControlPointItemUpsertRequest request) {
        var entity = controlPointItemRepository.findById(itemId).orElseThrow();
        applyControlPointItem(entity, request);
        controlPointItemRepository.save(entity);
        return listControlLines().stream()
                .flatMap(line -> line.controlPoints().stream())
                .flatMap(point -> point.items().stream())
                .filter(item -> item.id().equals(entity.getId()))
                .findFirst()
                .orElseThrow();
    }

    public void deleteControlPointItem(String itemId) {
        controlPointItemRepository.deleteById(itemId);
    }

    public StakeholderResponse createStakeholder(StakeholderUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.StakeholderEntity();
        entity.setId(nextId("SH"));
        applyStakeholder(entity, request);
        stakeholderRepository.save(entity);
        return toStakeholderResponse(foundationMapper.toRecord(entity));
    }

    public StakeholderResponse updateStakeholder(String stakeholderId, StakeholderUpsertRequest request) {
        var entity = stakeholderRepository.findById(stakeholderId).orElseThrow();
        applyStakeholder(entity, request);
        stakeholderRepository.save(entity);
        return toStakeholderResponse(foundationMapper.toRecord(entity));
    }

    public void deleteStakeholder(String stakeholderId) {
        stakeholderRepository.deleteById(stakeholderId);
    }

    public DocumentSummary createDocument(DocumentUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.DocumentEntity();
        entity.setId(nextId("DOC"));
        applyDocument(entity, request);
        documentRepository.save(entity);
        return toDocumentSummary(foundationMapper.toRecord(entity));
    }

    public DocumentSummary updateDocument(String documentId, DocumentUpsertRequest request) {
        var entity = documentRepository.findById(documentId).orElseThrow();
        applyDocument(entity, request);
        documentRepository.save(entity);
        return toDocumentSummary(foundationMapper.toRecord(entity));
    }

    public void deleteDocument(String documentId) {
        documentRepository.deleteById(documentId);
    }

    public ScheduleOverviewResponse.MilestoneItem createMilestone(MilestoneUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.MilestoneEntity();
        entity.setId(nextId("M"));
        applyMilestone(entity, request);
        milestoneRepository.save(entity);
        return toMilestoneItem(foundationMapper.toRecord(entity));
    }

    public ResearchPhaseRecord createPhase(PhaseUpsertRequest request) {
        var entity = new com.research.management.foundation.entity.PhaseEntity();
        entity.setId(nextId("PH"));
        applyPhase(entity, request);
        phaseRepository.save(entity);
        return foundationMapper.toRecord(entity);
    }

    public ResearchPhaseRecord updatePhase(String phaseId, PhaseUpsertRequest request) {
        var entity = phaseRepository.findById(phaseId).orElseThrow();
        applyPhase(entity, request);
        phaseRepository.save(entity);
        return foundationMapper.toRecord(entity);
    }

    public void deletePhase(String phaseId) {
        milestoneRepository.findAll().stream()
                .filter(item -> phaseId.equals(item.getPhaseId()))
                .forEach(milestoneRepository::delete);
        phaseRepository.deleteById(phaseId);
    }

    public ScheduleOverviewResponse.MilestoneItem updateMilestone(String milestoneId, MilestoneUpsertRequest request) {
        var entity = milestoneRepository.findById(milestoneId).orElseThrow();
        applyMilestone(entity, request);
        milestoneRepository.save(entity);
        return toMilestoneItem(foundationMapper.toRecord(entity));
    }

    public void deleteMilestone(String milestoneId) {
        milestoneRepository.deleteById(milestoneId);
    }

    private List<ResearchProjectRecord> listProjectRecords() {
        return projectRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .toList();
    }

    private List<ResearchMilestoneRecord> listMilestoneRecords() {
        return milestoneRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .toList();
    }

    private List<ResearchControlPointRecord> listControlPointRecords() {
        return controlPointRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .sorted(Comparator.comparing(ResearchControlPointRecord::sortOrder).thenComparing(ResearchControlPointRecord::plannedDate))
                .toList();
    }

    private List<ResearchControlPointItemRecord> listControlPointItemRecords() {
        return controlPointItemRepository.findAll().stream()
                .map(foundationMapper::toRecord)
                .sorted(Comparator.comparing(ResearchControlPointItemRecord::sortOrder).thenComparing(ResearchControlPointItemRecord::name))
                .toList();
    }

    private ResearchProjectRecord getProjectRecord(String projectId) {
        ProjectEntity entity = projectRepository.findById(projectId)
                .orElseGet(() -> projectRepository.findAll().stream().findFirst().orElseThrow());
        return foundationMapper.toRecord(entity);
    }

    private int averageCompletionRate() {
        List<ResearchProjectRecord> projects = listProjectRecords();
        if (projects.isEmpty()) {
            return 0;
        }
        return Math.round(projects.stream()
                .map(ResearchProjectRecord::milestoneCompletionRate)
                .reduce(0, Integer::sum) / (float) projects.size());
    }

    private int controlPointCompletionRate() {
        List<ControlLineResponse> controlLines = listControlLines();
        int total = controlLines.stream().mapToInt(ControlLineResponse::totalControlPoints).sum();
        if (total == 0) {
            return 0;
        }
        int done = controlLines.stream().mapToInt(ControlLineResponse::completedControlPoints).sum();
        return Math.round(done * 100f / total);
    }

    private void applyProject(ProjectEntity entity, ProjectUpsertRequest request) {
        entity.setName(request.name());
        entity.setOwner(request.owner());
        entity.setStatus(request.status());
        entity.setCurrentStage(request.currentStage());
        entity.setMilestoneCompletionRate(request.milestoneCompletionRate());
        entity.setDescription(request.description());
    }

    private void applyMeeting(com.research.management.foundation.entity.MeetingEntity entity, MeetingUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setTopic(request.topic());
        entity.setDate(request.date());
        entity.setHost(request.host());
        entity.setType(request.type());
        entity.setLocation(request.location());
        entity.setAttendees(request.attendees());
        entity.setSummary(request.summary());
        entity.setAttachmentNames(Objects.requireNonNullElse(request.attachmentNames(), ""));
    }

    private void applyMilestone(com.research.management.foundation.entity.MilestoneEntity entity, MilestoneUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setPhaseId(request.phaseId());
        entity.setName(request.name());
        entity.setDate(request.date());
        entity.setStatus(request.status());
        entity.setCritical(request.critical());
    }

    private void applyPhase(com.research.management.foundation.entity.PhaseEntity entity, PhaseUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setName(request.name());
        entity.setOwner(request.owner());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setStatus(request.status());
    }

    private void applyContract(com.research.management.foundation.entity.ContractEntity entity, ContractUpsertRequest request) {
        TaskBookItemEntity taskBookItem = taskBookItemRepository.findById(request.taskBookItemId()).orElseThrow();
        entity.setProjectId(request.projectId());
        entity.setTaskBookItemId(request.taskBookItemId());
        entity.setName(request.name());
        entity.setAmount(request.amount());
        entity.setStatus(request.status());
        entity.setTaskName(taskBookItem.getName());
        entity.setTaskBudget(taskBookItem.getBudget());
        entity.setSigningUnit(request.signingUnit());
        entity.setSigningDate(request.signingDate());
        entity.setDeliveryDate(request.deliveryDate());
        entity.setContent(request.content());
        entity.setDeliveryTerms(request.deliveryTerms());
        entity.setPaymentTerms(request.paymentTerms());
    }

    private void applyTaskBookItem(TaskBookItemEntity entity, TaskBookItemUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setName(request.name());
        entity.setBudget(request.budget());
        entity.setNote(request.note());
    }

    private void applyCalendarEvent(com.research.management.foundation.entity.CalendarEventEntity entity, CalendarEventUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setTitle(request.title());
        entity.setDate(request.date());
        entity.setTime(request.time());
        entity.setType(request.type());
        entity.setRemindBefore(request.remindBefore());
        entity.setNote(request.note());
    }

    private void applyControlLine(com.research.management.foundation.entity.ControlLineEntity entity, ControlLineUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setName(request.name());
        entity.setTargetDate(request.targetDate());
        entity.setOwner(request.owner());
        entity.setNote(request.note());
    }

    private void applyControlLine(com.research.management.foundation.entity.ControlLineEntity entity, ControlLineStructureUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setName(request.name());
        entity.setTargetDate(request.targetDate());
        entity.setOwner(request.owner());
        entity.setNote(request.note());
    }

    private void applyControlPoint(com.research.management.foundation.entity.ControlPointEntity entity, ControlPointUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setControlLineId(request.controlLineId());
        entity.setName(request.name());
        entity.setPlannedDate(request.plannedDate());
        entity.setStatus(request.status());
        entity.setSortOrder(request.sortOrder());
    }

    private void applyControlPointItem(com.research.management.foundation.entity.ControlPointItemEntity entity, ControlPointItemUpsertRequest request) {
        entity.setControlPointId(request.controlPointId());
        entity.setName(request.name());
        entity.setCompleted(request.completed());
        entity.setSortOrder(request.sortOrder());
    }

    private void applyStakeholder(com.research.management.foundation.entity.StakeholderEntity entity, StakeholderUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setStakeholderName(request.stakeholderName());
        entity.setRelatedWorkType(request.relatedWorkType());
        entity.setRelatedWorkValue(request.relatedWorkValue());
        entity.setMainContent(request.mainContent());
    }

    private void applyDocument(com.research.management.foundation.entity.DocumentEntity entity, DocumentUpsertRequest request) {
        entity.setProjectId(request.projectId());
        entity.setNodeName(request.nodeName());
        entity.setAttachmentCount(request.attachmentCount());
        entity.setStatus(request.status());
        entity.setOwner(request.owner());
        entity.setTargetDate(request.targetDate());
        entity.setDescription(request.description());
        entity.setNotes(request.notes());
        entity.setAttachmentNames(Objects.requireNonNullElse(request.attachmentNames(), ""));
    }

    private MeetingSummary toMeetingSummary(ResearchMeetingRecord item) {
        return new MeetingSummary(
                item.id(),
                item.topic(),
                getProjectName(item.projectId()),
                item.date(),
                item.host(),
                item.type(),
                item.location(),
                item.attendees(),
                item.summary(),
                item.attachmentNames()
        );
    }

    private List<String> splitAttachmentNames(String text) {
        return List.of(Objects.requireNonNullElse(text, "").split(","))
                .stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private ContractSummary toContractSummary(ResearchContractRecord item, Map<String, TaskBookItemSummary> taskBookMap) {
        String taskBookItemId = resolveTaskBookItemId(item);
        TaskBookItemSummary taskBookItem = taskBookMap.get(taskBookItemId);
        return new ContractSummary(
                item.id(),
                item.projectId(),
                item.name(),
                getProjectName(item.projectId()),
                taskBookItemId,
                item.amount(),
                item.status(),
                item.taskName(),
                item.taskBudget(),
                taskBookItem != null ? taskBookItem.usedAmount() : item.amount(),
                taskBookItem != null ? taskBookItem.remainingAmount() : BigDecimal.ZERO,
                item.signingUnit(),
                item.signingDate(),
                item.deliveryDate(),
                item.content(),
                item.deliveryTerms(),
                item.paymentTerms()
        );
    }

    private TaskBookItemSummary toTaskBookItemSummary(TaskBookItemEntity item, List<ResearchContractRecord> contracts) {
        List<ResearchContractRecord> matchedContracts = contracts.stream()
                .filter(contract -> item.getId().equals(resolveTaskBookItemId(contract)))
                .toList();
        BigDecimal usedAmount = matchedContracts.stream()
                .filter(contract -> !"已取消".equals(contract.status()))
                .map(ResearchContractRecord::amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingAmount = item.getBudget().subtract(usedAmount).max(BigDecimal.ZERO);
        return new TaskBookItemSummary(
                item.getId(),
                item.getProjectId(),
                getProjectName(item.getProjectId()),
                item.getName(),
                item.getBudget(),
                usedAmount,
                remainingAmount,
                matchedContracts.size(),
                item.getNote()
        );
    }

    private void ensureTaskBookItemsFromContracts() {
        contractRepository.findAll().forEach(contract -> {
            if (contract.getTaskBookItemId() != null && !contract.getTaskBookItemId().isBlank()) {
                return;
            }
            if (contract.getTaskName() == null || contract.getTaskName().isBlank() || contract.getTaskBudget() == null) {
                return;
            }
            TaskBookItemEntity taskBookItem = taskBookItemRepository
                    .findByProjectIdAndNameAndBudget(contract.getProjectId(), contract.getTaskName(), contract.getTaskBudget())
                    .orElseGet(() -> {
                        TaskBookItemEntity created = new TaskBookItemEntity();
                        created.setId(nextId("TB"));
                        created.setProjectId(contract.getProjectId());
                        created.setName(contract.getTaskName());
                        created.setBudget(contract.getTaskBudget());
                        created.setNote("由历史合同数据自动补齐");
                        return taskBookItemRepository.save(created);
                    });
            contract.setTaskBookItemId(taskBookItem.getId());
            contractRepository.save(contract);
        });
    }

    private String resolveTaskBookItemId(ResearchContractRecord contract) {
        if (contract.taskBookItemId() != null && !contract.taskBookItemId().isBlank()) {
            return contract.taskBookItemId();
        }
        return taskBookItemRepository.findByProjectIdAndNameAndBudget(contract.projectId(), contract.taskName(), contract.taskBudget())
                .map(TaskBookItemEntity::getId)
                .orElse("");
    }

    private ScheduleOverviewResponse.MilestoneItem toMilestoneItem(ResearchMilestoneRecord item) {
        return new ScheduleOverviewResponse.MilestoneItem(item.id(), getProjectName(item.projectId()), item.name(), item.date(), item.status());
    }

    private CalendarEventResponse toCalendarEventResponse(ResearchCalendarEventRecord item) {
        return new CalendarEventResponse(
                item.id(),
                item.title(),
                item.date(),
                item.time(),
                item.type(),
                getProjectName(item.projectId()),
                item.remindBefore(),
                item.note(),
                item.projectId()
        );
    }

    private ControlLineResponse toControlLineResponse(
            ResearchControlLineRecord line,
            List<ResearchControlPointRecord> points,
            List<ResearchControlPointItemRecord> items
    ) {
        List<ControlLineResponse.ControlPointResponse> pointResponses = points.stream()
                .filter(point -> Objects.equals(point.controlLineId(), line.id()))
                .sorted(
                        Comparator.comparing(ResearchControlPointRecord::sortOrder, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(ResearchControlPointRecord::plannedDate, Comparator.nullsLast(String::compareTo))
                )
                .map(point -> toControlPointResponse(point, items))
                .toList();
        IndicatorProgress indicatorProgress = summarizeIndicatorProgress(pointResponses);
        int total = indicatorProgress.total();
        int done = indicatorProgress.done();
        int progressPercent = total == 0 ? 0 : Math.round(done * 100f / total);
        return new ControlLineResponse(
                line.id(),
                line.projectId(),
                getProjectName(line.projectId()),
                line.name(),
                line.targetDate(),
                line.owner(),
                line.note(),
                total,
                done,
                progressPercent,
                pointResponses
        );
    }

    private ControlLineResponse.ControlPointResponse toControlPointResponse(
            ResearchControlPointRecord point,
            List<ResearchControlPointItemRecord> items
    ) {
        List<ControlLineResponse.ControlPointItemResponse> pointItems = items.stream()
                .filter(item -> Objects.equals(item.controlPointId(), point.id()))
                .sorted(
                        Comparator.comparing(ResearchControlPointItemRecord::sortOrder, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(ResearchControlPointItemRecord::name, Comparator.nullsLast(String::compareTo))
                )
                .map(item -> new ControlLineResponse.ControlPointItemResponse(
                        item.id(),
                        item.controlPointId(),
                        item.name(),
                        item.completed(),
                        item.sortOrder()
                ))
                .toList();
        boolean completedByStatus = point.status() != null && (point.status().contains("完成") || point.status().equalsIgnoreCase("DONE"));
        boolean completedByItems = !pointItems.isEmpty() && pointItems.stream().allMatch(item -> Boolean.TRUE.equals(item.completed()));
        return new ControlLineResponse.ControlPointResponse(
                point.id(),
                point.controlLineId(),
                point.projectId(),
                point.name(),
                point.plannedDate(),
                point.status(),
                point.sortOrder(),
                completedByStatus || completedByItems,
                pointItems
        );
    }

    private IndicatorProgress summarizeIndicatorProgress(List<ControlLineResponse.ControlPointResponse> pointResponses) {
        int total = 0;
        int done = 0;
        for (ControlLineResponse.ControlPointResponse point : pointResponses) {
            if (point.items() == null || point.items().isEmpty()) {
                total += 1;
                if (point.completed()) {
                    done += 1;
                }
                continue;
            }
            total += point.items().size();
            done += (int) point.items().stream().filter(item -> Boolean.TRUE.equals(item.completed())).count();
        }
        return new IndicatorProgress(total, done);
    }

    private record IndicatorProgress(int total, int done) {
    }

    private void syncControlLineStructure(
            String controlLineId,
            String projectId,
            List<ControlLineStructureUpsertRequest.ControlPointDraft> pointDrafts
    ) {
        List<ControlLineStructureUpsertRequest.ControlPointDraft> drafts = pointDrafts == null ? List.of() : pointDrafts;
        List<com.research.management.foundation.entity.ControlPointEntity> existingPoints = controlPointRepository.findByControlLineId(controlLineId);
        Map<String, com.research.management.foundation.entity.ControlPointEntity> pointById = existingPoints.stream()
                .collect(Collectors.toMap(com.research.management.foundation.entity.ControlPointEntity::getId, item -> item));
        LinkedHashSet<String> retainedPointIds = drafts.stream()
                .map(ControlLineStructureUpsertRequest.ControlPointDraft::id)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (com.research.management.foundation.entity.ControlPointEntity pointEntity : existingPoints) {
            if (!retainedPointIds.contains(pointEntity.getId())) {
                controlPointItemRepository.findByControlPointId(pointEntity.getId()).forEach(controlPointItemRepository::delete);
                controlPointRepository.delete(pointEntity);
            }
        }

        for (int pointIndex = 0; pointIndex < drafts.size(); pointIndex++) {
            ControlLineStructureUpsertRequest.ControlPointDraft pointDraft = drafts.get(pointIndex);
            com.research.management.foundation.entity.ControlPointEntity pointEntity =
                    pointDraft.id() != null && !pointDraft.id().isBlank()
                            ? pointById.getOrDefault(pointDraft.id(), new com.research.management.foundation.entity.ControlPointEntity())
                            : new com.research.management.foundation.entity.ControlPointEntity();
            if (pointEntity.getId() == null || pointEntity.getId().isBlank()) {
                pointEntity.setId(nextId("CP"));
            }
            pointEntity.setControlLineId(controlLineId);
            pointEntity.setProjectId(projectId);
            pointEntity.setName(pointDraft.name());
            pointEntity.setPlannedDate(pointDraft.plannedDate());
            pointEntity.setStatus(pointDraft.status());
            pointEntity.setSortOrder(pointIndex + 1);
            controlPointRepository.save(pointEntity);

            syncControlPointItems(pointEntity.getId(), pointDraft.items());
        }
    }

    private void syncControlPointItems(
            String controlPointId,
            List<ControlLineStructureUpsertRequest.ControlPointItemDraft> itemDrafts
    ) {
        List<ControlLineStructureUpsertRequest.ControlPointItemDraft> drafts = itemDrafts == null ? List.of() : itemDrafts;
        List<com.research.management.foundation.entity.ControlPointItemEntity> existingItems = controlPointItemRepository.findByControlPointId(controlPointId);
        Map<String, com.research.management.foundation.entity.ControlPointItemEntity> itemById = existingItems.stream()
                .collect(Collectors.toMap(com.research.management.foundation.entity.ControlPointItemEntity::getId, item -> item));
        LinkedHashSet<String> retainedItemIds = drafts.stream()
                .map(ControlLineStructureUpsertRequest.ControlPointItemDraft::id)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (com.research.management.foundation.entity.ControlPointItemEntity itemEntity : existingItems) {
            if (!retainedItemIds.contains(itemEntity.getId())) {
                controlPointItemRepository.delete(itemEntity);
            }
        }

        for (int itemIndex = 0; itemIndex < drafts.size(); itemIndex++) {
            ControlLineStructureUpsertRequest.ControlPointItemDraft itemDraft = drafts.get(itemIndex);
            com.research.management.foundation.entity.ControlPointItemEntity itemEntity =
                    itemDraft.id() != null && !itemDraft.id().isBlank()
                            ? itemById.getOrDefault(itemDraft.id(), new com.research.management.foundation.entity.ControlPointItemEntity())
                            : new com.research.management.foundation.entity.ControlPointItemEntity();
            if (itemEntity.getId() == null || itemEntity.getId().isBlank()) {
                itemEntity.setId(nextId("CI"));
            }
            itemEntity.setControlPointId(controlPointId);
            itemEntity.setName(itemDraft.name());
            itemEntity.setCompleted(Boolean.TRUE.equals(itemDraft.completed()));
            itemEntity.setSortOrder(itemIndex + 1);
            controlPointItemRepository.save(itemEntity);
        }
    }

    private ControlLineResponse findControlLineResponse(String controlLineId) {
        return listControlLines().stream()
                .filter(item -> item.id().equals(controlLineId))
                .findFirst()
                .orElseThrow();
    }

    private DocumentSummary toDocumentSummary(ResearchDocumentRecord item) {
        return new DocumentSummary(
                item.id(),
                item.nodeName(),
                getProjectName(item.projectId()),
                item.attachmentCount(),
                item.status(),
                item.owner(),
                item.targetDate(),
                item.description(),
                item.notes(),
                item.attachmentNames(),
                item.projectId()
        );
    }

    private StakeholderResponse toStakeholderResponse(ResearchStakeholderRecord item) {
        return new StakeholderResponse(
                item.id(),
                item.projectId(),
                getProjectName(item.projectId()),
                item.stakeholderName(),
                item.relatedWorkType(),
                item.relatedWorkValue(),
                item.mainContent()
        );
    }

    private String nextId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String getProjectName(String projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectEntity::getName)
                .orElse("Unknown Project");
    }

    private ProjectContext projectContext(String projectId) {
        return projectRepository.findById(projectId)
                .map(entity -> new ProjectContext(
                        foundationMapper.toRecord(entity),
                        phaseRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        milestoneRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        meetingRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        contractRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        documentRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        calendarEventRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        controlLineRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList(),
                        stakeholderRepository.findByProjectId(projectId).stream().map(foundationMapper::toRecord).toList()
                ))
                .orElse(null);
    }

    private record ProjectContext(
            ResearchProjectRecord project,
            List<ResearchPhaseRecord> phases,
            List<ResearchMilestoneRecord> milestones,
            List<ResearchMeetingRecord> meetings,
            List<ResearchContractRecord> contracts,
            List<ResearchDocumentRecord> documents,
            List<ResearchCalendarEventRecord> calendarEvents,
            List<ResearchControlLineRecord> controlLines,
            List<ResearchStakeholderRecord> stakeholders
    ) {
    }
}
