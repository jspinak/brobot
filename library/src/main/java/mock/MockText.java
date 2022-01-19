package mock;

import com.brobot.multimodule.actions.methods.time.TimeWrapper;
import com.brobot.multimodule.database.primitives.match.MatchHistory;
import com.brobot.multimodule.database.state.stateObject.StateObject;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static com.brobot.multimodule.actions.actionOptions.ActionOptions.Action.GET_TEXT;

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

    public String getString(StateObject stateObject) {
        timeWrapper.wait(GET_TEXT);
        MatchHistory matchHistory = stateObject.getMatchHistory();
        if (matchHistory.getRandomSnapshot(GET_TEXT).isEmpty()) return getRandomString();
        return matchHistory.getRandomString();
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
