package com.elsify.notification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationDashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }
}
