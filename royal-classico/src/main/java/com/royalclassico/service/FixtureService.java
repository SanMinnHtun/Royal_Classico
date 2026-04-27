package com.royalclassico.service;

import com.royalclassico.model.Fixture;
import com.royalclassico.repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Business logic for the single "Next Fixture" entry.
 * Uses an upsert pattern — only one Fixture document ever exists.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixtureService {

    private final FixtureRepository fixtureRepository;

    /** Returns the current fixture if one exists. */
    public Optional<Fixture> getNextFixture() {
        return fixtureRepository.findFirstBy();
    }

    /**
     * Creates or replaces the next fixture (upsert).
     * Deletes any existing document first, then saves the new one.
     */
    public Fixture upsertFixture(Fixture fixture) {
        fixtureRepository.findFirstBy().ifPresent(existing -> {
            fixture.setId(existing.getId()); // reuse same ID → MongoDB will update
        });
        Fixture saved = fixtureRepository.save(fixture);
        log.info("Upserted fixture: {} vs {} on {}", "Royal Classico", saved.getOpponent(), saved.getMatchDate());
        return saved;
    }

    /** Clears the fixture (e.g., after the match is played). */
    public void deleteFixture() {
        fixtureRepository.findFirstBy().ifPresent(f -> {
            fixtureRepository.deleteById(f.getId());
            log.info("Cleared next fixture.");
        });
    }
}
