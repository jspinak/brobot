package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.actions.BrobotSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    @BeforeAll
    public static void setBundlePath() {
        ImagePath.setBundlePath("images");
    }

    @Test
    void x() {
        Pattern pattern = new Pattern("topLeft");
        assertEquals(0, pattern.x());
    }

    @Test
    void w() {
        Pattern pattern = new Pattern("topLeft");
        assertEquals(107, pattern.w());
    }

    @Test
    void mat() {
        Pattern pattern = new Pattern("../" + BrobotSettings.screenshotPath + "floranext1");
        assertNotNull(pattern.getMat());
    }

    @Test
    void mat2() {
        Pattern pattern = new Pattern.Builder()
                .setFilename("../" + BrobotSettings.screenshotPath + "floranext1")
                .build();
        System.out.println("Mat's pointer = " + pattern.getMat().getPointer());
        assertNotNull(pattern.getMat());
    }
}