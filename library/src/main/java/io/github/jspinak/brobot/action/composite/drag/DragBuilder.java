package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;

/**
 * Builder utility for creating drag operations as a chain of actions.
 * <p>
 * This class provides convenience methods for constructing drag operations
 * using the ActionConfig chaining mechanism. A drag is composed of six actions:
 * 1. Find the source location
 * 2. Find the destination location  
 * 3. Move mouse to source
 * 4. Press mouse button down
 * 5. Move mouse to destination (while holding)
 * 6. Release mouse button
 * 
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * // Create a drag from one icon to another
 * ActionConfig dragAction = DragBuilder.createDrag()
 *     .fromPattern(new PatternFindOptions.Builder()
 *         .setStrategy(PatternFindOptions.Strategy.BEST)
 *         .build())
 *     .toPattern(new PatternFindOptions.Builder()
 *         .setStrategy(PatternFindOptions.Strategy.BEST)
 *         .build())
 *     .withDragSpeed(0.5f)
 *     .build();
 * 
 * // Execute with two ObjectCollections (from and to)
 * action.perform(dragAction, fromCollection, toCollection);
 * }
 * </pre>
 */
public class DragBuilder {
    
    private PatternFindOptions fromOptions;
    private PatternFindOptions toOptions;
    private MouseMoveOptions moveToSourceOptions;
    private MouseDownOptions mouseDownOptions;
    private MouseMoveOptions dragMoveOptions;
    private MouseUpOptions mouseUpOptions;
    
    private DragBuilder() {
        // Set defaults
        this.fromOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        this.toOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        this.moveToSourceOptions = new MouseMoveOptions.Builder().build();
        this.mouseDownOptions = new MouseDownOptions.Builder()
            .setPressOptions(MousePressOptions.builder()
                .setButton(MouseButton.LEFT)
                .build())
            .build();
        this.dragMoveOptions = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.5f) // Slower for drag operations
            .build();
        this.mouseUpOptions = new MouseUpOptions.Builder()
            .setPressOptions(MousePressOptions.builder()
                .setButton(MouseButton.LEFT)
                .build())
            .build();
    }
    
    /**
     * Creates a new DragBuilder instance.
     * @return A new DragBuilder
     */
    public static DragBuilder createDrag() {
        return new DragBuilder();
    }
    
    /**
     * Sets the pattern find options for locating the drag source.
     * @param options The find options for the source
     * @return This builder for chaining
     */
    public DragBuilder fromPattern(PatternFindOptions options) {
        this.fromOptions = options;
        return this;
    }
    
    /**
     * Sets the pattern find options for locating the drag destination.
     * @param options The find options for the destination
     * @return This builder for chaining
     */
    public DragBuilder toPattern(PatternFindOptions options) {
        this.toOptions = options;
        return this;
    }
    
    /**
     * Sets the mouse movement speed when moving to the source location.
     * @param delay The delay in seconds for each movement step
     * @return This builder for chaining
     */
    public DragBuilder withMoveToSourceSpeed(float delay) {
        this.moveToSourceOptions = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(delay)
            .build();
        return this;
    }
    
    /**
     * Sets the mouse movement speed during the drag operation.
     * @param delay The delay in seconds for each movement step
     * @return This builder for chaining
     */
    public DragBuilder withDragSpeed(float delay) {
        this.dragMoveOptions = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(delay)
            .build();
        return this;
    }
    
    /**
     * Sets the mouse button to use for dragging.
     * @param buttonType The mouse button type
     * @return This builder for chaining
     */
    public DragBuilder withButton(MouseButton button) {
        this.mouseDownOptions = new MouseDownOptions.Builder()
            .setPressOptions(MousePressOptions.builder()
                .setButton(button)
                .build())
            .build();
        this.mouseUpOptions = new MouseUpOptions.Builder()
            .setPressOptions(MousePressOptions.builder()
                .setButton(button)
                .build())
            .build();
        return this;
    }
    
    /**
     * Sets custom options for the mouse down action.
     * @param options The mouse down options
     * @return This builder for chaining
     */
    public DragBuilder withMouseDownOptions(MouseDownOptions options) {
        this.mouseDownOptions = options;
        return this;
    }
    
    /**
     * Sets custom options for the mouse up action.
     * @param options The mouse up options
     * @return This builder for chaining
     */
    public DragBuilder withMouseUpOptions(MouseUpOptions options) {
        this.mouseUpOptions = options;
        return this;
    }
    
    /**
     * Convenience method to add offsets to the destination location.
     * @param offsetX The X offset in pixels
     * @param offsetY The Y offset in pixels
     * @return This builder for chaining
     */
    public DragBuilder withDestinationOffset(int offsetX, int offsetY) {
        this.toOptions = new PatternFindOptions.Builder(toOptions)
            .setMatchAdjustment(MatchAdjustmentOptions.builder()
                .setTargetOffset(new Location(offsetX, offsetY))
                .build())
            .build();
        return this;
    }
    
    /**
     * Sets pauses before and after the mouse down action.
     * @param pauseBefore Pause in seconds before mouse down
     * @param pauseAfter Pause in seconds after mouse down
     * @return This builder for chaining
     */
    public DragBuilder withMouseDownPauses(double pauseBefore, double pauseAfter) {
        this.mouseDownOptions = new MouseDownOptions.Builder(mouseDownOptions)
            .setPressOptions(MousePressOptions.builder()
                .setPauseBeforeMouseDown(pauseBefore)
                .setPauseAfterMouseDown(pauseAfter)
                .build())
            .build();
        return this;
    }
    
    /**
     * Sets pauses before and after the mouse up action.
     * @param pauseBefore Pause in seconds before mouse up
     * @param pauseAfter Pause in seconds after mouse up
     * @return This builder for chaining
     */
    public DragBuilder withMouseUpPauses(double pauseBefore, double pauseAfter) {
        this.mouseUpOptions = new MouseUpOptions.Builder(mouseUpOptions)
            .setPressOptions(MousePressOptions.builder()
                .setPauseBeforeMouseUp(pauseBefore)
                .setPauseAfterMouseUp(pauseAfter)
                .build())
            .build();
        return this;
    }
    
    /**
     * Builds the drag action as a chain of Find->Find->MouseMove->MouseDown->MouseMove->MouseUp.
     * @return The chained ActionConfig representing the drag operation
     */
    public ActionConfig build() {
        // Chain the actions: Find source -> Find destination -> Move to source -> Mouse down -> Drag move -> Mouse up
        return new ActionChainOptions.Builder(fromOptions)
            .then(toOptions)
            .then(moveToSourceOptions)
            .then(mouseDownOptions)
            .then(dragMoveOptions)
            .then(mouseUpOptions)
            .build();
    }
}