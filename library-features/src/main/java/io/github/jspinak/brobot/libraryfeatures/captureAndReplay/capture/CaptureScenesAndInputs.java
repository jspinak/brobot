package io.github.jspinak.brobot.libraryfeatures.captureAndReplay.capture;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;

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

    private NativeHookDemo nativeHookDemo;
    private WriteXmlDomActions writeXmlDomActions;
    private Action action;
    private WriteXmlDomScenes writeXmlDomScenes;
    private BufferedImageUtilities bufferedImageOps;

    private int screensSaved = 0;

    public CaptureScenesAndInputs(BufferedImageUtilities bufferedImageOps,
                                  NativeHookDemo nativeHookDemo, WriteXmlDomActions writeXmlDomActions,
                                  Action action, WriteXmlDomScenes writeXmlDomScenes) {
        this.bufferedImageOps = bufferedImageOps;
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
        int numberOfScreenshots = (int) (FrameworkSettings.secondsToCapture / FrameworkSettings.captureFrequency);
        nativeHookDemo.start();
        int timelapse;
        LocalDateTime startTime = LocalDateTime.now();
        while (screensSaved < numberOfScreenshots) {
            timelapse = (int) Duration.between(startTime, LocalDateTime.now()).toMillis();
            if (timelapse > screensSaved * FrameworkSettings.captureFrequency * 1000) {
                try {
                    ImageIO.write(bufferedImageOps.getBuffImgFromScreen(new Region()),
                            "png", new File("capture/scene" + screensSaved + ".png"));
                    ConsoleReporter.println("Saved screenshot " + screensSaved);
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
                ActionResult matches = action.perform(actionOptions, objects);
                if (matches.getBestLocation().isPresent()) {
                    sceneAndObjectsForXML.addObject(stateImage.getName(), matches.getBestLocation().get());
                }
            }
            sceneObjectCollectionForXML.addScene(sceneAndObjectsForXML);
        }
        return sceneObjectCollectionForXML;
    }
}
