package io.github.jspinak.brobot.test.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.FindPatternsIteration;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindPatternsIterationTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    FindPatternsIteration findPatternsIteration;

    @Autowired
    GetScenes getScenes;

    @Autowired
    MatchesInitializer matchesInitializer;

    @Test
    void find_() {
        TestData testData = new TestData();

        List<Image> scenes = getScenes.getScenes(testData.getDefineInsideAnchors(), List.of(testData.getInsideAnchorObjects()));
        List<StateImage> stateImages = new ArrayList<>();
        stateImages.add(testData.getTopLeft());
        stateImages.add(testData.getBottomRight());

        Matches matches = matchesInitializer.init(testData.getDefineInsideAnchors(), testData.getInsideAnchorObjects());

        findPatternsIteration.find(matches, stateImages, scenes);
        for (Match matchObject : matches.getMatchList()) {
            System.out.println(matchObject);
        }
        assertFalse(matches.isEmpty());
    }
}