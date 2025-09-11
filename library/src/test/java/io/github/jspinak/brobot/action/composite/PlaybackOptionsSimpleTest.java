package io.github.jspinak.brobot.action.composite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;

/** Simple test to verify PlaybackOptions works without compilation issues */
@DisplayName("PlaybackOptions Simple Test")
public class PlaybackOptionsSimpleTest extends BrobotTestBase {

    @Test
    @DisplayName("Should create and configure PlaybackOptions")
    public void testPlaybackOptionsCreation() {
        // Test default values
        PlaybackOptions defaultOptions = new PlaybackOptions.Builder().build();
        assertNotNull(defaultOptions);
        assertEquals(-1, defaultOptions.getStartPlayback());
        assertEquals(5.0, defaultOptions.getPlaybackDuration());

        // Test custom values
        PlaybackOptions customOptions =
                new PlaybackOptions.Builder()
                        .setStartPlayback(10.0)
                        .setPlaybackDuration(20.0)
                        .build();

        assertNotNull(customOptions);
        assertEquals(10.0, customOptions.getStartPlayback());
        assertEquals(20.0, customOptions.getPlaybackDuration());

        // Test copy constructor
        PlaybackOptions copiedOptions =
                new PlaybackOptions.Builder(customOptions).setStartPlayback(15.0).build();

        assertNotNull(copiedOptions);
        assertEquals(15.0, copiedOptions.getStartPlayback());
        assertEquals(20.0, copiedOptions.getPlaybackDuration());
    }
}
