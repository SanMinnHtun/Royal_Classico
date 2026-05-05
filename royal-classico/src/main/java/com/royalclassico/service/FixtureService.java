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
        System.out.println("[FixtureService] getNextFixture() called");
        return fixtureRepository.findFirstBy();
    }

    /**
     * Creates or replaces the next fixture (upsert).
     * Reuses the same MongoDB document ID so it updates in place.
     */
    public Fixture upsertFixture(Fixture fixture) {
        System.out.println("[FixtureService] upsertFixture() — rivalTeam=" + fixture.getRivalTeam()
                + ", date=" + fixture.getDate() + ", time=" + fixture.getTime());
        fixtureRepository.findFirstBy().ifPresent(existing -> {
            fixture.setId(existing.getId()); // reuse same ID → MongoDB will update
        });
        Fixture saved = fixtureRepository.save(fixture);
        log.info("Upserted fixture: Royal Classico vs {} on {}", saved.getRivalTeam(), saved.getDate());
        return saved;
    }

    /** Clears the fixture (e.g., after the match is played). */
    public void deleteFixture() {
        System.out.println("[FixtureService] deleteFixture() called");
        fixtureRepository.findFirstBy().ifPresent(f -> {
            fixtureRepository.deleteById(f.getId());
            log.info("Cleared next fixture.");
        });
    }
}
