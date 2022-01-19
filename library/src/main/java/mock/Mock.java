package mock;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.time.TimeWrapper;
import com.brobot.multimodule.database.primitives.location.Location;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.state.State;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.services.StateService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

import static com.brobot.multimodule.actions.BrobotSettings.*;
import static com.brobot.multimodule.actions.BrobotSettings.mockTimeDrag;

/**
 * The Action methods should be unaware of the mock process, which should occur only
 * at the interface between Brobot and Sikuli, where the Wrapper classes live. They decide whether the
 * Action will be mocked or executed, and call methods in this class to provide mocked
 * Matches and mocked passage of time.
 */
@Component
public class Mock {

    private StateService stateService;
    private MockFind mockFind;
    private TimeWrapper timeWrapper;

    public Mock(StateService stateService, MockFind mockFind, TimeWrapper timeWrapper) {
        this.stateService = stateService;
        this.mockFind = mockFind;
        this.timeWrapper = timeWrapper;
    }

    public Matches getMatches(StateImageObject stateImageObject, Region searchRegion,
                              ActionOptions actionOptions) {
        timeWrapper.wait(actionOptions.getFind());
        timeWrapper.printNow();
        if (!stateExists(stateImageObject)) return new Matches(); // assuming no shared state images
        return mockFind.getMatches(stateImageObject, searchRegion, actionOptions);
    }

    private boolean stateExists(StateImageObject stateImageObject) {
        Optional<State> state = stateService.findByName(stateImageObject.getOwnerStateName());
        return state.filter(value -> value.getProbabilityExists() > new Random().nextInt(100)).isPresent();
    }

    /**
     * Drag succeeds when the images are found, but it still takes time to do the drag.
     */
    public boolean drag(Location from, Location to) {
        timeWrapper.wait(mockTimeDrag);
        System.out.print("drag:"+from.getX()+"."+from.getY()+">"+to.getX()+"."+to.getY());
        return true;
    }

    public Region getFocusedWindow() {
        return new Region();
    }
}
