package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
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
    private final GetStatelessImages getStatelessImages;
    private final IllustrateScreenObservation illustrateScreenObservation;
    private final Action action;
    private final StatelessImageRepo statelessImageRepo;

    public GetScreenObservationFromScreenshot(ScreenObservationManager screenObservationManager,
                                              GetScreenObservation getScreenObservation,
                                              GetStatelessImages getStatelessImages,
                                              IllustrateScreenObservation illustrateScreenObservation,
                                              Action action, StatelessImageRepo statelessImageRepo) {
        this.screenObservationManager = screenObservationManager;
        this.getScreenObservation = getScreenObservation;
        this.getStatelessImages = getStatelessImages;
        this.illustrateScreenObservation = illustrateScreenObservation;
        this.action = action;
        this.statelessImageRepo = statelessImageRepo;
    }

    public ScreenObservation getNewScreenObservationAndAddImagesToRepo(Pattern screenshot, int screenshotIndex) {
        ScreenObservation screenObservation = getNewScreenObservation(screenshot, screenshotIndex);
        statelessImageRepo.addUniqueImagesToRepo(screenObservation);
        return screenObservation;
    }

    /**
     * Finds words on the screenshot and saves them as images (TransitionImage).
     * @param screenshot the screenshot to search for words.
     * @param screenshotIndex the index is saved with the ScreenObservation
     * @return the resulting ScreenObservation
     */
    public ScreenObservation getNewScreenObservation(Pattern screenshot, int screenshotIndex) {
        Region usableArea = getScreenObservation.getUsableArea();
        ScreenObservation screenObservation = initNewScreenObservation(screenshot, usableArea);
        List<StatelessImage> links = findImagesWithWordsOnScreen(usableArea, screenshot, screenshotIndex);
        screenObservation.setImages(links);
        if (getScreenObservation.isSaveScreensWithMotionAndImages())
            illustrateScreenObservation.writeIllustratedSceneToHistory(screenObservation);
        return screenObservation;
    }

    public ScreenObservation initNewScreenObservation(Pattern screenshot, Region usableArea) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenshot.setSearchRegionsTo(usableArea);
        screenObservation.setPattern(screenshot);
        screenObservation.setId(screenObservationManager.getAndIncrementId());
        screenObservation.setScreenshot(screenshot.getMat()); //getImage.getMatFromFilename(screenshot, ColorCluster.ColorSchemaName.BGR));
        return screenObservation;
    }

    public List<StatelessImage> findImagesWithWordsOnScreen(Region usableArea, Pattern screenshot, int screenshotIndex) {
        List<StatelessImage> statelessImages = new ArrayList<>();
        ObjectCollection screens = new ObjectCollection.Builder()
                .withScenes(screenshot)
                .build();
        ActionOptions findAllWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .addSearchRegion(usableArea)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(getStatelessImages.getMinWidthBetweenImages(), 10)
                .build();
        Matches wordMatches = action.perform(findAllWords, screens);
        for (int i=0; i<wordMatches.getMatchList().size(); i++) {
            Match match = wordMatches.getMatchList().get(i);
            StatelessImage statelessImage = new StatelessImage(match, screenshotIndex);
            statelessImage.setMatch(match);
            statelessImages.add(statelessImage);
        }
        return statelessImages;
    }

}
