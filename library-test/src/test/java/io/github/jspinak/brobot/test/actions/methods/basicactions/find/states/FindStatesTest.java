package io.github.jspinak.brobot.test.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.FindStates;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindStatesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    FindStates findStates;
    
    @Autowired
    Action action;

    @Autowired
    MatchesInitializer matchesInitializer;

    @Test
    void returnsSomething() {
        Matches matches = new FindStatesData().getMatches(action, findStates, matchesInitializer, 100);
        System.out.println(matches.toStringAsTempStates());
        assertFalse(matches.isEmpty());
    }

    @Test
    void stateImagesMeetSizeRequirements() {
        Matches matches = new FindStatesData().getMatches(action, findStates, matchesInitializer, 100);
        matches.getMatchList().forEach(match -> {
            System.out.println(match);
            assertTrue(matches.getActionOptions().getMinArea() <= match.size());
        });
    }
}