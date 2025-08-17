package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleConfigDiagTest {
    
    @Test
    void testHeadlessEnvironment() {
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .forceHeadless(true)
            .allowScreenCapture(true)
            .build();
            
        assertFalse(env.hasDisplay(), "forceHeadless=true should mean hasDisplay()=false");
        assertFalse(env.canCaptureScreen(), "forceHeadless=true means no display, so canCaptureScreen()=false even with allowScreenCapture=true");
        
        BrobotConfiguration config = new BrobotConfiguration();
        config.getCore().setAllowScreenCapture(true);
        config.getCore().setForceHeadless(true);
        
        assertTrue(config.getCore().isAllowScreenCapture(), "Config should have allowScreenCapture=true");
        
        // Test the actual condition
        boolean shouldDetectIssue = !env.hasDisplay() && config.getCore().isAllowScreenCapture();
        assertTrue(shouldDetectIssue, "Should detect issue when !hasDisplay && allowScreenCapture");
    }
}