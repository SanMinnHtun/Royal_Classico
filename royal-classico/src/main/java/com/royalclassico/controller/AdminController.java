package com.royalclassico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the admin entrance and the obscured dashboard UI.
 */
@Controller
public class AdminController {

    @GetMapping("/royal-classico/entrance")
    public String entrance() {
        System.out.println("Entrance Route Hit!");
        return "entrance"; // resolves to templates/entrance.html
    }

    @GetMapping("/royal-classico/admin-control-system-april202650")
    public String adminDashboard() {
        return "dashboard"; // resolves to templates/dashboard.html
    }
}

