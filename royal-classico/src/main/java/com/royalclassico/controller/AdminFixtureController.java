package com.royalclassico.controller;

import com.royalclassico.model.Fixture;
import com.royalclassico.service.FixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
        System.out.println("[AdminFixtureController] GET /fixture");
        return fixtureService.getNextFixture()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT — Create or replace the next fixture.
     * Body (JSON):
     * {
     *   "rivalTeam": "FC Rivals",
     *   "date":      "2026-06-15",
     *   "time":      "15:00",
     *   "stadium":   "Municipal Stadium, Field 3",
     *   "result":    "vs",
     *   "isFinished": false
     * }
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upsertFixture(@RequestBody Fixture fixture) {
        System.out.println("[AdminFixtureController] PUT /fixture — rivalTeam=" + fixture.getRivalTeam()
                + ", date=" + fixture.getDate() + ", time=" + fixture.getTime()
                + ", stadium=" + fixture.getStadium());
        try {
            Fixture saved = fixtureService.upsertFixture(fixture);
            System.out.println("[AdminFixtureController] Fixture saved id=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("[AdminFixtureController] ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid fixture data: " + e.getMessage());
        }
    }

    /** DELETE — Clear the upcoming fixture (e.g., match is done) */
    @DeleteMapping
    public ResponseEntity<Void> deleteFixture() {
        System.out.println("[AdminFixtureController] DELETE /fixture");
        fixtureService.deleteFixture();
        return ResponseEntity.noContent().build();
    }
}
