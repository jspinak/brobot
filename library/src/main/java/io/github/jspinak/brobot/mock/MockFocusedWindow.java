package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

@Component
public class MockFocusedWindow {

    public Region getFocusedWindow() {
        return new Region();
    }
}
