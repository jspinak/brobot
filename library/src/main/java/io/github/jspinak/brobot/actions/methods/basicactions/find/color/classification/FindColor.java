package io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.MatchOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;
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
public class FindColor {

    private final GetClassMatches getClassMatches;
    private final MatchOps matchOps;
    private final GetSceneAnalysisCollection getSceneAnalysisCollection;

    public FindColor(GetClassMatches getClassMatches, MatchOps matchOps,
                     GetSceneAnalysisCollection getSceneAnalysisCollection) {
        this.getClassMatches = getClassMatches;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
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
     * @param matches The action configuration and existing matches
     * @param objectCollections The images to use for classification
     */
    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.getDiameter() < 0) return;
        Set<StateImage> targetImages = getSceneAnalysisCollection.getTargetImages(objectCollections);
        Matches classMatches = getClassMatches.getMatches(matches.getSceneAnalysisCollection(), targetImages, actionOptions);
        matches.addAllResults(classMatches);
        if (actionOptions.getAction() == ActionOptions.Action.CLASSIFY)
            matches.getMatchList().sort(Comparator.comparing(Match::size).reversed());
        else matches.getMatchList().sort(Comparator.comparingDouble(Match::getScore).reversed());
        matchOps.limitNumberOfMatches(matches, actionOptions);
    }

}
