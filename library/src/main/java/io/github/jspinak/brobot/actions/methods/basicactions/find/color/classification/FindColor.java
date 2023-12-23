package io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.MatchOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The steps for color classification are:
 * 1. get a screenshot or read the image parameters from file as hsv Mats
 *    - when passed as parameters, the images are taken from the third ObjectCollection
 * 2. get the images to use for classification either from StateMemory or passed as parameters
 * 3. classify each pixel in the hsv Mat(s), based on its color, as one of the images or as no match
 *    - refer to the class SparseMatrix for details on how the classification is performed
 * 4. return, as matches, the largest regions corresponding to the desired classes
 *    - the desired classes are those for the images in the first ObjectCollection
 *    - images in the second ObjectCollections are used in the classification, but not selected as matches
 */
@Component
public class FindColor implements ActionInterface {

    private GetClassMatches getClassMatches;
    private MatchOps matchOps;
    private GetSceneAnalysisCollection getSceneAnalysisCollection;
    private final MatchesInitializer matchesInitializer;

    public FindColor(GetClassMatches getClassMatches, MatchOps matchOps,
                     GetSceneAnalysisCollection getSceneAnalysisCollection, MatchesInitializer matchesInitializer) {
        this.getClassMatches = getClassMatches;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.matchesInitializer = matchesInitializer;
    }

    public void perform(Matches matches, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        find(matches, actionOptions, Arrays.asList(objectCollections));
    }

    /**
     * The first ObjectCollection contains the images to find. When empty, classification
     *   will be performed on the StateImages in the active states, and the classification Mat
     *   will be returned in the Matches object; however, no Match objects will exist.
     * The second ObjectCollection contains additional images to use for classification.
     *   Adding additional images to the classification task can make the selection more precise.
     *   When empty, StateImages from the active states will be used.
     *   The images in the first ObjectCollection are included in the classification and do not need
     *   to be added again to the second ObjectCollection.
     * The third ObjectCollection contains the images (or scenes) to classify.
     *   When empty, a screenshot will be taken.
     *   When not empty, the images will be read from file.
     *   Keep in mind that matches will be found for all scenes. If you illustrate the action, each scene
     *   will appear with matches for all scenes. Normally you would only want to use one scene.
     * @param actionOptions The action configuration
     * @param objColls The images to use for classification
     */
    public void find(Matches matches, ActionOptions actionOptions, List<ObjectCollection> objColls) {
        if (actionOptions.getDiameter() < 0) return;
        Set<StateImage> targetImages = getSceneAnalysisCollection.getTargetImages(objColls);
        Matches classMatches = getClassMatches.getMatches(matches.getSceneAnalysisCollection(), targetImages, actionOptions);
        matches.addAllResults(classMatches);
        if (actionOptions.getAction() == ActionOptions.Action.CLASSIFY)
            matches.getMatchObjects().sort(Comparator.comparing(MatchObject::size).reversed());
        else matches.getMatches().sort(Comparator.comparingDouble(Match::getScore).reversed());
        matchOps.limitNumberOfMatches(matches, actionOptions);
    }

}
