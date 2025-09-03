package io.github.jspinak.brobot;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.sikuli.basics.Settings;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationTest extends BrobotTestBase {
    
    @Test
    public void testEarlyInitialization() {
        System.out.println("=== Testing Configuration ===");
        
        // Check ImagePath is set
        String bundlePath = ImagePath.getBundlePath();
        System.out.println("Bundle path: " + bundlePath);
        assertNotNull(bundlePath, "Bundle path should be set");
        
        // Check DPI settings
        System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
        assertEquals(1.0f, Settings.AlwaysResize, 0.01f, "AlwaysResize should be 1.0 (no scaling)");
        
        System.out.println("=== Configuration Test Passed ===");
    }
}