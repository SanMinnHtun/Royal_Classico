package com.royalclassico.service;

import com.royalclassico.model.Player;
import com.royalclassico.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Business logic for Squad / Player management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final FileStorageService fileStorageService;

    // ── Read ────────────────────────────────────────────────────────────────

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Optional<Player> getPlayerById(String id) {
        return playerRepository.findById(id);
    }

    /**
     * Returns players grouped by position in display order: GK → DEF → MID → FWD.
     */
    public Map<Player.Position, List<Player>> getPlayersGroupedByPosition() {
        Map<Player.Position, List<Player>> grouped = new LinkedHashMap<>();
        for (Player.Position pos : Player.Position.values()) {
            List<Player> players = playerRepository.findByPositionOrderByJerseyNumberAsc(pos);
            if (!players.isEmpty()) {
                grouped.put(pos, players);
            }
        }
        return grouped;
    }

    // ── Write ───────────────────────────────────────────────────────────────

    public Player createPlayer(Player player, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.saveFile(imageFile, "players");
            player.setImagePath(path);
        }
        return playerRepository.save(player);
    }

    public Player updatePlayer(String id, Player updatedData, MultipartFile imageFile) throws IOException {
        Player existing = playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));

        existing.setName(updatedData.getName());
        existing.setPosition(updatedData.getPosition());
        existing.setJerseyNumber(updatedData.getJerseyNumber());

        // Handle image replacement — delete old file first (strict cleanup)
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorageService.deleteFile(existing.getImagePath());
            String newPath = fileStorageService.saveFile(imageFile, "players");
            existing.setImagePath(newPath);
        }

        return playerRepository.save(existing);
    }

    public void deletePlayer(String id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));
        fileStorageService.deleteFile(player.getImagePath()); // strict cleanup
        playerRepository.deleteById(id);
        log.info("Deleted player id={}, name={}", id, player.getName());
    }
}
