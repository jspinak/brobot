package io.github.jspinak.brobot.actions.methods.basicactions.capture;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.imageUtils.GetImage;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * The scene is captured along with the time of the capture.
 * The filename holds the time of the capture, and the file is saved in the 'capture' directory.
 * Once capture is finished, all scenes are processed and important information is extracted and
 * saved in a Mat in the 'capture' directory. This Mat can then be searched during real execution
 * to find the most similar scene, which allows us to find the sequence of actions to be performed.
 * The sequence of actions to be performed is saved in a Mat called 'actions.png'.
 */
@Component
public class CaptureScenesAndInputs {

    private GetImage getImage;
    private final Wait wait;
    private NativeHookDemo nativeHookDemo;
    private WriteXmlDomActions writeXmlDomActions;
    private Action action;
    private WriteXmlDomScenes writeXmlDomScenes;

    private int screensSaved = 0;

    public CaptureScenesAndInputs(GetImage getImage, Wait wait,
                                  NativeHookDemo nativeHookDemo, WriteXmlDomActions writeXmlDomActions,
                                  Action action, WriteXmlDomScenes writeXmlDomScenes) {
        this.getImage = getImage;
        this.wait = wait;
        this.nativeHookDemo = nativeHookDemo;
        this.writeXmlDomActions = writeXmlDomActions;
        this.action = action;
        this.writeXmlDomScenes = writeXmlDomScenes;
    }

    public void captureAndFindObjects(StateImageObject... stateImageObjects) {
        capture();
        SceneObjectCollectionForXML sceneObjectCollectionForXML = findObjects(stateImageObjects);
        saveSceneObjectCollectionForXML(sceneObjectCollectionForXML);
        System.runFinalization();
        System.exit(0);
    }

    private void saveSceneObjectCollectionForXML(SceneObjectCollectionForXML sceneObjectCollectionForXML) {
        writeXmlDomScenes.initDocument();
        for (SceneAndObjectsForXML sceneObject : sceneObjectCollectionForXML.getScenes()) {
            writeXmlDomScenes.addScene(sceneObject);
        }
        writeXmlDomScenes.writeXmlToFile();
    }

    /**
     * Takes and saves screenshots repeatedly in the folder 'capture'.
     */
    public void capture() {
        int numberOfScreenshots = (int) (BrobotSettings.secondsToCapture / BrobotSettings.captureFrequency);
        nativeHookDemo.start();
        int timelapse;
        LocalDateTime startTime = LocalDateTime.now();
        while (screensSaved < numberOfScreenshots) {
            timelapse = (int) Duration.between(startTime, LocalDateTime.now()).toMillis();
            if (timelapse > screensSaved * BrobotSettings.captureFrequency * 1000) {
                try {
                    ImageIO.write(getImage.getBuffImgFromScreen(new Region()),
                            "png", new File("capture/scene" + screensSaved + ".png"));
                    Report.println("Saved screenshot " + screensSaved);
                    screensSaved++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (nativeHookDemo.isWindowClosed()) break;
        }
        writeXmlDomActions.writeXmlToFile();
    }

    private SceneObjectCollectionForXML findObjects(StateImageObject... stateImageObjects) {
        SceneObjectCollectionForXML sceneObjectCollectionForXML = new SceneObjectCollectionForXML();
        for (int i=0; i<screensSaved; i++) {
            SceneAndObjectsForXML sceneAndObjectsForXML = new SceneAndObjectsForXML(i);
            for (StateImageObject stateImageObject : stateImageObjects) {
                ObjectCollection objects = new ObjectCollection.Builder()
                        .withScenes(new Image("../capture/scene" + i))
                        .withImages(stateImageObject)
                        .build();
                Matches matches = action.perform(ActionOptions.Action.FIND, objects);
                if (matches.getBestLocation().isPresent()) {
                    sceneAndObjectsForXML.addObject(stateImageObject.getName(), matches.getBestLocation().get());
                }
            }
            sceneObjectCollectionForXML.addScene(sceneAndObjectsForXML);
        }
        return sceneObjectCollectionForXML;
    }
}
