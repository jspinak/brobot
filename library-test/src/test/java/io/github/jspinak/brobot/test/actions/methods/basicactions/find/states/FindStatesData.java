package io.github.jspinak.brobot.test.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.states.FindStates;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;

import java.util.List;

public class FindStatesData {

    private final ActionOptions findWordsOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(ActionOptions.Find.ALL_WORDS)
            .setMinArea(50)
            .build();

    private final ActionOptions findStatesOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(ActionOptions.Find.STATES)
            .build();

    private ActionOptions getFindStatesOptions(int minSize) {
        findWordsOptions.setMinArea(minSize);
        return findWordsOptions;
    }

    private final ObjectCollection getObjectCollectionWithScene(Pattern scene) {
        return new ObjectCollection.Builder()
                .withScenes(scene)
                .build();
    }

    private ObjectCollection getStateObjectCollection(Action action, Pattern scene) {
        Matches matches = action.perform(findWordsOptions, getObjectCollectionWithScene(scene));
        return new ObjectCollection.Builder()
                .withImages(matches.getMatchListAsStateImages())
                .withScenes(scene)
                .build();
    }

    public List<ObjectCollection> getStateObjectCollections(Action action) {
        TestData testData = new TestData();
        ObjectCollection stateColl1 = getStateObjectCollection(action, testData.getFloranext0());
        ObjectCollection stateColl2 = getStateObjectCollection(action, testData.getFloranext1());
        ObjectCollection stateColl3 = getStateObjectCollection(action, testData.getFloranext2());
        ObjectCollection stateColl4 = getStateObjectCollection(action, testData.getFloranext3());
        ObjectCollection stateColl5 = getStateObjectCollection(action, testData.getFloranext4());
        return List.of(stateColl1, stateColl2, stateColl3, stateColl4, stateColl5);
    }

    public Matches getMatches(Action action, FindStates findStates, MatchesInitializer matchesInitializer, int minSize) {
        List<ObjectCollection> objectCollections = getStateObjectCollections(action);
        Matches matches = matchesInitializer.init(getFindStatesOptions(minSize), objectCollections);
        findStates.find(matches, objectCollections);
        return matches;
    }

    public Matches getMatches(Action action, FindStates findStates, MatchesInitializer matchesInitializer) {
        List<ObjectCollection> objectCollections = getStateObjectCollections(action);
        Matches matches = matchesInitializer.init(findStatesOptions, objectCollections);
        findStates.find(matches, objectCollections);
        return matches;
    }

}