package com.royalclassico.controller;

import com.royalclassico.model.NewsPost;
import com.royalclassico.model.Player;
import com.royalclassico.repository.NewsPostRepository;
import com.royalclassico.repository.PlayerRepository;
import com.royalclassico.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class ManagementApiController {

    private final PlayerRepository playerRepository;
    private final NewsPostRepository newsRepository;
    private final FileService fileService;

    public ManagementApiController(PlayerRepository playerRepository,
                                   NewsPostRepository newsRepository,
                                   FileService fileService) {
        this.playerRepository = playerRepository;
        this.newsRepository = newsRepository;
        this.fileService = fileService;
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
            // Self-healing: ensure uploads/players directory exists before saving
            Files.createDirectories(Paths.get("uploads", "players"));
            Files.createDirectories(Paths.get("uploads", "fixtures"));
            Files.createDirectories(Paths.get("uploads", "news"));

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
                System.out.println("[ManagementApiController] Uploading image: " + image.getOriginalFilename());
                String original = image.getOriginalFilename();
                String safeOriginal = (original == null) ? "upload" : original.replaceAll("[^a-zA-Z0-9._-]", "_");
                String ext = "";
                int i = safeOriginal.lastIndexOf('.');
                if (i >= 0) ext = safeOriginal.substring(i);
                String fileName = System.currentTimeMillis() + "_" + (i >= 0 ? safeOriginal.substring(0, i) : safeOriginal) + ext;

                java.nio.file.Path target = Paths.get("uploads", "players", fileName).toAbsolutePath().normalize();
                try {
                    image.transferTo(target.toFile());
                    System.out.println("[ManagementApiController] Image saved to: " + target);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to write uploaded file: " + ioe.getMessage());
                }
                // Save web-relative path relative to application root (uploads are served from filesystem root)
                p.setImagePath("players/" + fileName);
            }

            Player saved = playerRepository.save(p);
            System.out.println("[ManagementApiController] Player saved with id=" + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            e.printStackTrace();
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
    public ResponseEntity<?> createNews(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        System.out.println("[ManagementApiController] POST /news — title=" + title + ", hasImage=" + (image != null && !image.isEmpty()));
        try {
            // Self-healing: ensure uploads/news directory exists
            Files.createDirectories(Paths.get("uploads", "news"));
            NewsPost n = new NewsPost();
            n.setTitle(title);
            n.setContent(content);
            n.setCreatedAt(LocalDateTime.now());
            if (image != null && !image.isEmpty()) {
                System.out.println("[ManagementApiController] Uploading news image: " + image.getOriginalFilename());
                String path = fileService.storeFile(image, "news");
                n.setImagePath(path);
                System.out.println("[ManagementApiController] News image stored at: " + path);
            }
            NewsPost saved = newsRepository.save(n);
            System.out.println("[ManagementApiController] News saved with id=" + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IOException e) {
            System.err.println("[ManagementApiController] File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("[ManagementApiController] ERROR creating news: " + ex.getMessage());
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
            if (n.getImagePath() != null) fileService.deleteFile(n.getImagePath());
            newsRepository.deleteById(id);
            System.out.println("[ManagementApiController] Deleted news id=" + id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            System.err.println("[ManagementApiController] ERROR deleting news: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete news: " + ex.getMessage());
        }
    }
}
