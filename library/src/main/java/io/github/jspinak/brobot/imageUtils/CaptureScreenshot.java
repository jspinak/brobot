package io.github.jspinak.brobot.imageUtils;

import org.sikuli.script.Image;
import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Captures the screenshot from the screen where the mouse is located and saves it to the given directory.
 *
 * @author jspinak
 */
@Component
public class CaptureScreenshot {

    private final SaveToFile saveToFile;

    public CaptureScreenshot(SaveToFile saveToFile) {
        this.saveToFile = saveToFile;
    }

    public void saveScreenshotWithDate(String baseFileName) {
        Screen activeScreen = Mouse.at().getMonitor();
        Image screenshot = activeScreen.getImage();
        saveToFile.saveImageWithDate(screenshot, baseFileName);
    }

    public String captureScreenshot(String fileName) {
        try {
            String directory = "screenshots/";
            new File(directory).mkdirs();
            String filePath = directory + fileName + ".png";

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "png", new File(filePath));

            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
