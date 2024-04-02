package io.github.jspinak.brobot.test.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.methods.basicactions.define.DefineHelper;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DefineHelperTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    DefineHelper defineHelper;

    @Test
    void findMatches() {
        TestData testData = new TestData();
        Matches matches = new Matches();
        matches.setActionOptions(testData.getDefineInsideAnchors());
        defineHelper.findMatches(matches, testData.getInsideAnchorObjects());
        System.out.println(matches.getMatchList());
        assertEquals(2, matches.size());
    }
}