package io.github.jspinak.brobot.test.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.GetSceneCombinations;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.SceneCombination;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class GetSceneCombinationsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetSceneCombinations getSceneCombinations;

    @Autowired
    Action action;

    private ObjectCollection getStateObjectCollection(Pattern scene) {
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withScenes(scene)
                .build();
        ActionOptions findWordsOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL_WORDS)
                .build();
        Matches matches = action.perform(findWordsOptions, objColl);
        return new ObjectCollection.Builder()
                .withImages(matches.getMatchListAsStateImages())
                .withScenes(scene)
                .build();
    }

    private List<ObjectCollection> getStateObjectCollections() {
        TestData testData = new TestData();
        ObjectCollection stateColl1 = getStateObjectCollection(testData.getFloranext0());
        ObjectCollection stateColl2 = getStateObjectCollection(testData.getFloranext1());
        ObjectCollection stateColl3 = getStateObjectCollection(testData.getFloranext2());
        ObjectCollection stateColl4 = getStateObjectCollection(testData.getFloranext3());
        ObjectCollection stateColl5 = getStateObjectCollection(testData.getFloranext4());
        return List.of(stateColl1, stateColl2, stateColl3, stateColl4, stateColl5);
    }

    @Test
    void getAllSceneCombinations() {
        List<SceneCombination> sceneCombinations = getSceneCombinations.getAllSceneCombinations(getStateObjectCollections());
        sceneCombinations.forEach(System.out::println);
        assertFalse(sceneCombinations.isEmpty());
        //sceneCombinations.forEach(sc -> imageUtils.writeWithUniqueFilename(sc.getDynamicPixels(), "history/"+sc.getScene1()+"-"+sc.getScene2()));
    }

    /**
     * This tests the difference between the scenes FloraNext0 and FloraNext1. Since these scenes are not the same,
     * there should be non-zero cells in the dynamic pixel mask.
     */
    @Test
    void getDynamicPixelMat() {
        List<SceneCombination> sceneCombinations = getSceneCombinations.getAllSceneCombinations(getStateObjectCollections());
        SceneCombination sceneCombinationWithDifferentScenes =
                getSceneCombinations.getSceneCombinationWithDifferentScenes(sceneCombinations);
        assertNotNull(sceneCombinationWithDifferentScenes);
        Mat dynamicPixels = sceneCombinationWithDifferentScenes.getDynamicPixels();
        int nonzero = countNonZero(dynamicPixels);
        System.out.println("nonzero cells: " + nonzero + " between scenes " +
                sceneCombinationWithDifferentScenes.getScene1() + " and " + sceneCombinationWithDifferentScenes.getScene2());
        assertNotEquals(0, nonzero);
    }

}