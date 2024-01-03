package io.github.jspinak.brobot.actions.methods.time;

import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class Time {
    private final MockOrLive mockOrLive;

    public Time(MockOrLive mockOrLive) {
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
        //change(thingsYouWishYouCouldChange); // seems difficult, maybe replace with an 'accept' method
    }

}
