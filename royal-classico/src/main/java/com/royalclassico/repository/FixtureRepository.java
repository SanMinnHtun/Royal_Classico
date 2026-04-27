package com.royalclassico.repository;

import com.royalclassico.model.Fixture;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the single Fixture document.
 */
@Repository
public interface FixtureRepository extends MongoRepository<Fixture, String> {

    /** Gets the first (and only) fixture document. */
    Optional<Fixture> findFirstBy();
}
