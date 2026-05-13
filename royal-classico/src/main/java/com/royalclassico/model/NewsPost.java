package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Represents a news post/article for the club's homepage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "news_posts")
public class NewsPost {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    /** Relative path under /uploads/, e.g. "news/image.jpg" */
    private String imagePath;

    /** ImageKit file identifier for remote deletion */
    private String imageFileId;

    /** Auto-set on creation */
    private LocalDateTime createdAt;

    /** Lifecycle hook equivalent — set before saving via service */
    public void initCreatedAt() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
