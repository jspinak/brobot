package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;

import java.util.List;

public class FindStatesData {

    private final ActionOptions findWordsOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(ActionOptions.Find.ALL_WORDS)
            .build();

    private final ActionOptions findStatesOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(ActionOptions.Find.STATES)
            .build();

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

    private List<ObjectCollection> getStateObjectCollections(Action action) {
        TestData testData = new TestData();
        ObjectCollection stateColl1 = getStateObjectCollection(action, testData.getFloranext1());
        ObjectCollection stateColl2 = getStateObjectCollection(action, testData.getFloranext2());
        ObjectCollection stateColl3 = getStateObjectCollection(action, testData.getFloranext3());
        ObjectCollection stateColl4 = getStateObjectCollection(action, testData.getFloranext4());
        ObjectCollection stateColl5 = getStateObjectCollection(action, testData.getFloranext5());
        return List.of(stateColl1, stateColl2, stateColl3, stateColl4, stateColl5);
    }

    public Matches getMatches(Action action, FindStates findStates, MatchesInitializer matchesInitializer) {
        List<ObjectCollection> objectCollections = getStateObjectCollections(action);
        Matches matches = matchesInitializer.init(findStatesOptions, objectCollections);
        findStates.find(matches, objectCollections);
        return matches;
    }

}