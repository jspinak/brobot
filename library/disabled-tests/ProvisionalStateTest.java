package io.github.jspinak.brobot.model.analysis.state;

import io.github.jspinak.brobot.model.analysis.state.discovery.ProvisionalState;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ProvisionalState discovery.
 */
@DisplayName("ProvisionalState Tests")
public class ProvisionalStateTest extends BrobotTestBase {
    
    private ProvisionalState provisionalState;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        provisionalState = new ProvisionalState();
    }
    
    @Nested
    @DisplayName("State Discovery")
    class StateDiscovery {
        
        @Test
        @DisplayName("Should create provisional state")
        void shouldCreateProvisionalState() {
            assertNotNull(provisionalState);
        }
        
        @Test
        @DisplayName("Should identify state patterns")
        void shouldIdentifyStatePatterns() {
            // Test state pattern identification
            assertDoesNotThrow(() -> provisionalState.identifyPatterns());
        }
        
        @Test
        @DisplayName("Should calculate confidence score")
        void shouldCalculateConfidenceScore() {
            double confidence = provisionalState.getConfidence();
            assertTrue(confidence >= 0.0 && confidence <= 1.0);
        }
    }
}