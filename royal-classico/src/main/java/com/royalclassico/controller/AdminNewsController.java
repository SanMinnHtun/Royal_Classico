package com.royalclassico.controller;

import com.royalclassico.model.NewsPost;
import com.royalclassico.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Secret Admin REST API for News Post management.
 *
 * Base path: /api/v1/rc-internal-mgmt/news
 * Auth:       X-Admin-Secret header (enforced by AdminSecurityFilter)
 *
 * All routes are intentionally nested under the obscure path.
 */
@RestController
@RequestMapping("/api/v1/rc-internal-mgmt/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final NewsService newsService;

    /** GET all news posts */
    @GetMapping
    public ResponseEntity<List<NewsPost>> getAllPosts() {
        return ResponseEntity.ok(newsService.getAllPosts());
    }

    /** GET single post by ID */
    @GetMapping("/{id}")
    public ResponseEntity<NewsPost> getPost(@PathVariable String id) {
        return newsService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST — Create a new news post.
     * Uses multipart/form-data so image can be uploaded together with JSON fields.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
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
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * PUT — Update an existing news post.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable String id,
            @RequestPart("title")   String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            NewsPost updatedData = new NewsPost();
            updatedData.setTitle(title);
            updatedData.setContent(content);
            NewsPost updated = newsService.updatePost(id, updatedData, image);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * DELETE — Remove a news post and its associated image file.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        try {
            newsService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
