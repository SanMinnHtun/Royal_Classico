package com.royalclassico.controller;

import com.royalclassico.model.Player;
import com.royalclassico.service.FixtureService;
import com.royalclassico.service.NewsService;
import com.royalclassico.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.List;

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
        model.addAttribute("sliderPosts",  newsService.getSliderPosts());
        model.addAttribute("latestPosts",  newsService.getLatestPosts());
        model.addAttribute("nextFixture",  fixtureService.getNextFixture().orElse(null));
        return "index";
    }

    /** Squad page: players grouped by position */
    @GetMapping("/squad")
    public String squad(Model model) {
        Map<Player.Position, List<Player>> squad = playerService.getPlayersGroupedByPosition();
        model.addAttribute("squadByPosition", squad);
        model.addAttribute("nextFixture", fixtureService.getNextFixture().orElse(null));
        return "squad";
    }

    /** Full news archive page */
    @GetMapping("/news")
    public String news(Model model) {
        model.addAttribute("newsPosts", newsService.getLatestPosts());
        model.addAttribute("nextFixture", fixtureService.getNextFixture().orElse(null));
        return "news";
    }
}
