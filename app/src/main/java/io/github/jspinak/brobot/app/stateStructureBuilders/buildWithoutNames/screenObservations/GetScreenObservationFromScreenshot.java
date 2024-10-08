package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.ALL_WORDS;

/**
 * Screenshots can be used instead of live scraping to build a state structure. Here, dynamic pixels
 * are not considered and each screenshot represents a unique screen.
 */
@Component
@Setter
public class GetScreenObservationFromScreenshot {

    private final Action action;
    private final StatelessImageOps statelessImageOps;

    public GetScreenObservationFromScreenshot(Action action, StatelessImageOps statelessImageOps) {
        this.action = action;
        this.statelessImageOps = statelessImageOps;
    }

    public List<ScreenObservation> getScreenObservations(StateStructureConfiguration config,
                                                         List<StatelessImage> statelessImages) {
        List<ScreenObservation> observations = new ArrayList<>();
        config.getScenes().forEach(scene -> {
            ScreenObservation newObservation = getNewScreenObservationAndProcessImages(scene, config, statelessImages);
            newObservation.setId(observations.size());
            observations.add(newObservation);
        });
        return observations;
    }

    public ScreenObservation getNewScreenObservationAndProcessImages(Scene screenshot,
                                                                     StateStructureConfiguration config,
                                                                     List<StatelessImage> statelessImages) {
        ScreenObservation screenObservation = getNewScreenObservation(screenshot, config);
        statelessImageOps.addOrMergeStatelessImages(screenObservation, statelessImages, config);
        return screenObservation;
    }

    /**
     * Finds words on the screenshot and saves them as images (StatelessImage).
     * @param screenshot the screenshot to search for words.
     * @return the resulting ScreenObservation
     */
    public ScreenObservation getNewScreenObservation(Scene screenshot, StateStructureConfiguration config) {
        ScreenObservation screenObservation = initNewScreenObservation(screenshot, config);
        List<StatelessImage> images = findImagesWithWordsOnScreen(config, screenshot);
        screenObservation.setImages(images);
        return screenObservation;
    }

    public ScreenObservation initNewScreenObservation(Scene scene, StateStructureConfiguration config) {
        ScreenObservation screenObservation = new ScreenObservation();
        scene.getPattern().setSearchRegionsTo(config.getUsableArea());
        screenObservation.setScene(scene);
        return screenObservation;
    }

    public List<StatelessImage> findImagesWithWordsOnScreen(StateStructureConfiguration config, Scene scene) {
        List<StatelessImage> statelessImages = new ArrayList<>();
        ObjectCollection screens = new ObjectCollection.Builder()
                .withScenes(scene)
                .build();
        ActionOptions findAllWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .addSearchRegion(config.getUsableArea())
                .setMinArea(config.getMinImageArea())
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(config.getMinWidthBetweenImages(), 10)
                .build();
        Matches wordMatches = action.perform(findAllWords, screens);
        for (int i=0; i<wordMatches.getMatchList().size(); i++) {
            Match match = wordMatches.getMatchList().get(i);
            StatelessImage statelessImage = new StatelessImage(match, scene);
            statelessImages.add(statelessImage);
        }
        return statelessImages;
    }

}
