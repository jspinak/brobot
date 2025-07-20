package io.github.jspinak.brobot.action.composite;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;

/**
 * Composite action configuration that combines Find, Click, and Type operations.
 * <p>
 * This composite action provides a convenient way to configure the common
 * pattern of finding a text field, clicking on it, and then typing text.
 * It's designed for users who prefer the simplicity of the old embedded-find
 * approach.
 * </p>
 * 
 * <p>Usage:
 * <pre>{@code
 * // Simple usage - text will be passed via ObjectCollection
 * FindAndType findAndType = new FindAndType();
 * 
 * // With custom options
 * FindAndType findAndType = new FindAndType.Builder()
 *     .withFindOptions(customFindOptions)
 *     .withTypeOptions(customTypeOptions)
 *     .build();
 * }</pre>
 * </p>
 * 
 * @since 2.0
 */
public class FindAndType extends ActionConfig {
    
    private final PatternFindOptions findOptions;
    private final ClickOptions clickOptions;
    private final TypeOptions typeOptions;
    
    /**
     * Creates a FindAndType action with default options.
     */
    public FindAndType() {
        this(new PatternFindOptions.Builder().build(),
             new ClickOptions.Builder().build(),
             new TypeOptions.Builder().build());
    }
    
    /**
     * Creates a FindAndType action with custom options.
     * 
     * @param findOptions the find configuration to use
     * @param clickOptions the click configuration to use
     * @param typeOptions the type configuration to use
     */
    public FindAndType(PatternFindOptions findOptions, 
                      ClickOptions clickOptions,
                      TypeOptions typeOptions) {
        super(new Builder());
        this.findOptions = findOptions;
        this.clickOptions = clickOptions;
        this.typeOptions = typeOptions;
    }
    
    /**
     * Private constructor for builder.
     */
    private FindAndType(Builder builder) {
        super(builder);
        this.findOptions = builder.findOptions;
        this.clickOptions = builder.clickOptions;
        this.typeOptions = builder.typeOptions;
    }
    
    /**
     * Gets the find options.
     * 
     * @return the PatternFindOptions
     */
    public PatternFindOptions getFindOptions() {
        return findOptions;
    }
    
    /**
     * Gets the click options.
     * 
     * @return the ClickOptions
     */
    public ClickOptions getClickOptions() {
        return clickOptions;
    }
    
    /**
     * Gets the type options.
     * 
     * @return the TypeOptions
     */
    public TypeOptions getTypeOptions() {
        return typeOptions;
    }
    
    /**
     * Builder for creating FindAndType instances with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {
        private PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        private ClickOptions clickOptions = new ClickOptions.Builder().build();
        private TypeOptions typeOptions = new TypeOptions.Builder().build();
        
        /**
         * Sets the similarity threshold for finding.
         * 
         * @param similarity the minimum similarity score (0.0 to 1.0)
         * @return this builder for chaining
         */
        public Builder withSimilarity(double similarity) {
            this.findOptions = new PatternFindOptions.Builder(findOptions)
                .setSimilarity(similarity)
                .build();
            return self();
        }
        
        /**
         * Sets custom find options.
         * 
         * @param findOptions the find configuration
         * @return this builder for chaining
         */
        public Builder withFindOptions(PatternFindOptions findOptions) {
            this.findOptions = findOptions;
            return self();
        }
        
        /**
         * Sets custom click options.
         * 
         * @param clickOptions the click configuration
         * @return this builder for chaining
         */
        public Builder withClickOptions(ClickOptions clickOptions) {
            this.clickOptions = clickOptions;
            return self();
        }
        
        /**
         * Sets custom type options.
         * 
         * @param typeOptions the type configuration
         * @return this builder for chaining
         */
        public Builder withTypeOptions(TypeOptions typeOptions) {
            this.typeOptions = typeOptions;
            return self();
        }
        
        public FindAndType build() {
            return new FindAndType(this);
        }
        
        @Override
        protected Builder self() {
            return this;
        }
    }
}