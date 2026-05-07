package com.royalclassico.repository;

import com.royalclassico.model.Fixture;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data MongoDB repository for multi-entry fixtures.
 */
@Repository
public interface FixtureRepository extends MongoRepository<Fixture, String> {

    /** Find all upcoming fixtures (not finished) ordered by date asc */
    List<Fixture> findByIsFinishedFalseOrderByDateAsc();

    /** Find all finished fixtures ordered by date desc (most recent first) */
    List<Fixture> findByIsFinishedTrueOrderByDateDesc();

    /** Banner query: closest upcoming fixture (returns null when none) */
    Fixture findFirstByIsFinishedFalseOrderByDateAsc();

    // Convenience methods
    List<Fixture> findByIsFinishedTrue();
    List<Fixture> findByIsFinishedFalse();
}
