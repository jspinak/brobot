package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservations;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.absdiff;

@Component
public class ScreenMatMethods {

    /**
     * Compare the target screen with every other screen. The first time, you will get a region that is
     * different (after using a bounding box to eliminate noise). The next comparison is between this region
     * in the target screen and the same region in the next comparison screen. It's possible in using this method
     * that at the end no region will be left. This is because screens are saved because they are unique, but not
     * because they have unique parts. In the case that there are no unique parts, different states make up the
     * screen and these states can be found in different combinations in other screens. In this case we need
     * a more complicated algorithm to identify screens, one that recognizes the unique combination of states.
     * The app state can usually be determined using this method since the app state contains images that
     * are in every screen (for example, the app logo). It will also detect white space and background. Knowing
     * where white space and background is helps with finding states and state objects.
     * @param screen the target screen
     * @param observations all other screens
     * @return the unique area in the target screen
     */
    public Mat getUniqueArea(ScreenObservation screen, ScreenObservations observations) {
        return new Mat();
    }

    /**
     * This algorithm can be used to find shared pixels among a set of screenshots.
     * @param screenObservations contain the screens to analyze
     * @return a mask of the shared pixels
     */
    public Mat getSharedPixels(ScreenObservations screenObservations) {
        List<ScreenObservation> observations = screenObservations.getAll().stream().toList();
        if (observations.size() < 2) return new Mat();
        Mat sharedPixels = observations.get(0).getScreenshot();
        for (int i=1; i<observations.size(); i++) {
            sharedPixels = compareMats(sharedPixels, observations.get(i).getScreenshot());
        }
        return sharedPixels;
    }

    /**
     * Finds pixels shared by different subsets of screenshots and saves these as states.
     * Individual pixel values are mapped to screenshots. The resulting shared maps become states.
     * @param screenObservations the set of screenshots to use
     * @return a list of states and the ids of their corresponding screenshots
     */
    public List<ObservedState> getObservedStates(ScreenObservations screenObservations) {
        // method 1: go pixel for pixel
        /* method 2: find areas that can be described as regions and appear in more than one screenshot

         */


        return new ArrayList<>();
    }

    /**
     * Compare the screenshots to determine individual states. States can be shared among screenshots, and
     * screenshots can be uniquely identified by their collection of states.
     * @param screen
     * @param observations
     * @return
     */
    public List<ObservedState> getObservedStates(ScreenObservation screen, ScreenObservations observations) {
        for (ScreenObservation screenObservation : observations.getAll()) {
            Mat dist = compareMats(screen.getScreenshot(), screenObservation.getScreenshot());
            //ObservedState newObservedState = new ObservedState();
        }


        return new ArrayList<>();
    }

    public void getSharedBoundingBoxes(ScreenObservation obs1, ScreenObservation obs2) {

    }

    public Mat compareScreenObservations(ScreenObservation obs1, ScreenObservation obs2) {
        return compareMats(obs1.getScreenshot(), obs2.getScreenshot());
    }

    public Mat compareMats(Mat mat1, Mat mat2) {
        Mat dist = new Mat(mat1.size(), mat1.type());
        absdiff(mat1, mat2, dist);
        return dist;
    }

}
