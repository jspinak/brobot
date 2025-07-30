package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.FindState;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.TestDataUpdated;

import java.util.List;

/**
 * Updated FindStatesData class that uses the new ActionConfig API.
 * Migrated from Object// ActionOptions to TextFindOptions and PatternFindOptions.
 */
public class FindStatesDataUpdated {

    private final TextFindOptions findWordsOptions = new TextFindOptions.Builder()
            .setMaxMatchRetries(3)
            .build();

    private final PatternFindOptions findStatesOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();

    private TextFindOptions getFindStatesOptions(int minSize) {
        // TextFindOptions doesn't have minSize, but we can use maxMatchRetries
        return new TextFindOptions.Builder()
                .setMaxMatchRetries(3)
                .build();
    }

    private ObjectCollection getObjectCollectionWithScene(Pattern scene) {
        return new ObjectCollection.Builder()
                .withScenes(scene)
                .build();
    }

    private ObjectCollection getStateObjectCollection(Action action, Pattern scene) {
        try {
            ActionResult matches = action.perform(findWordsOptions, getObjectCollectionWithScene(scene));
            return new ObjectCollection.Builder()
                    .withImages(matches.getMatchListAsStateImages())
                    .withScenes(scene)
                    .build();
        } catch (Exception e) {
            // OCR may not be available in headless mode
            System.out.println("OCR not available for scene, returning empty ObjectCollection: " + e.getMessage());
            return new ObjectCollection.Builder()
                    .withScenes(scene)
                    .build();
        }
    }

    public List<ObjectCollection> getStateObjectCollections(Action action) {
        TestDataUpdated testData = new TestDataUpdated();
        ObjectCollection stateColl1 = getStateObjectCollection(action, testData.getFloranext0());
        ObjectCollection stateColl2 = getStateObjectCollection(action, testData.getFloranext1());
        ObjectCollection stateColl3 = getStateObjectCollection(action, testData.getFloranext2());
        ObjectCollection stateColl4 = getStateObjectCollection(action, testData.getFloranext3());
        ObjectCollection stateColl5 = getStateObjectCollection(action, testData.getFloranext4());
        return List.of(stateColl1, stateColl2, stateColl3, stateColl4, stateColl5);
    }

    public ActionResult getMatches(Action action, FindState findState, ActionResultFactory matchesInitializer, int minSize) {
        List<ObjectCollection> objectCollections = getStateObjectCollections(action);
        ActionResult matches = matchesInitializer.init(getFindStatesOptions(minSize), objectCollections);
        findState.find(matches, objectCollections);
        return matches;
    }

    public ActionResult getMatches(Action action, FindState findState, ActionResultFactory matchesInitializer) {
        List<ObjectCollection> objectCollections = getStateObjectCollections(action);
        ActionResult matches = matchesInitializer.init(findStatesOptions, objectCollections);
        findState.find(matches, objectCollections);
        return matches;
    }

}