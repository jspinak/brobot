package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Mock text for GetText Actions using Snapshots.
 */
@Component
public class MockText {

    private final MockTime mockTime;

    public MockText(MockTime mockTime) {
        this.mockTime = mockTime;
    }

    public String getString(Match match) {
        mockTime.wait(ActionOptions.Action.FIND);
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
