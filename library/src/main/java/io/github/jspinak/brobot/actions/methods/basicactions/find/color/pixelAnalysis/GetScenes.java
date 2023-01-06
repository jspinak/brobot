package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

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

    private GetImageJavaCV getImageJavaCV;
    private Wait wait;
    private IllustrateScreenshot illustrateScreenshot;

    public GetScenes(GetImageJavaCV getImageJavaCV, Wait wait, IllustrateScreenshot illustrateScreenshot) {
        this.getImageJavaCV = getImageJavaCV;
        this.wait = wait;
        this.illustrateScreenshot = illustrateScreenshot;
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
        if (isOkToTakeScreenshot(actionOptions, objectCollections.toArray(new ObjectCollection[0]))) {
            // take a screenshot
            Report.println("Taking screenshot");
            for (int i=0; i<scenesToCapture; i++) {
                scenes.add(new Scene("screenshot" + i + ".png", getImageJavaCV.getMatFromScreen()));
                if (i<scenesToCapture-1) wait.wait(secondsBetweenCaptures);
            }
            return scenes;
        }
        if (BrobotSettings.mock) {
            // If scenes are listed in the settings, use them.
            if (!BrobotSettings.screenshots.isEmpty()) {
                BrobotSettings.screenshots.forEach(
                        filename -> scenes.add(new Scene(filename, getImageJavaCV.getMatFromBundlePath(filename, BGR))));
            }
            // If no scenes are listed in the settings, use a randomly generated scene.
            else scenes.add(new Scene());
            return scenes;
        }
        // If scenes are passed as parameters, use them.
        else for (Image image : objectCollections.get(0).getScenes()) {
            image.getFilenames().forEach(filename -> {
                Mat mat = getImageJavaCV.getMatFromBundlePath(filename, BGR);
                scenes.add(new Scene(filename, mat));
            });
            return scenes;
        }
        scenes.add(new Scene());
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
     * - the action is FIND.Color, FIND.Histogram, or CLASSIFY (it's necessary for execution)
     * - illustration is allowed for this action (it's necessary for illustration)
     *
     * Otherwise, it's not ok to take a screenshot. The other methods use Sikuli for execution.
     *
     * @param objectCollections
     * @return
     */
    private boolean isOkToTakeScreenshot(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (BrobotSettings.mock) return false;
        if (objectCollections.length == 0) return false;
        if (!objectCollections[0].getScenes().isEmpty()) return false;
        ActionOptions.Action action = actionOptions.getAction();
        ActionOptions.Find find = actionOptions.getFind();
        if (find == ActionOptions.Find.COLOR || find == ActionOptions.Find.HISTOGRAM || action == ActionOptions.Action.CLASSIFY) return true;
        if (illustrateScreenshot.okToIllustrate(actionOptions, objectCollections)) return true;
        return false;
    }

}
