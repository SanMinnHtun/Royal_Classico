package com.royalclassico.controller;

import com.royalclassico.service.FixtureService;
import com.royalclassico.service.NewsService;
import com.royalclassico.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Public-facing Thymeleaf page controller.
 * Handles homepage, squad page, and news archive.
 */
@Controller
@RequiredArgsConstructor
public class PublicPageController {

    private final NewsService    newsService;
    private final PlayerService  playerService;
    private final FixtureService fixtureService;

    /** Homepage: slider posts + banner fixture widget */
    @GetMapping("/")
    public String home(Model model) {
        System.out.println("[PublicPageController] GET / (home)");
        model.addAttribute("sliderPosts",  newsService.getSliderPosts());
        model.addAttribute("latestPosts",  newsService.getLatestPosts());
        // Use the dedicated next_fixture collection for the banner (Dual-Sync pattern)
        model.addAttribute("nextMatch", fixtureService.getActiveNextFixture().orElse(null));
        return "index";
    }

    /** Squad page: passes all players and banner fixture */
    @GetMapping("/squad")
    public String squad(Model model) {
        System.out.println("[PublicPageController] GET /squad");
        model.addAttribute("allPlayers",  playerService.getAllPlayers());
        model.addAttribute("nextMatch", fixtureService.getActiveNextFixture().orElse(null));
        return "squad";
    }

    /** Full news archive page */
    @GetMapping("/news")
    public String news(Model model) {
        System.out.println("[PublicPageController] GET /news");
        model.addAttribute("newsPosts", newsService.getLatestPosts());
        model.addAttribute("nextMatch", fixtureService.getActiveNextFixture().orElse(null));
        return "news";
    }

    /** Fixtures & Results page */
    @GetMapping("/fixtures")
    public String fixtures(Model model) {
        System.out.println("[PublicPageController] GET /fixtures");
        model.addAttribute("nextMatch", fixtureService.getActiveNextFixture().orElse(null));
        model.addAttribute("upcomingMatches", fixtureService.getUpcomingFixtures());
        model.addAttribute("pastResults", fixtureService.getPastFixtures());
        return "fixtures";
    }
}
