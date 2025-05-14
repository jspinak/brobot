package io.github.jspinak.brobot.json.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jspinak.brobot.json.module.BrobotJsonModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides all JSON-related beans for testing.
 * This configuration avoids circular dependencies by carefully ordering bean creation.
 */
@TestConfiguration
public class BrobotJsonTestConfig {

    /**
     * Creates a standalone Jackson ObjectMapper for testing.
     * This is separate from the one in your custom ObjectMapper class.
     */
    @Bean
    @Primary
    public com.fasterxml.jackson.databind.ObjectMapper testJacksonObjectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        // Register the JavaTimeModule to handle LocalDateTime, Duration, etc.
        mapper.registerModule(new JavaTimeModule());

        // Register the custom module for Brobot classes
        mapper.registerModule(testBrobotJsonModule());

        // Configure serialization features
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    /**
     * Creates the BrobotJsonModule that contains custom serializers for Brobot classes.
     * Named differently to avoid conflicts with the production bean.
     */
    @Bean
    @Primary
    public BrobotJsonModule testBrobotJsonModule() {
        return new BrobotJsonModule();
    }

    /**
     * Creates the custom Brobot ObjectMapper with the module.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.parsing.ObjectMapper testBrobotObjectMapper() {
        return new io.github.jspinak.brobot.json.parsing.ObjectMapper(testBrobotJsonModule());
    }

    /**
     * Creates a SchemaManager with the custom ObjectMapper.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.parsing.SchemaManager testSchemaManager() {
        return new io.github.jspinak.brobot.json.parsing.SchemaManager(testBrobotObjectMapper());
    }

    /**
     * Creates the JsonParser bean using the SchemaManager and ObjectMapper.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.parsing.JsonParser testJsonParser() {
        return new io.github.jspinak.brobot.json.parsing.JsonParser(testSchemaManager(), testBrobotObjectMapper());
    }

    /**
     * Creates the JsonUtils bean with minimal dependencies.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.utils.JsonUtils testJsonUtils() {
        return new io.github.jspinak.brobot.json.utils.JsonUtils(testJsonParser(), testBrobotObjectMapper());
    }

    /**
     * Creates the ActionOptionsJsonUtils bean.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.utils.ActionOptionsJsonUtils testActionOptionsJsonUtils() {
        return new io.github.jspinak.brobot.json.utils.ActionOptionsJsonUtils(testJsonUtils(), testJsonParser());
    }

    /**
     * Creates the MatchesJsonUtils bean.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.utils.MatchesJsonUtils testMatchesJsonUtils() {
        return new io.github.jspinak.brobot.json.utils.MatchesJsonUtils(testJsonUtils(), testJsonParser());
    }

    /**
     * Creates the ObjectCollectionJsonUtils bean.
     */
    @Bean
    @Primary
    public io.github.jspinak.brobot.json.utils.ObjectCollectionJsonUtils testObjectCollectionJsonUtils() {
        return new io.github.jspinak.brobot.json.utils.ObjectCollectionJsonUtils(testJsonUtils(), testMatchesJsonUtils(), testJsonParser());
    }
}