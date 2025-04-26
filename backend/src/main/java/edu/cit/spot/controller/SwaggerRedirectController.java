package edu.cit.spot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to provide easy access to Swagger documentation
 */
@Controller
public class SwaggerRedirectController {

    /**
     * Redirects root context to Swagger UI
     * @return redirect to Swagger UI
     */
    @GetMapping("/")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui.html";
    }
}
