package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.imageUtils.GetBufferedImage;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;

/**
 * Scenes are used for
 * - finding color
 * - image segmentation (classification)
 * - action illustration
 * When running live, scenes should be taken in every Find iteration.
 * Scenes are not needed for pattern detection when illustrations are turned off.
 */
@Component
public class GetScenes {

    private final GetImageJavaCV getImageJavaCV;
    private final Time time;
    private final IllustrateScreenshot illustrateScreenshot;
    private final GetBufferedImage getBufferedImage;

    public GetScenes(GetImageJavaCV getImageJavaCV, Time time, IllustrateScreenshot illustrateScreenshot,
                     GetBufferedImage getBufferedImage) {
        this.getImageJavaCV = getImageJavaCV;
        this.time = time;
        this.illustrateScreenshot = illustrateScreenshot;
        this.getBufferedImage = getBufferedImage;
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
        boolean takeScreenshot = isOkToTakeScreenshot(actionOptions, objectCollections.toArray(new ObjectCollection[0]));
        if (takeScreenshot) {
            Report.println("Taking screenshot");
            for (int i=0; i<scenesToCapture; i++) {
                Mat bgr = getImageJavaCV.getMatFromScreen();
                BufferedImage bi = getBufferedImage.convert(bgr);
                scenes.add(new Scene("screenshot" + i + ".png", bgr, bi));
                if (i<scenesToCapture-1) time.wait(secondsBetweenCaptures);
            }
            return scenes;
        }
        if (BrobotSettings.mock) {
            // If scenes are listed in the settings, use them.
            if (!BrobotSettings.screenshots.isEmpty()) {
                BrobotSettings.screenshots.forEach(
                        filename -> {
                            String absolutePath = new File(BrobotSettings.screenshotPath+filename).getAbsolutePath();
                            Mat bgr = getImageJavaCV.getMatFromBundlePath(filename, BGR);
                            BufferedImage bi = getBufferedImage.convert(bgr);
                            scenes.add(new Scene(filename, absolutePath, bgr, bi));
                        });
            }
            // If no scenes are listed in the settings, use a randomly generated scene.
            else scenes.add(Scene.getEmptyScene());
            return scenes;
        }
        // If scenes are passed as parameters, use them.
        else for (Pattern pattern : objectCollections.get(0).getScenes()) {
            String filename = pattern.getFilename();
            String absolutePath = new File(filename).getAbsolutePath();
            Mat bgr = getImageJavaCV.getMatFromFilename(absolutePath, BGR);
            BufferedImage bi = getBufferedImage.convert(bgr);
            scenes.add(new Scene.Builder()
                    .setName(filename)
                    .setAbsolutePath(absolutePath)
                    .setBGR(bgr)
                    .setBufferedImageBGR(bi)
                    .build());
            return scenes;
        }
        scenes.add(Scene.getEmptyScene());
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
     * - scenes are passed as parameters
     *
     * If these conditions don't exclude taking a screenshot, it's ok to take one when either
     * - the action is FIND.Color, FIND.Histogram, FIND.REGIONS_OF_MOTION, or CLASSIFY (it's necessary for execution)
     * - illustration is allowed for this action (it's necessary for illustration)
     *
     * Otherwise, it's not ok to take a screenshot. The other methods use Sikuli for execution.
     *
     * @param actionOptions has the action and other relevant options
     * @param objectCollections may contain scenes
     * @return true if a screenshot is required
     */
    private boolean isOkToTakeScreenshot(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (BrobotSettings.mock) return false;
        if (objectCollections.length == 0) return false;
        if (!objectCollections[0].getScenes().isEmpty()) return false;
        ActionOptions.Action action = actionOptions.getAction();
        ActionOptions.Find find = actionOptions.getFind();
        if (find == ActionOptions.Find.COLOR || find == ActionOptions.Find.HISTOGRAM
                || action == ActionOptions.Action.CLASSIFY || find == ActionOptions.Find.REGIONS_OF_MOTION) return true;
        if (illustrateScreenshot.okToIllustrate(actionOptions, objectCollections)) return true;
        return false;
    }

}
