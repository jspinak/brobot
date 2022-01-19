package actions.methods.basicactions.define;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Defines a Region around a Match.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineWithMatch implements ActionInterface {

    private DefineHelper defineHelper;

    public DefineWithMatch(DefineHelper defineHelper) {
        this.defineHelper = defineHelper;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = defineHelper.findMatches(actionOptions, objectCollections);
        if (matches.getBestMatch().isEmpty()) return matches;
        Region region = new Region(matches.getBestMatch().get().getMatch());
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.BELOW_MATCH) region.y += region.h;
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.ABOVE_MATCH) region.y -= region.h;
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.LEFT_OF_MATCH) region.x -= region.w;
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.RIGHT_OF_MATCH) region.x += region.w;
        defineHelper.adjust(region, actionOptions);
        matches.addDefinedRegion(region);
        return matches;
    }
}
