package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.DynamicPixelFinder;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The goal of this class is to find all images that lead to transitions. These are images that can be acted on
 * to cause a change in the application. A simple example is a text link in a web page. Apps like Selenium do this
 * for webpages by looking at the source code. The task is much more complex when done visually but applies to
 * all types of applications.
 */
@Component
public class GetScreenObservation {

    private final GetImageJavaCV getImageJavaCV;
    private final DynamicPixelFinder dynamicPixelFinder;
    private final Action action;

    private Region usableArea = new Region();

    public GetScreenObservation(GetImageJavaCV getImageJavaCV, DynamicPixelFinder dynamicPixelFinder, Action action) {
        this.getImageJavaCV = getImageJavaCV;
        this.dynamicPixelFinder = dynamicPixelFinder;
        this.action = action;
    }

    /*
    Only add match(es) within the usable area.
     */
    public ScreenObservation takeScreenshotAndGetImages(int id) {
        ScreenObservation obs = initNewScreenObservation(id);
        List<Match> wordLocations = new Screen().findWords();
        List<Mat> images = getImagesFromLocations(wordLocations);
        for (int i=0; i<Math.min(wordLocations.size(), images.size()); i++) {
            Match match = wordLocations.get(i);
            if (usableArea.contains(match)) {
                TransitionImage transitionImage = new TransitionImage(match);
                Mat image = getImageJavaCV.getMatFromScreen(new Region(match)); // save the image
                transitionImage.setImage(image);
                obs.getImages().add(transitionImage);
            }
        }
        return obs;
    }

    public ScreenObservation initNewScreenObservation(int id) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenObservation.setId(id);
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
        //Mat dynamicPixelMask = dynamicPixelFinder.getDynamicPixelMask(new Region(), .1, 5);
        //screenObservation.setDynamicPixelMask(dynamicPixelMask);
        return screenObservation;
    }

    /**
     * Match objects contain Image objects, but these Image objects are null when the Match objects are created.
     * A Brobot Image can be added to the TransitionImage.
     * @param matches the Match objects found with screen.findWords().
     * @return the Image inside the match
     */
    public List<Mat> getImagesFromLocations(List<Match> matches) {
        List<Mat> images = new ArrayList<>();
        for (Match match : matches) {
            Mat mat = getImageJavaCV.getMatFromScreen(new Region(match));
            images.add(mat);
        }
        return images;
    }

    public void setUsableArea(Region region) {
        this.usableArea = region;
    }
}
