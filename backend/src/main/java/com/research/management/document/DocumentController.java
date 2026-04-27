package com.research.management.document;

import com.research.management.common.ApiResponse;
import com.research.management.foundation.UnifiedDataStore;
import com.research.management.foundation.entity.DocumentEntity;
import com.research.management.foundation.entity.ProjectEntity;
import com.research.management.foundation.mapper.FoundationMapper;
import com.research.management.foundation.repository.DocumentRepository;
import com.research.management.foundation.repository.ProjectRepository;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final UnifiedDataStore unifiedDataStore;
    private final DocumentAttachmentStorageService attachmentStorageService;
    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final FoundationMapper foundationMapper;

    public DocumentController(
            UnifiedDataStore unifiedDataStore,
            DocumentAttachmentStorageService attachmentStorageService,
            DocumentRepository documentRepository,
            ProjectRepository projectRepository,
            FoundationMapper foundationMapper
    ) {
        this.unifiedDataStore = unifiedDataStore;
        this.attachmentStorageService = attachmentStorageService;
        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
        this.foundationMapper = foundationMapper;
    }

    @GetMapping
    public ApiResponse<List<DocumentSummary>> list() {
        return ApiResponse.ok(unifiedDataStore.listDocuments());
    }

    @PostMapping
    public ApiResponse<DocumentSummary> create(@Valid @RequestBody DocumentUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.createDocument(request));
    }

    @PutMapping("/{documentId}")
    public ApiResponse<DocumentSummary> update(@PathVariable String documentId, @Valid @RequestBody DocumentUpsertRequest request) {
        return ApiResponse.ok(unifiedDataStore.updateDocument(documentId, request));
    }

    @PostMapping(value = "/{documentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DocumentSummary> uploadAttachments(
            @PathVariable String documentId,
            @RequestParam("files") MultipartFile[] files
    ) {
        List<String> storedNames = attachmentStorageService.store(documentId, files);
        DocumentEntity entity = documentRepository.findById(documentId).orElseThrow();
        LinkedHashSet<String> merged = Stream.concat(
                        splitAttachmentNames(entity.getAttachmentNames()).stream(),
                        storedNames.stream()
                )
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        entity.setAttachmentNames(String.join(", ", merged));
        entity.setAttachmentCount(merged.size());
        documentRepository.save(entity);
        return ApiResponse.ok(toDocumentSummary(entity));
    }

    @GetMapping("/{documentId}/attachments/{fileName:.+}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String documentId,
            @PathVariable String fileName
    ) {
        Resource resource = attachmentStorageService.loadAsResource(documentId, fileName);
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

    @DeleteMapping("/{documentId}")
    public ApiResponse<Boolean> delete(@PathVariable String documentId) {
        unifiedDataStore.deleteDocument(documentId);
        return ApiResponse.ok(true);
    }

    private List<String> splitAttachmentNames(String text) {
        return List.of(Objects.requireNonNullElse(text, "").split(","))
                .stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private DocumentSummary toDocumentSummary(DocumentEntity entity) {
        String projectName = projectRepository.findById(entity.getProjectId())
                .map(ProjectEntity::getName)
                .orElse(entity.getProjectId());
        var item = foundationMapper.toRecord(entity);
        return new DocumentSummary(
                item.id(),
                item.nodeName(),
                projectName,
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
}