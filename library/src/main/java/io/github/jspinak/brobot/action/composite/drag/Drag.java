package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.mouse.MouseDown;
import io.github.jspinak.brobot.action.basic.mouse.MouseUp;
import io.github.jspinak.brobot.action.basic.mouse.MoveMouse;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateLocation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

/**
 * Pure implementation of drag-and-drop operations for GUI automation.
 * <p>
 * This class provides a pure drag action that performs mouse drag operations
 * between provided locations. Unlike composite actions, it does NOT perform
 * any Find operations - it expects the source and target locations to be
 * provided directly through ObjectCollections.
 * </p>
 * 
 * <p><b>Usage Pattern:</b></p>
 * <pre>{@code
 * // Option 1: Direct drag between known locations
 * ObjectCollection source = new ObjectCollection.Builder()
 *     .withLocations(new Location(100, 100))
 *     .build();
 * ObjectCollection target = new ObjectCollection.Builder()
 *     .withLocations(new Location(300, 300))
 *     .build();
 * 
 * DragOptions options = new DragOptions.Builder().build();
 * ActionResult result = action.perform(options, source, target);
 * 
 * // Option 2: Find elements first, then drag between them
 * ActionResult sourceResult = action.find(sourceImage);
 * ActionResult targetResult = action.find(targetImage);
 * 
 * if (sourceResult.isSuccess() && targetResult.isSuccess()) {
 *     ObjectCollection sourceCol = new ObjectCollection.Builder()
 *         .withLocations(sourceResult.getMatchList().get(0).getTarget())
 *         .build();
 *     ObjectCollection targetCol = new ObjectCollection.Builder()
 *         .withLocations(targetResult.getMatchList().get(0).getTarget())
 *         .build();
 *     
 *     action.perform(dragOptions, sourceCol, targetCol);
 * }
 * 
 * // Option 3: Use ActionChainOptions for find-then-drag workflow
 * ActionChainOptions chain = new ActionChainOptions.Builder(
 *     new PatternFindOptions.Builder().build())
 *     .then(new PatternFindOptions.Builder().build())
 *     .then(new DragOptions.Builder().build())
 *     .build();
 * }</pre>
 * 
 * <p>This pure implementation follows the Single Responsibility Principle:
 * it only handles the drag operation itself, not the finding of elements.
 * This design eliminates circular dependencies and makes the action more
 * composable with other actions.</p>
 * 
 * @since 2.0
 * @see MouseDown
 * @see MoveMouse
 * @see MouseUp
 * @see DragOptions
 */
@Component
public class Drag implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.DRAG;
    }

    private final MouseDown mouseDown;
    private final MoveMouse moveMouse;
    private final MouseUp mouseUp;

    /**
     * Constructs a Drag action with required mouse operation dependencies.
     * 
     * @param mouseDown Service for pressing mouse buttons
     * @param moveMouse Service for moving the mouse cursor
     * @param mouseUp Service for releasing mouse buttons
     */
    public Drag(MouseDown mouseDown, MoveMouse moveMouse, MouseUp mouseUp) {
        this.mouseDown = mouseDown;
        this.moveMouse = moveMouse;
        this.mouseUp = mouseUp;
    }

    /**
     * Executes a drag-and-drop operation between provided locations.
     * <p>
     * This method performs a pure drag operation by:
     * <ol>
     *   <li>Extracting the source location from the first ObjectCollection</li>
     *   <li>Extracting the target location from the second ObjectCollection</li>
     *   <li>Moving to the source location</li>
     *   <li>Pressing the mouse button</li>
     *   <li>Moving to the target location (while holding the button)</li>
     *   <li>Releasing the mouse button</li>
     * </ol>
     * 
     * <p><b>Important behaviors:</b>
     * <ul>
     *   <li>Requires exactly 2 ObjectCollections with at least one location each</li>
     *   <li>Uses the first location from each collection</li>
     *   <li>Records the drag path as a Movement in the results</li>
     *   <li>Returns matches for both source and target locations</li>
     * </ul>
     * 
     * @param actionResult The ActionResult to populate with execution results
     * @param objectCollections Requires exactly 2 collections:
     *                          [0] = source location to drag from,
     *                          [1] = target location to drag to
     */
    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        // Validate we have the required object collections
        if (objectCollections == null || objectCollections.length < 2) {
            actionResult.setSuccess(false);
            return;
        }
        
        // Extract source and target locations
        Location sourceLocation = extractFirstLocation(objectCollections[0]);
        Location targetLocation = extractFirstLocation(objectCollections[1]);
        
        if (sourceLocation == null || targetLocation == null) {
            actionResult.setSuccess(false);
            return;
        }
        
        // Get drag configuration
        DragOptions dragOptions = extractDragOptions(actionResult.getActionConfig());
        
        // Step 1: Move to source location
        MouseMoveOptions moveToSourceOptions = new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build();
        
        ActionResult moveToSourceResult = new ActionResult();
        moveToSourceResult.setActionConfig(moveToSourceOptions);
        StateLocation sourceStateLocation = new StateLocation();
        sourceStateLocation.setLocation(sourceLocation);
        ObjectCollection sourceCol = new ObjectCollection.Builder()
            .withLocations(sourceStateLocation)
            .build();
        moveMouse.perform(moveToSourceResult, sourceCol);
        
        if (!moveToSourceResult.isSuccess()) {
            actionResult.setSuccess(false);
            return;
        }
        
        // Step 2: Press mouse button at source
        MouseDownOptions mouseDownOptions = new MouseDownOptions.Builder()
            .setPressOptions(dragOptions.getMousePressOptions())
            .setPauseAfterEnd(dragOptions.getDelayBetweenMouseDownAndMove())
            .build();
        
        ActionResult mouseDownResult = new ActionResult();
        mouseDownResult.setActionConfig(mouseDownOptions);
        mouseDown.perform(mouseDownResult, sourceCol);
        
        if (!mouseDownResult.isSuccess()) {
            actionResult.setSuccess(false);
            return;
        }
        
        // Step 3: Move to target location (while holding mouse button)
        MouseMoveOptions moveToTargetOptions = new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build();
        
        ActionResult moveToTargetResult = new ActionResult();
        moveToTargetResult.setActionConfig(moveToTargetOptions);
        StateLocation targetStateLocation = new StateLocation();
        targetStateLocation.setLocation(targetLocation);
        ObjectCollection targetCol = new ObjectCollection.Builder()
            .withLocations(targetStateLocation)
            .build();
        moveMouse.perform(moveToTargetResult, targetCol);
        
        if (!moveToTargetResult.isSuccess()) {
            // Try to release mouse button even if move failed
            mouseUp.perform(new ActionResult(), new ObjectCollection.Builder().build());
            actionResult.setSuccess(false);
            return;
        }
        
        // Step 4: Release mouse button at target
        MouseUpOptions mouseUpOptions = new MouseUpOptions.Builder()
            .setPressOptions(dragOptions.getMousePressOptions())
            .setPauseAfterEnd(dragOptions.getDelayAfterDrag())
            .build();
        
        ActionResult mouseUpResult = new ActionResult();
        mouseUpResult.setActionConfig(mouseUpOptions);
        mouseUp.perform(mouseUpResult, targetCol);
        
        // Set overall success
        actionResult.setSuccess(mouseUpResult.isSuccess());
        
        // Add matches for source and target locations
        if (actionResult.isSuccess()) {
            // Create matches for the locations
            Match sourceMatch = new Match();
            sourceMatch.setTarget(sourceLocation);
            sourceMatch.setScore(1.0);
            
            Match targetMatch = new Match();
            targetMatch.setTarget(targetLocation);
            targetMatch.setScore(1.0);
            
            List<Match> matches = new ArrayList<>();
            matches.add(sourceMatch);
            matches.add(targetMatch);
            actionResult.setMatchList(matches);
            
            // Record the drag movement
            Movement dragMovement = new Movement(sourceLocation, targetLocation);
            actionResult.addMovement(dragMovement);
        }
    }
    
    /**
     * Extracts the first location from an ObjectCollection.
     * Checks StateLocations, then StateRegions, then matches.
     * 
     * @param collection The ObjectCollection to extract from
     * @return The first location found, or null if none available
     */
    private Location extractFirstLocation(ObjectCollection collection) {
        if (collection == null) {
            return null;
        }
        
        // Check for StateLocations
        if (collection.getStateLocations() != null && !collection.getStateLocations().isEmpty()) {
            return collection.getStateLocations().get(0).getLocation();
        }
        
        // Check for StateRegions (use region's location)
        if (collection.getStateRegions() != null && !collection.getStateRegions().isEmpty()) {
            Region region = collection.getStateRegions().get(0).getSearchRegion();
            // Get the center of the region
            int centerX = region.getX() + region.getW() / 2;
            int centerY = region.getY() + region.getH() / 2;
            return new Location(centerX, centerY);
        }
        
        // Check for matches (previous ActionResults)
        if (collection.getMatches() != null && !collection.getMatches().isEmpty()) {
            ActionResult firstMatch = collection.getMatches().get(0);
            if (firstMatch.getMatchList() != null && !firstMatch.getMatchList().isEmpty()) {
                return firstMatch.getMatchList().get(0).getTarget();
            }
        }
        
        return null;
    }
    
    /**
     * Extracts DragOptions from the ActionConfig, or creates default options.
     * 
     * @param config The ActionConfig from the ActionResult
     * @return DragOptions to use for the operation
     */
    private DragOptions extractDragOptions(ActionConfig config) {
        if (config instanceof DragOptions) {
            return (DragOptions) config;
        }
        return new DragOptions.Builder().build();
    }
}