package com.royalclassico.controller;

import com.royalclassico.model.NewsPost;
import com.royalclassico.model.Player;
import com.royalclassico.repository.NewsPostRepository;
import com.royalclassico.repository.PlayerRepository;
import com.royalclassico.service.FileService;
import com.royalclassico.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Internal management REST API for the Admin Dashboard.
 * Base path: /api/v1/management-internal
 * Auth: X-Admin-Secret header (enforced by AdminSecurityFilter)
 */
@RestController
@RequestMapping("/api/v1/management-internal")
@SuppressWarnings("unused")
public class ManagementApiController {

    private final PlayerRepository playerRepository;
    private final NewsPostRepository newsRepository;
    private final FileService fileService;
    private final PlayerService playerService;

    public ManagementApiController(PlayerRepository playerRepository,
                                   NewsPostRepository newsRepository,
                                   FileService fileService,
                                   PlayerService playerService) {
        this.playerRepository = playerRepository;
        this.newsRepository = newsRepository;
        this.fileService = fileService;
        this.playerService = playerService;
    }

    /* ---------------- Players ---------------- */

    @GetMapping("/players")
    public ResponseEntity<?> listPlayers() {
        System.out.println("[ManagementApiController] GET /players — fetching all players");
        try {
            List<Player> players = playerRepository.findAll();
            System.out.println("[ManagementApiController] Found " + players.size() + " players");
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            System.err.println("[ManagementApiController] ERROR listing players: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to list players: " + e.getMessage());
        }
    }

    @PostMapping(value = "/players", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPlayer(
            @RequestPart("name") String name,
            @RequestPart(value = "jerseyName", required = false) String jerseyName,
            @RequestPart(value = "age", required = false) String ageStr,
            @RequestPart(value = "jerseyNumber", required = false) String jerseyNumberStr,
            @RequestPart(value = "positions", required = false) String positionsJson,
            @RequestPart(value = "tacticalRole", required = false) String tacticalRole,
            @RequestPart(value = "image", required = false) MultipartFile image)
    {
        System.out.println("[ManagementApiController] POST /players — name=" + name
                + ", jerseyName=" + jerseyName
                + ", age=" + ageStr
                + ", jerseyNumber=" + jerseyNumberStr
                + ", positions=" + positionsJson
                + ", tacticalRole=" + tacticalRole
                + ", hasImage=" + (image != null && !image.isEmpty()));

        try {
            Player p = new Player();
            p.setName(name);
            p.setJerseyName(jerseyName);
            p.setTacticalRole(tacticalRole);

            if (ageStr != null && !ageStr.isBlank()) {
                try { p.setAge(Integer.parseInt(ageStr.trim())); } catch (NumberFormatException ignored) {}
            }
            if (jerseyNumberStr != null && !jerseyNumberStr.isBlank()) {
                try { p.setJerseyNumber(Integer.parseInt(jerseyNumberStr.trim())); } catch (NumberFormatException ignored) {}
            }

            if (positionsJson != null && !positionsJson.isBlank()) {
                String trimmed = positionsJson.trim().replaceAll("[\\[\\]\"]", "");
                List<String> positions = new ArrayList<>();
                for (String s : trimmed.split("\\s*,\\s*")) {
                    if (!s.isBlank()) positions.add(s.trim());
                }
                p.setPositions(positions);
                System.out.println("[ManagementApiController] Parsed positions: " + positions);
            }

            if (image != null && !image.isEmpty()) {
                // Use PlayerService to handle image upload to cloud (ImageKit) and saving file metadata
                System.out.println("[ManagementApiController] Delegating image upload to PlayerService for: " + image.getOriginalFilename());
            }

            // Delegate creation to PlayerService which uploads the image (if any) and persists the Player
            Player saved = playerService.createPlayer(p, image);
            System.out.println("[ManagementApiController] Player saved with id=" + saved.getId());
            // Return OK so frontend knows cloud upload succeeded
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            System.err.println("[ManagementApiController] ERROR creating player: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create player: " + e.getMessage());
        }
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable String id) {
        System.out.println("[ManagementApiController] DELETE /players/" + id);
        Optional<Player> opt = playerRepository.findById(id);
        if (opt.isEmpty()) {
            System.out.println("[ManagementApiController] Player not found: " + id);
            return ResponseEntity.notFound().build();
        }
        Player p = opt.get();
        try {
            if (p.getImagePath() != null) {
                // Strip leading /uploads/ if present — FileService expects relative path
                String relPath = p.getImagePath().replaceFirst("^/uploads/", "");
                fileService.deleteFile(relPath);
            }
            playerRepository.deleteById(id);
            System.out.println("[ManagementApiController] Deleted player id=" + id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            System.err.println("[ManagementApiController] ERROR deleting player: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete player: " + ex.getMessage());
        }
    }

    /* ---------------- News ---------------- */

    @GetMapping("/news")
    public ResponseEntity<?> listNews() {
        System.out.println("[ManagementApiController] GET /news — fetching all news");
        try {
            List<NewsPost> posts = newsRepository.findAll();
            System.out.println("[ManagementApiController] Found " + posts.size() + " news posts");
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            System.err.println("[ManagementApiController] ERROR listing news: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to list news: " + e.getMessage());
        }
    }

    @PostMapping(value = "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNews(@ModelAttribute NewsPost n,
                                        @RequestPart(value = "image", required = false) MultipartFile image) {
        System.out.println("[ManagementApiController] POST /news — title=" + n.getTitle() + ", hasImage=" + (image != null && !image.isEmpty()));
        try {
            n.setCreatedAt(LocalDateTime.now());
            if (image != null && !image.isEmpty()) {
                System.out.println("[ManagementApiController] Uploading news image to cloud: " + image.getOriginalFilename());
                try {
                    FileService.UploadResult ur = fileService.uploadToCloud(image, "news");
                    if (ur != null) {
                        n.setImagePath(ur.url);
                        n.setImageFileId(ur.fileId);
                        System.out.println("[ManagementApiController] News image uploaded to: " + ur.url + " fileId=" + ur.fileId);
                    }
                } catch (Exception e) {
                    // Let service layer handle final fallback if needed; still continue to save here
                    System.err.println("[ManagementApiController] Image upload failed (controller): " + e.getMessage());
                }
            }
            NewsPost saved = newsRepository.save(n);
            System.out.println("[ManagementApiController] News saved with id=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.err.println("[ManagementApiController] ERROR creating news: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create news: " + ex.getMessage());
        }
    }

    @PostMapping(value = "/news", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewsJson(@RequestBody NewsPost n) {
        System.out.println("[ManagementApiController] POST /news (json) — title=" + n.getTitle());
        try {
            n.setCreatedAt(LocalDateTime.now());
            // No image to upload when JSON is used
            NewsPost saved = newsRepository.save(n);
            System.out.println("[ManagementApiController] News saved with id=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.err.println("[ManagementApiController] ERROR creating news (json): " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create news: " + ex.getMessage());
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable String id) {
        System.out.println("[ManagementApiController] DELETE /news/" + id);
        Optional<NewsPost> opt = newsRepository.findById(id);
        if (opt.isEmpty()) {
            System.out.println("[ManagementApiController] News post not found: " + id);
            return ResponseEntity.notFound().build();
        }
        NewsPost n = opt.get();
        try {
            if (n.getImageFileId() != null && !n.getImageFileId().isBlank()) {
                fileService.deleteRemoteByFileId(n.getImageFileId());
            } else if (n.getImagePath() != null) {
                // fallback: attempt local delete if the stored path is local
                fileService.deleteFile(n.getImagePath());
            }
            newsRepository.deleteById(id);
            System.out.println("[ManagementApiController] Deleted news id=" + id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            System.err.println("[ManagementApiController] ERROR deleting news: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete news: " + ex.getMessage());
        }
    }
}
