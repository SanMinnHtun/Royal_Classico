package com.royalclassico.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

// ...existing imports...

@RestController
@RequestMapping("/api/v1/management-internal")
public class AdminController {

    @Autowired
    private PlayerRepository playerRepository; // assume exists

    @Autowired
    private NewsRepository newsRepository; // assume exists

    @Autowired
    private com.royalclassico.service.FileService fileService; // implement deleteFile(path) to remove files from uploads

    /* ---------------- Players ---------------- */

    @GetMapping("/players")
    public ResponseEntity<List<PlayerDto>> listPlayers() {
        List<Player> players = playerRepository.findAll();
        List<PlayerDto> dtos = players.stream().map(PlayerDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/players")
    public ResponseEntity<?> createPlayer(
            @RequestParam("realName") String name,
            @RequestParam("jerseyName") String jerseyName,
            @RequestParam("age") Integer age,
            @RequestParam("jerseyNumber") Integer number,
            @RequestParam(value = "positions", required = false) String positionsJson,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String photoPath = null;
            if (image != null && !image.isEmpty()) {
                photoPath = fileService.storeFile(image, "players");
            }
            Player p = new Player();
            p.setRealName(name);
            p.setJerseyName(jerseyName);
            p.setAge(age);
            p.setJerseyNumber(number);
            // positionsJson is expected to be a JSON array string like ["GK","DEF"]
            if (positionsJson != null && !positionsJson.isBlank()) {
                // simplistic parsing — remove brackets and quotes
                String trimmed = positionsJson.trim();
                trimmed = trimmed.replaceAll("\\[|\\]|\\\"", "");
                List<String> positions = List.of(trimmed.split("\",?\s*"));
                p.setPositions(positions);
            }
            p.setImagePath(photoPath);
            playerRepository.save(p);
            return ResponseEntity.ok(p);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create player");
        }
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable("id") String id) {
        Optional<Player> opt = playerRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Player p = opt.get();
        try {
            // IMPORTANT: delete file first from disk, then remove DB record
            if (p.getImagePath() != null) {
                fileService.deleteFile(p.getImagePath());
            }
            playerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to delete player");
        }
    }

    /* ---------------- News ---------------- */

    @GetMapping("/news")
    public ResponseEntity<List<NewsDto>> listNews() {
        List<News> items = newsRepository.findAll();
        List<NewsDto> dtos = items.stream().map(NewsDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/news")
    public ResponseEntity<?> createNews(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String imagePath = null;
            if (image != null && !image.isEmpty()) {
                imagePath = fileService.storeFile(image, "news");
            }
            News n = new News();
            n.setTitle(title);
            n.setContent(content);
            n.setImagePath(imagePath);
            newsRepository.save(n);
            return ResponseEntity.ok(n);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create news");
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable("id") String id) {
        Optional<News> opt = newsRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        News n = opt.get();
        try {
            if (n.getImagePath() != null) {
                fileService.deleteFile(n.getImagePath());
            }
            newsRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to delete news");
        }
    }

    // DTOs and helper classes
    static class PlayerDto {
        public String id; public String realName; public String jerseyName; public Integer age; public Integer jerseyNumber; public List<String> positions; public String imagePath;
        static PlayerDto from(Player p) {
            PlayerDto d = new PlayerDto();
            d.id = p.getId();
            d.realName = p.getRealName();
            d.jerseyName = p.getJerseyName();
            d.age = p.getAge();
            d.jerseyNumber = p.getJerseyNumber();
            d.positions = p.getPositions();
            d.imagePath = p.getImagePath();
            return d;
        }
    }

    static class NewsDto {
        public String id; public String title; public String content; public String imagePath;
        static NewsDto from(News n) {
            NewsDto d = new NewsDto();
            d.id = n.getId();
            d.title = n.getTitle();
            d.content = n.getContent();
            d.imagePath = n.getImagePath();
            return d;
        }
    }

    // ...existing code...
}

/*
Notes:
- FileService should expose:
    String storeFile(MultipartFile file); // saves under uploads/... and returns relative path (e.g. players/abc.jpg)
    void deleteFile(String relativePath);  // physically deletes the file from the uploads directory

- For production, centralize header verification by implementing a Spring HandlerInterceptor that checks
  the X-Admin-Secret header for all /api/v1/management-internal/** routes and register it.

- Player, News entities and repositories assumed to exist. Adjust DTO mapping to match actual field names.
*/
