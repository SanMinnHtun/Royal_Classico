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
    private final FileService fileService;

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
            try {
                FileService.UploadResult ur = fileService.uploadToCloud(imageFile, "news");
                if (ur != null && ur.url != null && !ur.url.isBlank()) {
                    post.setImagePath(ur.url);
                } else {
                    log.warn("ImageKit upload returned null/empty URL for news image; using placeholder");
                    post.setImagePath("/images/default-logo.png");
                    // Force save immediately to ensure persistence even if upload failed
                    return newsPostRepository.save(post);
                }
                if (ur != null && ur.fileId != null && !ur.fileId.isBlank()) {
                    post.setImageFileId(ur.fileId);
                }
            } catch (Exception e) {
                log.error("Failed to upload news image to ImageKit; saving post with placeholder image", e);
                post.setImagePath("/images/default-logo.png");
                // Critical: force-save to Atlas even if upload failed
                return newsPostRepository.save(post);
            }
        }
        return newsPostRepository.save(post);
    }

    public NewsPost updatePost(String id, NewsPost updatedData, MultipartFile imageFile) throws IOException {
        NewsPost existing = newsPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("NewsPost not found: " + id));

        existing.setTitle(updatedData.getTitle());
        existing.setContent(updatedData.getContent());

        // Handle image replacement — upload new file to ImageKit and store public URL
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                FileService.UploadResult ur = fileService.uploadToCloud(imageFile, "news");
                if (ur != null && ur.url != null && !ur.url.isBlank()) {
                    existing.setImagePath(ur.url);
                } else {
                    log.warn("ImageKit upload returned empty URL for updated news; using placeholder and saving immediately");
                    existing.setImagePath("/images/default-logo.png");
                    return newsPostRepository.save(existing);
                }
                if (ur != null && ur.fileId != null && !ur.fileId.isBlank()) {
                    existing.setImageFileId(ur.fileId);
                }
            } catch (Exception e) {
                log.error("Failed to upload updated news image to ImageKit; saving existing post with placeholder", e);
                existing.setImagePath("/images/default-logo.png");
                // Force-save updated post to ensure persistence
                return newsPostRepository.save(existing);
            }
        }

        return newsPostRepository.save(existing);
    }

    public void deletePost(String id) {
        NewsPost post = newsPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("NewsPost not found: " + id));

        // If we have a remote ImageKit fileId, attempt to delete it from ImageKit first
        if (post.getImageFileId() != null && !post.getImageFileId().isBlank()) {
            try {
                fileService.deleteRemoteByFileId(post.getImageFileId());
            } catch (Exception nm) {
                log.warn("ImageKit delete call failed for fileId={} postId={}: {}", post.getImageFileId(), id, nm.getMessage());
            }
        }

        newsPostRepository.deleteById(id);
        log.info("Deleted news post id={}", id);
    }
}
