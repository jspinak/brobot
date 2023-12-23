package io.github.jspinak.brobot.buildStateStructure.buildFromNames.findImages;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.*;
import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.GROUP_DEFINE;

/**
 * Snapshots are created only when an appropriate Attribute is active on the page
 *   (i.e. APPEARS, APPEARS_EXCLUSIVELY, DEFINE, GROUP_DEFINE).
 * All MatchSnapshots are added with the ActionOptions set to Find.ALL. Find.ALL Snapshots
 *   can be used for Find.First and other Find operations as well.
 * Adds a failed match when the image is not found.
 **/
@Component
public class AddSnapshots {

    private List<AttributeTypes.Attribute> imageShouldExist = new ArrayList<>();
    {
        imageShouldExist.add(APPEARS);
        imageShouldExist.add(APPEARS_EXCLUSIVELY);
        imageShouldExist.add(DEFINE);
        imageShouldExist.add(GROUP_DEFINE);
    }

    /**
     * Snapshots are not added if the image should not be found on this screenshot. In a mock run, if the
     * image's state is not active, the image is assumed not to exist. Mocks currently don't allow for false
     * positives.
     * If there are no matches, add an empty snapshot. This signifies that the image exists but was not found.
     * Otherwise, add all matches as snapshots.
     * @param image the image searched for
     * @param matches matches found
     * @param attributes image attributes (tells us if the image should be found on this screen)
     */
    public void ifNeededAddSnapshot(StateImage image, List<Match> matches,
                                    List<AttributeTypes.Attribute> attributes) {
        //Report.println("size of matches = "+matches.size());
        if (!imageShouldExist(attributes)) return;
        if (matches.isEmpty()) image.addSnapshot(new MatchSnapshot());
        else image.addSnapshot(getSnapshot(matches));
    }

    private MatchSnapshot getSnapshot(List<Match> matches) {
        MatchSnapshot snapshot = new MatchSnapshot();
        matches.forEach(snapshot::addMatch);
        snapshot.setActionSuccess(true);
        return snapshot;
    }

    /**
     * The image should exist when the image attribute for this page is either
     *   APPEARS, APPEARS_EXCLUSIVELY, DEFINE, or GROUP_DEFINE.
     * @param attributes the image attributes for this page
     * @return yes if one of the image's attributes for this page is APPEARS, APPEARS_EXCLUSIVELY, DEFINE, or GROUP_DEFINE.
     */
    private boolean imageShouldExist(List<AttributeTypes.Attribute> attributes) {
        //Report.println("attributes: "+attributes+" imageShouldExist: "+imageShouldExist);
        boolean shouldExist = false;
        for (AttributeTypes.Attribute attribute : attributes) {
            if (imageShouldExist.contains(attribute)) {
                shouldExist = true;
                break;
            }
        }
        return shouldExist;
    }
}
