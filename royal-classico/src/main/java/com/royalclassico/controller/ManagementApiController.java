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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<Player> listPlayers() {
        return playerRepository.findAll();
    }

    @PostMapping(value = "/players", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPlayer(
            @RequestPart("realName") String realName,
            @RequestPart(value = "jerseyName", required = false) String jerseyName,
            @RequestPart(value = "age", required = false) Integer age,
            @RequestPart("jerseyNumber") Integer jerseyNumber,
            @RequestPart(value = "positions", required = false) String positionsJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            Player p = new Player();
            p.setRealName(realName);
            p.setJerseyName(jerseyName);
            p.setAge(age);
            p.setJerseyNumber(jerseyNumber);
            if (positionsJson != null && !positionsJson.isBlank()) {
                String trimmed = positionsJson.trim();
                // remove surrounding brackets and quotes, then split by comma
                trimmed = trimmed.replaceAll("[\\[\\]\"]", "");
                List<String> positions = new ArrayList<>();
                if (!trimmed.isBlank()) {
                    for (String s : trimmed.split("\\s*,\\s*")) {
                        if (!s.isBlank()) positions.add(s);
                    }
                }
                p.setPositions(positions);
            }
            if (image != null && !image.isEmpty()) {
                String path = fileService.storeFile(image, "players");
                p.setImagePath(path);
            }
            playerRepository.save(p);
            return ResponseEntity.status(HttpStatus.CREATED).body(p);
        } catch (IOException e) {
            System.err.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        } catch (Exception ex) {
            System.err.println("Failed to create player: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create player");
        }
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable String id) {
        Optional<Player> opt = playerRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Player p = opt.get();
        try {
            if (p.getImagePath() != null) fileService.deleteFile(p.getImagePath());
            playerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            System.err.println("Failed to delete player: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete player");
        }
    }

    /* ---------------- News ---------------- */

    @GetMapping("/news")
    public List<NewsPost> listNews() {
        return newsRepository.findAll();
    }

    @PostMapping(value = "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNews(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            NewsPost n = new NewsPost();
            n.setTitle(title);
            n.setContent(content);
            n.setCreatedAt(LocalDateTime.now());
            if (image != null && !image.isEmpty()) {
                String path = fileService.storeFile(image, "news");
                n.setImagePath(path);
            }
            newsRepository.save(n);
            return ResponseEntity.status(HttpStatus.CREATED).body(n);
        } catch (IOException e) {
            System.err.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        } catch (Exception ex) {
            System.err.println("Failed to create news: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create news");
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable String id) {
        Optional<NewsPost> opt = newsRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        NewsPost n = opt.get();
        try {
            if (n.getImagePath() != null) fileService.deleteFile(n.getImagePath());
            newsRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            System.err.println("Failed to delete news: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete news");
        }
    }
}
