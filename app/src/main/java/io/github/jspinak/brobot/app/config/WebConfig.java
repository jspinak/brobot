package io.github.jspinak.brobot.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Defines a global CORS configuration that applies to all endpoints in the Spring Boot application.
 * Since the Spring Boot application is hosted on a different domain or port than the React application,
 * you need to configure CORS to allow cross-origin requests.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Allow both React frontend and Client app backend
                .allowedOrigins(
                        "http://localhost:3001",  // React frontend
                        "http://localhost:8081"   // Client app backend
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Authorization")
                .maxAge(3600);
    }
}
