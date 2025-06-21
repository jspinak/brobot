package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.OffsetOps;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.DragLocation;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
 * @see DragLocation
 * @see GetDragLocation
 * @see ActionOptions
 * @see ObjectCollection
 */
@Component
public class Drag implements ActionInterface {

    private final DragLocation dragLocation;
    private final GetDragLocation getDragLocation;
    private final OffsetOps offsetOps;
    private final MatchesInitializer matchesInitializer;

    public Drag(DragLocation dragLocation, GetDragLocation getDragLocation, OffsetOps offsetOps,
                MatchesInitializer matchesInitializer) {
        this.dragLocation = dragLocation;
        this.getDragLocation = getDragLocation;
        this.offsetOps = offsetOps;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * The two Actions used are Find and Drag.
     * Find is used twice, once for the 'from' Match and once for the 'to' Match.
     * Matches with the 'to' Match. The 'from' Match is not returned. It additionally
     *   returns a DefinedRegion with x,y as the DragFrom Location and x2,y2 as the
     *   DragTo Location.
     *
     * @param matches has mostly options for Drag but also a few options for Find
     * @param objectCollections ObjectCollection #1 for the 'from' Match, and #2 for the 'to' Match.
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        matches = matchesInitializer.init(actionOptions, objectCollections);
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(matches, objectCollections);
        Optional<Location> optEndLoc = getDragLocation.getToLocation(matches, objectCollections);
        offsetOps.addOffset(List.of(objectCollections), matches, actionOptions);
        if (optStartLoc.isEmpty() || optEndLoc.isEmpty()) return;
        dragLocation.drag(optStartLoc.get(), optEndLoc.get(), actionOptions);
        matches.addDefinedRegion(new Region(
                optStartLoc.get().getCalculatedX(), optStartLoc.get().getCalculatedY(),
                optEndLoc.get().getCalculatedX() - optStartLoc.get().getCalculatedX(),
                optEndLoc.get().getCalculatedY() - optStartLoc.get().getCalculatedY()));
    }

}
