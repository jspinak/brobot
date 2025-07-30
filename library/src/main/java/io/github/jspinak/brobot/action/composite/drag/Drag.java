package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;


/**
 * Performs drag-and-drop operations between GUI elements in the Brobot model-based automation framework.
 * 
 * <p>Drag is a composite action in the Action Model (α) that combines multiple basic actions 
 * to implement complex drag-and-drop interactions. It seamlessly handles dragging between 
 * various target types including visually identified elements (Image Matches), screen areas 
 * (Regions), and specific coordinates (Locations), providing a unified interface for all 
 * drag operations.</p>
 * 
 * <p>Architecture:
 * <ul>
 *   <li><b>Composite Design</b>: Combines Find actions with platform-specific drag operations</li>
 *   <li><b>Two-phase Execution</b>: First finds the 'from' location, then the 'to' location</li>
 *   <li><b>Flexible Targeting</b>: Supports Image Matches, Regions, and Locations for both 
 *       source and destination</li>
 *   <li><b>Result Tracking</b>: Returns a defined region representing the drag path</li>
 * </ul>
 * </p>
 * 
 * <p>Supported drag patterns:
 * <ul>
 *   <li>Image to Image: Drag from one visual element to another</li>
 *   <li>Image to Region: Drag from a visual element to a screen area</li>
 *   <li>Location to Location: Precise coordinate-based dragging</li>
 *   <li>Any combination of Image Match, Region, or Location</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Moving files between folders in file managers</li>
 *   <li>Rearranging items in lists or grids</li>
 *   <li>Drawing or selecting areas in graphics applications</li>
 *   <li>Adjusting sliders or other draggable UI controls</li>
 *   <li>Drag-based gesture interactions in modern applications</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation details:
 * <ul>
 *   <li>Uses ObjectCollection #1 for the 'from' location</li>
 *   <li>Uses ObjectCollection #2 for the 'to' location</li>
 *   <li>Returns matches for the 'to' location, not the 'from' location</li>
 *   <li>Adds a DefinedRegion to matches with drag path coordinates</li>
 *   <li>Supports offset adjustments for precise positioning</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Drag actions represent high-level GUI interactions that 
 * would otherwise require multiple coordinated low-level actions. By encapsulating the 
 * complexity of drag-and-drop operations, the framework enables more natural and maintainable 
 * automation scripts that closely mirror human interactions with the GUI.</p>
 * 
 * @since 1.0
 * @see Find
 * @see DragCoordinateCalculator
 * @see GetDragLocation
 * @see ActionOptions
 * @see ObjectCollection
 */
@Component
public class Drag implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.DRAG;
    }

    private final ActionChainExecutor actionChainExecutor;

    public Drag(ActionChainExecutor actionChainExecutor) {
        this.actionChainExecutor = actionChainExecutor;
    }

    /**
     * Executes a drag-and-drop operation between two GUI elements or locations.
     * <p>
     * This method orchestrates a complete drag operation by:
     * <ol>
     *   <li>Finding the source location from the first ObjectCollection</li>
     *   <li>Finding the destination location from the second ObjectCollection</li>
     *   <li>Applying any configured offsets to both locations</li>
     *   <li>Performing the platform-specific drag operation</li>
     *   <li>Recording the drag path as a DefinedRegion in the results</li>
     * </ol>
     * 
     * <p><b>Important behaviors:</b>
     * <ul>
     *   <li>Only matches from the 'to' location are returned in the ActionResult</li>
     *   <li>The 'from' location matches are used internally but not returned</li>
     *   <li>A DefinedRegion is added with the drag path coordinates:
     *       <ul>
     *         <li>x,y = drag start (from) location</li>
     *         <li>width,height = delta to drag end (to) location</li>
     *       </ul>
     *   </li>
     *   <li>If either location cannot be found, the operation silently fails</li>
     * </ul>
     * 
     * <p><b>ActionOptions used:</b>
     * <ul>
     *   <li>Find options: similarity, search regions, offsets</li>
     *   <li>Drag options: timing delays, drag speed</li>
     *   <li>Offset adjustments: addX, addY for both locations</li>
     * </ul>
     * 
     * @param matches The ActionResult containing configuration options. Modified with
     *                results from the 'to' location and a DefinedRegion representing
     *                the drag path.
     * @param objectCollections Requires exactly 2 collections:
     *                          [0] = source elements to drag from,
     *                          [1] = destination elements to drag to
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Validate we have the required object collections
        if (objectCollections.length < 2) {
            matches.setSuccess(false);
            return;
        }
        
        // Get the configuration
        ActionConfig config = matches.getActionConfig();
        DragOptions dragOptions = (config instanceof DragOptions) ? 
            (DragOptions) config : new DragOptions.Builder().build();
        
        // Extract source and target collections
        ObjectCollection sourceCollection = objectCollections[0];
        ObjectCollection targetCollection = objectCollections[1];
        
        // Build the 6-action chain: Find source → Find target → MouseMove to source → 
        // MouseDown → MouseMove to target → MouseUp
        
        // Step 1: Find source
        PatternFindOptions findSourceOptions = new PatternFindOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build();
        
        // Step 2: Find target (will store both results)
        PatternFindOptions findTargetOptions = new PatternFindOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build();
        
        // Step 3: Move to source
        MouseMoveOptions moveToSourceOptions = new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build();
        
        // Step 4: Mouse down at source
        MouseDownOptions mouseDownOptions = new MouseDownOptions.Builder()
            .setPressOptions(dragOptions.getMousePressOptions().toBuilder().build())
            .setPauseAfterEnd(dragOptions.getDelayBetweenMouseDownAndMove())
            .build();
        
        // Step 5: Move to target (while holding mouse down)
        MouseMoveOptions moveToTargetOptions = new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build();
        
        // Step 6: Mouse up at target
        MouseUpOptions mouseUpOptions = new MouseUpOptions.Builder()
            .setPressOptions(dragOptions.getMousePressOptions().toBuilder().build())
            .setPauseAfterEnd(dragOptions.getDelayAfterDrag())
            .build();
        
        // Create the action chain
        ActionChainOptions chainOptions = new ActionChainOptions.Builder(findSourceOptions)
            .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
            .then(findTargetOptions)
            .then(moveToSourceOptions)
            .then(mouseDownOptions)
            .then(moveToTargetOptions)
            .then(mouseUpOptions)
            .build();
        
        // Execute the chain
        ActionResult result = actionChainExecutor.executeChain(chainOptions, matches, 
            sourceCollection, targetCollection);
        
        // Copy results back to the provided matches object
        matches.setMatchList(result.getMatchList());
        matches.setSuccess(result.isSuccess());
        matches.setDuration(result.getDuration());
        
        // Add the movement if successful
        if (result.isSuccess() && result.getMatchList().size() >= 2) {
            Location startLoc = result.getMatchList().get(0).getTarget();
            Location endLoc = result.getMatchList().get(result.getMatchList().size() - 1).getTarget();
            Movement dragMovement = new Movement(startLoc, endLoc);
            matches.addMovement(dragMovement);
        }
    }
}
