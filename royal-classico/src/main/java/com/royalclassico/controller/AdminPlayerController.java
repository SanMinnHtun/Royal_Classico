package com.royalclassico.controller;

import com.royalclassico.model.Player;
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
 * Secret Admin REST API for Player/Squad management.
 *
 * Base path: /api/v1/rc-internal-mgmt/players
 * Auth:       X-Admin-Secret header (enforced by AdminSecurityFilter)
 */
@RestController
@RequestMapping("/api/v1/rc-internal-mgmt/players")
@RequiredArgsConstructor
public class AdminPlayerController {

    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        System.out.println("[AdminPlayerController] GET /players");
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {
        System.out.println("[AdminPlayerController] GET /players/" + id);
        return playerService.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST — Add a new player.
     * positions: comma-separated string, e.g. "GK" or "DEF,MID"
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPlayer(
            @RequestPart("name")                              String name,
            @RequestPart(value = "jerseyName", required = false) String jerseyName,
            @RequestPart(value = "positions",  required = false) String positionsRaw,
            @RequestPart(value = "age", required = false)     String ageRaw,
            @RequestPart("jerseyNumber")                      String jerseyNumber,
            @RequestPart(value = "tacticalRole", required = false) String tacticalRole,
            @RequestPart(value = "image", required = false)  MultipartFile image) {

        System.out.println("[AdminPlayerController] POST /players — name=" + name);
        try {
            Player player = new Player();
            player.setName(name);
            player.setJerseyName(jerseyName);
            // parse jersey number safely
            if (jerseyNumber != null && !jerseyNumber.isBlank()) {
                player.setJerseyNumber(Integer.parseInt(jerseyNumber.trim()));
            }
            // parse age safely
            if (ageRaw != null && !ageRaw.isBlank()) {
                try {
                    player.setAge(Integer.parseInt(ageRaw.trim()));
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid age provided: " + ageRaw);
                    return ResponseEntity.badRequest().body("Invalid age");
                }
            }
            player.setTacticalRole(tacticalRole);
            if (positionsRaw != null && !positionsRaw.isBlank()) {
                player.setPositions(Arrays.asList(positionsRaw.trim().split("\\s*,\\s*")));
            }

            Player created = playerService.createPlayer(player, image);
            System.out.println("[AdminPlayerController] Created player id=" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            System.err.println("[AdminPlayerController] Bad request: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[AdminPlayerController] Image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create player");
        }
    }

    /**
     * PUT — Update an existing player.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePlayer(
            @PathVariable String id,
            @RequestPart("name")                              String name,
            @RequestPart(value = "jerseyName", required = false) String jerseyName,
            @RequestPart(value = "positions",  required = false) String positionsRaw,
            @RequestPart(value = "age", required = false)     String ageRaw,
            @RequestPart("jerseyNumber")                      String jerseyNumber,
            @RequestPart(value = "tacticalRole", required = false) String tacticalRole,
            @RequestPart(value = "image", required = false)  MultipartFile image) {

        System.out.println("[AdminPlayerController] PUT /players/" + id);
        try {
            // Ensure the player exists first so we can give 404 immediately
            var existingOpt = playerService.getPlayerById(id);
            if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();

            Player updatedData = new Player();
            // Name is required by the API contract — still validate non-empty
            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest().body("name is required");
            }
            updatedData.setName(name.trim());

            if (jerseyName != null && !jerseyName.isBlank()) {
                updatedData.setJerseyName(jerseyName.trim());
            }

            // jersey number: parse safely, leave null to indicate no-change
            if (jerseyNumber != null && !jerseyNumber.isBlank()) {
                try {
                    int num = Integer.parseInt(jerseyNumber.trim());
                    if (num < 0 || num > 999) return ResponseEntity.badRequest().body("jerseyNumber out of range");
                    updatedData.setJerseyNumber(num);
                } catch (NumberFormatException nfe) {
                    return ResponseEntity.badRequest().body("jerseyNumber must be an integer");
                }
            }

            // age: optional — only set when provided and valid
            if (ageRaw != null && !ageRaw.isBlank()) {
                try {
                    int age = Integer.parseInt(ageRaw.trim());
                    if (age < 12 || age > 70) return ResponseEntity.badRequest().body("age must be between 12 and 70");
                    updatedData.setAge(age);
                } catch (NumberFormatException nfe) {
                    return ResponseEntity.badRequest().body("age must be an integer");
                }
            }

            updatedData.setTacticalRole(tacticalRole);
            if (positionsRaw != null && !positionsRaw.isBlank()) {
                updatedData.setPositions(Arrays.asList(positionsRaw.trim().split("\\s*,\\s*")));
            }

            Player updated = playerService.updatePlayer(id, updatedData, image);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update player");
        }
    }

    /**
     * DELETE — Remove a player and their image file.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        System.out.println("[AdminPlayerController] DELETE /players/" + id);
        try {
            playerService.deletePlayer(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
