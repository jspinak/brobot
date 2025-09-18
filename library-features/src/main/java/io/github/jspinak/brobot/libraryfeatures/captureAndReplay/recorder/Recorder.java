package io.github.jspinak.brobot.libraryfeatures.captureAndReplay.recorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.util.file.SaveToFile;
import io.github.jspinak.brobot.util.image.capture.ScreenshotRecorder;

/**
 * This package implements a recorder for SikuliX. The saved recordings can be used in SikuliX or
 * other programs, e.g. for training a neural network.
 *
 * <p>Features include: - playback - code generation for scripting - a file containing mouse and
 * keyboard actions - screenshots saved at regular intervals during recording
 *
 * @author jspinak
 */
@Component
public class Recorder {

    @Autowired private BrobotProperties brobotProperties;

    private final ScreenshotRecorder captureScreenshots;
    private final CaptureUserInputs captureUserInputs;
    private final ProcessRecording processRecording;
    private final SaveToFile saveToFile;

    private boolean recording = false;
    // todo: review quick replacement done bc not in Sikuli 2.0.5 -> //private File
    // recordingFolder = new File(Commons.getTempFolder(), "Recorder");
    private int screenshotDelay = 1000;

    public Recorder(
            ScreenshotRecorder captureScreenshots,
            CaptureUserInputs captureUserInputs,
            ProcessRecording processRecording,
            SaveToFile saveToFile) {
        this.captureScreenshots = captureScreenshots;
        this.captureUserInputs = captureUserInputs;
        this.processRecording = processRecording;
        this.saveToFile = saveToFile;
    }

    /**
     * Starts capturing screenshots and recording the user's actions<br>
     * The screenshots are saved in the directory defined below as screenshot_TIMESTAMP.png.
     */
    public void startRecording() {
        if (recording) return;
        recording = true;

        // todo: not in Sikuli 2.0.5 -> //Commons.asFolder(new File(recordingFolder,
        // "recording" + "-" + new Date().getTime()).getAbsolutePath());

        captureScreenshots.startCapturing(saveToFile, "screenshot", screenshotDelay);
        captureUserInputs.startRecording();
    }

    /**
     * Stops recording the user's actions and capturing screenshots.<br>
     * The user's actions raw data are saved in the path defined below as: rawinputs.xml<br>
     * Finally the raw actions are reduced to relevant actions: inputs.xml
     */
    public void stopRecording() {
        if (!recording) return;
        recording = false;

        captureScreenshots.stopCapturing();

        Document rawDoc = captureUserInputs.finalizeRecording();
        saveToFile.saveXML(rawDoc, "rawinputs.xml");
        Document simplifiedDoc = processRecording.process(rawDoc);
        saveToFile.saveXML(simplifiedDoc, "inputs.xml");
    }

    public boolean setRecordingDirectory(String directory) {
        if (recording) return false;
        // Note: recordingFolder should be configured via properties
        // This method is deprecated as BrobotProperties is immutable
        return false;
    }

    public boolean setScreenshotDelay(int delay) {
        if (recording) return false;
        screenshotDelay = delay;
        return true;
    }
}
