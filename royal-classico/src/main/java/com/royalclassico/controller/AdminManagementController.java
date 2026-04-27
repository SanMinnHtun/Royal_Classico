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
import java.util.NoSuchElementException;

/**
 * Unified Secret Admin Management Controller.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  Base Path : /api/v1/rc-management-internal/                     │
 * │  Auth      : X-Admin-Secret: RC-ADMIN-XKDF92-CLASSIFIED         │
 * │  Enforced  : AdminSecurityFilter (returns HTTP 404 on failure)   │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Contains POST / PUT / DELETE for both Players and News Posts.
 * Read (GET) operations are handled by the public API & individual admin controllers.
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
     *
     * Form fields (multipart/form-data):
     *   name         — player's full name
     *   position     — GK | DEF | MID | FWD
     *   jerseyNumber — 1–99
     *   image        — (optional) player photo
     */
    @PostMapping(value = "/players", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPlayer(
            @RequestPart("name")         String name,
            @RequestPart("position")     String position,
            @RequestPart("jerseyNumber") String jerseyNumber,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            Player player = new Player();
            player.setName(name);
            player.setPosition(parsePosition(position));
            player.setJerseyNumber(parseJersey(jerseyNumber));

            Player created = playerService.createPlayer(player, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return serverError("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/rc-management-internal/players/{id}
     * Update an existing player. Old image is deleted from disk if a new one is uploaded.
     */
    @PutMapping(value = "/players/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePlayer(
            @PathVariable String id,
            @RequestPart("name")         String name,
            @RequestPart("position")     String position,
            @RequestPart("jerseyNumber") String jerseyNumber,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            Player updated = new Player();
            updated.setName(name);
            updated.setPosition(parsePosition(position));
            updated.setJerseyNumber(parseJersey(jerseyNumber));

            Player saved = playerService.updatePlayer(id, updated, image);
            return ResponseEntity.ok(saved);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return serverError("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/rc-management-internal/players/{id}
     * Remove a player and physically delete their image from disk.
     */
    @DeleteMapping("/players/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
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

    /**
     * POST /api/v1/rc-management-internal/news
     * Publish a new news article.
     *
     * Form fields (multipart/form-data):
     *   title   — article headline
     *   content — article body
     *   image   — (optional) cover image
     */
    @PostMapping(value = "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNews(
            @RequestPart("title")   String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            NewsPost post = new NewsPost();
            post.setTitle(title);
            post.setContent(content);

            NewsPost created = newsService.createPost(post, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return serverError("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/rc-management-internal/news/{id}
     * Update an existing article. Old image is deleted from disk if a new one is uploaded.
     */
    @PutMapping(value = "/news/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateNews(
            @PathVariable String id,
            @RequestPart("title")   String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            NewsPost updated = new NewsPost();
            updated.setTitle(title);
            updated.setContent(content);

            NewsPost saved = newsService.updatePost(id, updated, image);
            return ResponseEntity.ok(saved);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return serverError("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/rc-management-internal/news/{id}
     * Remove a news article and physically delete its image from disk.
     */
    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable String id) {
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

    private Player.Position parsePosition(String raw) {
        try {
            return Player.Position.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid position '" + raw + "'. Must be one of: GK, DEF, MID, FWD");
        }
    }

    private int parseJersey(String raw) {
        try {
            int n = Integer.parseInt(raw.trim());
            if (n < 1 || n > 99) throw new IllegalArgumentException("Jersey number must be 1–99");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("jerseyNumber must be an integer: " + raw);
        }
    }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.badRequest().body(msg);
    }

    private ResponseEntity<String> serverError(String msg) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}
