package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.database.services.StateImageService;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BrobotTestApplication.class)
class DefineHelperTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    DefineHelper defineHelper;

    @Autowired
    StateImageService stateImageService;

    @Test
    void findMatches() {
        TestData testData = new TestData();
        stateImageService.saveStateImages(testData.getBottomRight(), testData.getTopLeft());
        //saveStateImages(testData);
        Matches matches = new Matches();
        matches.setActionOptions(testData.getDefineInsideAnchors());
        defineHelper.findMatches(matches, testData.getInsideAnchorObjects());
        System.out.println(matches.getMatchList());
        assertEquals(2, matches.size());
    }
}