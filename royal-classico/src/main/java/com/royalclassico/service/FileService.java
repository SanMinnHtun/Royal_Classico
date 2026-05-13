package com.royalclassico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;

@Service
public class FileService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadsBase;

    // Public placeholder image used when cloud upload fails — ensures DB still has a valid image path
    public static final String PLACEHOLDER_IMAGE = "/images/photo_2026-04-26_23-43-31.jpg";

    private Path basePath() {
        return Paths.get(uploadsBase).toAbsolutePath().normalize();
    }

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        // Backwards-compatible shim that uploads directly to ImageKit and returns the public URL.
        if (file == null || file.isEmpty()) return null;
        UploadResult res = uploadToCloud(file, subfolder);
        return res == null ? null : res.url;
    }

    /**
     * Helper result for cloud uploads.
     */
    public static class UploadResult {
        public final String url;
        public final String fileId;
        public UploadResult(String url, String fileId) { this.url = url; this.fileId = fileId; }
    }

    /**
     * Uploads file to ImageKit and returns public URL and fileId.
     */
    public UploadResult uploadToCloud(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) return null;
        try {
            // Initialize ImageKit client using the SDK-provided OkHttp helper
            ImageKitClient ik = ImageKitOkHttpClient.fromEnv();

            byte[] bytes = file.getBytes();
            String filename = UUID.randomUUID().toString() + "-" + (file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());

            FileUploadParams.Body body = FileUploadParams.Body.builder()
                    .file(new java.io.ByteArrayInputStream(bytes))
                    .fileName(filename)
                    .useUniqueFileName(true)
                    .folder(folder)
                    .build();

            FileUploadParams params = FileUploadParams.builder().body(body).build();
            FileUploadResponse response = ik.files().upload(params);
            String publicUrl = response.url().orElse(null);
            String fileId = response.fileId().orElse(null);
            return new UploadResult(publicUrl, fileId);
        } catch (Exception ex) {
            throw new IOException("ImageKit upload failed", ex);
        }
    }

    /** Delete remote ImageKit file by fileId. Returns true if deleted or false on failure. */
    public boolean deleteRemoteByFileId(String fileId) {
        if (fileId == null || fileId.isBlank()) return false;
        try {
            ImageKitClient ik = ImageKitOkHttpClient.fromEnv();
            ik.files().delete(fileId);
            return true;
        } catch (Exception ex) {
            System.err.println("Failed to delete remote fileId=" + fileId + " — " + ex.getMessage());
            return false;
        }
    }

    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return false;
        // If the path looks like an ImageKit URL, do not attempt local delete.
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            // No fileId provided here — caller should use deleteRemoteByFileId when possible.
            System.err.println("deleteFile called with remote URL; expected fileId for remote deletion: " + relativePath);
            return false;
        }
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
