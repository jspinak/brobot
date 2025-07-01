package io.github.jspinak.brobot.action.basic.vanish;

import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import lombok.Getter;

/**
 * Configuration for vanish actions.
 * <p>
 * VanishOptions provides configuration for waiting until an object disappears from the screen.
 * This extends BaseFindOptions since vanishing is essentially a reverse find operation.
 * </p>
 */
@Getter
public final class VanishOptions extends BaseFindOptions {
    
    private final double timeout;
    
    private VanishOptions(Builder builder) {
        super(builder);
        this.timeout = builder.timeout;
    }
    
    @Override
    public FindStrategy getFindStrategy() {
        // Vanish typically checks each object individually to see if it has disappeared
        return FindStrategy.EACH;
    }
    
    /**
     * Builder for constructing VanishOptions with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {
        
        private double timeout = 10.0;
        
        /**
         * Default constructor for creating a new VanishOptions configuration.
         */
        public Builder() {}
        
        /**
         * Sets the timeout in seconds to wait for the object to vanish.
         *
         * @param seconds the timeout duration
         * @return this Builder instance for chaining
         */
        public Builder setTimeout(double seconds) {
            this.timeout = seconds;
            return self();
        }
        
        /**
         * Builds the immutable VanishOptions object.
         *
         * @return a new instance of VanishOptions
         */
        public VanishOptions build() {
            return new VanishOptions(this);
        }
        
        @Override
        protected Builder self() {
            return this;
        }
    }
}