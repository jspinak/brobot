package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;

@Component
public class GetScenes {

    private GetImageJavaCV getImageJavaCV;
    private Wait wait;

    public GetScenes(GetImageJavaCV getImageJavaCV, Wait wait) {
        this.getImageJavaCV = getImageJavaCV;
        this.wait = wait;
    }

    /**
     * If the ObjectCollection is empty and saving history is enabled, a screenshot will be taken.
     * The search regions do not affect the scene. The scene is the entire screen.
     * Taking a screenshot produces one scene, but multiple scenes can be passed in the ObjectCollection.
     * If passed as a parameter, the scenes to use are in the 1st ObjectCollection (index 0) in the Scene list.
     * Scenes can also be taken from the field BrobotSettings.screenshot when performing unit tests.
     */
    public List<Scene> getScenes(List<ObjectCollection> objectCollections,
                                 int scenesToCapture, double secondsBetweenCaptures) {
        List<Scene> scenes = getScenesNoScrenshots(objectCollections);
        /*
        Take screenshots
         */
        if (scenes.isEmpty() && BrobotSettings.saveHistory) {
            Report.println("Taking screenshot");
            for (int i=0; i<scenesToCapture; i++) {
                scenes.add(new Scene("screenshot" + i + ".png", getImageJavaCV.getMatFromScreen()));
                if (i<scenesToCapture-1) wait.wait(secondsBetweenCaptures);
            }
        }
        return scenes;
    }

    /**
     * Some actions require multiple scenes, which can be taken as screenshots or passed as parameters.
     * @param objectCollections these contain scenes as images when passed as parameters
     * @return a list of scenes to use in matrix format
     */
    public List<Scene> getScenes(List<ObjectCollection> objectCollections) {
        return getScenes(objectCollections, 1, 0);
    }

    public List<Scene> getScenesNoScrenshots(List<ObjectCollection> objectCollections) {
        List<Scene> scenes = new ArrayList<>();
        /*
        If scenes are listed in the settings, use them.
        */
        if (BrobotSettings.mock && !BrobotSettings.screenshots.isEmpty()) {
            BrobotSettings.screenshots.forEach(
                    filename -> scenes.add(new Scene(filename, getImageJavaCV.getMatFromBundlePath(filename, BGR))));
            return scenes;
        }
        /*
        If scenes are passed as parameters, use them.
        */
        else for (Image image : objectCollections.get(0).getScenes()) {
            image.getFilenames().forEach(filename -> {
                Mat mat = getImageJavaCV.getMatFromBundlePath(filename, BGR);
                scenes.add(new Scene(filename, mat));
            });
        }
        return scenes;
    }

}
