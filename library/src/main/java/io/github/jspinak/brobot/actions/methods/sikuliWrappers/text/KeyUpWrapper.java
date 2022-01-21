package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.primitives.region.Region;
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
        if (BrobotSettings.mock) {
            Report.format("release all keys| ");
            return;
        }
        new Region().keyUp();
    }

    public void release(String key) {
        if (BrobotSettings.mock) {
            Report.format("release %s| ", key);
            return;
        }
        new Region().keyUp(key);
    }

    /**
     * @param key when 'int', it is for special keys such as CTRL
     */
    public void release(int key) {
        if (BrobotSettings.mock) {
            Report.format("release %d| ", key);
            return;
        }
        new Region().keyUp(key);
    }
}