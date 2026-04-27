package com.royalclassico.controller;

import com.royalclassico.model.Fixture;
import com.royalclassico.service.FixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Secret Admin REST API for Next Fixture management.
 *
 * Base path: /api/v1/rc-internal-mgmt/fixture
 * Auth:       X-Admin-Secret header (enforced by AdminSecurityFilter)
 *
 * Single-entry pattern — only one fixture can exist at a time.
 */
@RestController
@RequestMapping("/api/v1/rc-internal-mgmt/fixture")
@RequiredArgsConstructor
public class AdminFixtureController {

    private final FixtureService fixtureService;

    /** GET the current fixture */
    @GetMapping
    public ResponseEntity<Fixture> getFixture() {
        return fixtureService.getNextFixture()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT — Create or replace the next fixture.
     * Body (JSON):
     * {
     *   "opponent":  "FC Rivals",
     *   "matchDate": "2025-06-15",
     *   "matchTime": "15:00",
     *   "pitch":     "Municipal Stadium, Field 3"
     * }
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upsertFixture(@RequestBody Fixture fixture) {
        try {
            Fixture saved = fixtureService.upsertFixture(fixture);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid fixture data: " + e.getMessage());
        }
    }

    /** DELETE — Clear the upcoming fixture (e.g., match is done) */
    @DeleteMapping
    public ResponseEntity<Void> deleteFixture() {
        fixtureService.deleteFixture();
        return ResponseEntity.noContent().build();
    }
}
