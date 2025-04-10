package edu.cit.spot.security;

import edu.cit.spot.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    
    // Public endpoints that don't require authentication
    private final RequestMatcher publicEndpoints = new OrRequestMatcher(
        Arrays.asList(
            // Auth endpoints
            new AntPathRequestMatcher("/api/auth/signup"),
            new AntPathRequestMatcher("/api/auth/login"),
            new AntPathRequestMatcher("/api/auth/google"),
            new AntPathRequestMatcher("/api/auth/test"),
            new AntPathRequestMatcher("/api/auth/health"),
            // Swagger/OpenAPI
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-ui.html"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/swagger-resources/**"),
            new AntPathRequestMatcher("/webjars/**"),
            // H2 Console
            new AntPathRequestMatcher("/h2-console/**")
        )
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip JWT validation for public endpoints
        boolean isPublic = publicEndpoints.matches(request);
        if (isPublic) {
            logger.debug("Skipping JWT filter for public endpoint: {} {}", request.getMethod(), request.getRequestURI());
        }
        return isPublic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        // If we got here, the endpoint requires authentication
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Processing JWT authentication for protected endpoint: {} {}", method, path);
        
        try {
            // Extract JWT token from the request
            String jwt = getJwtFromRequest(request);

            // Process only if token exists and is valid
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Get user details from the token
                String userEmail = tokenProvider.getUserEmailFromToken(jwt);
                UserDetails userDetails = userService.loadUserByUsername(userEmail);
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Successfully authenticated user: {}", userEmail);
            } else {
                logger.debug("No valid JWT token found in request, proceeding with unauthenticated access");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            // Clear any authentication data on error
            SecurityContextHolder.clearContext();
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
