package com.royalclassico.service;

import com.royalclassico.model.Fixture;
import com.royalclassico.model.NextFixture;
import com.royalclassico.repository.FixtureRepository;
import com.royalclassico.repository.NextFixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for multi-entry fixtures.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final NextFixtureRepository nextFixtureRepository;

    /** Returns the closest upcoming fixture (banner) */
    public Optional<Fixture> getNextUpcomingFixture() {
        System.out.println("[FixtureService] getNextUpcomingFixture() called");
        return Optional.ofNullable(fixtureRepository.findFirstByIsFinishedFalseOrderByDateAsc());
    }

    /** Returns upcoming fixtures ordered by date asc */
    public List<Fixture> getUpcomingFixtures() {
        return fixtureRepository.findByIsFinishedFalseOrderByDateAsc();
    }

    /** Returns past fixtures ordered by date desc */
    public List<Fixture> getPastFixtures() {
        return fixtureRepository.findByIsFinishedTrueOrderByDateDesc();
    }

    /** Create a new fixture (always inserts) */
    public Fixture createFixture(Fixture fixture) {
        System.out.println("[FixtureService] createFixture() — rivalTeam=" + fixture.getRivalTeam()
                + ", date=" + fixture.getDate() + ", time=" + fixture.getTime());
        fixture.setId(null); // ensure a new document is created
        Fixture saved = fixtureRepository.save(fixture);
        log.info("Created fixture: Royal Classico vs {} on {}", saved.getRivalTeam(), saved.getDate());
        return saved;
    }

    /** Save fixture and synchronize next_fixture collection according to isFinished */
    public Fixture saveAndSyncNext(Fixture fixture) {
        // Step A: always save to fixtures collection (insert if id null)
        fixture.setId(null); // force create
        Fixture saved = fixtureRepository.save(fixture);

        // Step B/C: sync the next_fixture collection
        if (!saved.isFinished()) {
            // if not finished, overwrite the single document in next_fixture
            NextFixture nf = new NextFixture();
            nf.setId(null);
            nf.setRivalTeam(saved.getRivalTeam());
            nf.setDate(saved.getDate());
            nf.setTime(saved.getTime());
            nf.setStadium(saved.getStadium());
            nf.setResult(saved.getResult());
            nf.setFinished(saved.isFinished());
            nf.setGoalScorers(saved.getGoalScorers());
            // Clear existing and save this as the sole document
            nextFixtureRepository.deleteAll();
            nextFixtureRepository.save(nf);
        } else {
            // if finished, clear the banner (no active next fixture)
            nextFixtureRepository.deleteAll();
        }
        return saved;
    }

    /** Update an existing fixture (by ID) */
    public Fixture updateFixture(Fixture fixture) {
        System.out.println("[FixtureService] updateFixture() id=" + fixture.getId());
        return fixtureRepository.save(fixture);
    }

    public void deleteFixtureById(String id) {
        fixtureRepository.deleteById(id);
        log.info("Deleted fixture id={}", id);
    }

    /** Returns the currently saved banner document in next_fixture collection */
    public Optional<NextFixture> getActiveNextFixture() {
        System.out.println("[FixtureService] getActiveNextFixture() called");
        return nextFixtureRepository.findFirstBy();
    }
}
