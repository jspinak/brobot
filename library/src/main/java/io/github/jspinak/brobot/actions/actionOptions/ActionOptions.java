package io.github.jspinak.brobot.actions.actionOptions;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickType;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Data;
import org.sikuli.basics.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * ActionOptions provides options for configuring an action.
 */
@Data
public class ActionOptions {

    /**
     * BasicActions:
     * FIND
     * CLICK
     * DEFINE return a Region with specific x,y,w,h
     * TYPE sends keyboard input
     * MOVE moves the mouse
     * VANISH is successful when an Image or State disappears
     * GET_TEXT reads text from a Region
     * HIGHLIGHT highlights a Match, Region, or Location
     * SCROLL_MOUSE_WHEEL
     * MOUSE_DOWN
     * MOUSE_UP
     * KEY_DOWN
     * KEY_UP
     *
     * CompositeActions:
     * CLICK_UNTIL clicks Matches, Regions, and/or Locations until a condition is fulfilled
     * DRAG
     */
    public enum Action {
        FIND, CLICK, DEFINE, TYPE, MOVE, VANISH, GET_TEXT, HIGHLIGHT, SCROLL_MOUSE_WHEEL,
        MOUSE_DOWN, MOUSE_UP, KEY_DOWN, KEY_UP,
        CLICK_UNTIL, DRAG
    }
    private Action action = Action.FIND;

    /**
     * Keep in mind:
     *   ObjectCollections can contain multiple Images.
     *   Images can contain multiple Patterns (or image files).
     *
     * FIRST: first Match found
     * EACH: one Match per Image
     * ALL: all Matches for all Patterns in all Images
     * BEST: the best match from all Patterns in all Images
     * UNIVERSAL: used for mocking. Initializing an Image with a UNIVERSAL Find allows it
     *   to be accessed by an operation of FIRST, EACH, ALL, and BEST.
     * CUSTOM: user-defined. Must be of type
     *         {@code BiFunction<ActionOptions, List<StateImageObject>, Matches>>}
     *
     * The options that return multiple Matches allow for overlapping Matches.
     */
    public enum Find {
        FIRST, EACH, ALL, BEST, UNIVERSAL, CUSTOM, HISTOGRAM, COLOR
    }
    private Find find = Find.FIRST;
    /**
     * tempFind is a user defined Find method that is not meant to be reused.
     */
    private BiFunction<ActionOptions, List<StateImageObject>, Matches> tempFind;

    /**
     * Find actions are performed in the order they appear in the list.
     * Actions are performed only on the match regions found in the previous action.
     * Matching ObjectCollections are used (i.e. ObejctCollection #2 for Find #2),
     *   unless the matching ObjectCollection is empty, in which case the last
     *   non-empty ObjectCollection will be used.
     */
    private List<Find> findActions = new ArrayList<>();
    /**
     * When set to true, subsequent Find operations will act as confirmations of the initial matches.
     * AN initial match from the first Find operation will be returned if the subsequent
     * Find operations all find matches within its region.
     */
    private boolean keepLargerMatches = false;

    /**
     * Specifies how similar the found Match must be to the original Image.
     * Specifies the minimum score for a histogram to be considered a Match.
     */
    private double similarity = Settings.MinSimilarity;

    /**
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

    /**
     * Instead of searching for a StateImageObject, use its defined Region to create a Match.
     * This is either the first found region if the StateImageObject uses a
     * RegionImagePairs object, or the first defined Region in SearchRegions.
     */
    private boolean useDefinedRegion;

    /**
     * For scrolling with the mouse wheel
     */
    public enum ScrollDirection {
        UP, DOWN
    }
    private ScrollDirection scrollDirection = ScrollDirection.DOWN;

    /**
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

    /**
     * TEXT_APPEARS: Keep searching for text until some text appears.
     * TEXT_VANISHES: Keep searching for text until it vanishes.
     */
    public enum GetTextUntil {
        NONE, TEXT_APPEARS, TEXT_VANISHES
    }
    GetTextUntil getTextUntil = GetTextUntil.NONE;

    /**
     * successEvaluation defines the success criteria for the Find operation.
     */
    private Predicate<Matches> successCriteria;

    /**
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
    private double pauseBeforeMouseUp = Settings.DelayBeforeDrop;
    private double pauseAfterMouseUp = 0;

    /**
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

    /**
     * We have 2 options for moving the mouse after a click:
     *   1) To an offset of the click point
     *   2) To a fixed location
     * If the offset is defined we move there; otherwise we move to the fixed location.
     *
     * These options are also used for drags, and can move the mouse once the drag is finished.
     */
    private boolean moveMouseAfterClick = false;
    private Location locationAfterClick = new Location(0, 0);
    private Location offsetLocationBy = new Location(0, 0);

    /**
     * Sets temporary SearchRegions for Images, and also for RegionImagePairs when the
     * SearchRegion is not already defined. It will not change the SearchRegion of RegionImagePairs
     * that have already been defined. For more information on RegionImagePairs, see the class.
     */
    private SearchRegions searchRegions = new SearchRegions();

    /**
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
     * that apply them to every click (pauseBeforeMouseDown & pauseAfterMouseUp), but the below
     * options give more granular control.
     */
    private double pauseBeforeBegin = 0;
    private double pauseAfterEnd = 0;

    /**
     * maxWait is used with FIND, and gives the max number of seconds to search.
     */
    private double maxWait = 0;

    /**
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
     *   is successful.
     */
    private int timesToRepeatIndividualAction = 1;
    private int maxTimesToRepeatActionSequence = 1;
    private double pauseBetweenIndividualActions = 0;
    private double pauseBetweenActionSequences = 0;

    /**
     * maxMatchesToActOn limits the number of Matches used when working with Find.ALL, Find.EACH, and
     * Find.HISTOGRAM.
     * When <=0 it is not used.
     */
    private int maxMatchesToActOn = -1;

    /**
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

    /**
     * The following variables make adjustments to the final results of many actions.
     * For example, a Region defined as x.y.w.h = 10.10.50.50 will be
     *   20.10.60.50 when addX = 10
     *   10.10.60.50 when addW = 10
     *   10.10.20.50 when absoluteW = 10
     * AbsoluteW and AbsoluteH are not used when set to <0
     * When AbsoluteW is used, addW is not used. Same for H.
     *
     * These variables are used for the dragFrom Location but not for the dragTo Location.
     */
    private int addW = 0;
    private int addH = 0;
    private int absoluteW = -1;
    private int absoluteH = -1;
    private int addX = 0;
    private int addY = 0;

    /**
     * Highlighting
     */
    private boolean highlightAllAtOnce = false;
    private double highlightSeconds = 1;
    private String highlightColor = "red"; // see sikuli region.highlight() for more info

    /**
     * The below options are for typing characters to the active window.
     * Modifiers are used for key combinations such as 'SHIFT a' or 'CTRL ALT DEL'
     */
    private double typeDelay = Settings.TypeDelay;
    private String modifiers = ""; // not used when ""

    /**
     * Specifies the width and height of color boxes to find (used with FIND.COLOR).
     */
    private int diameter = 1;
    /**
     * The number of k-means to use for finding color.
     */
    private int kmeans = 1;

    /**
     * The number of bins to use for an HSV color histogram.
     */
    private int hueBins = 12;
    private int saturationBins = 2;
    private int valueBins = 1;

    public static class Builder {
        private Action action = Action.FIND;
        private BiFunction<ActionOptions, List<StateImageObject>, Matches> tempFind;
        private ClickUntil clickUntil = ClickUntil.OBJECTS_APPEAR;
        private Find find = Find.FIRST;
        private List<Find> findActions = new ArrayList<>();
        private boolean keepLargerMatches = false;
        private DoOnEach doOnEach = DoOnEach.FIRST;
        private boolean useDefinedRegion = false;
        private Predicate<Matches> successCriteria;
        private double similarity = Settings.MinSimilarity;
        private double pauseBeforeMouseDown = Settings.DelayBeforeMouseDown;
        private double delayAfterMouseDown = BrobotSettings.delayAfterMouseDown; // Sikuli = Settings.DelayBeforeDrag
        private float moveMouseDelay = Settings.MoveMouseDelay;
        private double pauseBeforeMouseUp = Settings.DelayBeforeDrop;
        private double pauseAfterMouseUp = 0;
        private ClickType.Type clickType = ClickType.Type.LEFT;
        private boolean moveMouseAfterClick = false;
        private Location locationAfterClick = new Location(0, 0);
        private Location offsetLocationBy = new Location(0, 0);
        private SearchRegions searchRegions = new SearchRegions();
        private double pauseBeforeBegin = 0;
        private double pauseAfterEnd = 0;
        private double pauseBetweenActions = 0;
        private int timesToRepeatIndividualAction = 1; // for example, clicks per match
        private int maxTimesToRepeatActionSequence = 1;
        private double pauseBetweenActionRepetitions = 0; // action group: rename these to differentiate better between for example, clicks on different matches, and the repetition of clicks on different matches multiple times
        private double maxWait = 0;
        private int maxMatchesToActOn = 100;
        private int dragToOffsetX;
        private int dragToOffsetY;
        private DefineAs defineAs = DefineAs.MATCH;
        private int addW = 0;
        private int addH = 0;
        private int absoluteW = -1; // when negative, not used (addW is used)
        private int absoluteH = -1;
        private int addX = 0;
        private int addY = 0;
        private boolean highlightAllAtOnce = false;
        private double highlightSeconds = 1;
        private String highlightColor = "red";
        GetTextUntil getTextUntil = GetTextUntil.TEXT_APPEARS;
        private double typeDelay = Settings.TypeDelay;
        private String modifiers = "";
        private ScrollDirection scrollDirection = ScrollDirection.UP;
        private int diameter = 5;
        private int kmeans = 2;
        private int hueBins = 12;
        private int saturationBins = 2;
        private int valueBins = 1;

        public Builder() {}
        //public Builder(Action action) { this.action = action; }

        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder useTempFind(BiFunction<ActionOptions, List<StateImageObject>, Matches> tempFind) {
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
            this.delayAfterMouseDown = delayAfterMouseDown;
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

        public Builder setMoveMouseAfterClick(boolean setMoveMouse) {
            this.moveMouseAfterClick = setMoveMouse;
            return this;
        }

        public Builder setLocationAfterClick(Location location) {
            this.locationAfterClick = location;
            return this;
        }

        public Builder setLocationAfterClickByOffset(int offsetX, int offsetY) {
            this.offsetLocationBy = new Location(offsetX, offsetY);
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

        public Builder setPauseBetweenActions(double pause) {
            this.pauseBetweenActions = pause;
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
            this.pauseBetweenActionRepetitions = seconds;
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

        public ActionOptions build() {
            ActionOptions actionOptions = new ActionOptions();
            actionOptions.action = action;
            actionOptions.tempFind = tempFind;
            actionOptions.clickUntil = clickUntil;
            actionOptions.find = find;
            actionOptions.findActions = findActions;
            actionOptions.keepLargerMatches = keepLargerMatches;
            actionOptions.doOnEach = doOnEach;
            actionOptions.useDefinedRegion = useDefinedRegion;
            actionOptions.successCriteria = successCriteria;
            actionOptions.similarity = similarity;
            actionOptions.pauseBeforeMouseDown = pauseBeforeMouseDown;
            actionOptions.pauseAfterMouseUp = pauseAfterMouseUp;
            actionOptions.clickType = clickType;
            actionOptions.moveMouseAfterClick = moveMouseAfterClick;
            actionOptions.locationAfterClick = locationAfterClick;
            actionOptions.offsetLocationBy = offsetLocationBy;
            actionOptions.searchRegions = searchRegions;
            actionOptions.pauseBeforeBegin = pauseBeforeBegin;
            actionOptions.pauseAfterEnd = pauseAfterEnd;
            actionOptions.pauseBetweenIndividualActions = pauseBetweenActions;
            actionOptions.timesToRepeatIndividualAction = timesToRepeatIndividualAction;
            actionOptions.maxTimesToRepeatActionSequence = maxTimesToRepeatActionSequence;
            actionOptions.pauseBetweenActionSequences = pauseBetweenActionRepetitions;
            actionOptions.maxWait = maxWait;
            actionOptions.maxMatchesToActOn = maxMatchesToActOn;
            actionOptions.pauseAfterMouseDown = delayAfterMouseDown;
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
            actionOptions.highlightAllAtOnce = highlightAllAtOnce;
            actionOptions.highlightSeconds = highlightSeconds;
            actionOptions.highlightColor = highlightColor;
            actionOptions.getTextUntil = getTextUntil;
            actionOptions.typeDelay = typeDelay;
            actionOptions.modifiers = modifiers;
            actionOptions.scrollDirection = scrollDirection;
            actionOptions.diameter = diameter;
            actionOptions.kmeans = kmeans;
            actionOptions.hueBins = hueBins;
            actionOptions.saturationBins = saturationBins;
            actionOptions.valueBins = valueBins;
            return actionOptions;
        }
    }
}
