package com.research.management.stakeholder;

import com.research.management.common.ApiResponse;
import com.research.management.foundation.UnifiedDataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stakeholders")
public class StakeholderController {

    private final UnifiedDataStore unifiedDataStore;

    public StakeholderController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping
    public ApiResponse<List<StakeholderResponse>> list() {
        return ApiResponse.ok(unifiedDataStore.listStakeholders());
    }

    @PostMapping
    public ApiResponse<StakeholderResponse> create(@Valid @RequestBody StakeholderUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createStakeholder(request));
    }

    @PutMapping("/{stakeholderId}")
    public ApiResponse<StakeholderResponse> update(
            @PathVariable String stakeholderId,
            @Valid @RequestBody StakeholderUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateStakeholder(stakeholderId, request));
    }

    @DeleteMapping("/{stakeholderId}")
    public ApiResponse<Boolean> delete(@PathVariable String stakeholderId) {
        unifiedDataStore.deleteStakeholder(stakeholderId);
        return ApiResponse.ok(true);
    }
}
