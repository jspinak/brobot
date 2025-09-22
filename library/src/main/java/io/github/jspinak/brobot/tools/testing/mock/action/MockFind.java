package io.github.jspinak.brobot.tools.testing.mock.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.testing.mock.time.ActionDurations;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

/** Returns snapshots with matching actions and states. */
@Component
public class MockFind {
    private final ActionDurations actionDurations;
    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final MockTime mockTime;

    public MockFind(
            ActionDurations actionDurations,
            StateMemory stateMemory,
            StateService allStatesInProjectService,
            MockTime mockTime) {
        this.actionDurations = actionDurations;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.mockTime = mockTime;
    }

    /**
     * Finds mock matches based on matching actions and states in the MatchHistory. If no history
     * exists, generates a default successful match to support testing.
     *
     * @param pattern the image to find
     * @return a list of match objects for a randomly selected collection of matching snapshots
     */
    public List<Match> getMatches(Pattern pattern) {
        mockTime.wait(actionDurations.getActionDuration(ActionType.FIND));
        // Use PatternFindOptions instead of ActionType for non-deprecated method
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();

        // Check if pattern has match history
        if (pattern.getMatchHistory() == null || pattern.getMatchHistory().isEmpty()) {
            // Return default successful match for testing when no history exists
            // This ensures mock mode works even without pre-configured history
            return generateDefaultMockMatch(pattern);
        }

        Optional<ActionRecord> optionalMatchSnapshot =
                pattern.getMatchHistory()
                        .getRandomSnapshot(findOptions, stateMemory.getActiveStates());

        if (optionalMatchSnapshot.isEmpty()) {
            // If no matching snapshot found but history exists, return empty
            // This respects the configured history even if no matches for current state
            return new ArrayList<>();
        }

        return optionalMatchSnapshot.get().getMatchList();
    }

    /**
     * Generates a default mock match when no history is configured. This ensures mock mode works
     * for testing without extensive setup.
     *
     * @param pattern the pattern to generate a match for
     * @return a list containing a single default match
     */
    private List<Match> generateDefaultMockMatch(Pattern pattern) {
        Match defaultMatch =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 50, 50))
                        .setSimScore(0.95)
                        .setName(
                                pattern.getNameWithoutExtension() != null
                                        ? pattern.getNameWithoutExtension()
                                        : "MockMatch")
                        .build();
        List<Match> matches = new ArrayList<>();
        matches.add(defaultMatch);
        return matches;
    }

    /**
     * These matches are from searching a region for all words. As with image matches, all matches
     * should be returned and the matches falling within the regions will be selected at a higher
     * level. Words are likely to be state specific and are thus stored in the State variable
     * MatchHistory.
     *
     * @return a list of Match objects for the corresponding states.
     */
    public List<Match> getWordMatches() {
        mockTime.wait(actionDurations.getActionDuration(ActionType.FIND));
        List<Match> allMatches = new ArrayList<>();
        // Use PatternFindOptions instead of ActionType for non-deprecated method
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        for (Long stateId : stateMemory.getActiveStates()) {
            Optional<State> state = allStatesInProjectService.getState(stateId);
            state.ifPresent(
                    st -> {
                        Optional<ActionRecord> snapshot =
                                st.getMatchHistory().getRandomSnapshot(findOptions, stateId);
                        snapshot.ifPresent(snap -> allMatches.addAll(snap.getMatchList()));
                    });
        }
        return allMatches;
    }
}
