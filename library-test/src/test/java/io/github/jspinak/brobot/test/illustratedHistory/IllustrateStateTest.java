package io.github.jspinak.brobot.test.illustratedHistory;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.CreateStatesFromMatches;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.FindStates;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.illustratedHistory.IllustrateState;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.imageUtils.MatBuilder;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.find.states.FindStatesData;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.junit.jupiter.api.Assertions.assertTrue;


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

    @Autowired
    ImageUtils imageUtils;

    private List<State> createStates() {
        Matches matches = new FindStatesData().getMatches(action, findStates, matchesInitializer, 100);
        return createStatesFromMatches.create(matches);
    }

    @Test
    void getIllustratedState() {
        List<State> states = createStates();
        State first = states.get(0);
        System.out.println(first);
        Mat state0 = illustrateState.illustrateWithFixedSearchRegions(states.get(0));
        assertTrue(countNonZero(MatOps.toGrayscale(state0)) > 0);
    }

    @Test
    void illustrateStateWithOneImage() {
        List<State> states = createStates();
        State state = states.get(0);
        Optional<StateImage> optState = state.getStateImages().stream().findFirst();
        Mat mat = optState.get().getPatterns().get(0).getMat(); // the Mat of the first image in the state
        Region matRegion = optState.get().getPatterns().get(0).getRegion();
        MatBuilder matBuilder = new MatBuilder();
        Mat illustratedMat = matBuilder.init()
                .addSubMat(new Location(matRegion.x(), matRegion.y()), mat)
                .build();
        assertTrue(countNonZero(MatOps.toGrayscale(illustratedMat)) > 0);
        //imageUtils.writeWithUniqueFilename(illustratedMat, "history/illustratedTestMat");
    }

    @Test
    void illustrateStateWithAllImages() {
        List<State> states = createStates();
        State state = states.get(0);
        MatBuilder matBuilder = new MatBuilder().init();
        for (StateImage stateImage : state.getStateImages()) {
            Pattern pattern = stateImage.getPatterns().get(0);
            Mat mat = pattern.getMat();
            Region region = pattern.getRegion();
            matBuilder.addSubMat(new Location(region.x(), region.y()), mat);
        }
        Mat illustratedMat = matBuilder.build();
        assertTrue(countNonZero(MatOps.toGrayscale(illustratedMat)) > 0);
        //imageUtils.writeWithUniqueFilename(illustratedMat, "history/illustratedTestMat");
    }

    //@Test
    void writeIllustratedStateToFile() {
        List<State> states = createStates();
        illustrateState.writeIllustratedStateToFile(states.get(0), "history/state0");
    }

    @Test
    void writeAllStatesToFile() {
        List<State> states = createStates();
        states.forEach(state -> illustrateState.writeIllustratedStateToFile(state, "history/state"+state.getName()));
    }
}