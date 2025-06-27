package io.github.jspinak.brobot.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import io.github.jspinak.brobot.model.element.SearchRegions;
import lombok.Data;
import org.sikuli.basics.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Configuration object that defines how GUI automation actions are executed in Brobot.
 * 
 * <p>ActionOptions encapsulates all parameters needed to customize action behavior, from 
 * basic settings like action type and similarity thresholds to advanced options like 
 * custom find strategies and success criteria. This design allows actions to be highly 
 * configurable while maintaining sensible defaults for common use cases.</p>
 * 
 * <p>Key configuration categories:
 * <ul>
 *   <li><b>Action Type</b>: Specifies whether to Find, Click, Type, etc.</li>
 *   <li><b>Find Strategy</b>: Controls how pattern matching is performed (FIRST, ALL, BEST, etc.)</li>
 *   <li><b>Search Parameters</b>: Similarity thresholds, search regions, and timing</li>
 *   <li><b>Behavioral Options</b>: Pauses, retries, and action-specific settings</li>
 *   <li><b>Success Criteria</b>: Conditions that determine when an action succeeds</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ActionOptions provides the flexibility needed to handle 
 * the inherent stochasticity of GUI environments. By adjusting parameters like similarity 
 * thresholds and retry counts, automation can adapt to varying conditions while maintaining 
 * reliability.</p>
 * 
 * <p>All fields have sensible defaults, allowing simple actions to be performed with 
 * minimal configuration while complex scenarios can be precisely controlled.</p>
 * 
 * @since 1.0
 * @see Action
 * @see Find
 * @see ActionResult
 * @see ObjectCollection
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionOptions {

    /**
     * Defines the type of GUI automation action to perform.
     * <p>
     * Actions are divided into two categories:
     * <ul>
     * <li><strong>Basic Actions:</strong> Atomic operations that directly interact with the GUI</li>
     * <li><strong>Composite Actions:</strong> Complex operations that combine multiple basic actions</li>
     * </ul>
     * <p>
     * <strong>Basic Actions:</strong>
     * <ul>
     * <li>{@code FIND} - Searches for visual patterns on screen</li>
     * <li>{@code CLICK} - Performs mouse click operations</li>
     * <li>{@code DEFINE} - Captures a screen region with specific coordinates</li>
     * <li>{@code TYPE} - Sends keyboard input to the active window</li>
     * <li>{@code MOVE} - Moves the mouse cursor to a location</li>
     * <li>{@code VANISH} - Waits for elements to disappear from screen</li>
     * <li>{@code HIGHLIGHT} - Draws visual indicators on matches or regions</li>
     * <li>{@code SCROLL_MOUSE_WHEEL} - Performs mouse wheel scrolling</li>
     * <li>{@code MOUSE_DOWN} - Presses and holds mouse button</li>
     * <li>{@code MOUSE_UP} - Releases mouse button</li>
     * <li>{@code KEY_DOWN} - Presses and holds keyboard key</li>
     * <li>{@code KEY_UP} - Releases keyboard key</li>
     * <li>{@code CLASSIFY} - Performs color-based classification</li>
     * </ul>
     * <p>
     * <strong>Composite Actions:</strong>
     * <ul>
     * <li>{@code CLICK_UNTIL} - Repeatedly clicks until a condition is met</li>
     * <li>{@code DRAG} - Performs click-and-drag operations</li>
     * </ul>
     */
    public enum Action {
        FIND, CLICK, DEFINE, TYPE, MOVE, VANISH, HIGHLIGHT, SCROLL_MOUSE_WHEEL,
        MOUSE_DOWN, MOUSE_UP, KEY_DOWN, KEY_UP, CLASSIFY,
        CLICK_UNTIL, DRAG
    }
    private Action action = Action.FIND;

    /**
     * Specifies the pattern matching strategy for Find operations.
     * <p>
     * Find strategies control how the framework searches for and returns matches.
     * Remember that ObjectCollections can contain multiple Images, and Images can
     * contain multiple Patterns, creating a hierarchy of search targets.
     * <p>
     * <strong>Standard matching strategies:</strong>
     * <ul>
     * <li>{@code FIRST} - Returns the first match found. Stops searching once any Pattern
     *     finds a match, making it efficient for existence checks</li>
     * <li>{@code EACH} - Returns one match per Image. The {@link DoOnEach} option
     *     determines whether to return the first or best match per Image</li>
     * <li>{@code ALL} - Returns all matches for all Patterns across all Images during
     *     the search duration. Useful for counting or processing multiple instances</li>
     * <li>{@code BEST} - Performs a Find.ALL operation then returns only the match
     *     with the highest similarity score</li>
     * </ul>
     * <p>
     * <strong>Specialized strategies:</strong>
     * <ul>
     * <li>{@code UNIVERSAL} - Mock-friendly strategy that responds to any Find type.
     *     Used in testing to create versatile mock objects</li>
     * <li>{@code CUSTOM} - User-defined strategy implemented as
     *     {@code BiConsumer<ActionResult, List<ObjectCollection>>}</li>
     * <li>{@code HISTOGRAM} - Matches based on color histogram similarity rather than
     *     pixel-by-pixel comparison</li>
     * <li>{@code COLOR} - Finds regions matching specific color criteria</li>
     * <li>{@code MOTION} - Tracks moving objects across consecutive screenshots</li>
     * <li>{@code REGIONS_OF_MOTION} - Identifies all areas with pixel changes</li>
     * </ul>
     * <p>
     * <strong>Text and analysis strategies:</strong>
     * <ul>
     * <li>{@code ALL_WORDS} - OCR operation that finds individual words and their
     *     bounding boxes. Returns each word as a separate Match</li>
     * <li>{@code SIMILAR_IMAGES} - Compares images in the 2nd ObjectCollection against
     *     the 1st, returning those above the similarity threshold</li>
     * <li>{@code FIXED_PIXELS} - Returns mask and matches for unchanged pixels</li>
     * <li>{@code DYNAMIC_PIXELS} - Returns mask and matches for changed pixels</li>
     * <li>{@code STATES} - Analyzes ObjectCollections to identify GUI states and
     *     their associated StateImage objects</li>
     * </ul>
     */
    public enum Find {
        FIRST, EACH, ALL, BEST, UNIVERSAL, CUSTOM, HISTOGRAM, COLOR, MOTION, REGIONS_OF_MOTION,
        ALL_WORDS, SIMILAR_IMAGES, FIXED_PIXELS, DYNAMIC_PIXELS, STATES
    }
    private Find find = Find.FIRST;
    /**
     * Custom Find implementation for one-time use.
     * <p>
     * Allows defining ad-hoc Find logic without creating a permanent custom Find strategy.
     * The BiConsumer receives the ActionResult to populate and the ObjectCollections to search.
     * This field is marked JsonIgnore as it contains non-serializable function references.
     */
    @JsonIgnore
    private BiConsumer<ActionResult, List<ObjectCollection>> tempFind;

    /**
     * Sequence of Find operations to perform in order.
     * <p>
     * Enables multi-stage searches where each Find operation refines the results:
     * <ul>
     * <li>Operations execute sequentially in list order</li>
     * <li>Each Find searches only within regions found by the previous Find</li>
     * <li>ObjectCollections are matched by index (Find #2 uses ObjectCollection #2)</li>
     * <li>If a matching ObjectCollection is empty, the last non-empty collection is reused</li>
     * </ul>
     * This supports workflows like: Find a window → Find a button within it → Find text on the button
     */
    private List<Find> findActions = new ArrayList<>();
    /**
     * Controls match refinement behavior in multi-stage Find operations.
     * <p>
     * When true, subsequent Find operations validate rather than replace initial matches.
     * The original larger match is retained only if all subsequent Finds succeed within
     * its boundaries. This is useful for confirming complex UI elements by checking for
     * expected sub-elements while preserving the overall element bounds.
     */
    private boolean keepLargerMatches = false;

    /**
     * Minimum similarity threshold for pattern matching.
     * <p>
     * For standard Find operations, this value (0.0-1.0) specifies how closely a screen
     * region must match the search pattern. For histogram-based operations, it defines
     * the minimum correlation score. Lower values allow more variation but may produce
     * false positives; higher values are more strict but may miss valid matches.
     * <p>
     * Default: {@link Settings#MinSimilarity} (typically 0.7)
     */
    private double similarity = Settings.MinSimilarity;

    /**
     * Specifies match selection strategy when using {@link Find#EACH}.
     * <p>
     * Since Images can contain multiple Patterns, this enum determines which
     * match to return when multiple matches exist for a single Image:
     * <ul>
     * <li>{@code FIRST} - Returns the first match found for each Image (fastest)</li>
     * <li>{@code BEST} - Returns the match with highest similarity for each Image</li>
     * </ul>
     */
    public enum DoOnEach {
        FIRST, BEST
    }
    private DoOnEach doOnEach = DoOnEach.FIRST;

    /**
     * Controls whether to capture screenshots of match regions.
     * <p>
     * When true, each Match will include a captured image (Mat) of its screen region.
     * This is useful for visual debugging, logging, or further image processing.
     * Disabling saves memory and improves performance when captures aren't needed.
     */
    private boolean captureImage = true;

    /**
     * Bypasses image search and uses predefined regions.
     * <p>
     * When true, creates matches from StateImage's defined regions without searching:
     * <ul>
     * <li>For RegionImagePairs: uses the first found region</li>
     * <li>Otherwise: uses the first region in SearchRegions</li>
     * </ul>
     * This optimization is useful when target locations are known and fixed.
     */
    private boolean useDefinedRegion = false;

    /**
     * Specifies mouse wheel scroll direction.
     * <p>
     * Used with {@link Action#SCROLL_MOUSE_WHEEL} to control scrolling direction.
     */
    public enum ScrollDirection {
        UP, DOWN
    }
    private ScrollDirection scrollDirection = ScrollDirection.UP;

    /**
     * Defines termination condition for {@link Action#CLICK_UNTIL} operations.
     * <p>
     * Controls when repeated clicking should stop:
     * <ul>
     * <li>{@code OBJECTS_APPEAR} - Click until target objects become visible</li>
     * <li>{@code OBJECTS_VANISH} - Click until target objects disappear</li>
     * </ul>
     * <p>
     * <strong>Usage patterns:</strong>
     * <ul>
     * <li>1 ObjectCollection: Clicks the objects until they vanish (e.g., dismissing dialogs)</li>
     * <li>2 ObjectCollections: Clicks objects in #1 until objects in #2 appear/vanish
     *     (e.g., clicking "Next" until "Finish" appears)</li>
     * </ul>
     */
    public enum ClickUntil {
        OBJECTS_APPEAR, OBJECTS_VANISH
    }
    private ClickUntil clickUntil = ClickUntil.OBJECTS_APPEAR;

    /**
     * Specifies text-based termination conditions for actions.
     * <p>
     * Controls when actions should stop based on OCR text detection:
     * <ul>
     * <li>{@code NONE} - Text detection does not affect action termination (default)</li>
     * <li>{@code TEXT_APPEARS} - Continue until specified text is found</li>
     * <li>{@code TEXT_VANISHES} - Continue until specified text is no longer found</li>
     * </ul>
     * <p>
     * Note: Since v1.0.7, text is captured in all Find operations regardless of this setting.
     */
    public enum GetTextUntil {
        NONE, TEXT_APPEARS, TEXT_VANISHES
    }
    private GetTextUntil getTextUntil = GetTextUntil.NONE;
    /**
     * The specific text to monitor for appearance or vanishing.
     * <p>
     * When empty:
     * <ul>
     * <li>TEXT_APPEARS succeeds when any text is detected</li>
     * <li>TEXT_VANISHES succeeds when no text is detected</li>
     * </ul>
     * When specified, only this exact text is monitored for the condition.
     */
    private String textToAppearOrVanish = "";

    /**
     * Custom success evaluation function.
     * <p>
     * Allows defining complex success criteria beyond the standard options.
     * The predicate receives the ActionResult and returns true if the action
     * should be considered successful. Overrides default success determination.
     */
    @JsonIgnore
    private Predicate<ActionResult> successCriteria;

    /*
     * A Drag is a good example of how the below options work together.
     * The options work in the following order:
     *   1. pauseBeforeBegin
     *   2. moveMouseDelay (to go to the drag point)
     *   3. pauseBeforeMouseDown
     *   4. pauseAfterMouseDown
     *   5. moveMouseDelay (to go to the drop point)
     *   6. pauseBeforeMouseUp
     *   7. pauseAfterMouseUp
     *   8. pauseAfterEnd
     */
    private double pauseBeforeMouseDown = FrameworkSettings.pauseBeforeMouseDown; //Settings.DelayBeforeMouseDown;
    // pauseAfterMouseDown replaces Sikuli settings var DelayBeforeDrag for Drags and ClickDelay for Clicks.
    private double pauseAfterMouseDown = FrameworkSettings.pauseAfterMouseDown;
    private float moveMouseDelay = Settings.MoveMouseDelay;
    private double pauseBeforeMouseUp = FrameworkSettings.pauseBeforeMouseUp;
    private double pauseAfterMouseUp = FrameworkSettings.pauseAfterMouseUp;

    /*
     * These values provide an offset to the Match for the dragTo Location.
     * To select the location to drag to, objects are chosen in this order:
     *   1. Objects in the 2nd ObjectCollection + offsets
     *   2. The dragFrom Location + offsets (when there is no 2nd ObjectCollection)
     *
     * Other variables are used to adjust the dragFrom Location
     */
    private int dragToOffsetX = 0;
    private int dragToOffsetY = 0;

    private ClickType.Type clickType = ClickType.Type.LEFT;

    /*
     * We have 2 options for moving the mouse after a click:
     *   1) To an offset of the click point
     *   2) To a fixed location
     * If the offset is defined we move there; otherwise we move to the fixed location.
     *
     * These options are also used for drags, and can move the mouse once the drag is finished.
     */
    private boolean moveMouseAfterAction = false;
    private Location moveMouseAfterActionTo = new Location(-1, 0); // disabled by default (x = -1)
    private Location moveMouseAfterActionBy = new Location(-1, 0);

    /**
     * Target position within match bounds.
     * <p>
     * Overrides default click positions defined in Pattern or StateImage objects.
     * When specified, clicks occur at this position relative to the match
     * (e.g., TOP_LEFT, CENTER, BOTTOM_RIGHT). Takes precedence over data type modifiers.
     */
    private Position targetPosition;
    
    /**
     * Pixel offset from the target position.
     * <p>
     * Adds an x,y offset to the final click location after position calculation.
     * Useful for clicking near but not exactly on matched elements. These modifiers
     * take precedence over offsets defined in Pattern or StateImage objects.
     */
    private Location targetOffset;

    /*
     * Sets temporary SearchRegions for Images, and also for RegionImagePairs when the
     * SearchRegion is not already defined. It will not change the SearchRegion of RegionImagePairs
     * that have already been defined. For more information on RegionImagePairs, see the class.
     */
    private SearchRegions searchRegions = new SearchRegions();

    /*
     * pauseBeforeBegin and pauseAfterEnd occur at the very beginning and very end of an action.
     * Including these options and not including a separate 'pause' method was a design choice.
     * Having a pause method allows the programmer to think in a more procedural manner: for example,
     * do A and then wait a bit and then do B. The design goal of brobot is to incentivize the
     * programmer to think about the process as discrete process objects that can be combined and
     * recombined in many different configurations. Brobot provides a framework for a semi-intelligent
     * automation and not just for automating a process flow.
     *
     * Pauses are always associated with actions: for example, pausing before clicking can increase
     * the chance that the click will be successful. There are also BrobotSettings for these options
     * that apply them to every click (pauseBeforeMouseDown, pauseAfterMouseUp), but the below
     * options give more granular control.
     */
    private double pauseBeforeBegin = 0;
    private double pauseAfterEnd = 0;

    /*
     * maxWait is used with FIND, and gives the max number of seconds to search.
     */
    private double maxWait = 0;

    /*
     * IndividualAction refers to individual activities, such as clicking on a single Match.
     *   When clicking a Match, timesToRepeatIndividualAction gives the number of consecutive clicks
     *   on this Match before moving on to the next Match.
     * Sequence refers to all activities in one iteration of a BasicAction, such as:
     *   clicking on all Matches of an Image
     *   clicking on a Match for each Pattern in a set of Images contained in an ObjectCollection
     * maxTimesToRepeatActionSequence is a max because a successful result will stop the repetitions
     *   and this can occur before the max number of repetitions. It can be used with BasicActions
     *   to repeat the Action until it is successful, or with CompositeActions to repeat the Action
     *   until the last Action is successful. An example of this with a CompositeAction is ClickUntil.
     *   ClickUntil has two Actions, Click and Find. It will register success when the last action, Find,
     *   is successful. This variable is not used with Find because Find relies solely on the maxWait variable.
     */
    private int timesToRepeatIndividualAction = 1;
    /**
     * Maximum iterations of the complete action sequence.
     * <p>
     * A sequence includes all targets in all ObjectCollections. The action repeats until:
     * <ul>
     * <li>Success criteria are met</li>
     * <li>Maximum repetitions are reached</li>
     * </ul>
     * <p>
     * For CompositeActions like ClickUntil, success is determined by the final action.
     * Not used with Find operations (which use maxWait instead).
     */
    private int maxTimesToRepeatActionSequence = 1;
    private double pauseBetweenIndividualActions = 0;
    private double pauseBetweenActionSequences = 0;

    /*
     * maxMatchesToActOn limits the number of Matches used when working with Find.ALL, Find.EACH, and
     * Find.HISTOGRAM.
     * When negative or zero it is not used.
     * The default value for HISTOGRAM is 1.
     */
    private int maxMatchesToActOn = -1;

    /*
     * Anchors define Locations in Matches and specify how these Locations should be used
     * to define a Region (see the Anchor class for more info).
     * INSIDE_ANCHORS defines the region as the smallest rectangle from the anchors found
     * OUTSIDE_ANCHORS defines the region as the largest rectangle from the anchors found
     * MATCH, BELOW_MATCH, ABOVE_MATCH, LEFT_OF_MATCH, RIGHT_OF_MATCH all define a Region
     *   around a single Match
     * FOCUSED_WINDOW defines a Region around the active Window
     */
    public enum DefineAs {
        INSIDE_ANCHORS, OUTSIDE_ANCHORS, MATCH, BELOW_MATCH, ABOVE_MATCH, LEFT_OF_MATCH, RIGHT_OF_MATCH,
        FOCUSED_WINDOW, INCLUDING_MATCHES
    }
    private DefineAs defineAs = DefineAs.MATCH;

    /*
     * The following variables make adjustments to the final results of many actions.
     * For example, a Region defined as x.y.w.h = 10.10.50.50 will be
     *   20.10.60.50 when addX = 10
     *   10.10.60.50 when addW = 10
     *   10.10.20.50 when absoluteW = 10
     * AbsoluteW and AbsoluteH are not used when set to a negative number.
     * When AbsoluteW is used, addW is not used. Same for H.
     *
     * When used with DRAG, they are used for the dragFrom Location but not for the dragTo Location.
     */
    private int addW = 0;
    private int addH = 0;
    private int absoluteW = -1;
    private int absoluteH = -1;
    private int addX = 0;
    private int addY = 0;

    /*
     * These variables are useful with composite actions, such as Drag.
     * addX and addY are used to adjust the dragFrom Location, and addX2 and addY2 are used to adjust
     * the dragTo Location. Dragging to or from the current mouse position then can be done by setting
     * addX2 and addY2 or addX and addY, respectively, without including any objects in the ObjectCollection.
     * AddX2 and addY2 add an additional Match to the results by adding X2 to the x value of the last Match and
     * Y2 to the y value of the last Match, and creating a new Match with these coordinates.
     */
    private int addX2 = 0;
    private int addY2 = 0;

    /*
     * Highlighting
     */
    private boolean highlightAllAtOnce = false;
    private double highlightSeconds = 1;
    private String highlightColor = "red"; // see sikuli region.highlight() for more info

    /*
     * The below options are for typing characters to the active window.
     * Modifiers are used for key combinations such as 'SHIFT a' or 'CTRL ALT DEL'
     */
    private double typeDelay = Settings.TypeDelay;
    private String modifiers = ""; // not used when ""

    /*
     * KMEANS finds a selected number of RGB color cluster centers for each image.
     * MU takes all pixels from all images and finds the min, max, mean, and standard deviation
     *   of the HSV values.
     */
    public enum Color {
        KMEANS, MU, CLASSIFICATION
    }
    private Color color = Color.MU;
    /*
     * Specifies the width and height of color boxes to find (used with FIND.COLOR).
     */
    private int diameter = 5;
    /*
     * The number of k-means to use for finding color.
     */
    private int kmeans = 2;

    /*
     * The number of bins to use for an HSV color histogram.
     */
    private int hueBins = 12;
    private int saturationBins = 2;
    private int valueBins = 1;

    /*
     * In case of multiple Find operations that include a Find.COLOR,
     * it's necessary to be able to specify a MinSimilarity for the Pattern matching Find,
     * and a different minScore for the Color Find.
     * Scale is the same as for MinSimilarity (0-1).
     * This value is converted for the different methods (BGR, HSV, etc) in
     * the class DistSimConversion.
     */
    private double minScore = 0.7;

    /*
     * Used with color finds and motion detection.
     * Values below 0 disable the condition.
     *
     * For motion detection:
     * This is the minimum number of changed pixels needed to identify a moving object.
     * Less than the minimum corresponds to objects that are too small to return matches.
     *
     * For color finds:
     * This is the minimum number of pixels needed to identify a match.
     */
    private int minArea = 1;
    private int maxArea = -1;

    /*
     * For Find.MOTION, this is the maximum distance an object can move between frames.
     */
    private int maxMovement = 300;

    /*
     * Used with recording and playback.
     *
     * startPlayback is the point in the recording, in seconds, to start the playback sequence.
     * When set to -1, the start point will be found by comparing the location of a match to
     * matches of objects in the recorded scenes, or by other methods.
     *
     * playbackDuration is the length of the playback sequence, in seconds.
     */
    private double startPlayback = -1;
    private double playbackDuration = 5;

    /*
     * Overrides the global illustration setting for this action.
     * Maybe is the default and does not override the global setting.
     */
    public enum Illustrate {
        YES, NO, MAYBE
    }
    private Illustrate illustrate = Illustrate.MAYBE;

    /*
     * Match fusion combines matches based on different criteria. The initial application was to combine
     * words in proximity found after a Find.ALL_WORDS operation. If NONE is selected, no match objects
     * will be fused and no fusion code will run.
     */
    public enum MatchFusionMethod {
        NONE, ABSOLUTE, RELATIVE
    }
    private MatchFusionMethod fusionMethod = MatchFusionMethod.NONE;
    private int maxFusionDistanceX = 5;
    private int maxFusionDistanceY = 5;
    /*
     * After fusing matches, this variable decides which scene to use to set the underlying Mat and text.
     */
    private int sceneToUseForCaptureAfterFusingMatches = 0;

    private LogEventType logType = LogEventType.ACTION;

    @Override
    public String toString() {
        return "ActionOptions{" +
                "action=" + action +
                ", find=" + find +
                ", findActions=" + findActions +
                ", keepLargerMatches=" + keepLargerMatches +
                ", similarity=" + similarity +
                ", doOnEach=" + doOnEach +
                ", captureImage=" + captureImage +
                ", useDefinedRegion=" + useDefinedRegion +
                ", scrollDirection=" + scrollDirection +
                ", clickUntil=" + clickUntil +
                ", getTextUntil=" + getTextUntil +
                ", textToAppearOrVanish='" + textToAppearOrVanish + '\'' +
                ", pauseBeforeMouseDown=" + pauseBeforeMouseDown +
                ", pauseAfterMouseDown=" + pauseAfterMouseDown +
                ", moveMouseDelay=" + moveMouseDelay +
                ", pauseBeforeMouseUp=" + pauseBeforeMouseUp +
                ", pauseAfterMouseUp=" + pauseAfterMouseUp +
                ", dragToOffsetX=" + dragToOffsetX +
                ", dragToOffsetY=" + dragToOffsetY +
                ", clickType=" + clickType +
                ", moveMouseAfterAction=" + moveMouseAfterAction +
                ", moveMouseAfterActionTo=" + moveMouseAfterActionTo +
                ", moveMouseAfterActionBy=" + moveMouseAfterActionBy +
                ", targetPosition=" + targetPosition +
                ", targetOffset=" + targetOffset +
                ", searchRegions=" + searchRegions +
                ", pauseBeforeBegin=" + pauseBeforeBegin +
                ", pauseAfterEnd=" + pauseAfterEnd +
                ", maxWait=" + maxWait +
                ", timesToRepeatIndividualAction=" + timesToRepeatIndividualAction +
                ", maxTimesToRepeatActionSequence=" + maxTimesToRepeatActionSequence +
                ", pauseBetweenIndividualActions=" + pauseBetweenIndividualActions +
                ", pauseBetweenActionSequences=" + pauseBetweenActionSequences +
                ", maxMatchesToActOn=" + maxMatchesToActOn +
                ", defineAs=" + defineAs +
                ", addW=" + addW +
                ", addH=" + addH +
                ", absoluteW=" + absoluteW +
                ", absoluteH=" + absoluteH +
                ", addX=" + addX +
                ", addY=" + addY +
                ", addX2=" + addX2 +
                ", addY2=" + addY2 +
                ", highlightAllAtOnce=" + highlightAllAtOnce +
                ", highlightSeconds=" + highlightSeconds +
                ", highlightColor='" + highlightColor + '\'' +
                ", typeDelay=" + typeDelay +
                ", modifiers='" + modifiers + '\'' +
                ", color=" + color +
                ", diameter=" + diameter +
                ", kmeans=" + kmeans +
                ", hueBins=" + hueBins +
                ", saturationBins=" + saturationBins +
                ", valueBins=" + valueBins +
                ", minScore=" + minScore +
                ", minArea=" + minArea +
                ", maxArea=" + maxArea +
                ", maxMovement=" + maxMovement +
                ", startPlayback=" + startPlayback +
                ", playbackDuration=" + playbackDuration +
                ", illustrate=" + illustrate +
                ", fusionMethod=" + fusionMethod +
                ", maxFusionDistanceX=" + maxFusionDistanceX +
                ", maxFusionDistanceY=" + maxFusionDistanceY +
                ", sceneToUseForCaptureAfterFusingMatches=" + sceneToUseForCaptureAfterFusingMatches +
                ", logType=" + logType +
                '}';
    }

    /**
     * Builder for constructing ActionOptions with a fluent API.
     * <p>
     * Provides a convenient way to create ActionOptions instances with custom settings
     * while maintaining immutability of the built objects. All builder methods return
     * the builder instance for method chaining.
     * <p>
     * Example usage:
     * <pre>{@code
     * ActionOptions options = new ActionOptions.Builder()
     *     .setAction(Action.CLICK)
     *     .setFind(Find.BEST)
     *     .setMinSimilarity(0.9)
     *     .setPauseAfterEnd(0.5)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private Action action = Action.FIND;
        private BiConsumer<ActionResult, List<ObjectCollection>> tempFind;
        private ClickUntil clickUntil = ClickUntil.OBJECTS_APPEAR;
        private Find find = Find.FIRST;
        private List<Find> findActions = new ArrayList<>();
        private boolean keepLargerMatches = false;
        private DoOnEach doOnEach = DoOnEach.FIRST;
        private boolean captureImage = true;
        private boolean useDefinedRegion = false;
        private Predicate<ActionResult> successCriteria;
        private double similarity = Settings.MinSimilarity;
        private Double pauseBeforeMouseDown; // I want to know if explicitly set to 0.0. // = BrobotSettings.pauseBeforeMouseDown; //Settings.DelayBeforeMouseDown;
        private Double pauseAfterMouseDown; // = BrobotSettings.pauseAfterMouseDown; // Sikuli = Settings.DelayBeforeDrag
        private float moveMouseDelay = Settings.MoveMouseDelay;
        private Double pauseBeforeMouseUp; // = BrobotSettings.pauseBeforeMouseUp; //Settings.DelayBeforeDrop;
        private Double pauseAfterMouseUp; // = BrobotSettings.pauseAfterMouseUp;
        private ClickType.Type clickType = ClickType.Type.LEFT;
        private boolean moveMouseAfterAction = false;
        private Location moveMouseAfterActionTo = new Location(-1, 0);
        private Location moveMouseAfterActionBy = new Location(-1, 0);
        private Position targetPosition;
        private Location targetOffset;
        private SearchRegions searchRegions = new SearchRegions();
        private double pauseBeforeBegin = 0;
        private double pauseAfterEnd = 0;
        private double pauseBetweenIndividualActions = 0;
        private int timesToRepeatIndividualAction = 1; // for example, clicks per match
        private int maxTimesToRepeatActionSequence = 1;
        private double pauseBetweenActionSequences = 0; // action group: rename these to differentiate better between for example, clicks on different matches, and the repetition of clicks on different matches multiple times
        private double maxWait = 0;
        private int maxMatchesToActOn = -1;
        private int dragToOffsetX = 0;
        private int dragToOffsetY = 0;
        private DefineAs defineAs = DefineAs.MATCH;
        private int addW = 0;
        private int addH = 0;
        private int absoluteW = -1; // when negative, not used (addW is used)
        private int absoluteH = -1;
        private int addX = 0;
        private int addY = 0;
        private int addX2 = 0;
        private int addY2 = 0;
        private boolean highlightAllAtOnce = false;
        private double highlightSeconds = 1;
        private String highlightColor = "red";
        private GetTextUntil getTextUntil = GetTextUntil.TEXT_APPEARS;
        private double typeDelay = Settings.TypeDelay;
        private String modifiers = "";
        private ScrollDirection scrollDirection = ScrollDirection.UP;
        private Color color = Color.MU;
        private int diameter = 5;
        private int kmeans = 2;
        private int hueBins = 12;
        private int saturationBins = 2;
        private int valueBins = 1;
        private double minScore = .7;
        private int minArea = 1;
        private int maxArea = -1;
        private int maxMovement = 300;
        private Illustrate illustrate = Illustrate.MAYBE;
        private MatchFusionMethod fusionMethod = MatchFusionMethod.NONE;
        private int maxFusionDistanceX = 5;
        private int maxFusionDistanceY = 5;
        private int sceneToUseForCaptureAfterFusingMatches = 0;
        private LogEventType logType = LogEventType.ACTION;

        public Builder() {}
        //public Builder(Action action) { this.action = action; }

        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder useTempFind(BiConsumer<ActionResult, List<ObjectCollection>> tempFind) {
            this.tempFind = tempFind;
            return this;
        }

        public Builder setClickUntil(ClickUntil clickUntil) {
            this.clickUntil = clickUntil;
            return this;
        }

        public Builder setGetTextUntil(GetTextUntil getTextUntil) {
            this.getTextUntil = getTextUntil;
            return this;
        }

        public Builder setFind(Find find) {
            this.find = find;
            return this;
        }

        // adds this Find to the list if the list has been initialized, or adds both the current Find
        // and this Find to the list.
        public Builder addFind(Find find) {
            if (findActions.isEmpty()) findActions.add(this.find);
            findActions.add(find);
            return this;
        }

        public Builder keepLargerMatches(boolean keepLargerMatches) {
            this.keepLargerMatches = keepLargerMatches;
            return this;
        }

        public Builder doOnEach(DoOnEach doOnEach) {
            this.doOnEach = doOnEach;
            return this;
        }

        public Builder setUseDefinedRegion(boolean useDefinedRegion) {
            this.useDefinedRegion = useDefinedRegion;
            return this;
        }

        public Builder setSuccessCriteria(Predicate<ActionResult> successCriteria) {
            this.successCriteria = successCriteria;
            return this;
        }

        public Builder setMinSimilarity(double similarity) {
            this.similarity = similarity;
            return this;
        }

        public Builder setPauseBeforeMouseDown(double pause) {
            this.pauseBeforeMouseDown = pause;
            return this;
        }

        public Builder setPauseBeforeMouseUp(double pause) {
            this.pauseBeforeMouseUp = pause;
            return this;
        }

        public Builder setPauseAfterMouseUp(double pause) {
            this.pauseAfterMouseUp = pause;
            return this;
        }

        public Builder setPauseAfterMouseDown(double delayAfterMouseDown) {
            this.pauseAfterMouseDown = delayAfterMouseDown;
            return this;
        }

        public Builder pauseBeforeMouseUp(double pauseBeforeMouseUp) {
            this.pauseBeforeMouseUp = pauseBeforeMouseUp;
            return this;
        }

        public Builder setMoveMouseDelay(float delay) {
            this.moveMouseDelay = delay;
            return this;
        }

        public Builder setClickType(ClickType.Type clickType) {
            this.clickType = clickType;
            return this;
        }

        public Builder setMoveMouseAfterAction(boolean setMoveMouse) {
            this.moveMouseAfterAction = setMoveMouse;
            return this;
        }

        public Builder setMoveMouseAfterActionTo(Location location) {
            this.moveMouseAfterActionTo = location;
            return this;
        }

        public Builder setMoveMouseAfterActionBy(int offsetX, int offsetY) {
            this.moveMouseAfterActionBy = new Location(offsetX, offsetY);
            return this;
        }

        public Builder setTargetPosition(Position position) {
            this.targetPosition = position;
            return this;
        }

        public Builder setTargetPosition(int w, int h) {
            this.targetPosition = new Position(w, h);
            return this;
        }

        public Builder setTargetOffset(int offsetX, int offsetY) {
            this.targetOffset = new Location(offsetX, offsetY);
            return this;
        }

        public Builder setSearchRegions(SearchRegions searchRegions) {
            this.searchRegions = searchRegions;
            return this;
        }

        public Builder addSearchRegion(Region searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion);
            return this;
        }

        public Builder addSearchRegion(StateRegion searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion.getSearchRegion());
            return this;
        }

        public Builder setPauseBeforeBegin(double pause) {
            this.pauseBeforeBegin = pause;
            return this;
        }

        public Builder setPauseAfterEnd(double pause) {
            this.pauseAfterEnd = pause;
            return this;
        }

        public Builder setPauseBetweenIndividualActions(double pause) {
            this.pauseBetweenIndividualActions = pause;
            return this;
        }

        public Builder setTimesToRepeatIndividualAction(int timesToRepeatIndividualAction) {
            this.timesToRepeatIndividualAction = timesToRepeatIndividualAction;
            return this;
        }

        public Builder setMaxTimesToRepeatActionSequence(int maxTimesToRepeatActionSequence) {
            this.maxTimesToRepeatActionSequence = maxTimesToRepeatActionSequence;
            return this;
        }

        public Builder setPauseBetweenActionSequences(double seconds) {
            this.pauseBetweenActionSequences = seconds;
            return this;
        }

        public Builder setMaxWait(double maxWait) {
            this.maxWait = maxWait;
            return this;
        }

        public Builder setMaxMatchesToActOn(int maxMatchesToActOn) {
            this.maxMatchesToActOn = maxMatchesToActOn;
            return this;
        }

        public Builder setDragToOffsetX(int offsetX) {
            this.dragToOffsetX = offsetX;
            return this;
        }

        public Builder setDragToOffsetY(int offsetY) {
            this.dragToOffsetY = offsetY;
            return this;
        }

        public Builder setDefineAs(DefineAs defineAs) {
            this.defineAs = defineAs;
            return this;
        }

        public Builder setAddW(int addW) {
            this.addW = addW;
            return this;
        }

        public Builder setAddH(int addH) {
            this.addH = addH;
            return this;
        }

        public Builder setAbsoluteWidth(int width) {
            this.absoluteW = width;
            return this;
        }

        public Builder setAbsoluteHeight(int height) {
            this.absoluteH = height;
            return this;
        }

        public Builder setAddX(int addX) {
            this.addX = addX;
            return this;
        }

        public Builder setAddY(int addY) {
            this.addY = addY;
            return this;
        }

        public Builder setAddX2(int addX2) {
            this.addX2 = addX2;
            return this;
        }

        public Builder setAddY2(int addY2) {
            this.addY2 = addY2;
            return this;
        }

        public Builder setHighlightAllAtOnce(boolean highlightAllAtOnce) {
            this.highlightAllAtOnce = highlightAllAtOnce;
            return this;
        }

        public Builder setHighlightSeconds(double seconds) {
            this.highlightSeconds = seconds;
            return this;
        }

        public Builder setHighlightColor(String color) {
            this.highlightColor = color;
            return this;
        }

        public Builder getTextUntil(GetTextUntil getTextUntil) {
            this.getTextUntil = getTextUntil;
            return this;
        }

        public Builder setTypeDelay(double typeDelay) {
            this.typeDelay = typeDelay;
            return this;
        }

        public Builder setModifiers(String modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder setScrollDirection(ScrollDirection scrollDirection) {
            this.scrollDirection = scrollDirection;
            return this;
        }

        public Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder setDiameter(int diameter) {
            this.diameter = diameter;
            return this;
        }

        public Builder setKmeans(int kmeans) {
            this.kmeans = kmeans;
            return this;
        }

        public Builder setHueBins(int hueBins) {
            this.hueBins = hueBins;
            return this;
        }

        public Builder setSaturationBins(int saturationBins) {
            this.saturationBins = saturationBins;
            return this;
        }

        public Builder setValueBins(int valueBins) {
            this.valueBins = valueBins;
            return this;
        }

        public Builder setMinScore(double minScore) {
            this.minScore = minScore;
            return this;
        }

        public Builder setMinArea(int minArea) {
            this.minArea = minArea;
            return this;
        }

        public Builder setMaxArea(int maxArea) {
            this.maxArea = maxArea;
            return this;
        }

        public Builder setMaxMovement(int maxMovement) {
            this.maxMovement = maxMovement;
            return this;
        }

        public Builder setIllustrate(Illustrate illustrate) {
            this.illustrate = illustrate;
            return this;
        }

        public Builder setFusionMethod(MatchFusionMethod fusionMethod) {
            this.fusionMethod = fusionMethod;
            return this;
        }

        public Builder setMaxFusionDistances(int x, int y) {
            this.maxFusionDistanceX = x;
            this.maxFusionDistanceY = y;
            return this;
        }

        public Builder setCaptureImage(boolean capture) {
            this.captureImage = capture;
            return this;
        }

        public Builder setSceneToUseForCaptureAfterFusingMatches(int sceneIndex) {
            this.sceneToUseForCaptureAfterFusingMatches = sceneIndex;
            return this;
        }

        public Builder setLogType(LogEventType logType) {
            this.logType = logType;
            return this;
        }

        /*
        The defaults for DRAG and CLICK are different.
        Explicitly set pauses override the defaults.
         */
        private void setPauses() {
            if (pauseBeforeMouseDown == null) pauseBeforeMouseDown = FrameworkSettings.pauseBeforeMouseDown;
            if (pauseAfterMouseDown == null) pauseAfterMouseDown = action == Action.DRAG? Settings.DelayValue : FrameworkSettings.pauseAfterMouseDown;
            if (pauseBeforeMouseUp == null) pauseBeforeMouseUp = action == Action.DRAG? Settings.DelayValue : FrameworkSettings.pauseBeforeMouseUp;
            if (pauseAfterMouseUp == null) pauseAfterMouseUp = FrameworkSettings.pauseAfterMouseUp;
        }

        public ActionOptions build() {
            ActionOptions actionOptions = new ActionOptions();
            actionOptions.action = action;
            setPauses();
            actionOptions.tempFind = tempFind;
            actionOptions.clickUntil = clickUntil;
            actionOptions.find = find;
            actionOptions.findActions = findActions;
            actionOptions.keepLargerMatches = keepLargerMatches;
            actionOptions.doOnEach = doOnEach;
            actionOptions.captureImage = captureImage;
            actionOptions.useDefinedRegion = useDefinedRegion;
            actionOptions.successCriteria = successCriteria;
            actionOptions.similarity = similarity;
            actionOptions.pauseBeforeMouseDown = pauseBeforeMouseDown;
            actionOptions.pauseAfterMouseUp = pauseAfterMouseUp;
            actionOptions.clickType = clickType;
            actionOptions.moveMouseAfterAction = moveMouseAfterAction;
            actionOptions.moveMouseAfterActionTo = moveMouseAfterActionTo;
            actionOptions.moveMouseAfterActionBy = moveMouseAfterActionBy;
            actionOptions.targetPosition = targetPosition;
            actionOptions.targetOffset = targetOffset;
            actionOptions.searchRegions = searchRegions;
            actionOptions.pauseBeforeBegin = pauseBeforeBegin;
            actionOptions.pauseAfterEnd = pauseAfterEnd;
            actionOptions.pauseBetweenIndividualActions = pauseBetweenIndividualActions;
            actionOptions.timesToRepeatIndividualAction = timesToRepeatIndividualAction;
            actionOptions.maxTimesToRepeatActionSequence = maxTimesToRepeatActionSequence;
            actionOptions.pauseBetweenActionSequences = pauseBetweenActionSequences;
            actionOptions.maxWait = maxWait;
            actionOptions.maxMatchesToActOn = maxMatchesToActOn;
            actionOptions.pauseAfterMouseDown = pauseAfterMouseDown;
            actionOptions.pauseBeforeMouseUp = pauseBeforeMouseUp;
            actionOptions.moveMouseDelay = moveMouseDelay;
            actionOptions.dragToOffsetX = dragToOffsetX;
            actionOptions.dragToOffsetY = dragToOffsetY;
            actionOptions.defineAs = defineAs;
            actionOptions.addW = addW;
            actionOptions.addH = addH;
            actionOptions.absoluteW = absoluteW;
            actionOptions.absoluteH = absoluteH;
            actionOptions.addX = addX;
            actionOptions.addY = addY;
            actionOptions.addX2 = addX2;
            actionOptions.addY2 = addY2;
            actionOptions.highlightAllAtOnce = highlightAllAtOnce;
            actionOptions.highlightSeconds = highlightSeconds;
            actionOptions.highlightColor = highlightColor;
            actionOptions.getTextUntil = getTextUntil;
            actionOptions.typeDelay = typeDelay;
            actionOptions.modifiers = modifiers;
            actionOptions.scrollDirection = scrollDirection;
            actionOptions.color = color;
            actionOptions.diameter = diameter;
            actionOptions.kmeans = kmeans;
            actionOptions.hueBins = hueBins;
            actionOptions.saturationBins = saturationBins;
            actionOptions.valueBins = valueBins;
            actionOptions.minScore = minScore;
            actionOptions.minArea = minArea;
            actionOptions.maxArea = maxArea;
            actionOptions.maxMovement = maxMovement;
            actionOptions.illustrate = illustrate;
            actionOptions.fusionMethod = fusionMethod;
            actionOptions.maxFusionDistanceX = maxFusionDistanceX;
            actionOptions.maxFusionDistanceY = maxFusionDistanceY;
            actionOptions.sceneToUseForCaptureAfterFusingMatches = sceneToUseForCaptureAfterFusingMatches;
            actionOptions.logType = logType;
            return actionOptions;
        }
    }
}
