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

    private final DefineHelper defineHelper;

    public DefineWithMatch(DefineHelper defineHelper) {
        this.defineHelper = defineHelper;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        defineHelper.findMatches(matches, objectCollections);
        if (matches.getBestMatch().isEmpty()) return;
        Region region = new Region(matches.getBestMatch().get());
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.BELOW_MATCH) region.setY(region.y() + region.h());
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.ABOVE_MATCH) region.setY(region.y() - region.h());
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.LEFT_OF_MATCH) region.setX(region.x() - region.w());
        if (actionOptions.getDefineAs() == ActionOptions.DefineAs.RIGHT_OF_MATCH) region.setX(region.x() + region.w());
        defineHelper.adjust(region, actionOptions);
        matches.addDefinedRegion(region);
    }
}
