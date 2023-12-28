package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.ALL_WORDS;

/**
 * Screenshots can be used instead of live scraping to build a state structure. Here, dynamic pixels
 * are not considered and each screenshot represents a unique screen.
 */
@Component
public class GetScreenObservationFromScreenshot {

    private final ScreenObservationManager screenObservationManager;
    private final GetImageJavaCV getImage;
    private final GetScreenObservation getScreenObservation;
    private final GetTransitionImages getTransitionImages;
    private final IllustrateScreenObservation illustrateScreenObservation;
    private final Action action;

    public GetScreenObservationFromScreenshot(ScreenObservationManager screenObservationManager,
                                              GetImageJavaCV getImage,
                                              GetScreenObservation getScreenObservation,
                                              GetTransitionImages getTransitionImages,
                                              IllustrateScreenObservation illustrateScreenObservation,
                                              Action action) {
        this.screenObservationManager = screenObservationManager;
        this.getImage = getImage;
        this.getScreenObservation = getScreenObservation;
        this.getTransitionImages = getTransitionImages;
        this.illustrateScreenObservation = illustrateScreenObservation;
        this.action = action;
    }

    public ScreenObservation getNewScreenObservation(String screenshot) {
        String path = BrobotSettings.screenshotPath + screenshot + ".png";
        Region usableArea = getScreenObservation.getUsableArea();
        ScreenObservation screenObservation = initNewScreenObservation(path);
        List<TransitionImage> links = findAndCapturePotentialLinks(usableArea, path);
        screenObservation.setImages(links);
        if (getScreenObservation.isSaveScreensWithMotionAndImages())
            illustrateScreenObservation.writeIllustratedSceneToHistory(screenObservation);
        return screenObservation;
    }

    public ScreenObservation initNewScreenObservation(String path) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenObservation.setId(screenObservationManager.getNextUnassignedScreenId());
        screenObservation.setScreenshot(getImage.getMatFromFilename(path, ColorCluster.ColorSchemaName.BGR));
        return screenObservation;
    }

    public List<TransitionImage> findAndCapturePotentialLinks(Region usableArea, String path) {
        List<TransitionImage> transitionImages = new ArrayList<>();
        ObjectCollection screens = new ObjectCollection.Builder()
                .withScene_s(path)
                .build();
        ActionOptions findAllWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .addSearchRegion(usableArea)
                .build();
        Matches wordMatches = action.perform(findAllWords, screens);

        for (int i=0; i<wordMatches.size(); i++) {
            TransitionImageHelper transitionImageHelper = getTransitionImages.createTransitionImage(i, wordMatches.getMatchList());
            TransitionImage transitionImage = transitionImageHelper.getTransitionImage();
            Mat image = getImage.getMatFromFile(path, transitionImage.getRegion(), ColorCluster.ColorSchemaName.BGR);
            transitionImage.setImage(image);
            transitionImages.add(transitionImage);
            i = transitionImageHelper.getLastIndex();
        }
        return transitionImages;
    }

}
