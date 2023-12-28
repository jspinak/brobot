package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * When building the state structure with automation, Brobot will move around the application in real time.
 * This class provides convenience functions for defining the application boundaries.
 */
@Component
public class GetUsableArea {

    private final Action action;

    public GetUsableArea(Action action) {
        this.action = action;
    }

    /**
     * The DEFINE operation uses a Find.EACH for objects, so objects can be placed in a single
     * ObjectCollection. The bottom-left point of the topleft match and the top-right point of the
     * bottomright match will be used to define the usable area region.
     * Images need to be StateImage(s) because only StateObject(s) have anchors. Anchors are added
     * to the StateImage(s).
     * @param topleft sets the top left boundary of the usable area.
     * @param bottomright sets the bottom right boundary of the usable area.
     * @return the usable area.
     */
    public Region getBoundariesFromExcludedImages(Pattern topleft, Pattern bottomright) {
        ObjectCollection exteriorImages = getObjectCollection(topleft, bottomright);
        ActionOptions define = getActionOptions(ActionOptions.Illustrate.NO);
        return action.perform(define, exteriorImages).getDefinedRegion();
    }

    private ObjectCollection getObjectCollection(Pattern topleft, Pattern bottomright, Pattern... scenes) {
        return new ObjectCollection.Builder()
                .withImage_s(
                        new StateImage.Builder()
                                .addPattern(topleft)
                                .build(),
                        new StateImage.Builder()
                                .addPattern(bottomright)
                                .build())
                .withScene_s(scenes)
                .build();
    }

    private ActionOptions getActionOptions(ActionOptions.Illustrate illustrate) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                .setIllustrate(illustrate)
                .build();
    }

    public Region defineInFile(String patternName, Pattern topLeft, Pattern bottomRight) {
        File file = new File(BrobotSettings.screenshotPath + patternName);
        String path = file.getAbsolutePath();
        Pattern screen = new Pattern(path);
        ObjectCollection boundaryImages = getObjectCollection(topLeft, bottomRight, screen);
        ActionOptions defineInsideAnchors = getActionOptions(ActionOptions.Illustrate.YES);
        Matches matches = action.perform(defineInsideAnchors, boundaryImages);
        return matches.getDefinedRegion();
    }

    private Location getLocationFromMatches(List<Match> matchList, Position.Name position) {
        Region region;
        if (position == Position.Name.TOPLEFT) region = new Region(0,0,0,0);
        else region = new Region(new Screen().x, new Screen().y, 0,0);
        if (!matchList.isEmpty()) {
            region = new Region(matchList.get(0));
            for (int i=1; i<matchList.size(); i++) {
                region.setAsUnion(new Region(matchList.get(i)));
            }
        }
        return new Location(region, position);
    }

}
