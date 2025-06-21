package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.AddNonImageObjects;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.AdjustMatches;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.MatchFusion;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.OffsetOps;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.stringUtils.TextSelector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Core pattern matching action that locates GUI elements on the screen.
 * 
 * <p>Find is the fundamental action in Brobot's visual GUI automation, implementing various 
 * pattern matching strategies to locate GUI elements. It embodies the visual recognition 
 * capability that enables the framework to interact with any GUI regardless of the underlying 
 * technology.</p>
 * 
 * <p>Find strategies supported:
 * <ul>
 *   <li><b>FIRST</b>: Returns the first match found, optimized for speed</li>
 *   <li><b>BEST</b>: Returns the highest-scoring match from all possibilities</li>
 *   <li><b>EACH</b>: Returns one match per StateImage/Pattern</li>
 *   <li><b>ALL</b>: Returns all matches found, useful for lists and grids</li>
 *   <li><b>CUSTOM</b>: User-defined find strategies for special cases</li>
 * </ul>
 * </p>
 * 
 * <p>Advanced features:
 * <ul>
 *   <li>Multi-pattern matching with StateImages containing multiple templates</li>
 *   <li>Color-based matching using k-means profiles</li>
 *   <li>Text extraction from matched regions (OCR integration)</li>
 *   <li>Match fusion for combining overlapping results</li>
 *   <li>Dynamic offset adjustments for precise targeting</li>
 * </ul>
 * </p>
 * 
 * <p>Find operations also handle non-image objects in ObjectCollections:
 * <ul>
 *   <li>Existing Matches can be reused without re-searching</li>
 *   <li>Regions are converted to matches for consistent handling</li>
 *   <li>Locations provide direct targeting without pattern matching</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Find operations are context-aware through integration 
 * with StateMemory, automatically adjusting active states based on what is found. This 
 * enables the framework to maintain an accurate understanding of the current GUI state.</p>
 * 
 * @since 1.0
 * @see ActionOptions.Find
 * @see StateImage
 * @see Matches
 * @see Pattern
 * @author Joshua Spinak
 */
@Component
public class Find implements ActionInterface {

    private final FindFunctions findFunctions;
    private final StateMemory stateMemory;
    private final AddNonImageObjects addNonImageObjects;
    private final AdjustMatches adjustMatches;
    private final SetAllProfiles setAllProfiles;
    private final OffsetOps offsetOps;
    private final MatchFusion matchFusion;
    private final SetMatTextPattern setMatTextPattern;
    private final TextSelector textSelector;

    public Find(FindFunctions findFunctions, StateMemory stateMemory,
                AddNonImageObjects addNonImageObjects, AdjustMatches adjustMatches,
                SetAllProfiles setAllProfiles, OffsetOps offsetOps, MatchFusion matchFusion,
                SetMatTextPattern setMatTextPattern, TextSelector textSelector) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.setAllProfiles = setAllProfiles;
        this.offsetOps = offsetOps;
        this.matchFusion = matchFusion;
        this.setMatTextPattern = setMatTextPattern;
        this.textSelector = textSelector;
    }

    /**
     * Find is called sometimes outside of Action.perform(...) when used in another Action. This is done
     * to avoid creating Snapshots for each Find sequence. When called directly, the following
     * operations do not occur:
     * - Wait.pauseBeforeBegin
     * - Matches.setSuccess
     * - Matches.setDuration
     * - Matches.saveSnapshots
     * - Wait.pauseAfterEnd
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        //int actionId = actionLifecycleManagement.newActionLifecycle(actionOptions);
        createColorProfilesWhenNecessary(actionOptions, objectCollections);
        matches.setMaxMatches(actionOptions.getMaxMatchesToActOn());
        offsetOps.addOffsetAsOnlyMatch(List.of(objectCollections), matches, true);
        findFunctions.get(actionOptions).accept(matches, List.of(objectCollections));
        stateMemory.adjustActiveStatesWithMatches(matches);
        Matches nonImageMatches = addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
        matches.addMatchObjects(nonImageMatches);
        matchFusion.setFusedMatches(matches);
        matches.getMatchList().forEach(m -> adjustMatches.adjust(m, actionOptions));
        offsetOps.addOffsetAsLastMatch(matches, actionOptions);
        List<Match> mutableMatchList = new ArrayList<>(matches.getMatchList());
        mutableMatchList.removeIf(match -> match.size() < actionOptions.getMinArea()); // size is checked after potential match merges and adjustments
        matches.setMatchList(mutableMatchList);
        setMatTextPattern.set(matches);
        matches.setSelectedText(textSelector.getString(TextSelector.Method.MOST_SIMILAR, matches.getText()));
    }

    private void createColorProfilesWhenNecessary(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (!actionOptions.getFindActions().contains(ActionOptions.Find.COLOR)) return;
        List<StateImage> imgs = new ArrayList<>();
        if (objectCollections.length >= 1) imgs.addAll(objectCollections[0].getStateImages());
        if (objectCollections.length >= 2) imgs.addAll(objectCollections[1].getStateImages());
        List<StateImage> imagesWithoutColorProfiles = new ArrayList<>();
        for (StateImage img : imgs) {
            if (img.getKmeansProfilesAllSchemas() == null) {
                imagesWithoutColorProfiles.add(img);
            }
        }
        imagesWithoutColorProfiles.forEach(setAllProfiles::setMatsAndColorProfiles);
    }

}