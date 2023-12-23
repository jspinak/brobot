package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.time.TimeWrapper;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject_;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateService;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static io.github.jspinak.brobot.actions.BrobotSettings.mockTimeDrag;

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
    private final StateMemory stateMemory;

    public Mock(StateService stateService, MockFind mockFind, TimeWrapper timeWrapper, StateMemory stateMemory) {
        this.stateService = stateService;
        this.mockFind = mockFind;
        this.timeWrapper = timeWrapper;
        this.stateMemory = stateMemory;
    }

    public Matches getMatches(StateImage stateImage, Region searchRegion,
                              ActionOptions actionOptions) {
        Report.println("Finding " + stateImage.getName() + " in mock");
        timeWrapper.wait(actionOptions.getFind());
        timeWrapper.printNow();
        if (!stateExists(stateImage)) {
            Report.print("Owner State not found. ");
            return new Matches(); // assuming no shared state images
        }
        return mockFind.getMatches(stateImage, searchRegion, actionOptions);
    }

    public List<Match> getMatches(Pattern pattern) {
        timeWrapper.wait(ActionOptions.Find.ALL); // all finds are initially Find.ALL now
        Optional<MatchSnapshot> optionalMatchSnapshot =
                pattern.getMatchHistory().getRandomSnapshot(ActionOptions.Action.FIND, stateMemory.getActiveStates());
        if (optionalMatchSnapshot.isEmpty()) return new ArrayList<>();
        return optionalMatchSnapshot.get().getMatchList();
    }

    private boolean stateExists(StateImage stateImage) {
        Optional<State> state = stateService.findByName(stateImage.getOwnerStateName());
        return state.filter(value -> value.getProbabilityExists() > new Random().nextInt(100)).isPresent();
    }

    /**
     * Drag succeeds when the images are found, but it still takes time to do the drag.
     * @return true
     */
    public boolean drag() {
        timeWrapper.wait(mockTimeDrag);
        return true;
    }

    public Region getFocusedWindow() {
        return new Region();
    }
}
