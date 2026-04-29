package com.royalclassico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadsBase;

    private Path basePath() {
        return Paths.get(uploadsBase).toAbsolutePath().normalize();
    }

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Path dir = basePath();
        if (subfolder != null && !subfolder.isBlank()) {
            dir = dir.resolve(subfolder);
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IOException("Failed to create upload directory: " + dir, e);
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID().toString() + ext;
        Path target = dir.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to store file", e);
        }
        // Return path relative to uploads base (e.g. players/uuid.jpg or news/uuid.jpg)
        Path rel = basePath().relativize(target);
        return rel.toString().replace('\\', '/');
    }

    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return false;
        Path p = basePath().resolve(relativePath).normalize();
        try {
            return Files.deleteIfExists(p);
        } catch (IOException e) {
            // Log and swallow — do not crash the application
            System.err.println("Failed to delete file: " + p + " — " + e.getMessage());
            return false;
        }
    }
}
