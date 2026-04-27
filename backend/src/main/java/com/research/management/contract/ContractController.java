package com.research.management.contract;

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
@RequestMapping("/api/contracts")
public class ContractController {

    private final UnifiedDataStore unifiedDataStore;

    public ContractController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping
    public ApiResponse<List<ContractSummary>> list() {
        return ApiResponse.ok(unifiedDataStore.listContracts());
    }

    @GetMapping("/task-book-items")
    public ApiResponse<List<TaskBookItemSummary>> listTaskBookItems() {
        return ApiResponse.ok(unifiedDataStore.listTaskBookItems());
    }

    @PostMapping
    public ApiResponse<ContractSummary> create(@Valid @RequestBody ContractUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createContract(request));
    }

    @PostMapping("/task-book-items")
    public ApiResponse<TaskBookItemSummary> createTaskBookItem(@Valid @RequestBody TaskBookItemUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createTaskBookItem(request));
    }

    @PutMapping("/{contractId}")
    public ApiResponse<ContractSummary> update(@PathVariable String contractId, @Valid @RequestBody ContractUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.updateContract(contractId, request));
    }

    @PutMapping("/task-book-items/{taskBookItemId}")
    public ApiResponse<TaskBookItemSummary> updateTaskBookItem(
            @PathVariable String taskBookItemId,
            @Valid @RequestBody TaskBookItemUpsertRequest request
    ) {
        return ApiResponse.ok(unifiedDataStore.updateTaskBookItem(taskBookItemId, request));
    }

    @DeleteMapping("/{contractId}")
    public ApiResponse<Boolean> delete(@PathVariable String contractId) {
        unifiedDataStore.deleteContract(contractId);
        return ApiResponse.ok(true);
    }

    @DeleteMapping("/task-book-items/{taskBookItemId}")
    public ApiResponse<Boolean> deleteTaskBookItem(@PathVariable String taskBookItemId) {
        unifiedDataStore.deleteTaskBookItem(taskBookItemId);
        return ApiResponse.ok(true);
    }
}
