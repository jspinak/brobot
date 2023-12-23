package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.mock.MatchMaker;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Use the defined regions (if they exist) of objects as MatchObjects
 */
@Component
public class UseDefinedRegion {

    public Matches useRegion(ObjectCollection objectCollection) {
        Matches matches = new Matches();
        for (StateImage sio : objectCollection.getStateImages()) {
            Optional<Region> optReg = sio.getDefinedRegion();
            if (optReg.isPresent()) {
                try {
                    matches.add(new MatchObject(
                            new MatchMaker.Builder().setImageXYWH(optReg.get()).build(), sio, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return matches;
    }
}
