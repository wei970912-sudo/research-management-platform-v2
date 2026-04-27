package com.research.management.calendar;

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
@RequestMapping("/api/calendar")
public class CalendarController {

    private final UnifiedDataStore unifiedDataStore;

    public CalendarController(UnifiedDataStore unifiedDataStore) {
        this.unifiedDataStore = unifiedDataStore;
    }

    @GetMapping("/events")
    public ApiResponse<List<CalendarEventResponse>> list() {
        return ApiResponse.ok(unifiedDataStore.listCalendarEvents());
    }

    @PostMapping("/events")
    public ApiResponse<CalendarEventResponse> create(@Valid @RequestBody CalendarEventUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createCalendarEvent(request));
    }

    @PutMapping("/events/{eventId}")
    public ApiResponse<CalendarEventResponse> update(@PathVariable String eventId, @Valid @RequestBody CalendarEventUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.updateCalendarEvent(eventId, request));
    }

    @DeleteMapping("/events/{eventId}")
    public ApiResponse<Boolean> delete(@PathVariable String eventId) {
        unifiedDataStore.deleteCalendarEvent(eventId);
        return ApiResponse.ok(true);
    }
}
