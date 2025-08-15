package io.github.jspinak.brobot.tools.testing.mock.action;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.testing.mock.time.ActionDurations;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Returns snapshots with matching actions and states.
 */
@Component
public class MockFind {
    private final ActionDurations actionDurations;
    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final MockTime mockTime;

    public MockFind(ActionDurations actionDurations, StateMemory stateMemory, StateService allStatesInProjectService, MockTime mockTime) {
        this.actionDurations = actionDurations;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.mockTime = mockTime;
    }

    /**
     * Finds mock matches based on matching actions and states in the MatchHistory.
     * @param pattern the image to find
     * @return a list of match objects for a randomly selected collection of matching snapshots
     */
    public List<Match> getMatches(Pattern pattern) {
        mockTime.wait(actionDurations.getActionDuration(ActionType.FIND));
        Optional<ActionRecord> optionalMatchSnapshot =
                pattern.getMatchHistory().getRandomSnapshot(ActionType.FIND, stateMemory.getActiveStates());
        if (optionalMatchSnapshot.isEmpty()) return new ArrayList<>();
        return optionalMatchSnapshot.get().getMatchList();
    }

    /**
     * These matches are from searching a region for all words. As with image matches, all matches should be returned
     * and the matches falling within the regions will be selected at a higher level. Words are likely to be state
     * specific and are thus stored in the State variable MatchHistory.
     * @return a list of Match objects for the corresponding states.
     */
    public List<Match> getWordMatches() {
        mockTime.wait(actionDurations.getActionDuration(ActionType.FIND));
        List<Match> allMatches = new ArrayList<>();
        for (Long stateId : stateMemory.getActiveStates()) {
            Optional<State> state = allStatesInProjectService.getState(stateId);
            state.ifPresent(st -> {
                Optional<ActionRecord> snapshot = st.getMatchHistory().getRandomSnapshot(ActionType.FIND, stateId);
                snapshot.ifPresent(snap -> allMatches.addAll(snap.getMatchList()));
            });
        }
        return allMatches;
    }
}
