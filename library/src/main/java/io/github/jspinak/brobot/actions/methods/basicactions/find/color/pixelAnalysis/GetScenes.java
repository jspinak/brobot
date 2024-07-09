package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenes are used for
 * - finding color
 * - image segmentation (classification)
 * - action illustration
 * In Brobot version 1.0.7, scenes are used for all Find operations.
 */
@Component
public class GetScenes {
    private final Time time;
    private final GetImageJavaCV getImage;

    public GetScenes(Time time, GetImageJavaCV getImage) {
        this.time = time;
        this.getImage = getImage;
    }

    /**
     * If the ObjectCollection is empty and saving history is enabled, a screenshot will be taken.
     * The search regions do not affect the scene. The scene is the entire screen.
     * Taking a screenshot produces one scene, but multiple scenes can be passed in the ObjectCollection.
     * If passed as a parameter, the scenes to use are in the 1st ObjectCollection (index 0) in the Scene list.
     * Scenes can also be taken from the field BrobotSettings.screenshot when performing unit tests.
     */
    public List<Image> getScenes(ActionOptions actionOptions, List<ObjectCollection> objectCollections,
                                 int scenesToCapture, double secondsBetweenCaptures) {
        List<Image> scenes = new ArrayList<>();
        boolean takeScreenshot = isOkToTakeScreenshot(objectCollections.toArray(new ObjectCollection[0]));
        if (takeScreenshot) {
            for (int i=0; i<scenesToCapture; i++) {
                Mat bgr = getImage.getMatFromScreen(new Region());
                scenes.add(new Image(bgr, "screenshot" + i));
                if (i<scenesToCapture-1) time.wait(secondsBetweenCaptures);
            }
            return scenes;
        }
        if (BrobotSettings.mock) {
            // If no scenes are listed in the settings, use a randomly generated scene.
            if (BrobotSettings.screenshots.isEmpty()) scenes.add(Image.getEmptyImage());
            // If scenes are listed in the settings, use them.
            else for (String filename : BrobotSettings.screenshots){
                String relativePath = "../" + BrobotSettings.screenshotPath + filename;
                scenes.add(new Image(relativePath));
            }
            return scenes;
        }
        // If scenes are passed as parameters, use them.
        List<Pattern> scenesInObjectCollection = objectCollections.get(0).getScenes();
        if (!scenesInObjectCollection.isEmpty()) {
            for (Pattern pattern : scenesInObjectCollection) {
                scenes.add(new Image(pattern.getImgpath()));
            }
            return scenes;
        }
        scenes.add(Image.getEmptyImage());
        return scenes;
    }

    /**
     * Some actions require multiple scenes, which can be taken as screenshots or passed as parameters.
     * @param objectCollections these contain scenes as images when passed as parameters
     * @return a list of scenes to use in matrix format
     */
    public List<Image> getScenes(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        return getScenes(actionOptions, objectCollections, 1, 0);
    }

    /**
     * It's not ok to take a screenshot when either
     * - mocking is turned on
     * - scenes are passed as parameters, since these scenes will be used for GUI operations
     *
     * In Brobot version 1.0.7, all operations use screenshots. In previous versions, Brobot GUI operations asked
     *   SikuliX to perform operations on the screen. SikuliX takes a screenshot for pattern matching. This caused
     *   some actions to require two screenshots (once with SikuliX and once with Brobot).
     *
     * @param objectCollections may contain scenes
     * @return true if a screenshot is required
     */
    private boolean isOkToTakeScreenshot(ObjectCollection... objectCollections) {
        if (BrobotSettings.mock) return false;
        //if (objectCollections.length == 0) return false;
        if (!objectCollections[0].getScenes().isEmpty()) return false;
        return true;
    }

}
