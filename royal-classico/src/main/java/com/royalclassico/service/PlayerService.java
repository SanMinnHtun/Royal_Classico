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

    // ── Write ───────────────────────────────────────────────────────────────

    public Player createPlayer(Player player, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String path = fileStorageService.saveFile(imageFile, "players");
                if (path != null) {
                    player.setImagePath(path);
                }
            } catch (IOException e) {
                log.error("Failed to store player image", e);
                throw e;
            }
        }
        return playerRepository.save(player);
    }

    public Player updatePlayer(String id, Player updatedData, MultipartFile imageFile) throws IOException {
        Player existing = playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));

        existing.setName(updatedData.getName());
        existing.setJerseyName(updatedData.getJerseyName());
        existing.setAge(updatedData.getAge());
        existing.setJerseyNumber(updatedData.getJerseyNumber());
        if (updatedData.getPositions() != null) {
            existing.setPositions(updatedData.getPositions());
        }

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
        fileStorageService.deleteFile(player.getImagePath());
        playerRepository.deleteById(id);
        log.info("Deleted player id={}, name={}", id, player.getName());
    }
}
