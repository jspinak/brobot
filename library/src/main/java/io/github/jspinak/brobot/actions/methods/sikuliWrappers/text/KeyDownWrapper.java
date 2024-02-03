package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * KeyDown in SikuliX (Java Robot) does not actually hold the key down. There is
 * apparently no way to do this with code. Here, 'hold' means press down once, without releasing,
 * so that other keys can be pressed at the same time. KeyDown and wait on char 'a' won't produce
 * 'aaaaaaaaa'. It requires KeyUp before it functions again.
 *
 * Wrapper class for KeyDown, holds a Key down as a real or mock action.
 */
@Component
public class KeyDownWrapper {

    public void press(String key, String modifiers) {
        if (BrobotSettings.mock) {
            Report.format("hold %s %s| ", modifiers, key);
            return;
        }
        Report.print(key+" ");
        new Region().sikuli().keyDown(key + modifiers);
    }

}
