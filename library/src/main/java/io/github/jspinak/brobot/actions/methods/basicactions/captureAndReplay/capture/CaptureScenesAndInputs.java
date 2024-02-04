package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageOpenCV;
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

// todo: I added the code I wrote for SikuliX in the recorder folder. This code should be able to replace this class and NativeHookDemo.

@Component
public class CaptureScenesAndInputs {

    private GetImageOpenCV getImageOpenCV;
    private NativeHookDemo nativeHookDemo;
    private WriteXmlDomActions writeXmlDomActions;
    private Action action;
    private WriteXmlDomScenes writeXmlDomScenes;

    private int screensSaved = 0;

    public CaptureScenesAndInputs(GetImageOpenCV getImageOpenCV,
                                  NativeHookDemo nativeHookDemo, WriteXmlDomActions writeXmlDomActions,
                                  Action action, WriteXmlDomScenes writeXmlDomScenes) {
        this.getImageOpenCV = getImageOpenCV;
        this.nativeHookDemo = nativeHookDemo;
        this.writeXmlDomActions = writeXmlDomActions;
        this.action = action;
        this.writeXmlDomScenes = writeXmlDomScenes;
    }

    /**
     * Captures scenes, object locations, and mouse and keyboard input and saves it to the 'capture' directory.
     * Make sure to use the same ActionOptions when using the saved data in a live automation.
     * @param actionOptions
     * @param stateImages
     */
    public void captureAndFindObjects(ActionOptions actionOptions, StateImage... stateImages) {
        capture();
        SceneObjectCollectionForXML sceneObjectCollectionForXML = findObjects(actionOptions, stateImages);
        saveSceneObjectCollectionForXML(sceneObjectCollectionForXML);
        //System.runFinalization(); //runFinalization has been deprecated
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
                    ImageIO.write(getImageOpenCV.getBuffImgFromScreen(new Region()),
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

    private SceneObjectCollectionForXML findObjects(ActionOptions actionOptions, StateImage... stateImages) {
        SceneObjectCollectionForXML sceneObjectCollectionForXML = new SceneObjectCollectionForXML();
        for (int i=0; i<screensSaved; i++) {
            SceneAndObjectsForXML sceneAndObjectsForXML = new SceneAndObjectsForXML(i);
            for (StateImage stateImage : stateImages) {
                ObjectCollection objects = new ObjectCollection.Builder()
                        .withScenes(new Pattern("../capture/scene" + i))
                        .withImages(stateImage)
                        .build();
                Matches matches = action.perform(actionOptions, objects);
                if (matches.getBestLocation().isPresent()) {
                    sceneAndObjectsForXML.addObject(stateImage.getName(), matches.getBestLocation().get());
                }
            }
            sceneObjectCollectionForXML.addScene(sceneAndObjectsForXML);
        }
        return sceneObjectCollectionForXML;
    }
}
