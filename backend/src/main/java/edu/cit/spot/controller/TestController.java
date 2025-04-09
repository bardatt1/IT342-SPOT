package edu.cit.spot.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return String.format("Public endpoint is working! Authentication: %s", auth);
    }

    @GetMapping("/secured")
    public String securedEndpoint() {
        return "Secured endpoint is working!";
    }

    @GetMapping("/debug")
    public String debugEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return String.format("Debug info - Principal: %s, Authorities: %s, Details: %s",
            auth.getPrincipal(),
            auth.getAuthorities(),
            auth.getDetails());
    }
}
