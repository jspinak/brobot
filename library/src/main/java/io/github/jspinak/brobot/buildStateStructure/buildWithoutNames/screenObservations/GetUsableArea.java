package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
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
     * Images need to be StateImageObject(s) because only StateObject(s) have anchors. Anchors are added
     * to the StateImageObject(s).
     * @param topleft sets the top left boundary of the usable area.
     * @param bottomright sets the bottom right boundary of the usable area.
     * @return the usable area.
     */
    public Region getBoundariesFromExcludedImages(Image topleft, Image bottomright) {
        ObjectCollection exteriorImages = new ObjectCollection.Builder()
                .withImages(
                        new StateImageObject.Builder()
                                .withImages(topleft)
                                .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
                                .build(),
                        new StateImageObject.Builder()
                                .withImages(bottomright)
                                .addAnchor(Position.Name.BOTTOMRIGHT, Position.Name.TOPRIGHT)
                                .build())
                .build();
        ActionOptions define = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                .build();
        return action.perform(define, exteriorImages).getDefinedRegion();
    }

    public Region getFromFile(String patternName, Image topLeft, Image bottomRight) {
        File file = new File(BrobotSettings.screenshotPath + patternName);
        String path = file.getAbsolutePath();
        List<Match> topLeftMatches = getMatchesInFile(path, topLeft);
        List<Match> bottomRightMatches = getMatchesInFile(path, bottomRight);
        Location topLeftLocation = getLocationFromMatches(topLeftMatches, Position.Name.TOPLEFT);
        Location bottomRightLocation = getLocationFromMatches(bottomRightMatches, Position.Name.BOTTOMRIGHT);
        return new Region(topLeftLocation, bottomRightLocation);
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

    private List<Match> getMatchesInFile(String path, Image image) {
        List<Match> matchList = new ArrayList<>();
        image.getAllPatterns().forEach(p -> {
            Finder f = new Finder(path);
            f.findAll(p);
            while (f.hasNext()) matchList.add(f.next());
            f.destroy();
        });
        return matchList;
    }

}
