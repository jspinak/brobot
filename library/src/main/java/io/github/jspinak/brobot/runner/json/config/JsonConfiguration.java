package io.github.jspinak.brobot.runner.json.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;

/**
 * Spring configuration for JSON processing in the Brobot application.
 *
 * <p>This configuration class provides a centralized setup for Jackson ObjectMapper used throughout
 * the application. It registers custom modules and serializers to handle Brobot-specific classes
 * and third-party library objects that require special JSON serialization/deserialization handling.
 *
 * <p>The configured ObjectMapper includes:
 *
 * <ul>
 *   <li>Custom mixins for Sikuli, JavaCV, and Java AWT classes via {@link BrobotJsonModule}
 *   <li>Support for Java 8 time API via JavaTimeModule
 *   <li>Disabled empty bean failures for flexible serialization
 *   <li>ISO-8601 date formatting instead of timestamps
 * </ul>
 *
 * @see BrobotJsonModule
 * @see com.fasterxml.jackson.databind.ObjectMapper
 */
@Configuration
public class JsonConfiguration {

    private final BrobotJsonModule brobotJsonModule;

    public JsonConfiguration(BrobotJsonModule brobotJsonModule) {
        this.brobotJsonModule = brobotJsonModule;
    }

    /**
     * Creates and configures the primary ObjectMapper bean for JSON serialization/deserialization.
     *
     * <p>This method creates a Jackson ObjectMapper instance configured with:
     *
     * <ul>
     *   <li>{@link BrobotJsonModule} - Provides custom serializers and mixins for Brobot domain
     *       objects
     *   <li>{@link JavaTimeModule} - Handles Java 8 time types (LocalDateTime, Duration, etc.)
     *   <li>Disabled FAIL_ON_EMPTY_BEANS - Allows serialization of objects without properties
     *   <li>Disabled WRITE_DATES_AS_TIMESTAMPS - Outputs dates in ISO-8601 string format
     * </ul>
     *
     * <p>The bean is marked as @Primary to ensure it's the default ObjectMapper used by Spring when
     * multiple ObjectMappers exist in the application context.
     *
     * @return A fully configured ObjectMapper instance ready for JSON processing
     * @implNote The method name 'jacksonObjectMapper' is chosen to avoid naming conflicts with the
     *     custom ObjectMapper class in io.github.jspinak.brobot.json.parsing
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
