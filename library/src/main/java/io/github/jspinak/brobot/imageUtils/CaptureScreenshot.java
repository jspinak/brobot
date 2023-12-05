package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder.SaveToFile;
import org.sikuli.script.Image;
import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

/**
 * Captures the screenshot from the screen where the mouse is located and saves it to the given directory.
 *
 * @author jspinak
 */
@Component
public class CaptureScreenshot {

    private SaveToFile saveToFile;

    public CaptureScreenshot(SaveToFile saveToFile) {
        this.saveToFile = saveToFile;
    }

    public void saveScreenshot(String baseFileName) {
        Screen activeScreen = Mouse.at().getMonitor();
        Image screenshot = activeScreen.getImage();
        saveToFile.saveImageWithDate(screenshot, baseFileName);
    }

}
