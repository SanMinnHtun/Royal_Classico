package com.royalclassico.service;

import com.royalclassico.model.NewsPost;
import com.royalclassico.repository.NewsPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Business logic for News Posts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsPostRepository newsPostRepository;
    private final FileStorageService fileStorageService;

    // ── Read ────────────────────────────────────────────────────────────────

    public List<NewsPost> getAllPosts() {
        return newsPostRepository.findAll();
    }

    public Optional<NewsPost> getPostById(String id) {
        return newsPostRepository.findById(id);
    }

    /**
     * Fetches the 10 most recent posts, then returns 4 random ones from that list.
     * This ensures the homepage slider always looks "alive" on each page load.
     */
    public List<NewsPost> getSliderPosts() {
        List<NewsPost> top10 = newsPostRepository.findTop10ByOrderByCreatedAtDesc();
        if (top10.size() <= 4) {
            return top10;
        }
        // Shuffle a copy and take first 4
        List<NewsPost> shuffled = new ArrayList<>(top10);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, 4);
    }

    /**
     * Returns the 10 most recent posts (for full news section).
     */
    public List<NewsPost> getLatestPosts() {
        return newsPostRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // ── Write ───────────────────────────────────────────────────────────────

    public NewsPost createPost(NewsPost post, MultipartFile imageFile) throws IOException {
        post.initCreatedAt();
        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.saveFile(imageFile, "news");
            post.setImagePath(path);
        }
        return newsPostRepository.save(post);
    }

    public NewsPost updatePost(String id, NewsPost updatedData, MultipartFile imageFile) throws IOException {
        NewsPost existing = newsPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("NewsPost not found: " + id));

        existing.setTitle(updatedData.getTitle());
        existing.setContent(updatedData.getContent());

        // Handle image replacement — delete old file first
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorageService.deleteFile(existing.getImagePath()); // strict cleanup
            String newPath = fileStorageService.saveFile(imageFile, "news");
            existing.setImagePath(newPath);
        }

        return newsPostRepository.save(existing);
    }

    public void deletePost(String id) {
        NewsPost post = newsPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("NewsPost not found: " + id));
        fileStorageService.deleteFile(post.getImagePath()); // strict cleanup
        newsPostRepository.deleteById(id);
        log.info("Deleted news post id={}", id);
    }
}
