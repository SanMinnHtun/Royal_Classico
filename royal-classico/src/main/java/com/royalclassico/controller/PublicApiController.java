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
     * GET /api/v1/public/fixture/banner
     * Returns the closest upcoming match fixture, or 204 No Content if none is set.
     */
    @GetMapping("/fixture/banner")
    public ResponseEntity<Fixture> getBannerFixture() {
        System.out.println("[PublicApiController] GET /fixture/banner");
        return fixtureService.getNextUpcomingFixture()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * GET /api/v1/public/fixtures/upcoming
     * Returns upcoming fixtures (isFinished == false) ordered by date asc.
     */
    @GetMapping("/fixtures/upcoming")
    public ResponseEntity<List<Fixture>> getUpcomingFixtures() {
        System.out.println("[PublicApiController] GET /fixtures/upcoming");
        return ResponseEntity.ok(fixtureService.getUpcomingFixtures());
    }

    /**
     * GET /api/v1/public/fixtures/past
     * Returns past fixtures (isFinished == true) ordered by date desc.
     */
    @GetMapping("/fixtures/past")
    public ResponseEntity<List<Fixture>> getPastFixtures() {
        System.out.println("[PublicApiController] GET /fixtures/past");
        return ResponseEntity.ok(fixtureService.getPastFixtures());
    }

    /**
     * GET /api/v1/public/fixtures
     * Returns all fixtures (upcoming then past)
     */
    @GetMapping("/fixtures")
    public ResponseEntity<List<Fixture>> getAllFixtures() {
        System.out.println("[PublicApiController] GET /fixtures");
        List<Fixture> upcoming = fixtureService.getUpcomingFixtures();
        List<Fixture> past = fixtureService.getPastFixtures();
        upcoming.addAll(past);
        return ResponseEntity.ok(upcoming);
    }
}
