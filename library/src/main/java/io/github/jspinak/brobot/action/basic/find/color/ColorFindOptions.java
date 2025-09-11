package io.github.jspinak.brobot.action.basic.find.color;

import io.github.jspinak.brobot.action.basic.find.AreaFilteringOptions;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.find.HSVBinOptions;

import lombok.Getter;

/**
 * Configuration for color-based Find actions.
 *
 * <p>This class encapsulates all parameters for finding objects based on their color profiles,
 * using either k-means clustering, mean color statistics, or multi-class classification. It is an
 * immutable object and must be constructed using its inner {@link Builder}.
 *
 * <p>This specialized configuration class enhances type safety and API clarity by only exposing
 * options relevant to color-based operations.
 *
 * <p>Fluent API Usage:
 *
 * <pre>{@code
 * ColorFindOptions options = new ColorFindOptions.Builder()
 *     .setColorStrategy(ColorFindOptions.Color.KMEANS)
 *     .setAreaFiltering(new AreaFilteringOptions.Builder()
 *         .setMinArea(100)
 *         .setMaxArea(5000))
 *     .build();
 * }</pre>
 *
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
public final class ColorFindOptions extends BaseFindOptions {

    /** Defines the color analysis strategy to be used. */
    public enum Color {
        /**
         * Finds a selected number of RGB color cluster centers for each image using the k-means
         * algorithm. This is useful for identifying dominant colors.
         */
        KMEANS,
        /**
         * Takes all pixels from all images and finds the min, max, mean, and standard deviation of
         * the HSV values to create a color profile.
         */
        MU,
        /**
         * Performs a multi-class classification, assigning each pixel in the scene to the most
         * similar state image based on color profiles.
         */
        CLASSIFICATION
    }

    private final Color color;
    private final int diameter;
    private final int kmeans;
    private final AreaFilteringOptions areaFiltering;
    private final HSVBinOptions binOptions;

    private ColorFindOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.color = builder.color;
        this.diameter = builder.diameter;
        this.kmeans = builder.kmeans;
        this.areaFiltering = builder.areaFiltering;
        this.binOptions = builder.binOptions;
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.COLOR;
    }

    /** Builder for constructing {@link ColorFindOptions} with a fluent API. */
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        private Color color = Color.MU;
        private int diameter = 5;
        private int kmeans = 2;
        private AreaFilteringOptions areaFiltering = AreaFilteringOptions.builder().build();
        private HSVBinOptions binOptions = HSVBinOptions.builder().build();

        /** Default constructor for creating a new ColorFindOptions configuration. */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * ColorFindOptions object, allowing for easy modification or templating.
         *
         * @param original The ColorFindOptions instance to copy.
         */
        public Builder(ColorFindOptions original) {
            super(original); // Call parent copy logic
            this.color = original.color;
            this.diameter = original.diameter;
            this.kmeans = original.kmeans;
            this.areaFiltering = original.areaFiltering.toBuilder().build();
            this.binOptions = original.binOptions.toBuilder().build();
        }

        /**
         * Sets the color analysis strategy.
         *
         * @param color The strategy to use (e.g., KMEANS, MU, CLASSIFICATION).
         * @return this Builder instance for chaining.
         */
        public Builder setColorStrategy(Color color) {
            this.color = color;
            return self();
        }

        /**
         * Specifies the width and height of color boxes to find, used when finding distinct color
         * areas.
         *
         * @param diameter The diameter of the color boxes.
         * @return this Builder instance for chaining.
         */
        public Builder setDiameter(int diameter) {
            this.diameter = diameter;
            return self();
        }

        /**
         * Sets the number of k-means clusters to use when the color strategy is KMEANS.
         *
         * @param kmeans The number of clusters.
         * @return this Builder instance for chaining.
         */
        public Builder setKmeans(int kmeans) {
            this.kmeans = kmeans;
            return self();
        }

        /**
         * Configures the area-based filtering options for this color find.
         *
         * @param areaFilteringBuilder A builder for AreaFilteringOptions.
         * @return this Builder instance for chaining.
         */
        public Builder setAreaFiltering(AreaFilteringOptions areaFilteringOptions) {
            this.areaFiltering = areaFilteringOptions;
            return self();
        }

        /**
         * Sets the hue, saturation, and value bins.
         *
         * @param binOptionsBuilder A builder for hsv bins.
         * @return this Builder instance for chaining.
         */
        public Builder setBinOptions(HSVBinOptions binOptions) {
            this.binOptions = binOptions;
            return self();
        }

        /**
         * Builds the immutable {@link ColorFindOptions} object.
         *
         * @return A new instance of ColorFindOptions.
         */
        public ColorFindOptions build() {
            return new ColorFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
