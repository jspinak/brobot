package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Use the defined regions (if they exist) of objects as MatchObjects
 */
@Component
public class UseDefinedRegion {

    public Matches useRegion(Matches matches, ObjectCollection objectCollection) {
        for (StateImage si : objectCollection.getStateImages()) {
            for (Pattern pattern : si.getPatterns()) {
                for (Region region : pattern.getRegions()) {
                    matches.add(
                            new Match.Builder()
                                    .setRegion(region)
                                    .setSearchImage(pattern.getBImage())
                                    .setStateObjectData(si)
                                    .build()
                    );
                }
            }
        }
        return matches;
    }
}
