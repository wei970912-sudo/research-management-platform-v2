package com.research.management.dashboard;

import com.research.management.common.ApiResponse;
import com.research.management.foundation.UnifiedDataStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UnifiedDataStore unifiedDataStore;

    public DashboardController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.ok(unifiedDataStore.buildDashboardSummary());
    }
}
