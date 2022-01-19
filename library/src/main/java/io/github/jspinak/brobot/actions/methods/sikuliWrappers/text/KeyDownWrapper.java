package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * NOT WORKING AS EXPECTED
 * Wrapper class for KeyDown, holds a Key down as a real or mock action.
 */
@Component
public class KeyDownWrapper {

    public void press(String key, String modifiers) {
        if (BrobotSettings.mock) {
            Report.format("hold %s %s| ", modifiers, key);
            return;
        }
        new Region().keyDown(key + modifiers);
    }

}
