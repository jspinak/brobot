package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.database.api.PatternService;
import io.github.jspinak.brobot.database.api.StateImageService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
    PatternService patternService;

    @Autowired
    StateImageService stateImageService;

    public void savePatterns(TestData testData) {
        patternService.savePattern(testData.getScreenshot());
        patternService.savePattern(testData.getTopL());
        patternService.savePattern(testData.getBottomR());
        patternService.savePattern(testData.getFloranext0());
        patternService.savePattern(testData.getFloranext1());
        patternService.savePattern(testData.getFloranext2());
        patternService.savePattern(testData.getFloranext3());
        patternService.savePattern(testData.getFloranext4());
    }

    public void saveStateImages(TestData testData) {
        stateImageService.saveStateImage(testData.getBottomRight());
        stateImageService.saveStateImage(testData.getTopLeft());
    }

    @Test
    void findMatches() {
        TestData testData = new TestData();
        testData.saveStateImages();
        //saveStateImages(testData);
        Matches matches = new Matches();
        matches.setActionOptions(testData.getDefineInsideAnchors());
        defineHelper.findMatches(matches, testData.getInsideAnchorObjects());
        System.out.println(matches.getMatchList());
        assertEquals(2, matches.size());
    }
}