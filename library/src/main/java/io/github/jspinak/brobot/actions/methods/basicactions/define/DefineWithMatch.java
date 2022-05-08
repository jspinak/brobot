package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
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
