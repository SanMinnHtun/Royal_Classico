package com.royalclassico.controller;

import com.royalclassico.model.Player;
import com.royalclassico.service.FixtureService;
import com.royalclassico.service.NewsService;
import com.royalclassico.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
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
        List<Player> all = playerService.getAllPlayers();
        model.addAttribute("allPlayers", all);
        model.addAttribute("players", all);

        // Group players by primary position (first position in positions list) — null-safe
        List<Player> gk = new ArrayList<>();
        List<Player> def = new ArrayList<>();
        List<Player> mid = new ArrayList<>();
        List<Player> fwd = new ArrayList<>();
        List<Player> other = new ArrayList<>();

        for (Player p : all) {
            if (p.getPositions() != null && !p.getPositions().isEmpty()) {
                String first = p.getPositions().get(0);
                if (first == null) { other.add(p); continue; }
                switch (first.toUpperCase()) {
                    case "GK": gk.add(p); break;
                    case "DEF": def.add(p); break;
                    case "MID": mid.add(p); break;
                    case "FWD": fwd.add(p); break;
                    default: other.add(p); break;
                }
            } else {
                other.add(p);
            }
        }

        model.addAttribute("gkPlayers", gk);
        model.addAttribute("defPlayers", def);
        model.addAttribute("midPlayers", mid);
        model.addAttribute("fwdPlayers", fwd);
        model.addAttribute("otherPlayers", other);

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
