package io.github.jspinak.brobot;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Basic Framework Test")
@Timeout(value = 10, unit = TimeUnit.SECONDS) // Prevent CI/CD timeout
@DisabledInCI
public class BasicFrameworkTest extends BrobotTestBase {
    
    @BeforeAll
    public static void earlySetup() {
        // Set test properties BEFORE parent class static initialization
        System.setProperty("brobot.test.mode", "true");
        System.setProperty("brobot.test.type", "unit");
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    @Override
    @Timeout(value = 5, unit = TimeUnit.SECONDS) // Limit setup time
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Framework should be in mock mode")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testMockMode() {
        assertTrue(FrameworkSettings.mock, "Framework should be in mock mode for tests");
    }

    @Test
    @DisplayName("Basic assertion test")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testBasicAssertion() {
        assertEquals(2 + 2, 4);
        assertTrue(true);
        assertFalse(false);
    }

    @Test
    @DisplayName("Test setup should work")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testSetup() {
        assertNotNull(this);
        assertTrue(this instanceof BrobotTestBase);
    }
}