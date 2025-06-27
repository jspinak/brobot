package io.github.jspinak.brobot.action.basic.find.color;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages scene acquisition from screenshots or provided images for analysis operations.
 * 
 * <p>GetScenes provides a flexible scene acquisition strategy that supports multiple
 * workflows in the Brobot framework. Scenes represent the visual context being
 * analyzed and can come from real-time screenshots, pre-captured images, or
 * mock data for testing.</p>
 * 
 * <p>Scene sources in priority order:</p>
 * <ol>
 *   <li>Mock scenes from BrobotSettings (when mocking enabled)</li>
 *   <li>Scenes from ObjectCollection parameters</li>
 *   <li>Screenshots captured from current screen</li>
 *   <li>Empty scene as fallback</li>
 * </ol>
 * 
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Multiple screenshot capture with configurable delays</li>
 *   <li>Mock scene support for unit testing</li>
 *   <li>Parameter-based scene injection</li>
 *   <li>Automatic screenshot vs. provided scene detection</li>
 * </ul>
 * 
 * <p>Since version 1.0.7, all Brobot operations use scenes for consistency,
 * eliminating dual screenshot requirements from earlier versions.</p>
 * 
 * @see Scene
 * @see ObjectCollection
 * @see ImageLoader
 */
@Component
public class SceneProvider {
    private final TimeProvider time;
    private final ImageLoader getImage;

    public SceneProvider(TimeProvider time, ImageLoader getImage) {
        this.time = time;
        this.getImage = getImage;
    }

    /**
     * Acquires scenes for analysis through screenshots or provided collections.
     * 
     * <p>Scene acquisition follows a priority hierarchy:</p>
     * <ol>
     *   <li>If mocking enabled and screenshots configured: Use mock scenes</li>
     *   <li>If scenes provided in first ObjectCollection: Use those scenes</li>
     *   <li>If allowed to screenshot: Capture from screen</li>
     *   <li>Otherwise: Return empty scene</li>
     * </ol>
     * 
     * <p>Multiple screenshots can be captured with delays between them for
     * time-based analysis. Each screenshot captures the entire screen region,
     * not limited by search regions.</p>
     * 
     * <p>Side effects: May capture screenshots and wait between captures</p>
     * 
     * @param actionOptions configuration (currently unused but reserved)
     * @param objectCollections may contain scenes in first collection
     * @param scenesToCapture number of screenshots to take
     * @param secondsBetweenCaptures delay between multiple screenshots
     * @return list of scenes ready for analysis
     */
    public List<Scene> getScenes(ActionOptions actionOptions, List<ObjectCollection> objectCollections,
                                 int scenesToCapture, double secondsBetweenCaptures) {
        List<Scene> scenes = new ArrayList<>();
        boolean takeScreenshot = isOkToTakeScreenshot(objectCollections.toArray(new ObjectCollection[0]));
        if (takeScreenshot) {
            for (int i=0; i<scenesToCapture; i++) {
                Mat bgr = getImage.getMatFromScreen(new Region());
                scenes.add(new Scene(new Pattern(new Image(
                        BufferedImageUtilities.getBufferedImageFromScreen(new Region()), "screenshot" + i))));
                if (i<scenesToCapture-1) time.wait(secondsBetweenCaptures);
            }
            return scenes;
        }
        if (FrameworkSettings.mock) {
            // If no scenes are listed in the settings, use a randomly generated scene.
            if (FrameworkSettings.screenshots.isEmpty())
                scenes.add(new Scene(new Pattern(Image.getEmptyImage())));
            // If scenes are listed in the settings, use them.
            else for (String filename : FrameworkSettings.screenshots){
                // Check if filename is already an absolute path
                String imagePath;
                if (new java.io.File(filename).isAbsolute()) {
                    imagePath = filename;
                } else {
                    imagePath = "../" + FrameworkSettings.screenshotPath + filename;
                }
                scenes.add(new Scene(new Pattern(new Image(imagePath))));
            }
            return scenes;
        }
        // If scenes are passed as parameters, use them.
        List<Scene> scenesInObjectCollection = objectCollections.get(0).getScenes();
        if (!scenesInObjectCollection.isEmpty()) {
            for (Scene scene : scenesInObjectCollection) {
                scenes.add(scene);
            }
            return scenes;
        }
        scenes.add(new Scene(new Pattern(Image.getEmptyImage())));
        return scenes;
    }

    /**
     * Acquires a single scene using default capture settings.
     * 
     * <p>Convenience method that captures one scene with no delay.
     * Delegates to the full getScenes method with scenesToCapture=1
     * and secondsBetweenCaptures=0.</p>
     * 
     * @param actionOptions configuration (currently unused but reserved)
     * @param objectCollections may contain scenes as parameters
     * @return list containing a single scene
     */
    public List<Scene> getScenes(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        return getScenes(actionOptions, objectCollections, 1, 0);
    }

    /**
     * Determines if screenshot capture is appropriate for the current context.
     * 
     * <p>Screenshots are not taken when:</p>
     * <ul>
     *   <li>Mocking is enabled (uses mock scenes instead)</li>
     *   <li>Scenes are provided as parameters (uses provided scenes)</li>
     * </ul>
     * 
     * <p>This check prevents unnecessary screenshots when scenes are already
     * available, improving performance and enabling deterministic testing.</p>
     *
     * @param objectCollections may contain pre-provided scenes
     * @return true if screenshot should be captured, false otherwise
     */
    private boolean isOkToTakeScreenshot(ObjectCollection... objectCollections) {
        if (FrameworkSettings.mock) return false;
        //if (objectCollections.length == 0) return false;
        if (!objectCollections[0].getScenes().isEmpty()) return false;
        return true;
    }

}
