package com.royalclassico.controller;

import com.royalclassico.model.Fixture;
import com.royalclassico.service.FixtureService;
import com.royalclassico.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin REST API for multi-entry fixture management.
 * Provides endpoints to create, update, list, and delete fixtures.
 * The banner endpoint only returns the next upcoming (not finished) fixture.
 */
@RestController
@RequestMapping("/api/v1/rc-internal-mgmt/fixture")
@RequiredArgsConstructor
public class AdminFixtureController {

    private final FixtureService fixtureService;
    private final FileService fileService;

    /**
     * GET /banner — returns the next upcoming fixture (ignores finished matches)
     */
    @GetMapping("/banner")
    public ResponseEntity<Fixture> getBanner() {
        System.out.println("[AdminFixtureController] GET /fixture/banner");
        return fixtureService.getNextUpcomingFixture()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /upcoming — returns all upcoming (isFinished == false) fixtures
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Fixture>> getUpcoming() {
        System.out.println("[AdminFixtureController] GET /fixture/upcoming");
        return ResponseEntity.ok(fixtureService.getUpcomingFixtures());
    }

    /**
     * GET /past — returns all past (isFinished == true) fixtures
     */
    @GetMapping("/past")
    public ResponseEntity<List<Fixture>> getPast() {
        System.out.println("[AdminFixtureController] GET /fixture/past");
        return ResponseEntity.ok(fixtureService.getPastFixtures());
    }

    /**
     * GET /all — returns upcoming followed by past fixtures
     */
    @GetMapping("/all")
    public ResponseEntity<List<Fixture>> getAllFixtures() {
        System.out.println("[AdminFixtureController] GET /fixture/all");
        List<Fixture> combined = new ArrayList<>();
        combined.addAll(fixtureService.getUpcomingFixtures());
        combined.addAll(fixtureService.getPastFixtures());
        return ResponseEntity.ok(combined);
    }

    /**
     * POST / — create a new fixture. Accepts multipart form (optional image).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Fixture> createFixture(@ModelAttribute Fixture fixture,
                                                 @RequestPart(value = "image", required = false) MultipartFile image) {
        System.out.println("[AdminFixtureController] POST /fixture — create (model attribute)");
        try {
            // Ensure new document is created
            fixture.setId(null);
            Fixture saved = fixtureService.createFixtureWithImage(fixture, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.err.println("[AdminFixtureController] ERROR create: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT / — update an existing fixture. Requires fixture.id to be present.
     */
    @PutMapping(consumes = {"application/x-www-form-urlencoded", "multipart/form-data"})
    public ResponseEntity<Fixture> updateFixture(@ModelAttribute Fixture fixture) {
        System.out.println("[AdminFixtureController] PUT /fixture — update id=" + fixture.getId());
        if (fixture.getId() == null || fixture.getId().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            Fixture updated = fixtureService.updateFixture(fixture);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("[AdminFixtureController] ERROR update: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * DELETE /{id} — delete a fixture by id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFixture(@PathVariable String id) {
        System.out.println("[AdminFixtureController] DELETE /fixture/" + id);
        try {
            fixtureService.deleteFixtureById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("[AdminFixtureController] ERROR delete: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
