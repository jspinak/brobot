package actions.methods.sikuliWrappers.find;

import com.brobot.multimodule.database.primitives.match.MatchObject;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.mock.MatchMaker;
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
