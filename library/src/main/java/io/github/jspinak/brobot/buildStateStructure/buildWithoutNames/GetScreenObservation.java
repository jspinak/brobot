package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final DynamicPixelFinder dynamicPixelFinder;

    private Region usableArea = new Region();

    public GetScreenObservation(GetImageJavaCV getImageJavaCV, DynamicPixelFinder dynamicPixelFinder) {
        this.getImageJavaCV = getImageJavaCV;
        this.dynamicPixelFinder = dynamicPixelFinder;
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
        // todo wait a few seconds. make getMatFromScreen() an action.
        Mat screenshot = getImageJavaCV.getMatFromScreen();
        screenObservation.setScreenshot(screenshot);
        Mat dynamicPixelMask = dynamicPixelFinder.getDynamicPixelMask(new Region(), .1, 5);
        screenObservation.setDynamicPixelMask(dynamicPixelMask);
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
