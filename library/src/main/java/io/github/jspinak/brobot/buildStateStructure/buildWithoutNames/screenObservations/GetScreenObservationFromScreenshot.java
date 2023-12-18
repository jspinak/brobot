package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
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

    public GetScreenObservationFromScreenshot(ScreenObservationManager screenObservationManager,
                                              GetImageJavaCV getImage,
                                              GetScreenObservation getScreenObservation,
                                              GetTransitionImages getTransitionImages,
                                              IllustrateScreenObservation illustrateScreenObservation) {
        this.screenObservationManager = screenObservationManager;
        this.getImage = getImage;
        this.getScreenObservation = getScreenObservation;
        this.getTransitionImages = getTransitionImages;
        this.illustrateScreenObservation = illustrateScreenObservation;
    }

    public ScreenObservation getNewScreenObservation(Image screenshot) {
        ScreenObservation screenObservation = initNewScreenObservation(screenshot);
        setTransitionImages(screenObservation, getScreenObservation.getUsableArea());
        List<TransitionImage> links = findAndCapturePotentialLinks(
                getScreenObservation.getUsableArea(), screenshot.getFirstFilename());
        screenObservation.setImages(links);
        return screenObservation;
    }

    public ScreenObservation initNewScreenObservation(Image screenshot) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenObservation.setId(screenObservationManager.getNextUnassignedScreenId());
        screenObservation.setScreenshot(getImage.getMats(screenshot, ColorCluster.ColorSchemaName.BGR).get(0));
        return screenObservation;
    }

    public void setTransitionImages(ScreenObservation obs, Region usableArea) {
        List<TransitionImage> transitionImages = getTransitionImages.findAndCapturePotentialLinks(usableArea, new ArrayList<>());
        obs.setImages(transitionImages);
        if (getScreenObservation.isSaveScreensWithMotionAndImages()) illustrateScreenObservation.writeIllustratedSceneToHistory(obs);
    }

    public List<TransitionImage> findAndCapturePotentialLinks(Region usableArea, String filename) {
        List<TransitionImage> transitionImages = new ArrayList<>();
        List<Match> potentialLinks = getWordMatchesFromFile(usableArea, filename);
        for (int i=0; i<potentialLinks.size(); i++) {
            TransitionImageHelper transitionImageHelper = getTransitionImages.createTransitionImage(i, potentialLinks);
            TransitionImage transitionImage = transitionImageHelper.getTransitionImage();
            Mat image = getImage.getMatFromFile(filename, transitionImage.getRegion(), ColorCluster.ColorSchemaName.BGR);
            transitionImage.setImage(image);
            transitionImages.add(transitionImage);
            i = transitionImageHelper.getLastIndex();
        }
        return transitionImages;
    }

    public List<Match> getWordMatchesFromFile(Region usableArea, String filename) {
        File file = new File(BrobotSettings.screenshotPath + filename);
        String path = file.getAbsolutePath();
        List<Match> matches = new ArrayList<>();
        Finder f = new Finder(path);
        f.findWords();
        while (f.hasNext()) {
            Match match = f.next();
            if (usableArea.contains(match)) matches.add(f.next());
        }
        f.destroy();
        return matches;
    }
}
