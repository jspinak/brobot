package io.github.jspinak.brobot.actions;

import org.springframework.stereotype.Component;

@Component
public class Permissions {

    public boolean isMock() {
        return BrobotSettings.mock && BrobotSettings.screenshots.isEmpty();
    }
}
