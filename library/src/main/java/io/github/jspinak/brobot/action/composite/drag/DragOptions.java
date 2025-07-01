package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import lombok.Getter;

/**
 * Configuration for drag actions.
 * <p>
 * DragOptions configures a drag operation which is implemented as a chain of 6 actions:
 * Find source → Find target → MouseMove to source → MouseDown → MouseMove to target → MouseUp
 * </p>
 */
@Getter
public final class DragOptions extends ActionConfig {
    
    private final MousePressOptions mousePressOptions;
    private final double delayBetweenMouseDownAndMove;
    private final double delayAfterDrag;
    
    private DragOptions(Builder builder) {
        super(builder);
        this.mousePressOptions = builder.mousePressOptions;
        this.delayBetweenMouseDownAndMove = builder.delayBetweenMouseDownAndMove;
        this.delayAfterDrag = builder.delayAfterDrag;
    }
    
    /**
     * Builder for constructing DragOptions with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {
        
        private MousePressOptions mousePressOptions = new MousePressOptions.Builder()
            .setButton(MouseButton.LEFT)
            .build();
        private double delayBetweenMouseDownAndMove = 0.5;
        private double delayAfterDrag = 0.5;
        
        /**
         * Default constructor for creating a new DragOptions configuration.
         */
        public Builder() {}
        
        /**
         * Sets the mouse press options for the drag operation.
         *
         * @param mousePressOptions configuration for mouse button and timing
         * @return this Builder instance for chaining
         */
        public Builder setMousePressOptions(MousePressOptions mousePressOptions) {
            this.mousePressOptions = mousePressOptions;
            return self();
        }
        
        /**
         * Sets the delay between mouse down and the drag movement.
         *
         * @param seconds delay in seconds
         * @return this Builder instance for chaining
         */
        public Builder setDelayBetweenMouseDownAndMove(double seconds) {
            this.delayBetweenMouseDownAndMove = seconds;
            return self();
        }
        
        /**
         * Sets the delay after completing the drag operation.
         *
         * @param seconds delay in seconds
         * @return this Builder instance for chaining
         */
        public Builder setDelayAfterDrag(double seconds) {
            this.delayAfterDrag = seconds;
            return self();
        }
        
        /**
         * Builds the immutable DragOptions object.
         *
         * @return a new instance of DragOptions
         */
        public DragOptions build() {
            return new DragOptions(this);
        }
        
        @Override
        protected Builder self() {
            return this;
        }
    }
}