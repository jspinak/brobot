package io.github.jspinak.brobot.json.config;

import io.github.jspinak.brobot.json.module.BrobotJsonModule;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.utils.ActionOptionsJsonUtils;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import io.github.jspinak.brobot.json.utils.MatchesJsonUtils;
import io.github.jspinak.brobot.json.utils.ObjectCollectionJsonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Brobot-specific JSON processing components.
 * This complements the existing JsonConfig, not replaces it.
 * It adds custom serializers and utility classes for Brobot objects.
 */
@Configuration
public class BrobotJsonConfig {

    /**
     * Creates the BrobotJsonModule that contains custom serializers for Brobot classes.
     * This module will be automatically registered with the existing ObjectMapper.
     */
    @Bean
    public BrobotJsonModule brobotJsonModule() {
        return new BrobotJsonModule();
    }

    /**
     * Creates the JsonUtils bean that's required by other utility classes.
     */
    @Bean
    public JsonUtils jsonUtils(JsonParser jsonParser) {
        return new JsonUtils(jsonParser, null); // Second parameter would be ObjectMapper, but we'll use null to avoid circular dependency
    }

    /**
     * Creates the ActionOptionsJsonUtils bean.
     */
    @Bean
    public ActionOptionsJsonUtils actionOptionsJsonUtils(JsonUtils jsonUtils, JsonParser jsonParser) {
        return new ActionOptionsJsonUtils(jsonUtils, jsonParser);
    }

    /**
     * Creates the MatchesJsonUtils bean.
     */
    @Bean
    public MatchesJsonUtils matchesJsonUtils(JsonUtils jsonUtils, JsonParser jsonParser) {
        return new MatchesJsonUtils(jsonUtils, jsonParser);
    }

    /**
     * Creates the ObjectCollectionJsonUtils bean.
     */
    @Bean
    public ObjectCollectionJsonUtils objectCollectionJsonUtils(JsonUtils jsonUtils,
                                                               MatchesJsonUtils matchesJsonUtils,
                                                               JsonParser jsonParser) {
        return new ObjectCollectionJsonUtils(jsonUtils, matchesJsonUtils, jsonParser);
    }
}