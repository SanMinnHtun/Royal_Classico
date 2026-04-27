package com.royalclassico.repository;

import com.royalclassico.model.NewsPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data MongoDB repository for NewsPost documents.
 */
@Repository
public interface NewsPostRepository extends MongoRepository<NewsPost, String> {

    /**
     * Returns the top N most recent news posts ordered by creation date descending.
     */
    List<NewsPost> findTop10ByOrderByCreatedAtDesc();
}
