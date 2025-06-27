package io.github.jspinak.brobot.tools.testing.mock.action;

import static io.github.jspinak.brobot.config.FrameworkSettings.mockTimeDrag;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

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
