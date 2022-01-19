package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a Region with the Define method specified in the ActionOptions.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineRegion implements ActionInterface {

    private final Map<ActionOptions.DefineAs, ActionInterface> actions = new HashMap<>();

    public DefineRegion(DefineWithWindow defineWithWindow, DefineWithMatch defineWithMatch,
                        DefineInsideAnchors defineInsideAnchors, DefineOutsideAnchors defineOutsideAnchors) {
        actions.put(ActionOptions.DefineAs.FOCUSED_WINDOW, defineWithWindow);
        actions.put(ActionOptions.DefineAs.MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.BELOW_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.ABOVE_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.LEFT_OF_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.RIGHT_OF_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.INSIDE_ANCHORS, defineInsideAnchors);
        actions.put(ActionOptions.DefineAs.OUTSIDE_ANCHORS, defineOutsideAnchors);
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        System.out.print("Define as: " + actionOptions.getDefineAs()+"| ");
        return actions.get(actionOptions.getDefineAs()).perform(actionOptions, objectCollections);
    }

}
