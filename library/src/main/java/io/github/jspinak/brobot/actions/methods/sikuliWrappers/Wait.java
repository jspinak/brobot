package io.github.jspinak.brobot.actions.methods.sikuliWrappers;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.time.TimeWrapper;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for Wait, works with real or mocked actions.
 * Pauses for a given number of seconds.
 */
@Component
public class Wait {

    private TimeWrapper timeWrapper;

    public Wait(TimeWrapper timeWrapper) {
        this.timeWrapper = timeWrapper;
    }

    public void wait(double seconds) {
        if (BrobotSettings.mock) {
            if (Report.minReportingLevel(Report.OutputLevel.HIGH) && seconds > 0) {
                System.out.format("%s-%.1f ", "wait", seconds);
                timeWrapper.wait(seconds);
            }
            return;
        }
        new Region().wait(seconds);
    }

}