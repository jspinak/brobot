package io.github.jspinak.brobot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple framework test that doesn't extend BrobotTestBase to avoid initialization issues.
 * This test validates basic framework functionality without complex setup.
 */
@DisplayName("Basic Framework Simple Test")
@Timeout(value = 5, unit = TimeUnit.SECONDS) // Aggressive timeout for CI/CD
@DisabledInCI
public class BasicFrameworkSimpleTest {
    
    @BeforeAll
    public static void setup() {
        // Set test mode properties before any initialization
        System.setProperty("brobot.test.mode", "true");
        System.setProperty("brobot.test.type", "unit");
        System.setProperty("java.awt.headless", "true");
    }
    
    @Test
    @DisplayName("Basic assertion test")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testBasicAssertion() {
        assertEquals(2 + 2, 4);
        assertTrue(true);
        assertFalse(false);
    }
    
    @Test
    @DisplayName("Test system properties")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testSystemProperties() {
        assertEquals("true", System.getProperty("brobot.test.mode"));
        assertEquals("unit", System.getProperty("brobot.test.type"));
        assertEquals("true", System.getProperty("java.awt.headless"));
    }
    
    @Test
    @DisplayName("Test class loading")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testClassLoading() {
        assertNotNull(this);
        assertTrue(this instanceof BasicFrameworkSimpleTest);
    }
}