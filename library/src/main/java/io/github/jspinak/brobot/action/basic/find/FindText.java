package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Finds text elements within GUI scenes using OCR (Optical Character Recognition).
 * <p>
 * This component specializes in locating and extracting text from screenshots,
 * supporting the ALL_WORDS find strategy. It performs comprehensive text detection
 * across multiple scenes, organizing results into scene analyses for structured
 * access to text matches.
 * 
 * <p>The text finding process includes:
 * <ul>
 *   <li>Scene acquisition based on action options</li>
 *   <li>OCR processing to detect all text regions</li>
 *   <li>Region filtering based on search constraints</li>
 *   <li>Scene-specific match aggregation</li>
 *   <li>Action lifecycle management for proper execution control</li>
 * </ul>
 * 
 * <p>This component is essential for:
 * <ul>
 *   <li>GUI automation that needs to interact with text elements</li>
 *   <li>Screen content validation based on text presence</li>
 *   <li>Data extraction from application interfaces</li>
 *   <li>Text-based navigation and state detection</li>
 * </ul>
 * 
 * @see FindAll
 * @see SceneAnalysis
 * @see ActionLifecycleManagement
 */
@Component
public class FindText {

    private final ActionLifecycleManagement actionLifecycleManagement;
    private final FindAll findAll;
    private final SceneProvider getScenes;

    /**
     * Creates a new FindText instance with required dependencies.
     * 
     * @param actionLifecycleManagement Service for managing action lifecycle and execution control
     * @param findAll Service for performing text detection and filtering
     * @param getScenes Service for acquiring scenes based on action options
     */
    public FindText(ActionLifecycleManagement actionLifecycleManagement, FindAll findAll, SceneProvider getScenes) {
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.findAll = findAll;
        this.getScenes = getScenes;
    }

    /**
     * Finds all text words across multiple scenes with repetition support.
     * <p>
     * This method implements a repetitive text search strategy that continues
     * acquiring and processing scenes until the action lifecycle indicates completion.
     * This approach is useful for:
     * <ul>
     *   <li>Monitoring changing text content over time</li>
     *   <li>Waiting for specific text to appear</li>
     *   <li>Capturing text from dynamic interfaces</li>
     * </ul>
     * 
     * <p>The method respects action lifecycle constraints, allowing for:
     * <ul>
     *   <li>Maximum repetition limits</li>
     *   <li>Timeout conditions</li>
     *   <li>Early termination when sufficient matches are found</li>
     * </ul>
     * 
     * @param matches The ActionResult to populate with text matches and scene analyses.
     *                Modified throughout execution with cumulative results.
     * @param objectCollections Collections containing configuration for scene acquisition.
     *                         The first collection's state image count is used for
     *                         lifecycle management.
     */
    void findAllWordMatches(ActionResult matches, List<ObjectCollection> objectCollections) {
        while (actionLifecycleManagement.isOkToContinueAction(matches, objectCollections.get(0).getStateImages().size())) {
            List<Scene> scenes = getScenes.getScenes(matches.getActionOptions(), objectCollections, 1, 0);
            findWordsSetSceneAnalyses(matches, scenes);
            actionLifecycleManagement.incrementCompletedRepetitions(matches);
        }
    }

    /**
     * Finds text words in scenes and creates scene-specific analyses.
     * <p>
     * This method processes each scene to detect all text elements, creating both
     * a global collection of matches and scene-specific analyses. Each scene analysis
     * maintains its own match list, enabling scene-by-scene text examination.
     * 
     * <p>The method performs the following for each scene:
     * <ol>
     *   <li>Executes OCR to find all text words</li>
     *   <li>Filters results based on action options (search regions)</li>
     *   <li>Adds matches to the global result set</li>
     *   <li>Creates a SceneAnalysis with scene-specific matches</li>
     *   <li>Stores the analysis for later retrieval</li>
     * </ol>
     * 
     * @param matches The ActionResult to populate with found text matches. Contains
     *                ActionOptions for search configuration and is modified to include
     *                all matches and scene analyses.
     * @param scenes The list of scenes to search for text. Each scene represents
     *               a screenshot or screen capture to be processed.
     */
    public void findWordsSetSceneAnalyses(ActionResult matches, List<Scene> scenes) {
        actionLifecycleManagement.printActionOnce(matches);
        for (Scene scene : scenes) {
            List<Match> sceneMatches = findAll.findWords(scene, matches.getActionOptions());
            matches.addAll(sceneMatches); // holds all matches found
            SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
            sceneAnalysis.setMatchList(sceneMatches);
            matches.getSceneAnalysisCollection().add(sceneAnalysis);
        }
    }

}
