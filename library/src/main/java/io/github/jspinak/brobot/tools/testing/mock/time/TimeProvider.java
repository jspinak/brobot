package io.github.jspinak.brobot.tools.testing.mock.time;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;

import java.time.LocalDateTime;

@Component
public class TimeProvider {
    private final ExecutionModeController mockOrLive;

    public TimeProvider(ExecutionModeController mockOrLive) {
        this.mockOrLive = mockOrLive;
    }

    public LocalDateTime now() {
        return mockOrLive.now();
    }

    public void wait(double seconds) {
        mockOrLive.wait(seconds);
    }

    public void goBackInTime(double years, Object thingsYouWishYouCouldChange) {
        LocalDateTime now = mockOrLive.now().minusYears((long)years);
    }

}
