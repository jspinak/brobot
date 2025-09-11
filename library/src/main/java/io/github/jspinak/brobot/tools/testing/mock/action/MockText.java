package io.github.jspinak.brobot.tools.testing.mock.action;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

/** Mock text for GetText Actions using Snapshots. */
@Component
public class MockText {

    private final MockTime mockTime;

    public MockText(MockTime mockTime) {
        this.mockTime = mockTime;
    }

    public String getString(Match match) {
        mockTime.wait(ActionType.FIND);
        return match.getText();
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
