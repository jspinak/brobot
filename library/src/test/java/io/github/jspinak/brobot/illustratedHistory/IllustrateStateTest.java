package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.CreateStatesFromMatches;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.FindStates;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.FindStatesData;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = BrobotTestApplication.class)
class IllustrateStateTest {

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

    @Autowired
    IllustrateState illustrateState;

    private List<State> createStates() {
        Matches matches = new FindStatesData().getMatches(action, findStates, matchesInitializer);
        return createStatesFromMatches.create(matches);
    }

    @Test
    void getIllustratedState() {
        List<State> states = createStates();
        Mat state0 = illustrateState.illustrateWithFixedSearchRegions(states.get(0));
        Mat grayImage = new Mat();
        cvtColor(state0, grayImage, COLOR_BGR2GRAY);
        assertTrue(countNonZero(grayImage) > 0);
    }

    @Test
    void writeIllustratedStateToFile() {
        List<State> states = createStates();
        illustrateState.writeIllustratedStateToFile(states.get(0), "history/state0");
    }
}