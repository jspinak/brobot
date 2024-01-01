package io.github.jspinak.brobot.mock;

import org.springframework.stereotype.Component;
import static io.github.jspinak.brobot.actions.BrobotSettings.mockTimeDrag;

@Component
public class MockDrag {

    private final MockTime mockTime;

    public MockDrag(MockTime mockTime) {
        this.mockTime = mockTime;
    }

    /**
     * Drag succeeds when the images are found, but it still takes time to do the drag.
     * @return true
     */
    public boolean drag() {
        mockTime.wait(mockTimeDrag);
        return true;
    }
}
