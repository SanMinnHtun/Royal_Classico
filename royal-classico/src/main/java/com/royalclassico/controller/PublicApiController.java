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
import java.util.Map;

/**
 * Public REST API — no authentication required.
 *
 * These endpoints are consumed by the frontend (or any external client)
 * to display dynamic data without admin privileges.
 *
 * Base path: /api/v1/public
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final NewsService    newsService;
    private final PlayerService  playerService;
    private final FixtureService fixtureService;

    // ── News ─────────────────────────────────────────────────────────

    /**
     * GET /api/v1/public/news/slider
     *
     * Returns the top 10 most recent news posts (by createdAt DESC).
     * The frontend can use this list to drive a news slider or carousel.
     *
     * Slider randomisation note:
     *   The homepage Thymeleaf view already shuffles these 10 into 4 random
     *   items per page load. If you want the API to also return 4 random items
     *   (for a React/JS frontend), call /api/v1/public/news/slider/random instead.
     */
    @GetMapping("/news/slider")
    public ResponseEntity<List<NewsPost>> getNewsSlider() {
        List<NewsPost> top10 = newsService.getLatestPosts(); // top 10 by createdAt DESC
        return ResponseEntity.ok(top10);
    }

    /**
     * GET /api/v1/public/news/slider/random
     *
     * Returns 4 randomly selected posts from the top 10 most recent.
     * Useful when the slider is driven by a JavaScript framework (React, Vue, etc.)
     * rather than Thymeleaf, ensuring the page never looks "frozen".
     */
    @GetMapping("/news/slider/random")
    public ResponseEntity<List<NewsPost>> getRandomSliderPosts() {
        List<NewsPost> random4 = newsService.getSliderPosts(); // 4 random from top 10
        return ResponseEntity.ok(random4);
    }

    /**
     * GET /api/v1/public/news
     *
     * Returns all news posts (most recent 10).
     */
    @GetMapping("/news")
    public ResponseEntity<List<NewsPost>> getAllNews() {
        return ResponseEntity.ok(newsService.getLatestPosts());
    }

    // ── Squad ─────────────────────────────────────────────────────────

    /**
     * GET /api/v1/public/squad
     *
     * Returns all players as a flat list.
     */
    @GetMapping("/squad")
    public ResponseEntity<List<Player>> getSquad() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    /**
     * GET /api/v1/public/squad/grouped
     *
     * Returns players grouped by position (GK → DEF → MID → FWD).
     * Keys are position enum names as strings.
     */
    @GetMapping("/squad/grouped")
    public ResponseEntity<Map<Player.Position, List<Player>>> getSquadGrouped() {
        return ResponseEntity.ok(playerService.getPlayersGroupedByPosition());
    }

    // ── Fixture ───────────────────────────────────────────────────────

    /**
     * GET /api/v1/public/fixture
     *
     * Returns the upcoming match fixture, or 204 No Content if none is set.
     */
    @GetMapping("/fixture")
    public ResponseEntity<Fixture> getNextFixture() {
        return fixtureService.getNextFixture()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
