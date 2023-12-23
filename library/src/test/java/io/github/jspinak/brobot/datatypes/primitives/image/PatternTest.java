package io.github.jspinak.brobot.datatypes.primitives.image;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}