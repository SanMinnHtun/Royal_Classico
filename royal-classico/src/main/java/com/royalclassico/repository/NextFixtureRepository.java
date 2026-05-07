package com.royalclassico.repository;

import com.royalclassico.model.NextFixture;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NextFixtureRepository extends MongoRepository<NextFixture, String> {
    Optional<NextFixture> findFirstBy();
}

