package io.github.jspinak.brobot.actions.methods.basicactions.find.states;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.model.element.Pattern;

import java.util.List;

/**
 * Updated helper class for finding states data using new ActionConfig API.
 * Demonstrates migration from ActionOptions to TextFindOptions for OCR operations.
 */
public class FindStatesDataUpdated {

    private ObjectCollection getStateObjectCollection(Pattern scene, ActionService actionService) {
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withScenes(scene)
                .build();
        try {
            // NEW API: Use TextFindOptions for OCR
            TextFindOptions textFindOptions = new TextFindOptions.Builder()
                    .setMaxMatchRetries(3)
                    .setPauseAfterEnd(0.5)
                    .build();
            
            ActionResult matches = new ActionResult();
            matches.setActionConfig(textFindOptions);
            
            ActionInterface findWordsAction = actionService.getAction(textFindOptions).orElse(null);
            if (findWordsAction == null) {
                throw new IllegalStateException("Could not find action for TextFindOptions");
            }
            findWordsAction.perform(matches, objColl);
            
            return new ObjectCollection.Builder()
                    .withImages(matches.getMatchListAsStateImages())
                    .withScenes(scene)
                    .build();
        } catch (Exception e) {
            // OCR may not be available in headless mode
            System.out.println("OCR not available, returning empty ObjectCollection: " + e.getMessage());
            return new ObjectCollection.Builder()
                    .withScenes(scene)
                    .build();
        }
    }

    public List<ObjectCollection> getStateObjectCollections(ActionService actionService) {
        TestData testData = new TestData();
        ObjectCollection stateColl1 = getStateObjectCollection(testData.getFloranext0(), actionService);
        ObjectCollection stateColl2 = getStateObjectCollection(testData.getFloranext1(), actionService);
        ObjectCollection stateColl3 = getStateObjectCollection(testData.getFloranext2(), actionService);
        ObjectCollection stateColl4 = getStateObjectCollection(testData.getFloranext3(), actionService);
        ObjectCollection stateColl5 = getStateObjectCollection(testData.getFloranext4(), actionService);
        return List.of(stateColl1, stateColl2, stateColl3, stateColl4, stateColl5);
    }
}