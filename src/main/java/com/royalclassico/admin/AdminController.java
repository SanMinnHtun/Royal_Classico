package com.royalclassico.admin;

import com.royalclassico.model.Player;
import com.royalclassico.model.News;
import com.royalclassico.repository.PlayerRepository;
import com.royalclassico.repository.NewsRepository;
import io.imagekit.client.ImageKitClient;
import io.imagekit.client.ImageKitClientImpl;
import io.imagekit.core.ClientOptions;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/management-internal")
public class AdminController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NewsRepository newsRepository;

    // Helper method to handle Cloud Upload using installed ImageKit client (v3.x)
    private String uploadToImageKit(MultipartFile file, String folder) throws Exception {
        ClientOptions options = ClientOptions.fromEnv();
        ImageKitClient ik = new ImageKitClientImpl(options);

        byte[] bytes = file.getBytes();
        String filename = UUID.randomUUID().toString() + "-" + (file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());

        FileUploadParams.Body body = FileUploadParams.Body.builder()
                .file(new ByteArrayInputStream(bytes))
                .fileName(filename)
                .useUniqueFileName(true)
                .folder(folder)
                .build();

        FileUploadParams params = FileUploadParams.builder().body(body).build();
        FileUploadResponse response = ik.files().upload(params);
        return response.url().orElse(null);
    }

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
                // Now uploading to ImageKit cloud instead of local disk
                photoPath = uploadToImageKit(image, "players");
            }
            Player p = new Player();
            p.setRealName(name);
            p.setJerseyName(jerseyName);
            p.setAge(age);
            p.setJerseyNumber(number);

            if (positionsJson != null && !positionsJson.isBlank()) {
                String trimmed = positionsJson.trim();
                trimmed = trimmed.replaceAll("\\[|\\]|\\\"", "");
                List<String> positions = List.of(trimmed.split("\",?\\s*"));
                p.setPositions(positions);
            }

            p.setImagePath(photoPath); // Saves the URL (https://ik.imagekit.io/...)
            playerRepository.save(p);
            return ResponseEntity.ok(p);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create player: " + ex.getMessage());
        }
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable("id") String id) {
        Optional<Player> opt = playerRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        try {
            // Note: Cloud deletion logic can be added here later
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
                imagePath = uploadToImageKit(image, "news");
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

        try {
            newsRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to delete news");
        }
    }

    // DTOs
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
}