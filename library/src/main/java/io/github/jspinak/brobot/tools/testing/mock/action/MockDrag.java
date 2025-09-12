package io.github.jspinak.brobot.tools.testing.mock.action;

import static io.github.jspinak.brobot.config.core.FrameworkSettings.mockTimeDrag;
import static io.github.jspinak.brobot.config.core.FrameworkSettings.mockActionSuccessProbability;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

@Component
public class MockDrag {

    private final MockTime mockTime;

    public MockDrag(MockTime mockTime) {
        this.mockTime = mockTime;
    }

    /**
     * Drag succeeds based on the configured success probability.
     * The action still takes time to simulate the drag operation.
     *
     * @return true if the action succeeds based on mockActionSuccessProbability
     */
    public boolean drag() {
        mockTime.wait(mockTimeDrag);
        // Use the configured success probability to determine if the action succeeds
        return Math.random() < mockActionSuccessProbability;
    }
}
