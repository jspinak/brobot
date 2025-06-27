package io.github.jspinak.brobot.tools.ml.dataset;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.ml.dataset.encoding.ActionVectorTranslator;
import io.github.jspinak.brobot.tools.ml.dataset.encoding.OneHotActionVectorEncoder;
import io.github.jspinak.brobot.tools.ml.dataset.io.TrainingExampleWriter;
import io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector;
import io.github.jspinak.brobot.tools.ml.dataset.model.TrainingExample;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Manages the creation and storage of training datasets for machine learning.
 * <p>
 * DatasetManager coordinates the collection of training data during GUI automation
 * execution. It captures before/after screenshots, converts actions to vectors,
 * and stores the data for later use in training neural networks.
 * <p>
 * Only specific action types suitable for ML training are allowed:
 * CLICK, DRAG, MOVE, TYPE, SCROLL_MOUSE_WHEEL, and HIGHLIGHT. These represent
 * basic GUI interactions that directly modify the interface.
 * <p>
 * The typical workflow is:
 * <ol>
 * <li>An action is executed and produces an ActionResult</li>
 * <li>DatasetManager validates the action type</li>
 * <li>The "before" screenshot is extracted from the ActionResult</li>
 * <li>The action is converted to a vector representation</li>
 * <li>An "after" screenshot is captured</li>
 * <li>All data is saved via SaveTrainingData</li>
 * </ol>
 *
 * @see TrainingExampleWriter
 * @see ActionVectorTranslator
 * @see TrainingExample
 */
@Component
public class DatasetManager {

    private final ImageFileUtilities imageUtils;
    private final TrainingExampleWriter saveTrainingData;
    private final BufferedImageUtilities bufferedImageOps;
    private final ActionVectorTranslator actionVectorTranslation;
    private Set<ActionOptions.Action> allowed = EnumSet.of(
            ActionOptions.Action.CLICK,
            ActionOptions.Action.DRAG,
            ActionOptions.Action.MOVE,
            ActionOptions.Action.TYPE,
            ActionOptions.Action.SCROLL_MOUSE_WHEEL,
            ActionOptions.Action.HIGHLIGHT);

    public DatasetManager(ImageFileUtilities imageUtils, TrainingExampleWriter saveTrainingData,
                          BufferedImageUtilities bufferedImageOps, OneHotActionVectorEncoder actionVectorTranslation) {
        this.imageUtils = imageUtils;
        this.saveTrainingData = saveTrainingData;
        this.bufferedImageOps = bufferedImageOps;
        this.actionVectorTranslation = actionVectorTranslation;
    }

    private boolean isValidAction(ActionOptions actionOptions) {
        if (actionOptions == null) {
            System.out.println("ActionOptions is null");
            return false;
        }
        return allowed.contains(actionOptions.getAction());
    }

    /**
     * Captures and stores a complete training example from an executed action.
     * <p>
     * This method collects all necessary data for a training example:
     * <ul>
     * <li>The "before" screenshot from the ActionResult's scene analysis</li>
     * <li>The action converted to a vector representation</li>
     * <li>A text description of the action</li>
     * <li>An "after" screenshot captured from the current screen</li>
     * </ul>
     * <p>
     * The method will fail if:
     * <ul>
     * <li>The action type is not in the allowed set</li>
     * <li>No "before" screenshot is available in the ActionResult</li>
     * </ul>
     * <p>
     * Console output is produced for validation failures and successful saves.
     *
     * @param matches The ActionResult containing the executed action and its results
     * @return true if the data was successfully captured and saved, false otherwise
     */
    public boolean addSetOfData(ActionResult matches) {
        if (!isValidAction(matches.getActionOptions())) {
            System.out.println("action is not valid for dataset");
            return false;
        }
        Optional<Mat> optBGR = matches.getSceneAnalysisCollection().getLastSceneBGR();
        if (optBGR.isEmpty()) {
            System.out.println("a screenshot was not saved for this action");
            return false;
        }
        ActionVector actionVector = actionVectorTranslation.toVector(matches);
        String actionText = matches.getActionDescription();
        ArrayList<BufferedImage> screenshots = new ArrayList<>();
        screenshots.add(bufferedImageOps.convert(optBGR.get()));
        screenshots.add(bufferedImageOps.getBuffImgFromScreen(new Region()));
        saveTrainingData.addData(actionVector, actionText, screenshots);
        System.out.println("data saved");
        return true;
    }

    /**
     * Exports all collected screenshots to individual image files.
     * <p>
     * This utility method iterates through all stored training data and saves
     * each screenshot as a separate file. Files are named with the prefix
     * "screenshot_for_training_" followed by a timestamp.
     * <p>
     * This is useful for:
     * <ul>
     * <li>Manual inspection of training data quality</li>
     * <li>Debugging dataset collection issues</li>
     * <li>Creating alternative dataset formats</li>
     * </ul>
     */
    public void saveScreenshotsToFile() {
        saveTrainingData.getTrainingData().forEach(data ->
                data.getScreenshots().forEach(screenshot -> imageUtils.saveBuffImgToFile(screenshot, "screenshot_for_training_")));
    }


}
