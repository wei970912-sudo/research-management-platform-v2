package com.research.management.project;

import com.research.management.common.ApiResponse;
import com.research.management.foundation.UnifiedDataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final UnifiedDataStore unifiedDataStore;

    public ProjectController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping
    public ApiResponse<List<ProjectSummary>> list() {
        return ApiResponse.ok(unifiedDataStore.listProjectSummaries());
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> detail(@PathVariable String projectId) {
        return ApiResponse.ok(unifiedDataStore.getProjectDetail(projectId));
    }

    @PostMapping
    public ApiResponse<ProjectDetailResponse> create(@Valid @RequestBody ProjectUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createProject(request));
    }

    @PutMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> update(@PathVariable String projectId, @Valid @RequestBody ProjectUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.updateProject(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Boolean> delete(@PathVariable String projectId) {
        unifiedDataStore.deleteProject(projectId);
        return ApiResponse.ok(true);
    }
}
