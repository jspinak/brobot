package io.github.jspinak.brobot.tools.testing.data;

import java.util.Map;
import java.util.function.BiFunction;

import lombok.Data;
import lombok.Singular;

/**
 * Represents a variation of test data that modifies baseline scenario elements.
 *
 * <p>Test variations enable systematic testing of edge cases and different conditions by applying
 * transformations to baseline test data. Common variations include:
 *
 * <ul>
 *   <li>Screen size adjustments (mobile, desktop, ultra-wide)
 *   <li>Display settings (DPI, contrast, color depth)
 *   <li>Performance conditions (slow network, high load)
 *   <li>Accessibility modes (high contrast, screen reader compatibility)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TestVariation mobileVariation = TestVariation.builder()
 *     .name("mobile_layout")
 *     .description("Mobile screen layout with smaller elements")
 *     .transformation("scale_images", (name, obj) -> {
 *         if (obj instanceof StateImage) {
 *             return ((StateImage) obj).toBuilder()
 *                 .similarity(0.8) // Reduce similarity for smaller elements
 *                 .build();
 *         }
 *         return obj;
 *     })
 *     .build();
 * }</pre>
 *
 * @see TestScenario
 * @see TestDataBuilder
 */
@Data
@lombok.Builder(toBuilder = true, builderClassName = "TestVariationBuilder")
public class TestVariation {

    /** Unique name for this variation. */
    private final String name;

    /** Description of what this variation tests or simulates. */
    private final String description;

    /**
     * Transformations to apply to test data elements. Each transformation is a function that takes
     * (elementName, element) and returns modified element.
     */
    @Singular("transformation")
    private final Map<String, BiFunction<String, Object, Object>> transformations;

    /** Configuration properties specific to this variation. */
    @Singular("property")
    private final Map<String, Object> properties;

    /** Tags for categorizing variations (e.g., "performance", "mobile", "accessibility"). */
    @Singular("tag")
    private final java.util.List<String> tags;

    /**
     * Applies all transformations to a test data element.
     *
     * @param elementName name of the element being transformed
     * @param element the original element
     * @return transformed element
     */
    public Object applyTransformation(String elementName, Object element) {
        Object result = element;

        for (BiFunction<String, Object, Object> transformation : transformations.values()) {
            result = transformation.apply(elementName, result);
        }

        return result;
    }

    /**
     * Checks if this variation has a specific transformation.
     *
     * @param transformationName name of the transformation
     * @return true if transformation exists
     */
    public boolean hasTransformation(String transformationName) {
        return transformations.containsKey(transformationName);
    }

    /**
     * Gets a property value.
     *
     * @param propertyName name of the property
     * @return property value or null
     */
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Checks if this variation has a specific tag.
     *
     * @param tag the tag to check for
     * @return true if variation has the tag
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /** Builder class for creating test variations with fluent interface. */
    public static class Builder {
        private final String name;
        private final TestVariation.TestVariationBuilder variationBuilder;

        public Builder(String name) {
            this.name = name;
            this.variationBuilder = TestVariation.builder().name(name);
        }

        /**
         * Sets the description for this variation.
         *
         * @param description variation description
         * @return this builder
         */
        public Builder withDescription(String description) {
            variationBuilder.description(description);
            return this;
        }

        /**
         * Adds a transformation function.
         *
         * @param transformationName name of the transformation
         * @param transformation function to apply
         * @return this builder
         */
        public Builder withTransformation(
                String transformationName, BiFunction<String, Object, Object> transformation) {
            variationBuilder.transformation(transformationName, transformation);
            return this;
        }

        /**
         * Adds a property to this variation.
         *
         * @param propertyName name of the property
         * @param value property value
         * @return this builder
         */
        public Builder withProperty(String propertyName, Object value) {
            variationBuilder.property(propertyName, value);
            return this;
        }

        /**
         * Adds a tag to this variation.
         *
         * @param tag the tag to add
         * @return this builder
         */
        public Builder withTag(String tag) {
            variationBuilder.tag(tag);
            return this;
        }

        /**
         * Builds the test variation.
         *
         * @return completed test variation
         */
        public TestVariation build() {
            return variationBuilder.build();
        }

        /**
         * Ends the variation and returns to parent builder. This method should be overridden by
         * subclasses to return appropriate parent type.
         *
         * @return parent builder
         */
        public Builder endVariation() {
            return this;
        }
    }
}
