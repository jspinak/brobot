package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
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
    public List<Scene> getScenes(ActionOptions actionOptions, List<ObjectCollection> objectCollections,
                                 int scenesToCapture, double secondsBetweenCaptures) {
        List<Scene> scenes = new ArrayList<>();
        boolean takeScreenshot = isOkToTakeScreenshot(objectCollections.toArray(new ObjectCollection[0]));
        if (takeScreenshot) {
            for (int i=0; i<scenesToCapture; i++) {
                Mat bgr = getImage.getMatFromScreen(new Region());
                scenes.add(new Scene(new Pattern(new Image(
                        BufferedImageOps.getBufferedImageFromScreen(new Region()), "screenshot" + i))));
                if (i<scenesToCapture-1) time.wait(secondsBetweenCaptures);
            }
            return scenes;
        }
        if (BrobotSettings.mock) {
            // If no scenes are listed in the settings, use a randomly generated scene.
            if (BrobotSettings.screenshots.isEmpty())
                scenes.add(new Scene(new Pattern(Image.getEmptyImage())));
            // If scenes are listed in the settings, use them.
            else for (String filename : BrobotSettings.screenshots){
                // Check if filename is already an absolute path
                String imagePath;
                if (new java.io.File(filename).isAbsolute()) {
                    imagePath = filename;
                } else {
                    imagePath = "../" + BrobotSettings.screenshotPath + filename;
                }
                scenes.add(new Scene(new Pattern(new Image(imagePath))));
            }
            return scenes;
        }
        // If scenes are passed as parameters, use them.
        List<Scene> scenesInObjectCollection = objectCollections.get(0).getScenes();
        if (!scenesInObjectCollection.isEmpty()) {
            for (Scene scene : scenesInObjectCollection) {
                scenes.add(scene);
            }
            return scenes;
        }
        scenes.add(new Scene(new Pattern(Image.getEmptyImage())));
        return scenes;
    }

    /**
     * Some actions require multiple scenes, which can be taken as screenshots or passed as parameters.
     * @param objectCollections these contain scenes as images when passed as parameters
     * @return a list of scenes to use in matrix format
     */
    public List<Scene> getScenes(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
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
