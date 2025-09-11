package io.github.jspinak.brobot.model.analysis.state;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.model.analysis.state.discovery.ProvisionalState;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Test suite for ProvisionalState discovery. */
@DisplayName("ProvisionalState Tests")
public class ProvisionalStateTest extends BrobotTestBase {

    private ProvisionalState provisionalState;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        provisionalState = new ProvisionalState("TestState");
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
        @DisplayName("Should check if scene is contained")
        void shouldCheckSceneContainment() {
            // Test scene containment
            assertFalse(provisionalState.contains(1));
        }

        @Test
        @DisplayName("Should check equal scene sets")
        void shouldCheckEqualSceneSets() {
            Set<Integer> scenes = new HashSet<>();
            scenes.add(1);
            assertFalse(provisionalState.hasEqualSceneSets(scenes));
        }
    }
}
