package io.github.jspinak.brobot.action.basic.find.histogram;

import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.find.HSVBinOptions;
import lombok.Getter;

/**
 * Configuration for histogram-based Find actions.
 * <p>
 * This class encapsulates parameters for finding objects based on their histogram profiles.
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * <p>
 * By extending {@link BaseFindOptions}, it inherits common find functionality including
 * match adjustment support, while adding histogram-specific settings.
 *
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
public final class HistogramFindOptions extends BaseFindOptions {
    
    private final HSVBinOptions binOptions;

    private HistogramFindOptions(Builder builder) {
        super(builder);
        this.binOptions = builder.binOptions.build();
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.HISTOGRAM;
    }

    /**
     * Builder for constructing {@link HistogramFindOptions} with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {
        private HSVBinOptions.Builder binOptions = new HSVBinOptions.Builder();

        /**
         * Default constructor for creating a new HistogramFindOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * HistogramFindOptions object, allowing for easy modification or templating.
         *
         * @param original The HistogramFindOptions instance to copy.
         */
        public Builder(HistogramFindOptions original) {
            super(original);
            this.binOptions = new HSVBinOptions.Builder(original.binOptions);
        }

        /**
         * Sets the hue, saturation, and value bins for histogram analysis.
         * @param binOptionsBuilder A builder for HSV bins.
         * @return this Builder instance for chaining.
         */
        public Builder setBinOptions(HSVBinOptions.Builder binOptionsBuilder) {
            this.binOptions = binOptionsBuilder;
            return self();
        }

        /**
         * Builds the immutable {@link HistogramFindOptions} object.
         *
         * @return A new instance of HistogramFindOptions.
         */
        public HistogramFindOptions build() {
            return new HistogramFindOptions(this);
        }

        @Override
        protected Builder self() { return this; }
    }
}