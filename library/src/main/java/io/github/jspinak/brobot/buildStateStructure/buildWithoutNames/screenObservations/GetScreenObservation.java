package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.DynamicPixelFinder;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
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
    public ScreenObservation takeScreenshotAndGetImages() {
        ScreenObservation obs = initNewScreenObservation();
        List<Region> dynamicRegions = obs.getMatches().getMatchRegions();
        List<TransitionImage> transitionImages = getTransitionImages.findAndCapturePotentialLinks(usableArea, dynamicRegions);
        obs.setImages(transitionImages);
        if (saveScreensWithMotionAndImages) illustrateScreenObservation.writeIllustratedSceneToHistory(obs);
        return obs;
    }

    public ScreenObservation initNewScreenObservation() {
        ScreenObservation screenObservation = new ScreenObservation();
        screenObservation.setId(screenObservationManager.getNextUnassignedScreenId());
        ActionOptions observation = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.REGIONS_OF_MOTION)
                .setTimesToRepeatIndividualAction(20)
                .setPauseBetweenIndividualActions(.2)
                .setPauseBeforeBegin(3.0)
                .build();
        Matches matches = action.perform(observation);
        Optional<Mat> optScreenshot = matches.getSceneAnalysisCollection().getLastSceneBGR();
        if (optScreenshot.isEmpty()) screenObservation.setScreenshot(new Mat());
        else screenObservation.setScreenshot(optScreenshot.get());
        screenObservation.setMatches(matches);
        screenObservation.setDynamicPixelMask(matches.getPixelMatches());
        return screenObservation;
    }

    public void setUsableArea(Region region) {
        this.usableArea = region;
    }
}
