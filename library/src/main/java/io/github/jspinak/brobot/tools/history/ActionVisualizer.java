package io.github.jspinak.brobot.tools.history;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.history.draw.DrawRect;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.action.ActionResult;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Core drawing utilities for visualizing action results on screenshots.
 * <p>
 * This class provides fundamental drawing operations for Brobot's visual history system,
 * enabling clear visualization of automated actions like clicks, drags, and region
 * definitions. It serves as the primary drawing interface for action illustrations,
 * working with various visualization layers to create comprehensive visual reports.
 * <p>
 * Key visualization types:
 * <ul>
 * <li>Rectangle boundaries around matched regions</li>
 * <li>Circular points indicating click locations</li>
 * <li>Arrows showing drag operations and movements</li>
 * <li>Highlighted defined regions for context</li>
 * </ul>
 * <p>
 * Drawing conventions:
 * <ul>
 * <li>Click points: Filled circle with concentric rings for visibility</li>
 * <li>Drag operations: Arrows connecting sequential match locations</li>
 * <li>Rectangles: 1-pixel border offset for better visibility</li>
 * <li>Colors: Configurable via Scalar parameters (BGR format)</li>
 * </ul>
 * <p>
 * Integration with the illustration system:
 * <ul>
 * <li>Works directly with {@link Visualization} for layered visualizations</li>
 * <li>Draws on specific layers (matchesOnScene) for proper compositing</li>
 * <li>Coordinates with {@link DrawRect} for consistent rectangle rendering</li>
 * <li>Supports both individual matches and action result collections</li>
 * </ul>
 * <p>
 * Thread safety: This component is stateless except for the injected DrawRect
 * dependency, making it safe for concurrent use in visualization pipelines.
 *
 * @see Visualization
 * @see DrawRect
 * @see ActionResult
 * @see Match
 */
@Component
public class ActionVisualizer {

    private final DrawRect drawRect;

    public ActionVisualizer(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    /**
     * Draws a rectangle around a matched region with boundary safety.
     * <p>
     * Creates a visible rectangle with a 1-pixel offset from the match boundaries
     * to ensure the outline doesn't overlap with the matched content. The method
     * includes boundary checks to prevent drawing outside the screen dimensions.
     * <p>
     * Boundary handling:
     * <ul>
     * <li>Adds 1-pixel padding around the match for visibility</li>
     * <li>Clamps coordinates to screen boundaries (0 to width/height)</li>
     * <li>Ensures rectangle stays within valid Mat dimensions</li>
     * </ul>
     *
     * @param screen target Mat to draw on; not modified if match is invalid
     * @param match the match region to outline; must have valid position
     * @param color BGR color for the rectangle outline
     */
    public void drawRect(Mat screen, Match match, Scalar color) {
        int drawX = Math.max(0, match.x()-1);
        int drawY = Math.max(0, match.y()-1);
        int drawX2 = Math.min(screen.cols(), match.x() + match.w() + 1);
        int drawY2 = Math.min(screen.rows(), match.y() + match.h() + 1);
        Rect aroundMatch = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(screen, aroundMatch, color);
    }

    /**
     * Draws a multi-ring circular indicator at a match's target location.
     * <p>
     * Creates a distinctive visual marker consisting of:
     * <ul>
     * <li>Inner filled circle (radius 6) in the specified color</li>
     * <li>Middle white ring (radius 8) for contrast</li>
     * <li>Outer white ring (radius 10) for additional visibility</li>
     * </ul>
     * This three-ring design ensures visibility against various backgrounds
     * and clearly indicates precise click or interaction points.
     *
     * @param screen target Mat to draw on; modified in place
     * @param match match containing the target location to mark
     * @param color BGR color for the inner filled circle
     */
    public void drawPoint(Mat screen, Match match, Scalar color) {
        Location loc = match.getTarget();
        Point center = new Point(loc.getCalculatedX(), loc.getCalculatedY());
        circle(screen, center, 6, color, FILLED, LINE_8, 0); //fill
        circle(screen, center, 8, new Scalar(255));
        circle(screen, center, 10, new Scalar(255));
    }

    /**
     * Visualizes all click actions from an action result.
     * <p>
     * Draws circular indicators at each click location in the match list,
     * using a distinctive pink color (BGR: 255, 150, 255) for visibility.
     * Each click is logged with its coordinates for debugging purposes.
     * <p>
     * The clicks are drawn on the matchesOnScene layer to properly overlay
     * on the screenshot while preserving other visualization layers.
     *
     * @param illustrations container holding visualization layers; matchesOnScene layer is modified
     * @param matches action results containing click locations to visualize
     */
    public void drawClick(Visualization illustrations, ActionResult matches) {
        for (Match match : matches.getMatchList()) {
            ConsoleReporter.println("Drawing click on " + match.getTarget().getCalculatedX() + ", " + match.getTarget().getCalculatedY());
            drawPoint(illustrations.getMatchesOnScene(), match, new Scalar(255, 150, 255, 0));
        }
    }

    /**
     * Draws an arrow between two locations indicating direction of movement.
     * <p>
     * Creates a thick arrow with customized parameters for clear visibility:
     * <ul>
     * <li>Line thickness: 5 pixels for prominence</li>
     * <li>Line type: 8-connected line for smooth appearance</li>
     * <li>Arrow tip length: 4% of the line length for proportional sizing</li>
     * </ul>
     * Used primarily for visualizing drag operations and movement sequences.
     *
     * @param screen target Mat to draw on; modified in place
     * @param start origin location of the arrow
     * @param end destination location with arrowhead
     * @param color BGR color for the entire arrow
     */
    public void drawArrow(Mat screen, Location start, Location end, Scalar color) {
        Point startPoint = new Point(start.getCalculatedX(), start.getCalculatedY());
        Point endPoint = new Point(end.getCalculatedX(), end.getCalculatedY());
        arrowedLine(screen, startPoint, endPoint, color, 5, 8, 0, .04);
    }

    /**
     * Visualizes a drag operation as a sequence of connected arrows.
     * <p>
     * Draws arrows between consecutive matches in the result list to show
     * the path of a drag operation. Each arrow connects from one match to
     * the next, creating a visual trail of the drag movement.
     * <p>
     * Requirements:
     * <ul>
     * <li>At least 2 matches needed to draw a drag path</li>
     * <li>Arrows connect matches in sequence (0→1, 1→2, etc.)</li>
     * <li>Uses pink color (BGR: 255, 150, 255) for consistency</li>
     * </ul>
     *
     * @param illustrations container holding visualization layers; matchesOnScene layer is modified
     * @param matches action results with at least 2 matches representing drag path
     */
    public void drawDrag(Visualization illustrations, ActionResult matches) {
        if (matches.size() < 2) return;
        List<Match> matchList = new ArrayList<>(matches.getMatchList());
        for (int i = 0; i < matchList.size() - 1; i++) {
            Match match = matchList.get(i);
            Match nextMatch = matchList.get(i + 1);
            drawArrow(illustrations.getMatchesOnScene(), new Location(match), new Location(nextMatch), new Scalar(255, 150, 255, 0));
        }
    }

    /**
     * Highlights the defined region associated with an action.
     * <p>
     * Draws a dark semi-transparent rectangle around the defined region
     * to provide context for where an action was constrained. The dark
     * color (BGR: 10, 10, 10) with high alpha (255) creates a subtle
     * emphasis without obscuring the underlying content.
     * <p>
     * Debug output includes scene dimensions for troubleshooting
     * visualization issues.
     *
     * @param illustrations container holding visualization layers; scene layer is modified
     * @param matches action results containing the defined region to highlight
     */
    public void drawDefinedRegion(Visualization illustrations, ActionResult matches) {
        System.out.println("illustrations.getScene() rows = " + illustrations.getScene().rows());
        drawRect.drawRectAroundRegion(illustrations.getScene(), matches.getDefinedRegion(), new Scalar(10, 10, 10, 255)); // draw defined region
    }

    /**
     * Visualizes a movement path through multiple locations.
     * <p>
     * Renders movement differently based on the number of locations:
     * <ul>
     * <li>Single location: Draws a point marker at the location</li>
     * <li>Multiple locations: Draws arrows connecting consecutive points</li>
     * </ul>
     * This flexible approach handles both stationary positions and complex
     * movement paths. When used after a Move action, the previous position
     * can be included to show complete movement history.
     * <p>
     * The method modifies the screen Mat in place, adding the movement
     * visualization directly to the provided image.
     *
     * @param screen target Mat representing the screenshot; modified in place
     * @param locations ordered list of positions defining the movement path
     * @param color BGR color for all drawing operations
     */
    public void move(Mat screen, List<Location> locations, Scalar color) {
        if (locations.size() == 1) drawPoint(screen, locations.get(0).toMatch(), color);
        else for (int i=0; i<locations.size()-1; i++)
            drawArrow(screen, locations.get(i), locations.get(i+1), color);
    }

    /**
     * Visualizes a move action with optional starting positions.
     * <p>
     * Combines starting positions with match locations to create a complete
     * movement visualization. This is particularly useful for showing:
     * <ul>
     * <li>Movement from a previous action to the current matches</li>
     * <li>Complex paths involving pre-defined waypoints</li>
     * <li>Contextual movement history across multiple actions</li>
     * </ul>
     * The visualization uses pink color (BGR: 255, 150, 255) and draws
     * on the matchesOnScene layer for proper compositing.
     *
     * @param illustrations container holding visualization layers; matchesOnScene layer is modified
     * @param matches action results containing destination locations
     * @param startingPositions optional initial positions to include in the path
     */
    public void drawMove(Visualization illustrations, ActionResult matches, Location... startingPositions) {
        List<Location> locations = new ArrayList<>(List.of(startingPositions));
        locations.addAll(matches.getMatchLocations());
        move(illustrations.getMatchesOnScene(), locations, new Scalar(255, 150, 255, 0));
    }

}
