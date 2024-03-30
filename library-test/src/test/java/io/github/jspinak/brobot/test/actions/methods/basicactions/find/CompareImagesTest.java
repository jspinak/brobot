package io.github.jspinak.brobot.test.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages.CompareImages;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class CompareImagesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    CompareImages compareImages;

    @Test
    void comparePatterns1() {
        TestData testData = new TestData();
        Match match = compareImages.compare(testData.getFloranext0(), testData.getFloranext1());
        System.out.println(match.getScore());
        assertTrue(match.getScore() > .996);
    }

    @Test
    void comparePatterns2() {
        TestData testData = new TestData();
        Match match = compareImages.compare(testData.getFloranext0(), testData.getFloranext2());
        System.out.println(match.getScore());
        assertEquals(0.809767484664917, match.getScore());
    }

    @Test
    void compareImages1() {
        TestData testData = new TestData();
        StateImage flora1 = new StateImage.Builder()
                .addPattern(testData.getFloranext0())
                .build();
        StateImage flora2 = new StateImage.Builder()
                .addPattern(testData.getFloranext1())
                .build();
        Match match = compareImages.compare(flora1, flora2);
        assertEquals(0.9962218999862671, match.getScore());
    }

    @Test
    void compareImages2() {
        TestData testData = new TestData();
        StateImage flora1 = new StateImage.Builder()
                .addPattern(testData.getFloranext0())
                .addPattern(testData.getFloranext1())
                .build();
        StateImage flora2 = new StateImage.Builder()
                .addPattern(testData.getFloranext1())
                .build();
        Match match = compareImages.compare(flora1, flora2);
        System.out.println(match.getScore());
        assertEquals(0.9999988675117493, match.getScore());
    }
}