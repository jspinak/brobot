package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class CreateStatesFromMatchesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    CreateStatesFromMatches createStatesFromMatches;

    @Autowired
    Action action;

    @Autowired
    FindStates findStates;

    @Autowired
    MatchesInitializer matchesInitializer;

    private List<State> createStates() {
        Matches matches = new FindStatesData().getMatches(action, findStates, matchesInitializer);
        return createStatesFromMatches.create(matches);
    }

    @Test
    void create() {
        List<State> states = createStates();
        states.forEach(System.out::println);
        assertFalse(states.isEmpty());
    }

    @Test
    void statesHaveNames() {
        List<State> states = createStates();
        assertNotNull(states.get(0).getName());
        assertNotEquals("null", states.get(0).getName());
        System.out.println(states.get(0).getName());
    }
}