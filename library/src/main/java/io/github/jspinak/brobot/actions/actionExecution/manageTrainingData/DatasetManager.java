package io.github.jspinak.brobot.actions.actionExecution.manageTrainingData;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.trainingData.ActionVector;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.GetImageOpenCV;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DatasetManager {

    private final ImageUtils imageUtils;
    private final GetImageOpenCV getImageOpenCV;
    private final SaveTrainingData saveTrainingData;
    private final BufferedImageOps bufferedImageOps;
    private final ActionVectorTranslation actionVectorTranslation;
    private Set<ActionOptions.Action> allowed = EnumSet.of(
            ActionOptions.Action.CLICK,
            ActionOptions.Action.DRAG,
            ActionOptions.Action.MOVE,
            ActionOptions.Action.TYPE,
            ActionOptions.Action.SCROLL_MOUSE_WHEEL,
            ActionOptions.Action.HIGHLIGHT);

    public DatasetManager(ImageUtils imageUtils, GetImageOpenCV getImageOpenCV, SaveTrainingData saveTrainingData,
                          BufferedImageOps bufferedImageOps, ActionVectorOneHot actionVectorTranslation) {
        this.imageUtils = imageUtils;
        this.getImageOpenCV = getImageOpenCV;
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
     * Adds the screenshot taken before the action, the action vector, action description, and a new screenshot.
     * @param matches
     * @return
     */
    public boolean addSetOfData(Matches matches) {
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
        screenshots.add(getImageOpenCV.getScreenshot());
        saveTrainingData.addData(actionVector, actionText, screenshots);
        System.out.println("data saved");
        return true;
    }

    public void saveScreenshotsToFile() {
        saveTrainingData.getTrainingData().forEach(data ->
                data.getScreenshots().forEach(screenshot -> imageUtils.saveBuffImgToFile(screenshot, "screenshot_for_training_")));
    }


}
