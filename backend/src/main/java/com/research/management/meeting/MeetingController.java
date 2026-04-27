package com.research.management.meeting;

import com.research.management.common.ApiResponse;
import com.research.management.foundation.UnifiedDataStore;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final UnifiedDataStore unifiedDataStore;
    private final MeetingAttachmentStorageService attachmentStorageService;

    public MeetingController(UnifiedDataStore unifiedDataStore, MeetingAttachmentStorageService attachmentStorageService) {
        this.unifiedDataStore = unifiedDataStore;
        this.attachmentStorageService = attachmentStorageService;
    }

    @GetMapping
    public ApiResponse<List<MeetingSummary>> list() {
        return ApiResponse.ok(unifiedDataStore.listMeetings());
    }

    @PostMapping
    public ApiResponse<MeetingSummary> create(@Valid @RequestBody MeetingUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createMeeting(request));
    }

    @PutMapping("/{meetingId}")
    public ApiResponse<MeetingSummary> update(@PathVariable String meetingId, @Valid @RequestBody MeetingUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.updateMeeting(meetingId, request));
    }

    @PostMapping(value = "/{meetingId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MeetingSummary> uploadAttachments(
            @PathVariable String meetingId,
            @RequestParam("files") MultipartFile[] files
    ) {
        List<String> storedNames = attachmentStorageService.store(meetingId, files);
        return ApiResponse.ok(unifiedDataStore.appendMeetingAttachments(meetingId, storedNames));
    }

    @GetMapping("/{meetingId}/attachments/{fileName:.+}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String meetingId,
            @PathVariable String fileName
    ) {
        Resource resource = attachmentStorageService.loadAsResource(meetingId, fileName);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resource.getFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(resource);
    }

    @DeleteMapping("/{meetingId}")
    public ApiResponse<Boolean> delete(@PathVariable String meetingId) {
        unifiedDataStore.deleteMeeting(meetingId);
        return ApiResponse.ok(true);
    }
}
