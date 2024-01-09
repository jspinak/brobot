package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

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
        ObjectCollection stateColl1 = getStateObjectCollection(testData.getFloranext1());
        ObjectCollection stateColl2 = getStateObjectCollection(testData.getFloranext2());
        ObjectCollection stateColl3 = getStateObjectCollection(testData.getFloranext3());
        ObjectCollection stateColl4 = getStateObjectCollection(testData.getFloranext4());
        ObjectCollection stateColl5 = getStateObjectCollection(testData.getFloranext5());
        return List.of(stateColl1, stateColl2, stateColl3, stateColl4, stateColl5);
    }

    private Matches getStatesFromFiveScreens(List<ObjectCollection> objectCollections) {
        ActionOptions findStatesOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.STATES)
                .build();
        return matchesInitializer.init(findStatesOptions, objectCollections);
    }

    @Test
    void returnsSomething() {
        List<ObjectCollection> objectCollections = getStateObjectCollections();
        Matches matches = getStatesFromFiveScreens(objectCollections);
        findStates.find(matches, objectCollections);
        System.out.println(matches.toStringAsTempStates());
        assertFalse(matches.isEmpty());
    }
}