package io.github.jspinak.brobot.buildStateStructure.buildFromNames.findImages;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
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

    public void ifNeededAddSnapshot(StateImageObject image, List<Match> matches,
                                    List<AttributeTypes.Attribute> attributes) {
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

    private boolean imageShouldExist(List<AttributeTypes.Attribute> attributes) {
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
