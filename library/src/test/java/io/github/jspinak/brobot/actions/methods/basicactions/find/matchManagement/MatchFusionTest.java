package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class MatchFusionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    MatchFusion matchFusion;

    @Test
    void fuseMatchObjects() {
        Match match1 = new Match(0,0,30,20);
        Match match2 = new Match(30,0,30,20);
        Match match3 = new Match(31,32,30,20);
        Match match4 = new Match(30,34,30,20);
        Matches matches = new Matches();
        matches.add(match1, match2, match3, match4);
        Matches fusedMatches = matchFusion.fuseMatchObjects(matches, 5, 5);
        assertTrue(fusedMatches.size() == 2);
        Match fusedMatch = fusedMatches.getMatchList().get(0);
        assertEquals(0, fusedMatch.x);
        assertEquals(0, fusedMatch.y);
        assertEquals(60, fusedMatch.w);
        assertEquals(20, fusedMatch.h);
        Match fusedMatch2 = fusedMatches.getMatchList().get(1);
        assertEquals(30, fusedMatch2.x);
        assertEquals(32, fusedMatch2.y);
        assertEquals(31, fusedMatch2.w);
        assertEquals(22, fusedMatch2.h);
    }
}