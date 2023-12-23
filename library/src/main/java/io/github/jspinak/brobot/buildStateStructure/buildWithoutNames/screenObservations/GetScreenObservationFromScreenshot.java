package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private final GetWordsFromFile getWordsFromFile;

    public GetScreenObservationFromScreenshot(ScreenObservationManager screenObservationManager,
                                              GetImageJavaCV getImage,
                                              GetScreenObservation getScreenObservation,
                                              GetTransitionImages getTransitionImages,
                                              IllustrateScreenObservation illustrateScreenObservation,
                                              GetWordsFromFile getWordsFromFile) {
        this.screenObservationManager = screenObservationManager;
        this.getImage = getImage;
        this.getScreenObservation = getScreenObservation;
        this.getTransitionImages = getTransitionImages;
        this.illustrateScreenObservation = illustrateScreenObservation;
        this.getWordsFromFile = getWordsFromFile;
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
        List<Match> potentialLinks = getWordsFromFile.getWordMatchesFromFile(usableArea, path);
        for (int i=0; i<potentialLinks.size(); i++) {
            TransitionImageHelper transitionImageHelper = getTransitionImages.createTransitionImage(i, potentialLinks);
            TransitionImage transitionImage = transitionImageHelper.getTransitionImage();
            Mat image = getImage.getMatFromFile(path, transitionImage.getRegion(), ColorCluster.ColorSchemaName.BGR);
            transitionImage.setImage(image);
            transitionImages.add(transitionImage);
            i = transitionImageHelper.getLastIndex();
        }
        return transitionImages;
    }

}
