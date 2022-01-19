package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.actionOptions.CopyActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Helper functions for various Define classes
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineHelper {

    private Find find;

    public DefineHelper(Find find) {
        this.find = find;
    }

    /**
     * Adjust the defined region according to the ActionOptions.
     */
    public void adjust(Region region, ActionOptions actionOptions) {
        region.x += actionOptions.getAddX();
        region.y += actionOptions.getAddY();
        if (actionOptions.getAbsoluteW() >= 0) region.w = actionOptions.getAbsoluteW();
        else region.w += actionOptions.getAddW();
        if (actionOptions.getAbsoluteH() >= 0) region.h = actionOptions.getAbsoluteH();
        else region.h += actionOptions.getAddH();
    }

    /**
     * The original ActionOptions brought us here, and now we can change it to find region matches.
     * There may be options in the actionOptions parameter such as addX and addW that
     * should apply only to the Define operation and not the Find operation. If these are not removed
     * from the ActionOptions during the Find operation then all matches will also be adjusted by
     * these values. Since they are meant for the Define operation, i.e. AbsoluteW of 800, this will
     * cause unwanted behavior for the Find operation. There are, however, other values that are
     * meant for the Find operation (such as MinSimilarity).
     *
     * Uses Find.EACH, which returns 1 Match per object in the first ObjectCollection
     *
     */
    public Matches findMatches(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        ActionOptions findOptions = CopyActionOptions.copyImmutableOptions(actionOptions);
        findOptions.setFind(ActionOptions.Find.EACH);
        findOptions.setAddH(0);
        findOptions.setAddW(0);
        findOptions.setAddY(0);
        findOptions.setAddX(0);
        findOptions.setAbsoluteH(-1);
        findOptions.setAbsoluteW(-1);
        return find.perform(findOptions, objectCollections);
    }


}
