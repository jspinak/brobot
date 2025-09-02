package io.github.jspinak.brobot.util.image.capture;

import org.sikuli.script.Image;
import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;
import org.sikuli.script.Region;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.file.SaveToFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Provides screenshot capture functionality with multiple capture strategies.
 * <p>
 * This component offers two approaches for capturing screenshots:
 * <ol>
 * <li>Sikuli-based capture using the active monitor where the mouse is located</li>
 * <li>SikuliX Screen-based capture of the primary screen</li>
 * </ol>
 * Both methods support saving screenshots with automatic file naming.
 * <p>
 * Key features:
 * <ul>
 * <li>Multi-monitor support (Sikuli method follows mouse location)</li>
 * <li>Timestamp-based naming to prevent overwrites</li>
 * <li>PNG format for lossless compression</li>
 * <li>Automatic directory creation</li>
 * </ul>
 * <p>
 * Use cases:
 * <ul>
 * <li>Debugging automation scripts with visual evidence</li>
 * <li>Creating documentation of UI states</li>
 * <li>Error reporting with screenshots</li>
 * <li>Visual logging of automation runs</li>
 * </ul>
 * <p>
 * Method comparison:
 * <ul>
 * <li>saveScreenshotWithDate: Uses SaveToFile for consistent naming, follows mouse monitor</li>
 * <li>captureScreenshot: Direct file writing, primary screen only, custom naming</li>
 * </ul>
 * <p>
 * Thread safety: Methods are thread-safe as they don't maintain mutable state.
 * However, concurrent captures may interfere with each other at the OS level.
 *
 * @author jspinak
 * @see SaveToFile
 * @see Screen
 */
@Component
public class ScreenshotCapture {

    private final SaveToFile saveToFile;

    public ScreenshotCapture(SaveToFile saveToFile) {
        this.saveToFile = saveToFile;
    }

    /**
     * Captures and saves a screenshot from the monitor containing the mouse cursor.
     * <p>
     * This method is multi-monitor aware, capturing from whichever screen
     * currently contains the mouse pointer. The screenshot is saved with
     * a timestamp suffix to ensure unique filenames.
     * <p>
     * File naming: {baseFileName}-{timestamp}.png
     * <p>
     * This is the preferred method for capturing screenshots during
     * automation as it follows the user's focus (mouse location).
     *
     * @param baseFileName base name for the screenshot file (without extension)
     */
    public void saveScreenshotWithDate(String baseFileName) {
        Screen activeScreen = Mouse.at().getMonitor();
        Image screenshot = activeScreen.getImage();
        saveToFile.saveImageWithDate(screenshot, baseFileName);
    }

    /**
     * Captures a screenshot of the primary screen using SikuliX Screen.
     * <p>
     * This method provides a simpler alternative that always captures
     * the primary screen, regardless of mouse position. It creates the
     * screenshots directory if it doesn't exist and saves the file
     * with the exact filename provided.
     * <p>
     * Directory structure: screenshots/{fileName}.png
     * <p>
     * Limitations:
     * <ul>
     * <li>Only captures primary screen in multi-monitor setups</li>
     * <li>May overwrite existing files with same name</li>
     * <li>Returns null on failure instead of throwing exception</li>
     * </ul>
     * <p>
     * Error handling: Catches all exceptions, prints stack trace,
     * and returns null. Consider using saveScreenshotWithDate for
     * more robust error handling.
     *
     * @param fileName name for the screenshot file (without extension)
     * @return full file path if successful, null on any error
     */
    public String captureScreenshot(String fileName) {
        try {
            String directory = "screenshots/";
            new File(directory).mkdirs();
            String filePath = directory + fileName + ".png";

            Screen screen = new Screen();
            BufferedImage capture = screen.capture().getImage();
            ImageIO.write(capture, "png", new File(filePath));

            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
