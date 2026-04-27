package com.research.management.schedule;

import com.research.management.common.ApiResponse;
import com.research.management.foundation.UnifiedDataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final UnifiedDataStore unifiedDataStore;

    public ScheduleController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping("/overview")
    public ApiResponse<ScheduleOverviewResponse> overview() {
        return ApiResponse.ok(unifiedDataStore.buildScheduleOverview());
    }

    @GetMapping("/milestones")
    public ApiResponse<List<ScheduleOverviewResponse.MilestoneItem>> milestones() {
        return ApiResponse.ok(unifiedDataStore.listMilestones());
    }

    @GetMapping("/phases")
    public ApiResponse<List<com.research.management.foundation.ResearchPhaseRecord>> phases() {
        return ApiResponse.ok(unifiedDataStore.listPhases());
    }

    @GetMapping("/control-lines")
    public ApiResponse<List<ControlLineResponse>> controlLines() {
        return ApiResponse.ok(unifiedDataStore.listControlLines());
    }

    @PostMapping("/phases")
    public ApiResponse<com.research.management.foundation.ResearchPhaseRecord> createPhase(@Valid @RequestBody PhaseUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createPhase(request));
    }

    @PutMapping("/phases/{phaseId}")
    public ApiResponse<com.research.management.foundation.ResearchPhaseRecord> updatePhase(
            @PathVariable String phaseId,
            @Valid @RequestBody PhaseUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updatePhase(phaseId, request));
    }

    @DeleteMapping("/phases/{phaseId}")
    public ApiResponse<Boolean> deletePhase(@PathVariable String phaseId) {
        unifiedDataStore.deletePhase(phaseId);
        return ApiResponse.ok(true);
    }

    @PostMapping("/control-lines")
    public ApiResponse<ControlLineResponse> createControlLine(@Valid @RequestBody ControlLineUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createControlLine(request));
    }

    @PostMapping("/control-lines/structured")
    public ApiResponse<ControlLineResponse> createControlLineStructured(@Valid @RequestBody ControlLineStructureUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createControlLineStructured(request));
    }

    @PutMapping("/control-lines/{controlLineId}")
    public ApiResponse<ControlLineResponse> updateControlLine(
            @PathVariable String controlLineId,
            @Valid @RequestBody ControlLineUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateControlLine(controlLineId, request));
    }

    @PutMapping("/control-lines/{controlLineId}/structured")
    public ApiResponse<ControlLineResponse> updateControlLineStructured(
            @PathVariable String controlLineId,
            @Valid @RequestBody ControlLineStructureUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateControlLineStructured(controlLineId, request));
    }

    @DeleteMapping("/control-lines/{controlLineId}")
    public ApiResponse<Boolean> deleteControlLine(@PathVariable String controlLineId) {
        unifiedDataStore.deleteControlLine(controlLineId);
        return ApiResponse.ok(true);
    }

    @PostMapping("/control-points")
    public ApiResponse<ControlLineResponse.ControlPointResponse> createControlPoint(@Valid @RequestBody ControlPointUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createControlPoint(request));
    }

    @PutMapping("/control-points/{controlPointId}")
    public ApiResponse<ControlLineResponse.ControlPointResponse> updateControlPoint(
            @PathVariable String controlPointId,
            @Valid @RequestBody ControlPointUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateControlPoint(controlPointId, request));
    }

    @DeleteMapping("/control-points/{controlPointId}")
    public ApiResponse<Boolean> deleteControlPoint(@PathVariable String controlPointId) {
        unifiedDataStore.deleteControlPoint(controlPointId);
        return ApiResponse.ok(true);
    }

    @PostMapping("/control-point-items")
    public ApiResponse<ControlLineResponse.ControlPointItemResponse> createControlPointItem(@Valid @RequestBody ControlPointItemUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createControlPointItem(request));
    }

    @PutMapping("/control-point-items/{itemId}")
    public ApiResponse<ControlLineResponse.ControlPointItemResponse> updateControlPointItem(
            @PathVariable String itemId,
            @Valid @RequestBody ControlPointItemUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateControlPointItem(itemId, request));
    }

    @DeleteMapping("/control-point-items/{itemId}")
    public ApiResponse<Boolean> deleteControlPointItem(@PathVariable String itemId) {
        unifiedDataStore.deleteControlPointItem(itemId);
        return ApiResponse.ok(true);
    }

    @PostMapping("/milestones")
    public ApiResponse<ScheduleOverviewResponse.MilestoneItem> createMilestone(@Valid @RequestBody MilestoneUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createMilestone(request));
    }

    @PutMapping("/milestones/{milestoneId}")
    public ApiResponse<ScheduleOverviewResponse.MilestoneItem> updateMilestone(
            @PathVariable String milestoneId,
            @Valid @RequestBody MilestoneUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateMilestone(milestoneId, request));
    }

    @DeleteMapping("/milestones/{milestoneId}")
    public ApiResponse<Boolean> deleteMilestone(@PathVariable String milestoneId) {
        unifiedDataStore.deleteMilestone(milestoneId);
        return ApiResponse.ok(true);
    }
}
