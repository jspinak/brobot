package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The goal of this class is to find all images that lead to transitions. These are images that can be acted on
 * to cause a change in the application. A simple example is a text link in a web page. Apps like Selenium do this
 * for webpages by looking at the source code. The task is much more complex when done visually but applies to
 * all types of applications.
 */
@Component
public class GetScreenObservation {

    private final GetImageJavaCV getImageJavaCV;
    private final Action action;

    Region usableArea = new Region();

    public GetScreenObservation(GetImageJavaCV getImageJavaCV, Action action) {
        this.getImageJavaCV = getImageJavaCV;
        this.action = action;
    }

    /* Add only match(es) within the usable area.
     */
    public ScreenObservation takeScreenshotAndGetImages(int id) {
        ScreenObservation obs = initNewScreenObservation(id);
        List<Match> wordLocations = getWordLocations();
        for (Match match : wordLocations) {
            if (usableArea.contains(match)) {
                obs.getImages().add(new TransitionImage(match));
            }
        }
        return obs;
    }

    public ScreenObservation initNewScreenObservation(int id) {
        ScreenObservation screenObservation = new ScreenObservation();
        screenObservation.setId(id);
        Mat screenshot = getImageJavaCV.getMatFromScreen();
        screenObservation.setScreenshot(screenshot);
        return screenObservation;
    }

    /*
    One method of finding transition images is to locate and click on words. This works especially well for webpages.
     */
    public List<Match> getWordLocations() {
        Screen screen = new Screen();
        List<Match> wordLocations = screen.findWords();
        return wordLocations;
    }

    public void setUsableArea(Region region) {
        this.usableArea = region;
    }
}
