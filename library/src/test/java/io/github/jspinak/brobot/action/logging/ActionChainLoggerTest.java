package io.github.jspinak.brobot.action.logging;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ActionChainLogger.
 */
@DisplayName("ActionChainLogger Tests")
public class ActionChainLoggerTest extends BrobotTestBase {
    
    private ActionChainLogger logger;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        logger = new ActionChainLogger();
    }
    
    @Test
    @DisplayName("Should create logger")
    void shouldCreateLogger() {
        assertNotNull(logger);
    }
    
    @Test
    @DisplayName("Should log action chain")
    void shouldLogActionChain() {
        // Verify logger can handle action chains
        assertDoesNotThrow(() -> logger.logChain("Test chain"));
    }
}