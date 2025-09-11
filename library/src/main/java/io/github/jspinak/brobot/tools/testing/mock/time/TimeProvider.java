package io.github.jspinak.brobot.tools.testing.mock.time;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * TimeProvider delegates to TimeWrapper for all time operations. This class acts as a compatibility
 * layer for existing code that uses TimeProvider. New code should directly use TimeWrapper instead.
 *
 * @deprecated Use {@link TimeWrapper} directly
 */
@Deprecated
@Component
public class TimeProvider {

    @Autowired private TimeWrapper timeWrapper;

    public LocalDateTime now() {
        return timeWrapper.now();
    }

    public void wait(double seconds) {
        timeWrapper.wait(seconds);
    }

    public void goBackInTime(double years, Object thingsYouWishYouCouldChange) {
        // This is a fun method that doesn't actually do anything
        LocalDateTime past = now().minusYears((long) years);
        // If only we could actually go back in time...
    }
}
