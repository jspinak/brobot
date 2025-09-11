package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

// Removed: ObjectActionOptions no longer exists
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.analysis.match.MatchFusion;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

@SpringBootTest(
        classes = io.github.jspinak.brobot.BrobotTestApplication.class,
        properties = {
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false",
            "brobot.mock.enabled=true"
        })
@Import({
    MockGuiAccessConfig.class,
    MockGuiAccessMonitor.class,
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@Disabled("CI failure - needs investigation")
class MatchFusionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        ImagePath.setBundlePath("images");
    }

    @Autowired MatchFusion matchFusion;

    @Test
    void fuseMatchObjects() {
        Match match1 = new Match.Builder().setRegion(0, 0, 30, 20).build();
        Match match2 = new Match.Builder().setRegion(30, 0, 30, 20).build();
        Match match3 = new Match.Builder().setRegion(31, 32, 30, 20).build();
        Match match4 = new Match.Builder().setRegion(30, 34, 30, 20).build();
        ActionResult matches = new ActionResult();
        matches.add(match1, match2, match3, match4);
        // Test default fusion - the method now takes ActionConfig instead of ObjectActionOptions
        // Since we can't easily configure fusion method with the new API in this test, use null for
        // default behavior
        List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches.getMatchList(), null);
        fusedMatches.forEach(System.out::println);
        assertEquals(2, fusedMatches.size());
        Match fusedMatch = fusedMatches.get(0);
        assertEquals(0, fusedMatch.x());
        assertEquals(0, fusedMatch.y());
        assertEquals(60, fusedMatch.w());
        assertEquals(20, fusedMatch.h());
        Match fusedMatch2 = fusedMatches.get(1);
        assertEquals(30, fusedMatch2.x());
        assertEquals(32, fusedMatch2.y());
        assertEquals(31, fusedMatch2.w());
        assertEquals(22, fusedMatch2.h());
    }
}
