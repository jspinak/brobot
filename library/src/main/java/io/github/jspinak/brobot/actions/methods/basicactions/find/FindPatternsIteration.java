package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Sends the Image object to either FindImage or FindRIP depending on whether the Image location can vary.
 * Contains methods for Find options FIRST, EACH, ALL, BEST
 */
@Component
public class FindPatternsIteration {

    private final ActionLifecycleManagement actionLifecycleManagement;
    private final FindAll findAll;

    public FindPatternsIteration(ActionLifecycleManagement actionLifecycleManagement, FindAll findAll) {
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.findAll = findAll;
    }

    /**
     * If Find.FIRST, it returns the first positive results.
     * Otherwise, it returns all matches.
     */
    public void find(Matches matches, List<StateImage> stateImages, List<Image> scenes) {
        actionLifecycleManagement.printActionOnce(matches);
        for (Image scene : scenes) {
            List<Match> singleSceneMatchList = new ArrayList<>(); // holds finds for a specific scene
            for (int i=0; i<stateImages.size(); i++) { // run for each StateImage
                List<Match> newMatches = findAll.find(stateImages.get(i), scene, matches.getActionOptions());
                singleSceneMatchList.addAll(newMatches);
                matches.addAll(newMatches); // holds all matches found
                if (!actionLifecycleManagement.isOkToContinueAction(matches, stateImages.size())) return;
            }
            SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
            sceneAnalysis.setMatchList(singleSceneMatchList);
            matches.getSceneAnalysisCollection().add(sceneAnalysis);
        }
    }

}
