package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
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
    private final GetScreenObservation getScreenObservation;
    private final GetTransitionImages getTransitionImages;
    private final IllustrateScreenObservation illustrateScreenObservation;
    private final Action action;
    private final TransitionImageRepo transitionImageRepo;

    public GetScreenObservationFromScreenshot(ScreenObservationManager screenObservationManager,
                                              GetScreenObservation getScreenObservation,
                                              GetTransitionImages getTransitionImages,
                                              IllustrateScreenObservation illustrateScreenObservation,
                                              Action action, TransitionImageRepo transitionImageRepo) {
        this.screenObservationManager = screenObservationManager;
        this.getScreenObservation = getScreenObservation;
        this.getTransitionImages = getTransitionImages;
        this.illustrateScreenObservation = illustrateScreenObservation;
        this.action = action;
        this.transitionImageRepo = transitionImageRepo;
    }

    public ScreenObservation getNewScreenObservationAndAddImagesToRepo(Pattern screenshot, int screenshotIndex) {
        ScreenObservation screenObservation = getNewScreenObservation(screenshot, screenshotIndex);
        transitionImageRepo.addUniqueImagesToRepo(screenObservation);
        return screenObservation;
    }

    public ScreenObservation getNewScreenObservation(Pattern screenshot, int screenshotIndex) {
        Region usableArea = getScreenObservation.getUsableArea();
        ScreenObservation screenObservation = initNewScreenObservation(screenshot, usableArea);
        List<TransitionImage> links = findImagesWithWordsOnScreen(usableArea, screenshot, screenshotIndex);
        screenObservation.setImages(links);
        if (getScreenObservation.isSaveScreensWithMotionAndImages())
            illustrateScreenObservation.writeIllustratedSceneToHistory(screenObservation);
        return screenObservation;
    }

    public ScreenObservation initNewScreenObservation(Pattern screenshot, Region usableArea) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenshot.setSearchRegionsTo(usableArea);
        screenObservation.setPattern(screenshot);
        screenObservation.setId(screenObservationManager.getNextUnassignedScreenId());
        screenObservation.setScreenshot(screenshot.getMat()); //getImage.getMatFromFilename(screenshot, ColorCluster.ColorSchemaName.BGR));
        return screenObservation;
    }

    public List<TransitionImage> findImagesWithWordsOnScreen(Region usableArea, Pattern screenshot, int screenshotIndex) {
        List<TransitionImage> transitionImages = new ArrayList<>();
        ObjectCollection screens = new ObjectCollection.Builder()
                .withScenes(screenshot)
                .build();
        ActionOptions findAllWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .addSearchRegion(usableArea)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(getTransitionImages.getMinWidthBetweenImages(), 10)
                .build();
        Matches wordMatches = action.perform(findAllWords, screens);
        wordMatches.getMatchList().forEach(match -> {
            TransitionImage transitionImage = new TransitionImage(match, screenshotIndex);
            transitionImage.setImage(match.getMat());
            transitionImages.add(transitionImage);
        });
        return transitionImages;
    }

}
