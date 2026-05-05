package com.royalclassico.controller;

import com.royalclassico.model.NewsPost;
import com.royalclassico.model.Player;
import com.royalclassico.service.NewsService;
import com.royalclassico.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Unified Secret Admin Management Controller.
 *
 * Base Path : /api/v1/rc-management-internal/
 * Auth      : X-Admin-Secret: RC-ADMIN-XKDF92-CLASSIFIED
 * Enforced  : AdminSecurityFilter (returns HTTP 404 on failure)
 */
@RestController
@RequestMapping("/api/v1/rc-management-internal")
@RequiredArgsConstructor
public class AdminManagementController {

    private final PlayerService playerService;
    private final NewsService   newsService;

    // ═══════════════════════════════════════════════════════════════
    //  PLAYER ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/rc-management-internal/players
     * Add a new player to the squad.
     */
    @PostMapping(value = "/players", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPlayer(
            @RequestPart("name")                              String name,
            @RequestPart(value = "jerseyName", required = false) String jerseyName,
            @RequestPart(value = "positions",  required = false) String positionsRaw,
            @RequestPart("jerseyNumber")                      String jerseyNumber,
            @RequestPart(value = "image", required = false)  MultipartFile image) {

        System.out.println("[AdminManagementController] POST /players — name=" + name);
        try {
            Player player = new Player();
            player.setName(name);
            player.setJerseyName(jerseyName);
            player.setJerseyNumber(parseJersey(jerseyNumber));
            if (positionsRaw != null && !positionsRaw.isBlank()) {
                List<String> pos = Arrays.asList(positionsRaw.trim().split("\\s*,\\s*"));
                player.setPositions(pos);
            }

            Player created = playerService.createPlayer(player, image);
            System.out.println("[AdminManagementController] Player created id=" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            System.err.println("[AdminManagementController] Bad request: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            System.err.println("[AdminManagementController] Image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/rc-management-internal/players/{id}
     * Update an existing player.
     */
    @PutMapping(value = "/players/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePlayer(
            @PathVariable String id,
            @RequestPart("name")                              String name,
            @RequestPart(value = "jerseyName", required = false) String jerseyName,
            @RequestPart(value = "positions",  required = false) String positionsRaw,
            @RequestPart("jerseyNumber")                      String jerseyNumber,
            @RequestPart(value = "image", required = false)  MultipartFile image) {

        System.out.println("[AdminManagementController] PUT /players/" + id);
        try {
            Player updated = new Player();
            updated.setName(name);
            updated.setJerseyName(jerseyName);
            updated.setJerseyNumber(parseJersey(jerseyNumber));
            if (positionsRaw != null && !positionsRaw.isBlank()) {
                List<String> pos = Arrays.asList(positionsRaw.trim().split("\\s*,\\s*"));
                updated.setPositions(pos);
            }

            Player saved = playerService.updatePlayer(id, updated, image);
            return ResponseEntity.ok(saved);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/rc-management-internal/players/{id}
     */
    @DeleteMapping("/players/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        System.out.println("[AdminManagementController] DELETE /players/" + id);
        try {
            playerService.deletePlayer(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  NEWS POST ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    @PostMapping(value = "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNews(
            @RequestPart("title")   String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        System.out.println("[AdminManagementController] POST /news — title=" + title);
        try {
            NewsPost post = new NewsPost();
            post.setTitle(title);
            post.setContent(content);

            NewsPost created = newsService.createPost(post, image);
            System.out.println("[AdminManagementController] News created id=" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        }
    }

    @PutMapping(value = "/news/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateNews(
            @PathVariable String id,
            @RequestPart("title")   String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        System.out.println("[AdminManagementController] PUT /news/" + id);
        try {
            NewsPost updated = new NewsPost();
            updated.setTitle(title);
            updated.setContent(content);

            NewsPost saved = newsService.updatePost(id, updated, image);
            return ResponseEntity.ok(saved);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable String id) {
        System.out.println("[AdminManagementController] DELETE /news/" + id);
        try {
            newsService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    private int parseJersey(String raw) {
        try {
            int n = Integer.parseInt(raw.trim());
            if (n < 1 || n > 99) throw new IllegalArgumentException("Jersey number must be 1–99");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("jerseyNumber must be an integer: " + raw);
        }
    }
}
