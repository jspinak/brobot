package io.github.jspinak.brobot.test.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Jackson Mix-in classes for Brobot model objects.
 * These provide Jackson annotations without modifying the original classes.
 */
public class BrobotJacksonMixins {

    /**
     * Mix-in for classes with Lombok @Builder that need Jackson deserialization.
     * Apply this to classes that use @Builder but don't have @JsonDeserialize annotation.
     */
    @JsonDeserialize(builder = GenericBuilder.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class BuilderMixin {
    }

    /**
     * Generic builder mix-in for Jackson deserialization of Lombok builders.
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenericBuilder {
    }

    /**
     * Mix-in for ActionRecord class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class ActionRecordMixin {
        // Ensures LocalDateTime is handled properly
    }

    /**
     * Mix-in for StateImage class
     */
    @JsonDeserialize(builder = StateImageBuilder.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class StateImageMixin {
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateImageBuilder {
    }

    /**
     * Mix-in for Match class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class MatchMixin {
    }

    /**
     * Mix-in for Pattern class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class PatternMixin {
    }

    /**
     * Mix-in for Region class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class RegionMixin {
    }

    /**
     * Mix-in for Location class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class LocationMixin {
    }

    /**
     * Mix-in for Image class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class ImageMixin {
        // BufferedImage field should be ignored
    }

    /**
     * Mix-in for Scene class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class SceneMixin {
    }

    /**
     * Mix-in for SearchRegions class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class SearchRegionsMixin {
    }

    /**
     * Registers all mix-ins with an ObjectMapper
     */
    public static void registerMixins(com.fasterxml.jackson.databind.ObjectMapper mapper) {
        // Register mix-ins for model classes
        mapper.addMixIn(io.github.jspinak.brobot.model.action.ActionRecord.class, ActionRecordMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.state.StateImage.class, StateImageMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.match.Match.class, MatchMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.Pattern.class, PatternMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.Region.class, RegionMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.Location.class, LocationMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.Image.class, ImageMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.Scene.class, SceneMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.SearchRegions.class, SearchRegionsMixin.class);
    }
}