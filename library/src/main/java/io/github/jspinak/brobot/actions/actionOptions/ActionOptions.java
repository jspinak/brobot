package io.github.jspinak.brobot.actions.actionOptions;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickType;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.basics.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * ActionOptions provides options for configuring an action.
 * Since an Action can be performed without selecting and building an ActionOptions object,
 *   the variables need to be initialized with default values.
 */
@Getter
@Setter
public class ActionOptions {

    /*
     * BasicActions:
     * FIND
     * CLICK
     * DEFINE return a Region with specific x,y,w,h
     * TYPE sends keyboard input
     * MOVE moves the mouse
     * VANISH is successful when an Image or State disappears
     * HIGHLIGHT highlights a Match, Region, or Location
     * SCROLL_MOUSE_WHEEL
     * MOUSE_DOWN
     * MOUSE_UP
     * KEY_DOWN
     * KEY_UP
     * CLASSIFY
     *
     * CompositeActions:
     * CLICK_UNTIL clicks Matches, Regions, and/or Locations until a condition is fulfilled
     * DRAG
     */
    public enum Action {
        FIND, CLICK, DEFINE, TYPE, MOVE, VANISH, HIGHLIGHT, SCROLL_MOUSE_WHEEL,
        MOUSE_DOWN, MOUSE_UP, KEY_DOWN, KEY_UP, CLASSIFY,
        CLICK_UNTIL, DRAG
    }
    private Action action = Action.FIND;

    /*
     * Keep in mind:
     *   ObjectCollections can contain multiple Images.
     *   Images can contain multiple Patterns (or image files).
     *
     * FIRST: first Match found. Finds all matches for a given Pattern, but does not continue searching if at least one Pattern finds a match in any iteration.
     * EACH: one Match per Image. The DoOnEach option determines specifics of how EACH is performed.
     * ALL: all Matches for all Patterns in all Images for the duration of the find operation.
     * BEST: the match with the best score from a Find.ALL operation.
     * UNIVERSAL: used for mocking. Initializing an Image with a UNIVERSAL Find allows it
     *   to be accessed by an operation of FIRST, EACH, ALL, and BEST.
     * CUSTOM: user-defined. Must be of type
     *         {@code BiFunction<ActionOptions, List<StateImage>, Matches>>}
     * HISTOGRAM: match the histogram from the input image(s)
     * MOTION: find the locations of a moving object across screens
     * REGIONS_OF_MOTION: find all dynamic pixel regions from a series of screens
     * ALL_WORDS: find all words and their regions. To find all text in a specific region and have that
     *   text as part of one Match object, use a normal Find operation.
     *   Normal find operations, when given a region, will return the text in that region.
     *   This operation will find words and return each word and its region as a separate Match object.
     * SIMILAR_IMAGES: finds the images in the 2nd ObjectCollection that are above a similarity threshold
     *   to the images in the 1st ObjectCollection.
     * FIXED_PIXELS: returns a mask of all pixels that are the same and a corresponding Match list from the contours.
     * DYNAMIC_PIXELS: returns a mask of all pixels that are changed and a corresponding Match list from the contours.
     * STATES: each ObjectCollection contains the images found on a screen and the screenshot (as a scene). All
     *   ObjectCollection(s) are analyzed to produce states with StateImage objects. The Match objects returned
     *   hold the state owner's name and a Pattern, and this data is all that's needed to create the state
     *   structure (without transitions).
     *
     */
    public enum Find {
        FIRST, EACH, ALL, BEST, UNIVERSAL, CUSTOM, HISTOGRAM, COLOR, MOTION, REGIONS_OF_MOTION,
        ALL_WORDS, SIMILAR_IMAGES, FIXED_PIXELS, DYNAMIC_PIXELS, STATES
    }
    private Find find = Find.FIRST;
    /*
     * tempFind is a user defined Find method that is not meant to be reused.
     */
    private BiConsumer<Matches, List<ObjectCollection>> tempFind;

    /*
     * Find actions are performed in the order they appear in the list.
     * Actions are performed only on the match regions found in the previous action.
     * Matching ObjectCollections are used (i.e. ObejctCollection #2 for Find #2),
     *   unless the matching ObjectCollection is empty, in which case the last
     *   non-empty ObjectCollection will be used.
     */
    private List<Find> findActions = new ArrayList<>();
    /*
     * When set to true, subsequent Find operations will act as confirmations of the initial matches.
     * An initial match from the first Find operation will be returned if the subsequent
     * Find operations all find matches within its region.
     */
    private boolean keepLargerMatches = false;

    /*
     * Specifies how similar the found Match must be to the original Image.
     * Specifies the minimum score for a histogram to be considered a Match.
     */
    private double similarity = Settings.MinSimilarity;

    /*
     * Images can contain multiple Patterns.
     * DoOnEach specifies how Find.EACH should approach individual Images.
     *
     * FIRST: first Match on each Image
     * BEST: best Match on each Image
     */
    public enum DoOnEach {
        FIRST, BEST
    }
    private DoOnEach doOnEach = DoOnEach.FIRST;

    /*
     * The Match's Mat can be captured from the match region
     */
    private boolean captureImage = true;

    /*
     * Instead of searching for a StateImage, use its defined Region to create a Match.
     * This is either the first found region if the StateImage uses a
     * RegionImagePairs object, or the first defined Region in SearchRegions.
     */
    private boolean useDefinedRegion = false;

    /*
     * For scrolling with the mouse wheel
     */
    public enum ScrollDirection {
        UP, DOWN
    }
    private ScrollDirection scrollDirection = ScrollDirection.UP;

    /*
     * Specifies the condition to fulfill after a Click.
     * The Objects in the 1st ObjectCollection are acted on by the CLICK method.
     * If there is a 2nd ObjectCollection, it is acted on by the FIND method.
     * If there is only 1 ObjectCollection, the FIND method also uses these objects.
     *
     * Translation:
     * 1 ObjectCollection: Click this until it disappears.
     * 2 ObjectCollections: Click #1 until #2 appears or disappears.
     */
    public enum ClickUntil {
        OBJECTS_APPEAR, OBJECTS_VANISH
    }
    private ClickUntil clickUntil = ClickUntil.OBJECTS_APPEAR;

    /*
     * NONE: Text is not used as an exit condition. Important in versions >= 1.0.7 where text is saved in all Find operations.
     * TEXT_APPEARS: Keep searching for text until some text appears.
     * TEXT_VANISHES: Keep searching for text until it vanishes.
     */
    public enum GetTextUntil {
        NONE, TEXT_APPEARS, TEXT_VANISHES
    }
    private GetTextUntil getTextUntil = GetTextUntil.NONE;
    /*
    When empty, TEXT_APPEARS is achieved when any text appears and
    TEXT_VANISHES is achieved when no text is found.
     */
    private String textToAppearOrVanish = "";

    /*
     * successEvaluation defines the success criteria for the Find operation.
     */
    private Predicate<Matches> successCriteria;

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
    private double pauseBeforeMouseDown = Settings.DelayBeforeMouseDown;
    // pauseAfterMouseDown replaces Sikuli settings var DelayBeforeDrag for Drags and ClickDelay for Clicks.
    private double pauseAfterMouseDown = BrobotSettings.delayAfterMouseDown;
    private float moveMouseDelay = Settings.MoveMouseDelay;
    private double pauseBeforeMouseUp = Settings.DelayBeforeDrag;
    private double pauseAfterMouseUp = 0;

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

    /*
    These options modify the click location for matches.
    They take precedence over the modifiers in the datatypes (Pattern, StateImage, etc).
    It's best to keep the Position separate from the Location. These values are not used when they are null.
    Since offsetX and offsetY in Location are int values, they cannot be null when a Location is not null, which
    would be the case when creating a Position within a Location.
     */
    private Position targetPosition;
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

    public static class Builder {
        private Action action = Action.FIND;
        private BiConsumer<Matches, List<ObjectCollection>> tempFind;
        private ClickUntil clickUntil = ClickUntil.OBJECTS_APPEAR;
        private Find find = Find.FIRST;
        private List<Find> findActions = new ArrayList<>();
        private boolean keepLargerMatches = false;
        private DoOnEach doOnEach = DoOnEach.FIRST;
        private boolean captureImage = true;
        private boolean useDefinedRegion = false;
        private Predicate<Matches> successCriteria;
        private double similarity = Settings.MinSimilarity;
        private double pauseBeforeMouseDown = Settings.DelayBeforeMouseDown;
        private double pauseAfterMouseDown = BrobotSettings.delayAfterMouseDown; // Sikuli = Settings.DelayBeforeDrag
        private float moveMouseDelay = Settings.MoveMouseDelay;
        private double pauseBeforeMouseUp = Settings.DelayBeforeDrop;
        private double pauseAfterMouseUp = 0;
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

        public Builder() {}
        //public Builder(Action action) { this.action = action; }

        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder useTempFind(BiConsumer<Matches, List<ObjectCollection>> tempFind) {
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

        public Builder setSuccessCriteria(Predicate<Matches> successCriteria) {
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

        public ActionOptions build() {
            ActionOptions actionOptions = new ActionOptions();
            actionOptions.action = action;
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
            return actionOptions;
        }
    }
}
