package com.royalclassico.controller;

import com.royalclassico.model.Player;
import com.royalclassico.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {
        return playerService.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST — Add a new player.
     * position must be one of: GK, DEF, MID, FWD
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPlayer(
            @RequestPart("name")         String name,
            @RequestPart("position")     String position,
            @RequestPart("jerseyNumber") String jerseyNumber,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            Player player = new Player();
            player.setName(name);
            player.setPosition(Player.Position.valueOf(position.toUpperCase()));
            player.setJerseyNumber(Integer.parseInt(jerseyNumber));
            Player created = playerService.createPlayer(player, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid position. Use: GK, DEF, MID, FWD. Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * PUT — Update an existing player.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePlayer(
            @PathVariable String id,
            @RequestPart("name")         String name,
            @RequestPart("position")     String position,
            @RequestPart("jerseyNumber") String jerseyNumber,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            Player updatedData = new Player();
            updatedData.setName(name);
            updatedData.setPosition(Player.Position.valueOf(position.toUpperCase()));
            updatedData.setJerseyNumber(Integer.parseInt(jerseyNumber));
            Player updated = playerService.updatePlayer(id, updatedData, image);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid data: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * DELETE — Remove a player and their image file.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        try {
            playerService.deletePlayer(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
