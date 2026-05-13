package com.royalclassico.service;

import com.royalclassico.model.Player;
import com.royalclassico.repository.PlayerRepository;
import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Business logic for Squad / Player management.
 * Uses ImageKit SDK v3.0.0 to upload images to the cloud and stores the public URL in the Player document.
 * No local file saving.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    // Helper to create an ImageKitClient using the installed SDK's helper that wires OkHttp
    private ImageKitClient createImageKitClient() {
        try {
            // Use the SDK-provided OkHttp helper which reads env/properties and returns a ready ImageKitClient
            return ImageKitOkHttpClient.fromEnv();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize ImageKit client", ex);
        }
    }

    // ── Read ────────────────────────────────────────────────────────────────

    public List<Player> getAllPlayers() {
        List<Player> list = playerRepository.findAll();
        for (Player p : list) {
            if (p.getImagePath() == null) p.setImagePath("");
            if (p.getImageFileId() == null) p.setImageFileId("");
        }
        return list;
    }

    public Optional<Player> getPlayerById(String id) {
        return playerRepository.findById(id);
    }

    // ── Write ───────────────────────────────────────────────────────────────

    public Player createPlayer(Player player, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                ImageKitClient ik = createImageKitClient();
                byte[] bytes = imageFile.getBytes();
                String filename = UUID.randomUUID() + "-" + (imageFile.getOriginalFilename() == null ? "upload" : imageFile.getOriginalFilename());

                FileUploadParams.Body body = FileUploadParams.Body.builder()
                        .file(new ByteArrayInputStream(bytes))
                        .fileName(filename)
                        .useUniqueFileName(true)
                        .folder("players")
                        .build();

                FileUploadParams params = FileUploadParams.builder().body(body).build();
                FileUploadResponse response = ik.files().upload(params);
                String publicUrl = response.url().orElse(null);
                String fileId = response.fileId().orElse(null);

                if (publicUrl != null && !publicUrl.isEmpty()) {
                    player.setImagePath(publicUrl);
                }
                if (fileId != null && !fileId.isEmpty()) {
                    player.setImageFileId(fileId);
                } else {
                    log.warn("ImageKit upload returned empty fileId for file {}", filename);
                }
            } catch (Exception e) {
                log.error("Failed to upload player image to ImageKit", e);
                throw new IOException("Image upload failed", e);
            }
        }
        return playerRepository.save(player);
    }

    public Player updatePlayer(String id, Player updatedData, MultipartFile imageFile) throws IOException {
        Player existing = playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));

        // Only overwrite when incoming values are non-null — prevents accidental clearing of fields
        if (updatedData.getName() != null) {
            existing.setName(updatedData.getName());
        }
        if (updatedData.getJerseyName() != null) {
            existing.setJerseyName(updatedData.getJerseyName());
        }
        if (updatedData.getAge() != null) {
            Integer age = updatedData.getAge();
            // Basic validation: reasonable human footballing age
            if (age < 12 || age > 70) {
                throw new IllegalArgumentException("age must be between 12 and 70");
            }
            existing.setAge(age);
        }
        if (updatedData.getJerseyNumber() != null) {
            Integer num = updatedData.getJerseyNumber();
            if (num < 0 || num > 999) {
                throw new IllegalArgumentException("jerseyNumber out of allowed range");
            }
            existing.setJerseyNumber(num);
        }
        if (updatedData.getPositions() != null) {
            existing.setPositions(updatedData.getPositions());
        }
        // tacticalRole mapping
        if (updatedData.getTacticalRole() != null) {
            existing.setTacticalRole(updatedData.getTacticalRole());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                ImageKitClient ik = createImageKitClient();
                byte[] bytes = imageFile.getBytes();
                String filename = UUID.randomUUID() + "-" + (imageFile.getOriginalFilename() == null ? "upload" : imageFile.getOriginalFilename());

                FileUploadParams.Body body = FileUploadParams.Body.builder()
                        .file(new ByteArrayInputStream(bytes))
                        .fileName(filename)
                        .useUniqueFileName(true)
                        .folder("players")
                        .build();

                FileUploadParams params = FileUploadParams.builder().body(body).build();
                FileUploadResponse response = ik.files().upload(params);
                String publicUrl = response.url().orElse(null);
                String fileId = response.fileId().orElse(null);

                if (publicUrl != null && !publicUrl.isEmpty()) {
                    existing.setImagePath(publicUrl);
                }
                if (fileId != null && !fileId.isEmpty()) {
                    existing.setImageFileId(fileId);
                } else {
                    log.warn("ImageKit upload returned empty fileId for file {}", filename);
                }
            } catch (Exception e) {
                log.error("Failed to upload updated player image to ImageKit", e);
                throw new IOException("Image upload failed", e);
            }
        }

        return playerRepository.save(existing);
    }

    public void deletePlayer(String id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));

        // If we have a remote ImageKit fileId, attempt to delete it from ImageKit first
        if (player.getImageFileId() != null && !player.getImageFileId().isBlank()) {
            try {
                ImageKitClient ik = createImageKitClient();
                try {
                    ik.files().delete(player.getImageFileId());
                } catch (Exception nm) {
                    log.warn("ImageKit delete call failed for fileId={} playerId={}: {}", player.getImageFileId(), id, nm.getMessage());
                }
            } catch (Exception ex) {
                log.warn("Failed to delete ImageKit file id={} for player id={}: {}", player.getImageFileId(), id, ex.getMessage());
            }
        }

        // Delete the DB document after attempting cloud cleanup
        playerRepository.deleteById(id);
        log.info("Deleted player id={}, name={}", id, player.getName());
    }
}
