package edu.cit.spot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class DebugController {
    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    @GetMapping("/debug/hello")
    public String hello() {
        log.info("Debug hello endpoint called");
        return "Hello from debug controller";
    }
}
