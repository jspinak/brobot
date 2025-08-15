package io.github.jspinak.brobot.action.composite;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for Click and Type composite action.
 * 
 * <p>This configuration combines find, click, and type operations
 * into a single composite action that is commonly used for text input fields.</p>
 * 
 * @since 2.0
 */
@Getter
@JsonDeserialize(builder = ClickAndTypeOptions.Builder.class)
public final class ClickAndTypeOptions extends ActionConfig {
    
    private final PatternFindOptions findOptions;
    private final ClickOptions clickOptions;
    private final TypeOptions typeOptions;
    
    private ClickAndTypeOptions(Builder builder) {
        super(builder);
        this.findOptions = builder.findOptions;
        this.clickOptions = builder.clickOptions;
        this.typeOptions = builder.typeOptions;
    }
    
    /**
     * Builder for constructing ClickAndTypeOptions with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {
        
        @JsonProperty("findOptions")
        private PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        
        @JsonProperty("clickOptions")
        private ClickOptions clickOptions = new ClickOptions.Builder().build();
        
        @JsonProperty("typeOptions")
        private TypeOptions typeOptions = new TypeOptions.Builder().build();
        
        /**
         * Default constructor for creating a new ClickAndTypeOptions configuration.
         */
        @JsonCreator
        public Builder() {}
        
        /**
         * Sets the find options for locating the target element.
         *
         * @param findOptions configuration for finding the target
         * @return this Builder instance for chaining
         */
        public Builder setFindOptions(PatternFindOptions findOptions) {
            this.findOptions = findOptions;
            return self();
        }
        
        /**
         * Sets the click options for clicking on the target.
         *
         * @param clickOptions configuration for clicking
         * @return this Builder instance for chaining
         */
        public Builder setClickOptions(ClickOptions clickOptions) {
            this.clickOptions = clickOptions;
            return self();
        }
        
        /**
         * Sets the type options for typing text.
         *
         * @param typeOptions configuration for typing
         * @return this Builder instance for chaining
         */
        public Builder setTypeOptions(TypeOptions typeOptions) {
            this.typeOptions = typeOptions;
            return self();
        }
        
        @Override
        protected Builder self() {
            return this;
        }
        
        public ClickAndTypeOptions build() {
            return new ClickAndTypeOptions(this);
        }
    }
}