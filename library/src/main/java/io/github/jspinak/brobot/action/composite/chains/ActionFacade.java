package io.github.jspinak.brobot.action.composite.chains;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.special.UnknownState;
import io.github.jspinak.brobot.navigation.service.StateService;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Action.*;

/**
 * Provides a facade of commonly used action patterns for GUI automation.
 * 
 * @deprecated Use {@link ActionConfigFacade} instead. This class uses the deprecated
 *             ActionOptions API. The new ActionConfigFacade provides the same functionality
 *             with the modern ActionConfig architecture and better type safety.
 * <p>
 * This class encapsulates frequent action combinations into simple, reusable methods,
 * reducing code duplication and improving readability across automation scripts.
 * It serves as a higher-level abstraction over the core {@link Action} API, providing
 * convenience methods for common GUI interaction patterns like clicking, dragging,
 * and waiting for state changes.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Single-method calls for complex action sequences</li>
 *   <li>Pre-configured action options for common scenarios</li>
 *   <li>State-aware operations that leverage the state management system</li>
 *   <li>Consistent error handling through success/failure return values</li>
 * </ul>
 * 
 * <p>This class is designed to be extended or copied for application-specific
 * automation needs. Teams are encouraged to create their own CommonActions
 * implementations with methods tailored to their application's UI patterns.</p>
 * 
 * @see Action
 * @see ActionOptions
 * @see StateService
 */
@Component
@Deprecated
public class ActionFacade {

    private final Action action;
    private final StateService allStatesInProjectService;

    public ActionFacade(Action action, StateService allStatesInProjectService) {
        this.action = action;
        this.allStatesInProjectService = allStatesInProjectService;
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
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.CLICK)
                        .setMaxWait(maxWait)
                        .build(),
                stateImages).isSuccess();
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
        return action.perform(CLICK, new ObjectCollection.Builder().withLocations(location).build())
                .isSuccess();
    }

    /**
     * Repeatedly clicks an image until it disappears from the screen.
     * <p>
     * This method is useful for dismissing dialogs, clearing notifications, or
     * interacting with UI elements that require multiple clicks to disappear.
     * The operation stops either when the image vanishes or the maximum number
     * of clicks is reached.
     * 
     * @param timesToClick Maximum number of times to click the image
     * @param pauseBetweenClicks Delay in seconds between each click attempt
     * @param image The state image to click repeatedly
     * @return true if the image vanished before reaching the click limit, false otherwise
     */
    public boolean clickImageUntilItVanishes(int timesToClick, double pauseBetweenClicks, StateImage image) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(CLICK_UNTIL)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .setPauseBetweenActionSequences(pauseBetweenClicks)
                .setMaxTimesToRepeatActionSequence(timesToClick)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        return action.perform(actionOptions, objectCollection).isSuccess();
    }

    /**
     * Repeatedly right-clicks an image until it disappears, with mouse movement after each click.
     * <p>
     * Similar to {@link #clickImageUntilItVanishes} but uses right-click and moves the
     * mouse after each click. This is useful for context menu operations or when the
     * mouse needs to be moved away to properly detect if the element has vanished.
     * 
     * @param timesToClick Maximum number of times to right-click the image
     * @param pauseBetweenClicks Delay in seconds between each click attempt
     * @param image The state image to right-click repeatedly
     * @param xMove Horizontal offset in pixels to move the mouse after each click
     * @param yMove Vertical offset in pixels to move the mouse after each click
     * @return true if the image vanished before reaching the click limit, false otherwise
     */
    public boolean rightClickImageUntilItVanishes(int timesToClick, double pauseBetweenClicks,
                                                  StateImage image, int xMove, int yMove) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(CLICK_UNTIL)
                .setClickType(ClickOptions.Type.RIGHT)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .setPauseBetweenActionSequences(pauseBetweenClicks)
                .setMaxTimesToRepeatActionSequence(timesToClick)
                .setMoveMouseAfterAction(true)
                .setAddX(xMove)
                .setAddY(yMove)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        return action.perform(actionOptions, objectCollection).isSuccess();
    }

    /**
     * Performs a double-click on the first found state image.
     * 
     * @param maxWait Maximum time in seconds to wait for the image to appear
     * @param stateImages Variable number of state images to search for. The first
     *                    found image will be double-clicked.
     * @return true if an image was found and double-clicked successfully, false otherwise
     */
    public boolean doubleClick(double maxWait, StateImage... stateImages) {
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.CLICK)
                        .setMaxWait(maxWait)
                        .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                        .build(),
                stateImages).isSuccess();
    }

    /**
     * Performs a right-click on the first found state image.
     * 
     * @param maxWait Maximum time in seconds to wait for the image to appear
     * @param stateImages Variable number of state images to search for. The first
     *                    found image will be right-clicked.
     * @return true if an image was found and right-clicked successfully, false otherwise
     */
    public boolean rightClick(double maxWait, StateImage... stateImages) {
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.CLICK)
                        .setMaxWait(maxWait)
                        .setClickType(ClickOptions.Type.RIGHT)
                        .build(),
                stateImages).isSuccess();
    }

    /**
     * Waits for all specified images to disappear from the screen.
     * <p>
     * This method blocks until all images have vanished or the timeout is reached.
     * Useful for waiting for loading screens, animations, or temporary UI elements
     * to disappear before proceeding.
     * 
     * @param wait Maximum time in seconds to wait for all images to vanish
     * @param images Variable number of state images that should disappear
     * @return true if all images vanished within the timeout, false otherwise
     */
    public boolean waitVanish(double wait, StateImage... images) {
        ActionOptions vanish = new ActionOptions.Builder()
                .setAction(VANISH)
                .setMaxWait(wait)
                .build();
        return action.perform(vanish, images).isSuccess();
    }

    /**
     * Searches for a pattern on the screen without performing any action.
     * <p>
     * Converts the pattern to a state image in the null state before searching.
     * This is a convenience method for pattern-based searches.
     * 
     * @param maxWait Maximum time in seconds to search for the pattern
     * @param image The pattern to search for
     * @return true if the pattern was found, false otherwise
     */
    public boolean find(double maxWait, Pattern image) {
        return find(maxWait, image.inNullState());
    }

    /**
     * Searches for a state image on the screen without performing any action.
     * <p>
     * This method is useful for checking element presence without interaction,
     * often used in conditional logic or state verification.
     * 
     * @param maxWait Maximum time in seconds to search for the image
     * @param stateImage The state image to search for
     * @return true if the image was found, false otherwise
     */
    public boolean find(double maxWait, StateImage stateImage) {
        return action.perform(new ActionOptions.Builder().setMaxWait(maxWait).build(), stateImage)
                .isSuccess();
    }

    /**
     * Waits for a named state to be present or absent based on the action type.
     * <p>
     * This method retrieves the state by name from the state service and performs
     * the specified action (typically FIND or VANISH) on all images in that state.
     * The UNKNOWN state is handled as a special case that always returns true.
     * 
     * @param maxWait Maximum time in seconds to wait for the state condition
     * @param stateName Name of the state to wait for
     * @param actionType The action to perform (FIND to wait for presence, VANISH for absence)
     * @return true if the state condition was met within the timeout, false if the
     *         state doesn't exist or the condition wasn't met
     */
    public boolean waitState(double maxWait, String stateName, ActionOptions.Action actionType) {
        if (Objects.equals(stateName, UnknownState.Enum.UNKNOWN.toString())) return true;
        Optional<State> state = allStatesInProjectService.getState(stateName);
        if (state.isEmpty()) return false;
        return action.perform(
                new ActionOptions.Builder()
                        .setAction(actionType)
                        .setMaxWait(maxWait)
                        .build(),
                new ObjectCollection.Builder()
                        .withAllStateImages(state.get())
                        .build())
                .isSuccess();
    }

    /**
     * Waits for a named state to appear on the screen.
     * <p>
     * This method also prints diagnostic information about the state's probability
     * to the console for debugging purposes.
     * 
     * @param maxWait Maximum time in seconds to wait for the state to appear
     * @param stateName Name of the state to find
     * @return true if the state was found within the timeout, false otherwise
     * @see #waitState
     */
    public boolean findState(double maxWait, String stateName) {
        System.out.print("\n__findState:"+stateName+"__ ");
        allStatesInProjectService.getState(stateName).ifPresent(
                state -> System.out.println("prob="+state.getProbabilityExists()));
        return waitState(maxWait, stateName, FIND);
    }

    /**
     * Waits for a named state to disappear from the screen.
     * <p>
     * This method prints diagnostic information and waits for all images
     * associated with the state to vanish.
     * 
     * @param maxWait Maximum time in seconds to wait for the state to disappear
     * @param stateName Name of the state that should vanish
     * @return true if the state vanished within the timeout, false otherwise
     * @see #waitState
     */
    public boolean waitVanishState(double maxWait, String stateName) {
        System.out.print("\n__waitVanishState:"+stateName+"__ ");
        return waitState(maxWait, stateName, VANISH);
    }

    /**
     * Highlights a state region on the screen for visual debugging.
     * <p>
     * Delegates to {@link #highlightRegion(double, Region)} using the
     * state region's search region.
     * 
     * @param seconds Duration in seconds to display the highlight
     * @param region The state region to highlight
     */
    public void highlightRegion(double seconds, StateRegion region) {
        highlightRegion(seconds, region.getSearchRegion());
    }
    /**
     * Highlights a region on the screen for visual debugging.
     * <p>
     * This method draws a visible border around the specified region,
     * useful for debugging region calculations or demonstrating automation flow.
     * 
     * @param seconds Duration in seconds to display the highlight
     * @param region The region to highlight on screen
     */
    public void highlightRegion(double seconds, Region region) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.HIGHLIGHT)
                .setHighlightSeconds(seconds)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        action.perform(actionOptions, objectCollection);
    }

    /**
     * Repeatedly clicks an object until a specified state appears.
     * <p>
     * This method is useful for navigation scenarios where clicking a button
     * or link should eventually lead to a new state. The operation continues
     * until either the target state appears or the maximum click count is reached.
     * 
     * @param repeatClickTimes Maximum number of times to click the object
     * @param pauseBetweenClicks Delay in seconds between each click
     * @param objectToClick The image to click repeatedly
     * @param state Name of the state that should appear as a result of clicking
     * @return true if the state appeared before reaching the click limit,
     *         false if the state doesn't exist or didn't appear
     */
    public boolean clickUntilStateAppears(int repeatClickTimes, double pauseBetweenClicks,
                                          StateImage objectToClick, String state) {
        if (allStatesInProjectService.getState(state).isEmpty()) return false;
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
                .setMaxTimesToRepeatActionSequence(repeatClickTimes)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .build();
        ObjectCollection objectsToClick = new ObjectCollection.Builder()
                .withImages(objectToClick)
                .build();
        ObjectCollection objectsToAppear = new ObjectCollection.Builder()
                .withAllStateImages(allStatesInProjectService.getState(state).get())
                .build();
        return action.perform(actionOptions, objectsToClick, objectsToAppear).isSuccess();
    }

    /**
     * Repeatedly clicks one image until another image appears.
     * <p>
     * Similar to {@link #clickUntilStateAppears} but works with individual images
     * rather than states. Useful for simpler scenarios where full state detection
     * isn't necessary.
     * 
     * @param repeatClickTimes Maximum number of times to click the first image
     * @param pauseBetweenClicks Delay in seconds between each click
     * @param toClick The image to click repeatedly
     * @param toAppear The image that should appear as a result of clicking
     * @return true if the target image appeared before reaching the click limit,
     *         false otherwise
     */
    public boolean clickUntilImageAppears(int repeatClickTimes, double pauseBetweenClicks,
                                          StateImage toClick, StateImage toAppear) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
                .setMaxTimesToRepeatActionSequence(repeatClickTimes)
                .setPauseBetweenIndividualActions(pauseBetweenClicks)
                .build();
        ObjectCollection objectsToClick = new ObjectCollection.Builder()
                .withImages(toClick)
                .build();
        ObjectCollection objectsToAppear = new ObjectCollection.Builder()
                .withImages(toAppear)
                .build();
        return action.perform(actionOptions, objectsToClick, objectsToAppear).isSuccess();
    }

    /**
     * Performs a drag operation between two screen locations.
     * 
     * @param from Starting location of the drag operation
     * @param to Ending location of the drag operation
     * @return true if the drag was executed successfully, false otherwise
     */
    public boolean drag(Location from, Location to) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();
        ObjectCollection fromOC = new ObjectCollection.Builder()
                .withLocations(from)
                .build();
        ObjectCollection toOC = new ObjectCollection.Builder()
                .withLocations(to)
                .build();
        return action.perform(actionOptions, fromOC, toOC).isSuccess();
    }

    /**
     * Drags from a region's center to a position offset from that center.
     * <p>
     * This method calculates the drag endpoint by adding the offsets to the
     * region's center coordinates.
     * 
     * @param region The region whose center is the drag starting point
     * @param plusX Horizontal offset in pixels from the center
     * @param plusY Vertical offset in pixels from the center
     */
    public void dragCenterToOffset(Region region, int plusX, int plusY) {
        dragCenterOfRegion(region, region.sikuli().getCenter().x + plusX, region.sikuli().getCenter().y + plusY);
    }

    /**
     * Drags from a region's center to absolute screen coordinates.
     * 
     * @param region The region whose center is the drag starting point
     * @param toX Absolute X coordinate of the drag endpoint
     * @param toY Absolute Y coordinate of the drag endpoint
     */
    public void dragCenterOfRegion(Region region, int toX, int toY) {
        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();
        ObjectCollection from = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        ObjectCollection to = new ObjectCollection.Builder()
                .withLocations(new Location(region.sikuli().getCenter(), toX, toY))
                .build();
        action.perform(drag, from, to);
    }

    /**
     * Drags from a region's center to an offset position with a pause before release.
     * <p>
     * Similar to {@link #dragCenterToOffset} but includes a pause before releasing
     * the mouse button, useful for drag operations that require a hold period.
     * 
     * @param region The region whose center is the drag starting point
     * @param plusX Horizontal offset in pixels from the center
     * @param plusY Vertical offset in pixels from the center
     */
    public void dragCenterToOffsetStop(Region region, int plusX, int plusY) {
        dragCenterStop(region,region.sikuli().getCenter().x + plusX, region.sikuli().getCenter().y + plusY);
    }

    /**
     * Drags from a region's center to a location with a pause before release.
     * 
     * @param region The region whose center is the drag starting point
     * @param location The location object containing the drag endpoint
     */
    public void dragCenterStop(Region region, Location location) {
        dragCenterStop(region, location.getCalculatedX(), location.getCalculatedY());
    }

    /**
     * Drags from a region's center to absolute coordinates with a pause before release.
     * <p>
     * The 0.8 second pause before mouse release helps ensure the drag operation
     * is recognized by applications that require a minimum hold duration.
     * 
     * @param region The region whose center is the drag starting point
     * @param toX Absolute X coordinate of the drag endpoint
     * @param toY Absolute Y coordinate of the drag endpoint
     */
    public void dragCenterStop(Region region, int toX, int toY) {
        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setPauseBeforeMouseUp(.8)
                .build();
        ObjectCollection from = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        ObjectCollection to = new ObjectCollection.Builder()
                .withLocations(new Location(region.sikuli().getCenter(), toX, toY))
                .build();
        action.perform(drag, from, to);
    }

    /**
     * Extracts text from a region using OCR.
     * <p>
     * Converts the region to a state region in the null state before extraction.
     * 
     * @param region The region from which to extract text
     * @return The extracted text, or empty string if extraction fails
     */
    public String getText(Region region) {
        return getText(region.inNullState());
    }

    /**
     * Extracts text from a state region using OCR with retry logic.
     * <p>
     * This method attempts text extraction up to 3 times with 0.5 second pauses
     * between attempts, improving reliability for dynamic or slow-loading text.
     * 
     * @param region The state region from which to extract text
     * @return The extracted text, or empty string if all attempts fail
     */
    public String getText(StateRegion region) {
        ActionOptions getText = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .getTextUntil(ActionOptions.GetTextUntil.TEXT_APPEARS)
                .setMaxTimesToRepeatActionSequence(3)
                .setPauseBetweenActionSequences(.5)
                .build();
        ObjectCollection textRegion = new ObjectCollection.Builder()
                .withRegions(region)
                .build();
        return action.perform(getText, textRegion).getSelectedText();
    }

    /**
     * Clicks a state image multiple times with pauses between clicks.
     * 
     * @param times Number of times to click the image
     * @param pause Delay in seconds between each click
     * @param objectToClick The state image to click
     * @return true if all clicks were executed successfully, false otherwise
     */
    public boolean clickXTimes(int times, double pause, StateImage objectToClick) {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(CLICK)
                .setTimesToRepeatIndividualAction(times)
                .setPauseBetweenIndividualActions(pause)
                .build();
        return action.perform(click, objectToClick).isSuccess();
    }

    /**
     * Clicks a region multiple times with pauses between clicks.
     * 
     * @param times Number of times to click the region
     * @param pause Delay in seconds between each click
     * @param reg The region to click
     * @return true if all clicks were executed successfully, false otherwise
     */
    public boolean clickXTimes(int times, double pause, Region reg) {
        ActionOptions click = new ActionOptions.Builder()
                .setAction(CLICK)
                .setTimesToRepeatIndividualAction(times)
                .setPauseBetweenIndividualActions(pause)
                .build();
        ObjectCollection regs = new ObjectCollection.Builder()
                .withRegions(reg)
                .build();
        return action.perform(click, regs).isSuccess();
    }

    /**
     * Moves the mouse cursor to a specific screen location.
     * <p>
     * This method only moves the cursor without clicking or other interactions.
     * 
     * @param location The target location for the mouse cursor
     * @return true if the movement was executed successfully, false otherwise
     */
    public boolean moveMouseTo(Location location) {
        ActionOptions move = new ActionOptions.Builder()
                .setAction(MOVE)
                .build();
        ObjectCollection loc = new ObjectCollection.Builder()
                .withLocations(location)
                .build();
        return action.perform(move, loc).isSuccess();
    }

    /**
     * Performs a swipe gesture from one corner of a region to the opposite corner.
     * <p>
     * This method is useful for swipe-based UI interactions like dismissing cards,
     * revealing menus, or scrolling content. The swipe starts from the specified
     * position and moves to the opposite corner of the region.
     * 
     * @param region The region within which to perform the swipe
     * @param grabPosition The starting corner position (e.g., TOPLEFT, BOTTOMRIGHT)
     * @return true if the swipe was executed successfully, false otherwise
     */
    public boolean swipeToOppositePosition(Region region, Position grabPosition) {
        Location grabLocation = new Location(region, grabPosition);
        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setPauseAfterEnd(.5)
                .build();
        ObjectCollection from = new ObjectCollection.Builder()
                .withLocations(grabLocation)
                .build();
        ObjectCollection to = new ObjectCollection.Builder()
                .withLocations(grabLocation.getOpposite())
                .build();
        return action.perform(drag, from, to).isSuccess();
    }

    /**
     * Types the specified text at the current focus location.
     * 
     * @param str The text to type
     */
    public void type(String str) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(TYPE)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withStrings(str)
                .build();
        action.perform(actionOptions, objectCollection);
    }

    /**
     * Types text with a modifier key held down.
     * <p>
     * This method is useful for keyboard shortcuts that involve typing
     * while holding modifier keys.
     * 
     * @param str The text to type
     * @param modifier The modifier key to hold (e.g., "CTRL", "SHIFT", "ALT")
     */
    public void type(String str, String modifier) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(TYPE)
                .setModifiers(modifier)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withStrings(str)
                .build();
        action.perform(actionOptions, objectCollection);
    }

    /**
     * Holds down a key for a specified duration then releases all keys.
     * <p>
     * This method automatically releases all keys after the hold duration
     * to ensure no keys remain stuck.
     * 
     * @param str The key to hold down
     * @param seconds Duration in seconds to hold the key
     */
    public void holdKey(String str, double seconds) {
        ActionOptions holdKey = new ActionOptions.Builder()
                .setAction(KEY_DOWN)
                .setPauseAfterEnd(seconds)
                .build();
        ObjectCollection key = new ObjectCollection.Builder()
                .withStrings(str)
                .build();
        action.perform(holdKey, key);
        releaseKeys();
    }

    /**
     * Releases all currently pressed keys.
     * <p>
     * This is a safety method to ensure no keys remain in a pressed state,
     * which could interfere with subsequent operations or user interaction.
     */
    public void releaseKeys() {
        ActionOptions releaseKey = new ActionOptions.Builder()
                .setAction(KEY_UP)
                .build();
        ObjectCollection allKeys = new ObjectCollection.Builder()
                .build();
        action.perform(releaseKey, allKeys);
    }

}
