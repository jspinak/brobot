package io.github.jspinak.brobot.json.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jspinak.brobot.json.module.BrobotJsonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for JSON processing in the Brobot application.
 * Sets up the ObjectMapper with necessary modules and configurations.
 */
@Configuration
public class JsonConfig {

    private final BrobotJsonModule brobotJsonModule;

    public JsonConfig(BrobotJsonModule brobotJsonModule) {
        this.brobotJsonModule = brobotJsonModule;
    }

    /**
     * Creates and configures the ObjectMapper for JSON serialization/deserialization.
     * Named to avoid conflicts with io.github.jspinak.brobot.json.parsing.ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register the custom module for Brobot classes
        mapper.registerModule(brobotJsonModule);

        // Add JavaTimeModule for handling LocalDateTime, Duration, etc.
        mapper.registerModule(new JavaTimeModule());

        // Configure serialization features
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }
}