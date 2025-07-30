package io.github.jspinak.brobot.action.composite.chains;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.composite.repeat.ClickUntilOptions;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateRegion;

import org.springframework.stereotype.Component;

/**
 * Provides a facade of commonly used action patterns for GUI automation using the modern ActionConfig API.
 * <p>
 * This class is the ActionConfig-based replacement for {@link ActionFacade}, encapsulating frequent 
 * action combinations into simple, reusable methods. It reduces code duplication and improves 
 * readability across automation scripts by providing a higher-level abstraction over the core 
 * {@link Action} API with type-safe configuration objects.
 * 
 * <p>Key improvements over ActionFacade:</p>
 * <ul>
 *   <li>Type-safe configuration with specific Options classes</li>
 *   <li>Better IDE support with auto-completion</li>
 *   <li>Clearer API with method names that match intent</li>
 *   <li>Support for action chaining through fluent API</li>
 * </ul>
 * 
 * <p>Common usage patterns:</p>
 * <pre>{@code
 * // Simple click with timeout
 * facade.click(5.0, stateImage);
 * 
 * // Double-click at location
 * facade.doubleClick(location);
 * 
 * // Right-click on best match
 * facade.rightClickBest(0.9, stateImage);
 * 
 * // Type with modifiers
 * facade.typeWithModifiers("v", "CTRL");
 * 
 * // Click until vanished
 * facade.clickUntilVanished(10, 0.5, stateImage);
 * }</pre>
 * 
 * @see Action
 * @see ActionConfig
 * @see StateService
 * @since 2.0
 */
@Component
public class ActionConfigFacade {

    private final Action action;

    public ActionConfigFacade(Action action) {
        this.action = action;
    }

    /**
     * Performs a single left click on the first found state image.
     * 
     * @param maxWait Maximum time in seconds to wait for the image to appear
     * @param stateImages Variable number of state images to search for. The first
     *                    found image will be clicked.
     * @return true if an image was found and clicked successfully, false otherwise
     */
    public boolean click(double maxWait, StateImage... stateImages) {
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setPauseAfterEnd(0.5) // Small pause after click for UI stability
                .build();
                
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseAfterEnd(0.1)
                .build();
                
        // Chain find and click
        ActionChainOptions chain = new ActionChainOptions.Builder(findOptions)
                .then(clickOptions)
                .build();
                
        return action.perform(chain, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Performs a single left click at the specified screen location.
     * <p>
     * This method bypasses visual search and clicks directly at the given coordinates,
     * useful for clicking at calculated positions or known screen locations.
     * 
     * @param location The exact screen coordinates where the click should occur
     * @return true if the click was executed successfully, false otherwise
     */
    public boolean click(Location location) {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        
        return action.perform(clickOptions, new ObjectCollection.Builder()
                .withLocations(location)
                .build()).isSuccess();
    }

    /**
     * Performs a double-click on the first found state image.
     * 
     * @param maxWait Maximum time in seconds to wait for the image to appear
     * @param stateImages Variable number of state images to search for
     * @return true if an image was found and double-clicked successfully, false otherwise
     */
    public boolean doubleClick(double maxWait, StateImage... stateImages) {
        ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
                
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseAfterEnd(0.1)
                .build();
                
        // Chain find and double-click
        ActionChainOptions chain = new ActionChainOptions.Builder(findOptions)
                .then(doubleClickOptions)
                .build();
                
        return action.perform(chain, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Performs a double-click at the specified location.
     * 
     * @param location The screen coordinates where to double-click
     * @return true if the double-click was executed successfully, false otherwise
     */
    public boolean doubleClick(Location location) {
        ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
                
        return action.perform(doubleClickOptions, new ObjectCollection.Builder()
                .withLocations(location)
                .build()).isSuccess();
    }

    /**
     * Performs a right-click on the first found state image.
     * 
     * @param maxWait Maximum time in seconds to wait for the image to appear
     * @param stateImages Variable number of state images to search for
     * @return true if an image was found and right-clicked successfully, false otherwise
     */
    public boolean rightClick(double maxWait, StateImage... stateImages) {
        ClickOptions rightClickOptions = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                        .button(MouseButton.RIGHT)
                        .build())
                .build();
                
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseAfterEnd(0.1)
                .build();
                
        // Chain find and right-click
        ActionChainOptions chain = new ActionChainOptions.Builder(findOptions)
                .then(rightClickOptions)
                .build();
                
        return action.perform(chain, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Finds and clicks the best match among the provided images.
     * 
     * @param similarity Minimum similarity threshold (0.0 to 1.0)
     * @param stateImages Images to search for
     * @return true if a match was found and clicked, false otherwise
     */
    public boolean clickBest(double similarity, StateImage... stateImages) {
        PatternFindOptions findBest = new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
                
        ClickOptions click = new ClickOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(findBest)
                .then(click)
                .build();
                
        return action.perform(chain, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Finds and right-clicks the best match among the provided images.
     * 
     * @param similarity Minimum similarity threshold (0.0 to 1.0)
     * @param stateImages Images to search for
     * @return true if a match was found and right-clicked, false otherwise
     */
    public boolean rightClickBest(double similarity, StateImage... stateImages) {
        PatternFindOptions findBest = new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
                
        ClickOptions rightClick = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                        .button(MouseButton.RIGHT)
                        .build())
                .build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(findBest)
                .then(rightClick)
                .build();
                
        return action.perform(chain, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Repeatedly clicks an image until it disappears from the screen.
     * <p>
     * This method is useful for dismissing dialogs, clearing notifications, or
     * interacting with UI elements that require multiple clicks to disappear.
     * 
     * @param timeout Maximum time in seconds to wait for images to vanish
     * @param stateImages Images to click until they vanish
     * @return true if the images vanished, false if timeout reached
     */
    public boolean clickUntilVanished(double timeout, StateImage... stateImages) {
        ClickUntilOptions clickUntil = new ClickUntilOptions.Builder()
                .setCondition(ClickUntilOptions.Condition.OBJECTS_VANISH)
                .setPauseAfterEnd(0.5)
                .build();
                
        return action.perform(clickUntil, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Types text at the current cursor position.
     * 
     * @param text The text to type
     * @return true if the text was typed successfully, false otherwise
     */
    public boolean type(String text) {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        
        return action.perform(typeOptions, new ObjectCollection.Builder()
                .withStrings(text)
                .build()).isSuccess();
    }

    /**
     * Types text with keyboard modifiers (e.g., CTRL+V for paste).
     * 
     * @param text The text or key to type
     * @param modifiers Modifier keys (CTRL, ALT, SHIFT, CMD)
     * @return true if the text was typed successfully, false otherwise
     */
    public boolean typeWithModifiers(String text, String modifiers) {
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setModifiers(modifiers)
                .build();
                
        return action.perform(typeOptions, new ObjectCollection.Builder()
                .withStrings(text)
                .build()).isSuccess();
    }

    /**
     * Drags from one location to another.
     * 
     * @param from Starting location
     * @param to Destination location
     * @return true if the drag was performed successfully, false otherwise
     */
    public boolean drag(Location from, Location to) {
        DragOptions dragOptions = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.5)
                .setDelayAfterDrag(0.5)
                .build();
                
        return action.perform(dragOptions, 
                new ObjectCollection.Builder().withLocations(from).build(),
                new ObjectCollection.Builder().withLocations(to).build())
                .isSuccess();
    }

    /**
     * Drags from the first found image to a destination location.
     * 
     * @param fromImage Image to drag from
     * @param to Destination location
     * @return true if the drag was performed successfully, false otherwise
     */
    public boolean drag(StateImage fromImage, Location to) {
        DragOptions drag = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.5)
                .setDelayAfterDrag(0.5)
                .build();
                
        return action.perform(drag,
                new ObjectCollection.Builder().withImages(fromImage).build(),
                new ObjectCollection.Builder().withLocations(to).build())
                .isSuccess();
    }

    /**
     * Waits for the specified images to appear on screen.
     * 
     * @param stateImages Images to wait for
     * @return true if at least one image appeared, false if timeout
     */
    public boolean waitFor(StateImage... stateImages) {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .build();
                
        return action.perform(findOptions, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Waits for the specified images to disappear from screen.
     * 
     * @param maxWait Maximum time in seconds to wait
     * @param stateImages Images to wait to vanish
     * @return true if all images vanished, false if timeout
     */
    public boolean waitToVanish(double maxWait, StateImage... stateImages) {
        VanishOptions vanishOptions = new VanishOptions.Builder()
                .setTimeout(maxWait)
                .build();
                
        return action.perform(vanishOptions, new ObjectCollection.Builder()
                .withImages(stateImages)
                .build()).isSuccess();
    }

    /**
     * Clicks on a state region.
     * 
     * @param stateRegion The state region to click
     * @return true if the region was clicked successfully, false otherwise
     */
    public boolean clickRegion(StateRegion stateRegion) {
        if (stateRegion == null) return false;
        
        ClickOptions click = new ClickOptions.Builder().build();
        
        return action.perform(click, new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .build()).isSuccess();
    }

    /**
     * Performs a click at a specific position within a region.
     * 
     * @param region The region to click within
     * @param positionName The relative position within the region (TOPLEFT, MIDDLEMIDDLE, etc.)
     * @return true if the click was performed successfully, false otherwise
     */
    public boolean clickAt(Region region, Positions.Name positionName) {
        Position position = new Position(positionName);
        
        // Calculate the location based on the position percentages
        int x = region.getX() + (int)(region.getW() * position.getPercentW());
        int y = region.getY() + (int)(region.getH() * position.getPercentH());
        
        return click(new Location(x, y));
    }
}