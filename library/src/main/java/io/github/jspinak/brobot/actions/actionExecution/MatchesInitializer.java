package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycle;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Manages the Matches throughout the lifecycle of the action. There are many actions that pass Matches and transfer
 * Matches from one class to another, and this can be confusing. With this class, the Matches object will be setup
 * at the time the action is initiated, and any changes to the Matches object can be performed here.
 */
@Component
@Getter
@Setter
public class MatchesInitializer {

    private final GetSceneAnalysisCollection getSceneAnalysisCollection;
    private final Time time;

    public MatchesInitializer(GetSceneAnalysisCollection getSceneAnalysisCollection, Time time) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.time = time;
    }

    /**
     * Initialize new Matches with ActionOptions, a description, and a SceneAnalysisCollection.
     * @param actionOptions the action options
     * @param actionDescription a description of the action
     * @param objectCollections the objects to act on
     */
    public Matches init(ActionOptions actionOptions, String actionDescription, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        matches.setActionLifecycle(new ActionLifecycle(time.now(), actionOptions.getMaxWait()));
        matches.setActionOptions(actionOptions);
        matches.setActionDescription(actionDescription);
        SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
        if (actionOptions.getFind() == ActionOptions.Find.COLOR) sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections), 1, 0, actionOptions);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        return matches;
    }

    public Matches init(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        return init(actionOptions, "", objectCollections.toArray(new ObjectCollection[0]));
    }

    public Matches init(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        return init(actionOptions, "", objectCollections);
    }

    /**
     * In every iteration of an action, Matches will be produced. Some actions will require adding all
     * information from the new Matches to the global Matches; other actions will add only specific variables.
     * VANISH should produce the Image objects that were once found and later not found.
     * FIND should add every MatchObject that has a new match region.
     * The SceneAnalysisCollection is initially created with one SceneAnalysis. A SceneAnalysis contains the
     * scene, illustrations, match list and contours (as well as other variables). For most actions, one SceneAnalysis
     * should be sufficient and new SceneAnalysis objects shouldn't be created. The initial SceneAnalysis object can
     * be modified in the action's classes.
     */

}
