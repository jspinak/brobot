package io.github.jspinak.brobot.action.internal.find.match;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;
import io.github.jspinak.brobot.action.ActionResult;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Captures visual and text content from matched regions after find operations.
 * <p>
 * This component is responsible for post-processing matches to extract their visual
 * representations (Mat/BufferedImage) and text content from the scenes where they
 * were found. It operates after all match adjustments (fusion, position shifts) have
 * been completed to ensure accurate content capture from the final match regions.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Captures images from specific scenes based on action options</li>
 *   <li>Extracts text using OCR when required</li>
 *   <li>Handles scene selection for multi-scene captures</li>
 *   <li>Respects text extraction conditions based on action type</li>
 * </ul>
 * 
 * <p>The component ensures that matches contain the actual visual content they
 * represent, enabling subsequent operations that require image data or text
 * extraction from the matched regions.
 * 
 * @see Match
 * @see Scene
 * @see ActionResult
 */
@Component
public class MatchContentExtractor {

    private final ExecutionModeController mockOrLive;

    /**
     * Creates a new MatchContentExtractor instance.
     * 
     * @param mockOrLive Service for executing operations in mock or live mode
     */
    public MatchContentExtractor(ExecutionModeController mockOrLive) {
        this.mockOrLive = mockOrLive;
    }

    /**
     * Captures both visual and text content from matched regions.
     * <p>
     * This method should run after all adjustments are made to the match objects,
     * including position shifts, match fusion, and other transformations specified
     * by ActionOptions. The order is critical because these adjustments affect the
     * underlying regions from which content is captured.
     * 
     * <p>The method performs two operations:
     * <ol>
     *   <li>Captures the visual content (Mat/BufferedImage) from the scene</li>
     *   <li>Extracts text using OCR (currently commented out due to empty returns)</li>
     * </ol>
     * 
     * @param matches The ActionResult containing matches to process. The matches
     *                within this object are modified by setting their visual content
     *                and scene references.
     */
    public void set(ActionResult matches) {
        setMat(matches);
        //setText(matches); // this method always returns "" despite doing OCR on a BufferedImage
    }

    /**
     * Captures visual content from the specified scene for all matches.
     * <p>
     * This method extracts the image data (Mat/BufferedImage) from the scene at the
     * regions defined by each match. The scene to use is determined by the
     * {@code sceneToUseForCaptureAfterFusingMatches} setting in ActionOptions, which
     * allows capturing from different scenes than where the match was originally found.
     * 
     * <p>This flexibility is useful when:
     * <ul>
     *   <li>Matches are found in one scene but content needs to be captured from another</li>
     *   <li>Multiple scenes represent different states of the same UI element</li>
     *   <li>Post-processing requires content from a specific scene index</li>
     * </ul>
     * 
     * <p><b>Side Effects:</b> Each match in the ActionResult is modified by:
     * <ul>
     *   <li>Setting its scene reference to the specified scene</li>
     *   <li>Capturing and storing the image content from that scene</li>
     * </ul>
     * 
     * @param matches The ActionResult containing matches to process. The match list
     *                is modified by setting scene references and capturing images.
     */
    public void setMat(ActionResult matches) {
        int sceneIndex = matches.getActionOptions().getSceneToUseForCaptureAfterFusingMatches();
        List<Scene> scenes = matches.getSceneAnalysisCollection().getScenes();
        if (sceneIndex < 0 || sceneIndex >= scenes.size()) return;
        Scene scene = matches.getSceneAnalysisCollection().getScenes().get(sceneIndex);
        for (Match m : matches.getMatchList()) {
            m.setScene(scene);
            m.setImageWithScene();
        }
    }

    /**
     * Extracts text content from match regions using OCR.
     * <p>
     * Text extraction is performed conditionally based on:
     * <ul>
     *   <li>Whether the match already has text (avoids redundant OCR)</li>
     *   <li>The action type and find configuration</li>
     *   <li>Whether match fusion was applied (which removes original text)</li>
     * </ul>
     * 
     * <p>The method delegates the actual OCR operation to the MockOrLive service,
     * which handles the difference between mock testing and live execution environments.
     * Text extraction is computationally expensive, so it's only performed when necessary.
     * 
     * <p><b>Side Effects:</b> Each match may be modified by setting its text content
     * based on OCR results from the match region.
     * 
     * @param matches The ActionResult containing matches to process. Matches without
     *                text or those meeting reset criteria will have text extracted
     *                and set via OCR.
     */
    public void setText(ActionResult matches) {
        for (Match match : matches.getMatchList()) {
            if (match.getText() == null || match.getText().isEmpty() || isOkToReset(matches.getActionOptions()))
                mockOrLive.setText(match);
        }
    }

    /**
     * Determines whether existing text content should be replaced with new OCR results.
     * <p>
     * Text reset is appropriate in specific scenarios:
     * <ul>
     *   <li>FIND actions - May need fresh text extraction from updated regions</li>
     *   <li>ALL_WORDS searches - Require comprehensive text extraction</li>
     *   <li>After fusion - Original text is lost and needs re-extraction</li>
     * </ul>
     * 
     * <p>The commented fusion check is handled implicitly because fused matches
     * lose their text content, triggering re-extraction through the null check
     * in {@link #setText(ActionResult)}.
     * 
     * @param actionOptions The configuration to evaluate for reset conditions
     * @return true if existing text should be replaced with new OCR results,
     *         false if existing text should be preserved
     */
    private boolean isOkToReset(ActionOptions actionOptions) {
        return actionOptions.getAction() == ActionOptions.Action.FIND ||
                actionOptions.getFind() == ActionOptions.Find.ALL_WORDS;
                /*
                The Pattern disappears when match objects are fused, but this will be caught by match.getText() == null
                and in cases where a match was not fused and has text, we don't need to search it again.
                 */
                //actionOptions.getFusionMethod() != NONE;
    }

}
