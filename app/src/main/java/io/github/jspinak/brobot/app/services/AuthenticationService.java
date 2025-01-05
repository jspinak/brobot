package io.github.jspinak.brobot.app.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final RestTemplate restTemplate;
    private final String clientAppUrl;
    private final String username;
    private final String password;

    private String jwtToken;

    public AuthenticationService(
            RestTemplate restTemplate,
            @Value("${client.app.url}") String clientAppUrl,
            @Value("${client.app.username}") String username,
            @Value("${client.app.password}") String password) {
        this.restTemplate = restTemplate;
        this.clientAppUrl = clientAppUrl;
        this.username = username;
        this.password = password;

        logger.info("AuthenticationService initialized with URL: {}", clientAppUrl);
        logger.debug("Using username: {}", username);
    }

    public String getJwtToken() {
        if (jwtToken == null) {
            logger.debug("No JWT token found, initiating authentication");
            authenticate();
        } else {
            logger.debug("Using existing token (first 10 chars): {}...",
                    jwtToken.substring(0, Math.min(10, jwtToken.length())));
        }
        return jwtToken;
    }

    private synchronized void authenticate() {
        try {
            String loginUrl = clientAppUrl + "/api/auth/login";
            logger.debug("Attempting authentication at URL: {}", loginUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            logger.debug("Set content type to: {}", MediaType.APPLICATION_JSON);

            // Create login request body
            String loginRequestBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    username, password
            );
            logger.debug("Created login request for user: {}", username);

            // Log complete request details (except password)
            HttpEntity<String> request = new HttpEntity<>(loginRequestBody, headers);
            logger.debug("Request headers: {}", request.getHeaders());

            // Make the authentication request
            logger.debug("Sending authentication request...");
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    loginUrl,
                    request,
                    LoginResponse.class
            );

            // Log response details
            logger.debug("Response status: {}", response.getStatusCode());
            logger.debug("Response headers: {}", response.getHeaders());

            if (response.getBody() != null) {
                this.jwtToken = response.getBody().getToken();
                logger.info("Successfully authenticated with the client application");
                logger.debug("Received token (first 10 chars): {}...",
                        jwtToken.substring(0, Math.min(10, jwtToken.length())));
            } else {
                logger.error("Authentication response body was empty");
                throw new RuntimeException("Authentication response body was empty");
            }
        } catch (Exception e) {
            logger.error("Authentication failed", e);
            logger.error("Exception class: {}", e.getClass().getName());
            logger.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to authenticate with client application", e);
        }
    }

    private static class LoginResponse {
        private String token;
        private boolean admin;

        public LoginResponse() {}

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }
}