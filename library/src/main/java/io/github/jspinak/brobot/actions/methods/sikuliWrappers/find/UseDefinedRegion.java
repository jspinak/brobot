package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.database.primitives.match.MatchObject;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.mock.MatchMaker;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UseDefinedRegion {

    public Matches useRegion(ObjectCollection objectCollection) {
        Matches matches = new Matches();
        for (StateImageObject sio : objectCollection.getStateImages()) {
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
