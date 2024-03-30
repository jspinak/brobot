package io.github.jspinak.brobot.app;

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
                .allowedOrigins("*") // Allow all origins
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow specific HTTP methods
                .allowedHeaders("*") // Allow all headers
                .exposedHeaders("Authorization");
    }
}
