package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.actionOptions.CopyActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

/**
 * Helper functions for various Define classes
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineHelper {

    private Find find;
    private final MatchesInitializer matchesInitializer;

    public DefineHelper(Find find, MatchesInitializer matchesInitializer) {
        this.find = find;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * Adjust the defined region according to the ActionOptions.
     * @param region is adjusted depending on the action configuration.
     * @param actionOptions holds the action configuration.
     */
    public void adjust(Region region, ActionOptions actionOptions) {
        region.setX(region.x() + actionOptions.getAddX());
        region.setY(region.y() + actionOptions.getAddY());
        if (actionOptions.getAbsoluteW() >= 0) region.setW(actionOptions.getAbsoluteW());
        else region.setW(region.w() + actionOptions.getAddW());
        if (actionOptions.getAbsoluteH() >= 0) region.setH(actionOptions.getAbsoluteH());
        else region.setH(region.h() + actionOptions.getAddH());
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
     * @param matches holds the action configuration.
     * @param objectCollections holds the objects to find.
     */
    public void findMatches(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions findOptions = CopyActionOptions.copyImmutableOptions(matches.getActionOptions());
        findOptions.setFind(ActionOptions.Find.EACH);
        findOptions.setAddH(0);
        findOptions.setAddW(0);
        findOptions.setAddY(0);
        findOptions.setAddX(0);
        findOptions.setAbsoluteH(-1);
        findOptions.setAbsoluteW(-1);
        Matches findMatches = matchesInitializer.init(findOptions, objectCollections);
        find.perform(findMatches, objectCollections);
        matches.addMatchObjects(findMatches);
    }

}
