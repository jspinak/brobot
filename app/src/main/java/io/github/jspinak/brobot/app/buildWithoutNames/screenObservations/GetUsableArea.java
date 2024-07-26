package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import io.github.jspinak.brobot.app.services.StateImageService;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * When building the state structure with automation, Brobot will move around the application in real time.
 * This class provides convenience functions for defining the application boundaries.
 */
@Component
public class GetUsableArea {

    private final Action action;
    private final StateImageService stateImageService;

    public GetUsableArea(Action action, StateImageService stateImageService) {
        this.action = action;
        this.stateImageService = stateImageService;
    }

    /**
     * The DEFINE operation uses a Find.EACH for objects, so objects can be placed in a single
     * ObjectCollection. The bottom-left point of the topleft match and the top-right point of the
     * bottomright match will be used to define the usable area region.
     * Images need to be StateImage(s) because only StateObject(s) have anchors. Anchors are added
     * to the StateImage(s).
     * @param stateStructureTemplate holds the images and scenes
     * @return the usable area.
     */
    public Region getBoundariesFromExcludedImages(StateStructureTemplate stateStructureTemplate) {
        ObjectCollection exteriorImages = getObjectCollection(
                stateStructureTemplate.getTopLeftBoundary(), stateStructureTemplate.getBottomRightBoundary());
        if (!stateStructureTemplate.isLive()) exteriorImages.setScenes(stateStructureTemplate.getScreenshots());
        ActionOptions define = getActionOptions(ActionOptions.Illustrate.NO);
        return action.perform(define, exteriorImages).getDefinedRegion();
    }

    private ObjectCollection getObjectCollection(Pattern topleft, Pattern bottomright, Pattern... scenes) {
        StateImage tlSI = topleft.inNullState();
        StateImage brSI = bottomright.inNullState();
        stateImageService.saveStateImages(tlSI, brSI); // saving allocates the Ids, needed for Find.EACH
        return new ObjectCollection.Builder()
                .withImages(tlSI, brSI)
                .withScenes(scenes)
                .build();
    }

    private ActionOptions getActionOptions(ActionOptions.Illustrate illustrate) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                .setIllustrate(illustrate)
                .build();
    }

    public Region defineInFile(Pattern screen, Pattern topLeft, Pattern bottomRight) {
        ObjectCollection boundaryImages = getObjectCollection(topLeft, bottomRight, screen);
        ActionOptions defineInsideAnchors = getActionOptions(ActionOptions.Illustrate.YES);
        Matches matches = action.perform(defineInsideAnchors, boundaryImages);
        System.out.println(matches);
        return matches.getDefinedRegion();
    }

    private Location getLocationFromMatches(List<Match> matchList, Positions.Name position) {
        Region region;
        if (position == Positions.Name.TOPLEFT) region = new Region(0,0,0,0);
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
