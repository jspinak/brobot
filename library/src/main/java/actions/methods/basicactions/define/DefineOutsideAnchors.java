package actions.methods.basicactions.define;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
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
