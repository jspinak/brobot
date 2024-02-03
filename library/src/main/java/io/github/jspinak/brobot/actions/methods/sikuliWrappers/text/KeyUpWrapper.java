package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for KeyUp, works for real or mocked operations.
 * Releases a Key (or all Keys).
 */
@Component
public class KeyUpWrapper {

    /**
     * When no parameters, it releases all keys
     */
    public void release() {
        Report.format("release all keys| ");
        if (BrobotSettings.mock) return;
        new Region().sikuli().keyUp();
    }

    public void release(String key) {
        Report.format("release %s| ", key);
        if (BrobotSettings.mock) return;
        new Region().sikuli().keyUp(key);
    }

    /**
     * @param key when 'int', it is for special keys such as CTRL
     */
    public void release(int key) {
        if (BrobotSettings.mock) {
            Report.format("release %d| ", key);
            return;
        }
        new Region().sikuli().keyUp(key);
    }
}
