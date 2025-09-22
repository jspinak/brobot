package io.github.jspinak.brobot.tools.testing.data;

import java.util.List;
import java.util.Map;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;

import lombok.Data;
import lombok.Singular;

/**
 * Represents a complete test scenario with baseline data and variations.
 *
 * <p>A test scenario contains:
 *
 * <ul>
 *   <li>Baseline test data (images, strings, regions)
 *   <li>Multiple variations for edge case testing
 *   <li>Metadata for organization and tracking
 *   <li>Validation rules and expected behaviors
 * </ul>
 *
 * @see TestDataBuilder
 * @see TestVariation
 */
@Data
@lombok.Builder(toBuilder = true, builderClassName = "TestScenarioBuilder")
public class TestScenario {

    /** Unique name for this test scenario. */
    private final String name;

    /** Description of what this scenario tests. */
    private final String description;

    /** Version of this test data for tracking changes. */
    private final String version;

    /** State images used in this scenario. */
    @Singular("stateImage")
    private final Map<String, StateImage> stateImages;

    /** State strings used in this scenario. */
    @Singular("stateString")
    private final Map<String, StateString> stateStrings;

    /** Regions defined for this scenario. */
    @Singular("region")
    private final Map<String, Region> regions;

    /** Test variations that modify the baseline data. */
    @Singular("variation")
    private final Map<String, TestVariation> variations;

    /** Tags for categorizing and filtering scenarios. */
    @Singular("tag")
    private final List<String> tags;

    /** Custom metadata for scenario-specific information. */
    @Singular("metadata")
    private final Map<String, Object> metadata;

    /**
     * Gets a specific variation of this scenario.
     *
     * @param variationName name of the variation
     * @return the variation or null if not found
     */
    public TestVariation getVariation(String variationName) {
        return variations.get(variationName);
    }

    /**
     * Applies a variation to create a modified version of this scenario.
     *
     * @param variationName name of the variation to apply
     * @return new scenario with variation applied
     * @throws IllegalArgumentException if variation not found
     */
    public TestScenario withVariation(String variationName) {
        TestVariation variation = variations.get(variationName);
        if (variation == null) {
            throw new IllegalArgumentException("Variation '" + variationName + "' not found");
        }

        TestScenario.TestScenarioBuilder builder = this.toBuilder();
        builder.clearStateImages();
        builder.clearStateStrings();
        builder.clearRegions();

        // Apply transformations to state images
        stateImages.forEach(
                (name, image) -> {
                    Object transformed = variation.applyTransformation(name, image);
                    if (transformed instanceof StateImage) {
                        builder.stateImage(name, (StateImage) transformed);
                    }
                });

        // Apply transformations to state strings
        stateStrings.forEach(
                (name, string) -> {
                    Object transformed = variation.applyTransformation(name, string);
                    if (transformed instanceof StateString) {
                        builder.stateString(name, (StateString) transformed);
                    }
                });

        // Apply transformations to regions
        regions.forEach(
                (name, region) -> {
                    Object transformed = variation.applyTransformation(name, region);
                    if (transformed instanceof Region) {
                        builder.region(name, (Region) transformed);
                    }
                });

        return builder.build();
    }

    /**
     * Checks if this scenario has a specific tag.
     *
     * @param tag the tag to check for
     * @return true if scenario has the tag
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * Gets metadata value by key.
     *
     * @param key the metadata key
     * @return the metadata value or null
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /** Builder class for creating test scenarios with fluent interface. */
    public static class Builder {
        private final String name;
        private final TestDataBuilder parent;
        private final TestScenarioBuilder scenarioBuilder;

        public Builder(String name, TestDataBuilder parent) {
            this.name = name;
            this.parent = parent;
            this.scenarioBuilder = TestScenario.builder().name(name);
        }

        /**
         * Sets the description for this scenario.
         *
         * @param description scenario description
         * @return this builder
         */
        public Builder withDescription(String description) {
            scenarioBuilder.description(description);
            return this;
        }

        /**
         * Sets the version for this scenario.
         *
         * @param version version string
         * @return this builder
         */
        public Builder withVersion(String version) {
            scenarioBuilder.version(version);
            return this;
        }

        /**
         * Adds a state image to this scenario.
         *
         * @param name name of the state image
         * @param filename image filename
         * @return this builder
         */
        public Builder withStateImage(String name, String filename) {
            StateImage image = new StateImage.Builder().addPattern(filename).build();
            scenarioBuilder.stateImage(name, image);
            return this;
        }

        /**
         * Adds a configured state image to this scenario.
         *
         * @param name name of the state image
         * @param image the state image
         * @return this builder
         */
        public Builder withStateImage(String name, StateImage image) {
            scenarioBuilder.stateImage(name, image);
            return this;
        }

        /**
         * Adds a state string to this scenario.
         *
         * @param name name of the state string
         * @param text the text content
         * @return this builder
         */
        public Builder withStateString(String name, String text) {
            StateString string = new StateString.Builder().setString(text).build();
            scenarioBuilder.stateString(name, string);
            return this;
        }

        /**
         * Adds a region to this scenario.
         *
         * @param name name of the region
         * @param region the region
         * @return this builder
         */
        public Builder withRegion(String name, Region region) {
            scenarioBuilder.region(name, region);
            return this;
        }

        /**
         * Adds a tag to this scenario.
         *
         * @param tag the tag to add
         * @return this builder
         */
        public Builder withTag(String tag) {
            scenarioBuilder.tag(tag);
            return this;
        }

        /**
         * Adds metadata to this scenario.
         *
         * @param key metadata key
         * @param value metadata value
         * @return this builder
         */
        public Builder withMetadata(String key, Object value) {
            scenarioBuilder.metadata(key, value);
            return this;
        }

        /**
         * Uses baseline data from the parent builder.
         *
         * @return this builder
         */
        public Builder withBaselineData() {
            // Could load from external source or use predefined baseline
            return this;
        }

        /**
         * Starts building a variation of this scenario.
         *
         * @param variationName name of the variation
         * @return variation builder
         */
        public TestVariation.Builder withVariation(String variationName) {
            return new TestVariation.Builder(variationName) {
                @Override
                public TestVariation.Builder endVariation() {
                    TestVariation variation = this.build();
                    scenarioBuilder.variation(variationName, variation);
                    // Return this variation builder to match parent's return type
                    return this;
                }
            };
        }

        /**
         * Builds the final test scenario.
         *
         * @return completed test scenario
         */
        public TestScenario build() {
            TestScenario scenario = scenarioBuilder.build();
            parent.storeBaselineScenario(name, scenario);
            return scenario;
        }
    }
}
