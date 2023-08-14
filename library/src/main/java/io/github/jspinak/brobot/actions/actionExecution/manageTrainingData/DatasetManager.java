package io.github.jspinak.brobot.actions.actionExecution.manageTrainingData;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.trainingData.ActionVector;
import io.github.jspinak.brobot.imageUtils.GetBufferedImage;
import io.github.jspinak.brobot.imageUtils.GetImage;
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
    private final GetImage getImage;
    private final SaveTrainingData saveTrainingData;
    private final GetBufferedImage getBufferedImage;
    private final ActionVectorTranslation actionVectorTranslation;
    private Set<ActionOptions.Action> allowed = EnumSet.of(
            ActionOptions.Action.CLICK,
            ActionOptions.Action.DRAG,
            ActionOptions.Action.MOVE,
            ActionOptions.Action.TYPE,
            ActionOptions.Action.SCROLL_MOUSE_WHEEL,
            ActionOptions.Action.HIGHLIGHT);

    public DatasetManager(ImageUtils imageUtils, GetImage getImage, SaveTrainingData saveTrainingData,
                          GetBufferedImage getBufferedImage, ActionVectorOneHot actionVectorTranslation) {
        this.imageUtils = imageUtils;
        this.getImage = getImage;
        this.saveTrainingData = saveTrainingData;
        this.getBufferedImage = getBufferedImage;
        this.actionVectorTranslation = actionVectorTranslation;
    }

    private boolean isValidAction(ActionOptions actionOptions) {
        return allowed.contains(actionOptions.getAction());
    }

    public boolean addSetOfData(Matches matches) {
        if (!isValidAction(matches.getActionOptions())) return false;
        Optional<Mat> optBGR = matches.getSceneAnalysisCollection().getLastSceneBGR();
        if (optBGR.isEmpty()) return false;
        ActionVector actionVector = actionVectorTranslation.toVector(matches);
        String actionText = matches.getActionDescription();
        ArrayList<BufferedImage> screenshots = new ArrayList<>();
        screenshots.add(getBufferedImage.convert(optBGR.get()));
        screenshots.add(getImage.getScreenshot());
        saveTrainingData.addData(actionVector, actionText, screenshots);
        return true;
    }

    public void saveScreenshotsToFile() {
        saveTrainingData.getTrainingData().forEach(data ->
                data.getScreenshots().forEach(screenshot -> imageUtils.saveBuffImgToFile(screenshot, "screenshot_for_training_")));
    }


}
