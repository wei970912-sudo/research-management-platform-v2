package com.research.management.foundation;

import com.research.management.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foundation")
public class FoundationController {

    private final UnifiedDataStore unifiedDataStore;

    public FoundationController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping("/snapshot")
    public ApiResponse<FoundationSnapshotResponse> snapshot() {
        return ApiResponse.ok(unifiedDataStore.snapshot());
    }
}

