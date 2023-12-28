package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.actions.methods.time.TimeWrapper;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.GET_TEXT;

/**
 * Mock text for GetText Actions using the probability method (and not Snapshots).
 */
@Component
public class MockText {

    private MockStatus mockStatus;
    private TimeWrapper timeWrapper;

    public MockText(MockStatus mockStatus, TimeWrapper timeWrapper) {
        this.mockStatus = mockStatus;
        this.timeWrapper = timeWrapper;
    }

    public String getString(Match match) {
        timeWrapper.wait(GET_TEXT);
        MatchHistory matchHistory = match.getPattern().getMatchHistory();
        if (matchHistory.getRandomSnapshot(GET_TEXT).isEmpty()) return getRandomString();
        return matchHistory.getRandomText();
    }

    public String getRandomString() {
        return getRandomString(new Random().nextInt(15));
    }

    public String getRandomString(int stringSize) {
        byte[] array = new byte[stringSize];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }
}
