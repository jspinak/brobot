package io.github.jspinak.brobot.actions.methods.sikuliWrappers;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.mock.Mock;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Gets the window of the App in focus.
 */
@Component
public class App {

    private Mock mock;

    public App(Mock mock) {
        this.mock = mock;
    }

    public Optional<Region> focusedWindow() {
        if (BrobotSettings.mock) return Optional.of(mock.getFocusedWindow());
        org.sikuli.script.Region reg = org.sikuli.script.App.focusedWindow();
        if (reg == null) return Optional.empty();
        return Optional.of(new Region(reg));
    }
}
