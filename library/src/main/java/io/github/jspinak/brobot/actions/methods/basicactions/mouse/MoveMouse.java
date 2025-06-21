package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.report.Report;
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
 * <p>Movement patterns supported:
 * <ul>
 *   <li><b>Single Target</b>: Direct movement to a specific location</li>
 *   <li><b>Multiple Targets</b>: Sequential movement through multiple locations</li>
 *   <li><b>Pattern-based</b>: Movement to visually identified elements</li>
 *   <li><b>Collection-based</b>: Processing multiple ObjectCollections in sequence</li>
 * </ul>
 * </p>
 * 
 * <p>Processing order:
 * <ul>
 *   <li>Within an ObjectCollection: Points are visited as recorded by Find operations 
 *       (Images → Matches → Regions → Locations)</li>
 *   <li>Between ObjectCollections: Processed in the order they appear in the parameters</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Visual Integration</b>: Uses Find to locate targets before movement</li>
 *   <li><b>Batch Processing</b>: Can move through multiple locations in one action</li>
 *   <li><b>Timing Control</b>: Configurable pauses between movements</li>
 *   <li><b>State Awareness</b>: Updates framework's understanding of cursor position</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Hovering over elements to trigger tooltips or dropdown menus</li>
 *   <li>Positioning cursor for subsequent click or drag operations</li>
 *   <li>Following paths through UI elements for gesture-based interactions</li>
 *   <li>Moving mouse away from active areas to prevent interference</li>
 * </ul>
 * </p>
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
 * @see ActionOptions
 */
@Component
public class MoveMouse implements ActionInterface {

    private final Find find;
    private final MoveMouseWrapper moveMouseWrapper;
    private final Time time;

    public MoveMouse(Find find, MoveMouseWrapper moveMouseWrapper, Time time) {
        this.find = find;
        this.moveMouseWrapper = moveMouseWrapper;
        this.time = time;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        for (ObjectCollection objColl : collections) {
            find.perform(matches, objColl);
            matches.getMatchLocations().forEach(moveMouseWrapper::move);
            Report.print("finished move. ");
            if (collections.indexOf(objColl) < collections.size() - 1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }



}
