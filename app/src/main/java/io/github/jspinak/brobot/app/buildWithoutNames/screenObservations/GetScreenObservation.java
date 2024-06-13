package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * The goal of this class is to find all images that lead to transitions. These are images that can be acted on
 * to cause a change in the application. A simple example is a text link in a web page. Apps like Selenium do this
 * for webpages by looking at the source code. The task is much more complex when done visually but applies to
 * all types of applications.
 */
@Component
@Setter
@Getter
public class GetScreenObservation {
    private final Action action;
    private final IllustrateScreenObservation illustrateScreenObservation;
    private final GetTransitionImages getTransitionImages;
    private final ScreenObservationManager screenObservationManager;

    private Region usableArea = new Region();
    private boolean saveScreensWithMotionAndImages;

    public GetScreenObservation(Action action, IllustrateScreenObservation illustrateScreenObservation,
                                GetTransitionImages getTransitionImages, ScreenObservationManager screenObservationManager) {
        this.action = action;
        this.illustrateScreenObservation = illustrateScreenObservation;
        this.getTransitionImages = getTransitionImages;
        this.screenObservationManager = screenObservationManager;
    }

    /**
     * Observe the screen, record screenshots and determine fixed and dynamic pixels,
     * and save potential transition images in the usable area.
     * @return the newly created screenshot
     */
    public ScreenObservation takeScreenshotAndGetImages(StateStructureTemplate stateStructureTemplate) {
        ScreenObservation obs = initNewScreenObservation(stateStructureTemplate);
        List<TransitionImage> transitionImages = getTransitionImages.findAndCapturePotentialLinks(
                usableArea, obs, obs.getId(), stateStructureTemplate);
        obs.setImages(transitionImages);
        if (saveScreensWithMotionAndImages) illustrateScreenObservation.writeIllustratedSceneToHistory(obs);
        return obs;
    }

    public ScreenObservation initNewScreenObservation(StateStructureTemplate stateStructureTemplate) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenObservation.setId(screenObservationManager.getNextUnassignedScreenId());
        if (stateStructureTemplate.isLive()) setScreenObservationLive(screenObservation);
        else setScreenObservationData(screenObservation, stateStructureTemplate);
        return screenObservation;
    }

    private void setScreenObservationData(ScreenObservation screenObservation, StateStructureTemplate stateStructureTemplate) {
        int index = getScreenObservationManager().getScreenIndex();
        Pattern screen = stateStructureTemplate.getScreenshots().get(index);
        getScreenObservationManager().setScreenIndex(index + 1);
        screenObservation.setPattern(screen);
        screenObservation.setScreenshot(screen.getMat());
        //there are no regions of motion when not running live
    }

    private void setScreenObservationLive(ScreenObservation screenObservation) {
        ActionOptions observation = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.REGIONS_OF_MOTION)
                .setTimesToRepeatIndividualAction(20)
                .setPauseBetweenIndividualActions(.2)
                .setPauseBeforeBegin(3.0)
                .build();
        Matches matches = action.perform(observation);
        matches.getSceneAnalysisCollection().getLastScene().ifPresent(scene -> screenObservation.setPattern(new Pattern(scene)));
        Optional<Mat> optScreenshot = matches.getSceneAnalysisCollection().getLastSceneBGR();
        if (optScreenshot.isEmpty()) screenObservation.setScreenshot(new Mat());
        else screenObservation.setScreenshot(optScreenshot.get());
        screenObservation.setMatches(matches);
        screenObservation.setDynamicPixelMask(matches.getMask());
    }

}
