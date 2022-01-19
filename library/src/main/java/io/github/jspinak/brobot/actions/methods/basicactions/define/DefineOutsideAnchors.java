package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Defines a Region as the largest rectangle produced by Matches and Locations.
 * Matches can contain Anchors that specify the point to use.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineOutsideAnchors implements ActionInterface {

    private DefineHelper adjustRegion;

    public DefineOutsideAnchors(DefineHelper adjustRegion) {
        this.adjustRegion = adjustRegion;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        return new Matches(); // placeholder
    }
}
