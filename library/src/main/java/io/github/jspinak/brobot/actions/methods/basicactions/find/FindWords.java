package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindWords {

    private final ActionLifecycleManagement actionLifecycleManagement;
    private final FindAll findAll;
    private final GetScenes getScenes;

    public FindWords(ActionLifecycleManagement actionLifecycleManagement, FindAll findAll, GetScenes getScenes) {
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.findAll = findAll;
        this.getScenes = getScenes;
    }

    void findAllWordMatches(Matches matches, List<ObjectCollection> objectCollections) {
        while (actionLifecycleManagement.isOkToContinueAction(matches, objectCollections.get(0).getStateImages().size())) {
            List<Image> scenes = getScenes.getScenes(matches.getActionOptions(), objectCollections, 1, 0);
            findWordsSetSceneAnalyses(matches, scenes);
            actionLifecycleManagement.incrementCompletedRepetitions(matches);
        }
    }

    /**
     * Saves all word regions found in the scenes to the Matches parameter.
     * @param matches contains ActionOptions
     * @param scenes the scenes to search
     */
    public void findWordsSetSceneAnalyses(Matches matches, List<Image> scenes) {
        actionLifecycleManagement.printActionOnce(matches);
        for (Image scene : scenes) {
            List<Match> sceneMatches = findAll.findWords(scene, matches.getActionOptions());
            matches.addAll(sceneMatches); // holds all matches found
            SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
            sceneAnalysis.setMatchList(sceneMatches);
            matches.getSceneAnalysisCollection().add(sceneAnalysis);
        }
    }

}
