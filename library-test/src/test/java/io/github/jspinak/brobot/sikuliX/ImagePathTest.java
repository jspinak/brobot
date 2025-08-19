package io.github.jspinak.brobot.sikuliX;

import io.github.jspinak.brobot.model.element.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ImagePathTest {

    private static String imagesPath;
    private static String screenshotsPath;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        
        // Determine the correct paths based on current working directory
        String currentDir = System.getProperty("user.dir");
        File imageDir = new File(currentDir, "images");
        
        // If running from project root, adjust path
        if (!imageDir.exists()) {
            imageDir = new File(currentDir, "library-test/images");
        }
        
        // Set absolute paths
        imagesPath = imageDir.getAbsolutePath();
        screenshotsPath = new File(imageDir.getParentFile(), "screenshots").getAbsolutePath();
        
        System.out.println("Test images path: " + imagesPath);
        System.out.println("Test screenshots path: " + screenshotsPath);
    }

    @Test
    void setupMultipleImagePaths() {
        ImagePath.add("images");
        ImagePath.add("screenshots");
        
        // Use absolute paths for Pattern creation in headless mode
        Pattern fromImages = new Pattern.Builder()
                .setFilename(Paths.get(imagesPath, "bottomR.png").toString())
                .build();
        Pattern fromScreenshots = new Pattern.Builder()
                .setFilename(Paths.get(screenshotsPath, "floranext0.png").toString())
                .build();
                
        assertEquals("bottomR", fromImages.getName());
        assertEquals("floranext0", fromScreenshots.getName());
        
        // Verify images were actually loaded
        assertNotNull(fromImages.getImage());
        assertNotNull(fromScreenshots.getImage());
    }

    @Test
    void specifyPathFromTheDefaultPath() {
        // Use absolute path with proper resolution
        File screenshotFile = new File(screenshotsPath, "floranext0.png");
        Pattern fromScreenshots = new Pattern.Builder()
                .setFilename(screenshotFile.getAbsolutePath())
                .build();
                
        assertEquals("floranext0", fromScreenshots.getName());
        assertNotNull(fromScreenshots.getImage());
    }
}