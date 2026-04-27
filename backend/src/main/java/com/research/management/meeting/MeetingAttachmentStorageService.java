package com.research.management.meeting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class MeetingAttachmentStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt", "wps");

    private final Path uploadRoot;

    public MeetingAttachmentStorageService(@Value("${app.storage.meeting-upload-dir:./uploads/meetings}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<String> store(String meetingId, MultipartFile[] files) {
        try {
            Path meetingDir = uploadRoot.resolve(meetingId).normalize();
            Files.createDirectories(meetingDir);

            List<String> savedNames = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String originalName = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
                if (originalName.isBlank()) {
                    continue;
                }

                String extension = extensionOf(originalName);
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持上传 Word、PDF、TXT、WPS 文件。");
                }

                String safeName = uniqueFileName(meetingDir, sanitizeFileName(originalName));
                Path target = meetingDir.resolve(safeName).normalize();
                if (!target.startsWith(meetingDir)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名不合法。");
                }

                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
                savedNames.add(safeName);
            }

            if (savedNames.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请至少选择一个附件。");
            }
            return savedNames;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "保存会议附件失败。", ex);
        }
    }

    public Resource loadAsResource(String meetingId, String fileName) {
        try {
            Path meetingDir = uploadRoot.resolve(meetingId).normalize();
            Path filePath = meetingDir.resolve(sanitizeFileName(fileName)).normalize();
            if (!filePath.startsWith(meetingDir) || !Files.exists(filePath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "附件不存在。");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "附件不可读取。");
            }
            return resource;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "读取会议附件失败。", ex);
        }
    }

    private String uniqueFileName(Path dir, String fileName) {
        String extension = extensionOf(fileName);
        String baseName = fileName;
        if (!extension.isBlank()) {
            baseName = fileName.substring(0, fileName.length() - extension.length() - 1);
        }

        String candidate = fileName;
        int index = 1;
        while (Files.exists(dir.resolve(candidate))) {
            candidate = extension.isBlank()
                    ? baseName + "-" + index
                    : baseName + "-" + index + "." + extension;
            index++;
        }
        return candidate;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
