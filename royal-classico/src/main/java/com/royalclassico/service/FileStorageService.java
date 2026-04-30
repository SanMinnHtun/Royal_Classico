package com.royalclassico.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Handles all image upload and deletion logic.
 * Images are stored under ./uploads/{subDir}/ and served statically.
 */
@Slf4j
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Saves a MultipartFile to the given subdirectory.
     *
     * @param file   the uploaded file
     * @param subDir subdirectory within uploads (e.g. "news", "players")
     * @return relative path from uploads root (e.g. "news/abc123.jpg")
     */
    public String saveFile(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType
                    + ". Allowed: JPEG, PNG, WEBP, GIF");
        }

        // Resolve and create subdirectory
        Path targetDir = Paths.get(uploadDir).resolve(subDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        // Build a unique filename to avoid collisions — use timestamp to keep filenames readable
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // Use timestamp + random suffix (short) for uniqueness
        String uniqueFilename = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;

        Path targetPath = targetDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Saved file: {}", targetPath);
        return subDir + "/" + uniqueFilename;
    }

    /**
     * Deletes a file from the upload directory by its relative path.
     * Silently ignores missing files (idempotent cleanup).
     *
     * @param relativePath e.g. "news/abc123.jpg"
     */
    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath).toAbsolutePath().normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Deleted file: {}", filePath);
            } else {
                log.warn("File not found for deletion (already gone?): {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {} — {}", relativePath, e.getMessage());
        }
    }
}
