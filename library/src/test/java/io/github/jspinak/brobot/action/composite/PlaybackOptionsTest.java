package io.github.jspinak.brobot.action.composite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackOptionsTest {
    
    private PlaybackOptions.Builder builder;
    
    @BeforeEach
    void setUp() {
        builder = new PlaybackOptions.Builder();
    }
    
    @Test
    void testDefaultValues() {
        // Execute
        PlaybackOptions options = builder.build();
        
        // Verify
        assertEquals(-1, options.getStartPlayback());
        assertEquals(5.0, options.getPlaybackDuration());
    }
    
    @Test
    void testSetStartPlayback() {
        // Execute
        PlaybackOptions options = builder
            .setStartPlayback(10.5)
            .build();
        
        // Verify
        assertEquals(10.5, options.getStartPlayback());
    }
    
    @Test
    void testSetPlaybackDuration() {
        // Execute
        PlaybackOptions options = builder
            .setPlaybackDuration(30.0)
            .build();
        
        // Verify
        assertEquals(30.0, options.getPlaybackDuration());
    }
    
    @Test
    void testBuilderChaining() {
        // Execute
        PlaybackOptions options = builder
            .setStartPlayback(5.0)
            .setPlaybackDuration(20.0)
            .build();
        
        // Verify
        assertEquals(5.0, options.getStartPlayback());
        assertEquals(20.0, options.getPlaybackDuration());
    }
    
    @Test
    void testCopyConstructor() {
        // Setup
        PlaybackOptions original = new PlaybackOptions.Builder()
            .setStartPlayback(15.0)
            .setPlaybackDuration(25.0)
            .build();
        
        // Execute
        PlaybackOptions copy = new PlaybackOptions.Builder(original).build();
        
        // Verify
        assertEquals(original.getStartPlayback(), copy.getStartPlayback());
        assertEquals(original.getPlaybackDuration(), copy.getPlaybackDuration());
        assertNotSame(original, copy); // Should be different objects
    }
    
    @Test
    void testModifyCopy() {
        // Setup
        PlaybackOptions original = new PlaybackOptions.Builder()
            .setStartPlayback(10.0)
            .setPlaybackDuration(15.0)
            .build();
        
        // Execute
        PlaybackOptions modified = new PlaybackOptions.Builder(original)
            .setStartPlayback(20.0)
            .build();
        
        // Verify
        assertEquals(10.0, original.getStartPlayback());
        assertEquals(20.0, modified.getStartPlayback());
        assertEquals(15.0, modified.getPlaybackDuration()); // Duration should be copied
    }
    
    @Test
    void testZeroValues() {
        // Execute
        PlaybackOptions options = builder
            .setStartPlayback(0.0)
            .setPlaybackDuration(0.0)
            .build();
        
        // Verify
        assertEquals(0.0, options.getStartPlayback());
        assertEquals(0.0, options.getPlaybackDuration());
    }
    
    @Test
    void testNegativeValues() {
        // Execute
        PlaybackOptions options = builder
            .setStartPlayback(-10.0)
            .setPlaybackDuration(-5.0)
            .build();
        
        // Verify
        assertEquals(-10.0, options.getStartPlayback());
        assertEquals(-5.0, options.getPlaybackDuration());
    }
    
    @Test
    void testLargeValues() {
        // Execute
        PlaybackOptions options = builder
            .setStartPlayback(Double.MAX_VALUE)
            .setPlaybackDuration(Double.MAX_VALUE)
            .build();
        
        // Verify
        assertEquals(Double.MAX_VALUE, options.getStartPlayback());
        assertEquals(Double.MAX_VALUE, options.getPlaybackDuration());
    }
    
    @Test
    void testDynamicStartPlayback() {
        // Test the special -1 value for dynamic start
        PlaybackOptions options = builder
            .setStartPlayback(-1)
            .setPlaybackDuration(10.0)
            .build();
        
        // Verify
        assertEquals(-1, options.getStartPlayback());
        assertEquals(10.0, options.getPlaybackDuration());
    }
    
    @Test
    void testInheritanceFromActionConfig() {
        // Verify that PlaybackOptions extends ActionConfig
        PlaybackOptions options = builder.build();
        assertTrue(options instanceof io.github.jspinak.brobot.action.ActionConfig);
    }
    
    @Test
    void testBuilderSelfMethod() {
        // Test that the self() method returns the correct type
        PlaybackOptions.Builder returnedBuilder = builder.setStartPlayback(5.0);
        assertSame(builder, returnedBuilder);
    }
    
    @Test
    void testImmutability() {
        // Create an options object
        PlaybackOptions options = builder
            .setStartPlayback(5.0)
            .setPlaybackDuration(10.0)
            .build();
        
        // Modify the builder after building
        builder.setStartPlayback(15.0);
        builder.setPlaybackDuration(20.0);
        
        // Verify the options object wasn't affected
        assertEquals(5.0, options.getStartPlayback());
        assertEquals(10.0, options.getPlaybackDuration());
    }
}