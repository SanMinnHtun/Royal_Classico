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

    /** Homepage: slider posts + next fixture widget */
    @GetMapping("/")
    public String home(Model model) {
        System.out.println("[PublicPageController] GET / (home)");
        model.addAttribute("sliderPosts",  newsService.getSliderPosts());
        model.addAttribute("latestPosts",  newsService.getLatestPosts());
        model.addAttribute("nextFixture",  fixtureService.getNextFixture().orElse(null));
        return "index";
    }

    /** Squad page: passes all players as flat list — grouped by JS on the frontend */
    @GetMapping("/squad")
    public String squad(Model model) {
        System.out.println("[PublicPageController] GET /squad");
        model.addAttribute("allPlayers",  playerService.getAllPlayers());
        model.addAttribute("nextFixture", fixtureService.getNextFixture().orElse(null));
        return "squad";
    }

    /** Full news archive page */
    @GetMapping("/news")
    public String news(Model model) {
        System.out.println("[PublicPageController] GET /news");
        model.addAttribute("newsPosts", newsService.getLatestPosts());
        model.addAttribute("nextFixture", fixtureService.getNextFixture().orElse(null));
        return "news";
    }
}
