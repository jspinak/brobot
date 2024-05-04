package io.github.jspinak.brobot.test.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.methods.basicactions.define.DefineInsideAnchors;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BrobotTestApplication.class)
class DefineInsideAnchorsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    DefineInsideAnchors defineInsideAnchors;

    /**
     * It was unable to find the bottom right image until I removed a few columns from the right of the image.
     * Apparently, images that are cut at the very right side of the screenshot are not findable.
     */
    @Test
    void perform() {
        TestData testData = new TestData();
        Matches matches = new Matches();
        matches.setActionOptions(testData.getDefineInsideAnchors());
        defineInsideAnchors.perform(matches, testData.getInsideAnchorObjects());
        System.out.println(matches);
        assertEquals(2, matches.getMatchList().size());
        assertEquals(0, matches.getDefinedRegion().x());
        assertEquals(77, matches.getDefinedRegion().y());
        assertEquals(1915, matches.getDefinedRegion().x2());
        assertEquals(1032, matches.getDefinedRegion().y2());
    }
}