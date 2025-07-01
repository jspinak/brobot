package io.github.jspinak.brobot.action.basic.find.text;

import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import lombok.Getter;

/**
 * Configuration for text-based Find actions using OCR (Optical Character Recognition).
 * <p>
 * This class encapsulates all parameters for finding text elements within GUI scenes.
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * <p>
 * TextFindOptions supports the ALL_WORDS find strategy, which performs comprehensive 
 * text detection across multiple scenes, organizing results into scene analyses for 
 * structured access to text matches.
 * 
 * Fluent API Usage:
 * <pre>
 * {@code
 * TextFindOptions options = new TextFindOptions.Builder()
 *     .setMaxMatchRetries(3)
 *     .setSearchRegions(searchRegions)
 *     .setPauseAfterEnd(0.5)
 *     .build();
 * }
 * </pre>
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
public final class TextFindOptions extends BaseFindOptions {

    /**
     * The maximum number of times to retry finding text.
     * This is useful for waiting for dynamic text to appear.
     */
    private final int maxMatchRetries;

    private TextFindOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.maxMatchRetries = builder.maxMatchRetries;
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.ALL_WORDS;
    }

    /**
     * Builder for constructing {@link TextFindOptions} with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        private int maxMatchRetries = 1;

        /**
         * Default constructor for creating a new TextFindOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * TextFindOptions object, allowing for easy modification or templating.
         *
         * @param original The TextFindOptions instance to copy.
         */
        public Builder(TextFindOptions original) {
            super(original); // Call parent copy logic
            this.maxMatchRetries = original.maxMatchRetries;
        }

        /**
         * Sets the maximum number of times to retry finding text.
         * @param maxMatchRetries The maximum number of retries.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxMatchRetries(int maxMatchRetries) {
            this.maxMatchRetries = maxMatchRetries;
            return self();
        }
        
        /**
         * Builds the immutable {@link TextFindOptions} object.
         *
         * @return A new instance of TextFindOptions.
         */
        public TextFindOptions build() {
            return new TextFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}