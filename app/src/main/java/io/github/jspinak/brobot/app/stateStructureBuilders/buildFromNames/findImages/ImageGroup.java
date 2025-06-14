package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.findImages;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.stateStructureBuilders.ExtendedStateImageDTO;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.report.Report;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects Images and Match objects from Images with an active GROUP_DEFINE Attribute.
 * If a match is found for each Image, the Region is ready to be defined. If the defined Region
 * is larger than the currently defined Region, or no SearchRegion has been defined yet, the
 * SearchRegions for all Images in the ImageGroup are set to the newly defined Region.
 */
@Getter
public class ImageGroup {

    private Region searchRegion = new Region();
    private List<ExtendedStateImageDTO> images = new ArrayList<>(); // Images in the Group
    private List<Match> matches = new ArrayList<>(); // for defining the Region (best match / image)
    private ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.DEFINE)
            .setDefineAs(ActionOptions.DefineAs.INCLUDING_MATCHES)
            .build();

    public void addImage(ExtendedStateImageDTO image) {
        images.add(image);
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public ObjectCollection getObjectCollection() {
        return new ObjectCollection.Builder()
                .withMatchObjectsAsRegions(matches.toArray(new Match[0]))
                .build();
    }

    public boolean isEmpty() {
        return images.isEmpty();
    }

    public boolean allImagesFound() {
        return !isEmpty() && images.size() == matches.size();
    }

    public boolean processNewRegion(Region newReg) {
        if (!regionIsLarger(newReg)) return false;
        setSearchRegions(newReg);
        return true;
    }

    public boolean regionIsLarger(Region newReg) {
        if (!searchRegion.isDefined()) return true;
        return newReg.size() > searchRegion.size();
    }

    public void setSearchRegions(Region region) {
        searchRegion = new Region(region);
        images.forEach(img -> img.getStateImage().setSearchRegions(searchRegion));
    }

    public void print() {
        if (images.isEmpty()) return;
        Report.print("Group Define for State."+images.get(0).getAttributes().getStateName() +
                " images: ");
        images.forEach(img -> Report.print(img.getAttributes().getImageName()+" "));
        Report.println();
    }
}
