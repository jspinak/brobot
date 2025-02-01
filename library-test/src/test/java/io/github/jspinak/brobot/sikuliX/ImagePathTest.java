package io.github.jspinak.brobot.sikuliX;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ImagePathTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void setupMultipleImagePaths() {
        ImagePath.add("images");
        ImagePath.add("screenshots");
        Pattern fromImages = new Pattern.Builder()
                .setFilename("bottomR")
                .build();
        Pattern fromScreenshots = new Pattern.Builder()
                .setFilename("floranext0")
                .build();
        assertEquals("bottomR", fromImages.getName());
        assertEquals("floranext0", fromScreenshots.getName());
    }

    @Test
    void specifyPathFromTheDefaultPath() {
        Pattern fromScreenshots = new Pattern.Builder()
                .setFilename("../screenshots/floranext0")
                .build();
        assertEquals("floranext0", fromScreenshots.getName());
    }
}
