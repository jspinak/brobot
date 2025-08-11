package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Moves the mouse to one or more locations in the Brobot model-based GUI automation framework.
 * 
 * <p>MoveMouse is a fundamental action in the Action Model (α) that provides precise cursor 
 * control for GUI automation. It bridges the gap between visual element identification and 
 * physical mouse positioning, enabling complex interaction patterns including hover effects, 
 * drag operations, and tooltip activation.</p>
 * 
 * <p>Movement patterns supported:</p>
 * <ul>
 *   <li><b>Single Target</b>: Direct movement to a specific location</li>
 *   <li><b>Multiple Targets</b>: Sequential movement through multiple locations</li>
 *   <li><b>Pattern-based</b>: Movement to visually identified elements</li>
 *   <li><b>Collection-based</b>: Processing multiple ObjectCollections in sequence</li>
 * </ul>
 * 
 * <p>Processing order:</p>
 * <ul>
 *   <li>Within an ObjectCollection: Points are visited as recorded by Find operations 
 *       (Images → Matches → Regions → Locations)</li>
 *   <li>Between ObjectCollections: Processed in the order they appear in the parameters</li>
 * </ul>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Visual Integration</b>: Uses Find to locate targets before movement</li>
 *   <li><b>Batch Processing</b>: Can move through multiple locations in one action</li>
 *   <li><b>Timing Control</b>: Configurable pauses between movements</li>
 *   <li><b>State Awareness</b>: Updates framework's understanding of cursor position</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Hovering over elements to trigger tooltips or dropdown menus</li>
 *   <li>Positioning cursor for subsequent click or drag operations</li>
 *   <li>Following paths through UI elements for gesture-based interactions</li>
 *   <li>Moving mouse away from active areas to prevent interference</li>
 * </ul>
 * 
 * <p>In the model-based approach, MoveMouse actions contribute to the framework's spatial 
 * understanding of the GUI. By tracking cursor movements, the framework can model hover 
 * states, anticipate UI reactions, and optimize subsequent actions based on current 
 * cursor position.</p>
 * 
 * @since 1.0
 * @see Find
 * @see Click
 * @see MouseDown
 * @see MouseMoveOptions
 */
@Component
public class MoveMouse implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.MOVE;
    }

    private final Find find;
    private final MoveMouseWrapper moveMouseWrapper;
    private final TimeProvider time;

    public MoveMouse(Find find, MoveMouseWrapper moveMouseWrapper, TimeProvider time) {
        this.find = find;
        this.moveMouseWrapper = moveMouseWrapper;
        this.time = time;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - MouseMoveOptions or any ActionConfig is acceptable
        // since MoveMouse mainly uses Find and basic timing
        ActionConfig config = matches.getActionConfig();
        
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        for (ObjectCollection objColl : collections) {
            // Check if we have locations directly - no need to find anything
            if (objColl.getStateLocations() != null && !objColl.getStateLocations().isEmpty()) {
                // Move directly to the locations without finding
                objColl.getStateLocations().forEach(stateLocation -> {
                    moveMouseWrapper.move(stateLocation.getLocation());
                    matches.getMatchLocations().add(stateLocation.getLocation());
                });
            } else if (objColl.getStateRegions() != null && !objColl.getStateRegions().isEmpty()) {
                // Move to center of regions without finding
                objColl.getStateRegions().forEach(stateRegion -> {
                    moveMouseWrapper.move(stateRegion.getSearchRegion().getLocation());
                    matches.getMatchLocations().add(stateRegion.getSearchRegion().getLocation());
                });
            } else {
                // Only use find if we have images/patterns to search for
                find.perform(matches, objColl);
                matches.getMatchLocations().forEach(moveMouseWrapper::move);
            }
            ConsoleReporter.print("finished move. ");
            
            // Pause between collections if there are more to process
            if (collections.indexOf(objColl) < collections.size() - 1) {
                // Use pause from config if available, otherwise use default
                double pauseDuration = config.getPauseAfterEnd();
                if (pauseDuration > 0) {
                    time.wait(pauseDuration);
                }
            }
        }
    }



}
