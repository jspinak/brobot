package io.github.jspinak.brobot;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Basic Framework Test")
public class BasicFrameworkTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Test
    @DisplayName("Framework should be in mock mode")
    public void testMockMode() {
        assertTrue(FrameworkSettings.mock, "Framework should be in mock mode for tests");
    }
    
    @Test
    @DisplayName("Basic assertion test")
    public void testBasicAssertion() {
        assertEquals(2 + 2, 4);
        assertTrue(true);
        assertFalse(false);
    }
    
    @Test
    @DisplayName("Test setup should work")
    public void testSetup() {
        assertNotNull(this);
        assertTrue(this instanceof BrobotTestBase);
    }
}