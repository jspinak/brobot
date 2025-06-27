package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.test.BaseIntegrationTest;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Pattern class.
 * Tests loading real images from files in both regular and headless environments.
 */
class PatternTest extends BaseIntegrationTest {

    @BeforeAll
    public static void setBundlePath() {
        // Only set bundle path if not in full mock mode
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        if (!env.shouldSkipSikuliX()) {
            ImagePath.setBundlePath("images");
        }
    }

    @Test
    void w() {
        Pattern pattern = new Pattern(TestPaths.getImagePath("topLeft"));
        assertEquals(107, pattern.w());
    }

    @Test
    void mat() {
        Pattern pattern = new Pattern(TestPaths.getScreenshotPath("floranext1"));
        assertNotNull(pattern.getMat());
    }

    @Test
    void mat2() {
        Pattern pattern = new Pattern.Builder()
                .setFilename(TestPaths.getScreenshotPath("floranext1"))
                .build();
        System.out.println("Mat's pointer = " + pattern.getMat().getPointer());
        assertNotNull(pattern.getMat());
    }
}