package io.github.jspinak.brobot.test.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.MatchFusion;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
        Match match1 = new Match.Builder().setRegion(0,0,30,20).build();
        Match match2 = new Match.Builder().setRegion(30,0,30,20).build();
        Match match3 = new Match.Builder().setRegion(31,32,30,20).build();
        Match match4 = new Match.Builder().setRegion(30,34,30,20).build();
        Matches matches = new Matches();
        matches.add(match1, match2, match3, match4);
        matches.setActionOptions(new ActionOptions.Builder().setFusionMethod(ActionOptions.MatchFusionMethod.ABSOLUTE).build());
        List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches.getMatchList(), matches.getActionOptions());
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