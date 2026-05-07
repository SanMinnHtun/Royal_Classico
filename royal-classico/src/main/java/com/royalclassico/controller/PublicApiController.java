package com.royalclassico.controller;

import com.royalclassico.model.Fixture;
import com.royalclassico.model.NewsPost;
import com.royalclassico.model.Player;
import com.royalclassico.service.FixtureService;
import com.royalclassico.service.NewsService;
import com.royalclassico.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public REST API — no authentication required.
 * Base path: /api/v1/public
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final NewsService    newsService;
    private final PlayerService  playerService;
    private final FixtureService fixtureService;

    // ── News ──────────────────────────────────────────────────────────

    @GetMapping("/news/slider")
    public ResponseEntity<List<NewsPost>> getNewsSlider() {
        System.out.println("[PublicApiController] GET /news/slider");
        return ResponseEntity.ok(newsService.getLatestPosts());
    }

    @GetMapping("/news/slider/random")
    public ResponseEntity<List<NewsPost>> getRandomSliderPosts() {
        System.out.println("[PublicApiController] GET /news/slider/random");
        return ResponseEntity.ok(newsService.getSliderPosts());
    }

    @GetMapping("/news")
    public ResponseEntity<List<NewsPost>> getAllNews() {
        System.out.println("[PublicApiController] GET /news");
        return ResponseEntity.ok(newsService.getLatestPosts());
    }

    // ── Squad ──────────────────────────────────────────────────────────

    /**
     * GET /api/v1/public/squad
     * Returns all players as a flat list.
     */
    @GetMapping("/squad")
    public ResponseEntity<List<Player>> getSquad() {
        System.out.println("[PublicApiController] GET /squad");
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    // ── Fixture ───────────────────────────────────────────────────────

    /**
     * GET /api/v1/public/fixture
     * Returns the upcoming match fixture, or 204 No Content if none is set.
     */
    @GetMapping("/fixture")
    public ResponseEntity<Fixture> getNextFixture() {
        System.out.println("[PublicApiController] GET /fixture");
        return fixtureService.getNextFixture()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * GET /api/v1/public/fixtures
     * Returns all fixtures as a list (currently wraps the single fixture entry).
     * Returns empty array if no fixture is set.
     */
    @GetMapping("/fixtures")
    public ResponseEntity<java.util.List<Fixture>> getAllFixtures() {
        System.out.println("[PublicApiController] GET /fixtures");
        return fixtureService.getNextFixture()
                .map(f -> ResponseEntity.ok(java.util.List.of(f)))
                .orElse(ResponseEntity.ok(java.util.List.of()));
    }
}
