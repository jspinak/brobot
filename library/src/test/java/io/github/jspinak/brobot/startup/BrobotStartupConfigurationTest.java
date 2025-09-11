package io.github.jspinak.brobot.startup;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Test suite for BrobotStartupConfiguration. Tests configuration properties for startup behavior.
 */
@DisplayName("BrobotStartupConfiguration Tests")
public class BrobotStartupConfigurationTest extends BrobotTestBase {

    private StartupConfiguration configuration;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        configuration = new StartupConfiguration();
    }

    @Nested
    @DisplayName("Default Configuration")
    class DefaultConfiguration {

        @Test
        @DisplayName("Should have default values")
        void shouldHaveDefaultValues() {
            assertFalse(configuration.isVerifyInitialStates());
            assertNotNull(configuration.getInitialStates());
            assertTrue(configuration.getInitialStates().isEmpty());
            assertFalse(configuration.isFallbackSearch());
            assertFalse(configuration.isActivateFirstOnly());
            assertEquals(0, configuration.getStartupDelay());
        }

        @Test
        @DisplayName("Initial states list should be mutable")
        void initialStatesListShouldBeMutable() {
            configuration.getInitialStates().add("State1");
            configuration.getInitialStates().add("State2");

            assertEquals(2, configuration.getInitialStates().size());
            assertTrue(configuration.getInitialStates().contains("State1"));
            assertTrue(configuration.getInitialStates().contains("State2"));
        }
    }

    @Nested
    @DisplayName("Property Setters")
    class PropertySetters {

        @Test
        @DisplayName("Should set verify initial states")
        void shouldSetVerifyInitialStates() {
            configuration.setVerifyInitialStates(true);
            assertTrue(configuration.isVerifyInitialStates());

            configuration.setVerifyInitialStates(false);
            assertFalse(configuration.isVerifyInitialStates());
        }

        @Test
        @DisplayName("Should set initial states list")
        void shouldSetInitialStatesList() {
            List<String> states = Arrays.asList("Login", "Dashboard", "MainMenu");
            configuration.setInitialStates(states);

            assertEquals(states, configuration.getInitialStates());
            assertEquals(3, configuration.getInitialStates().size());
        }

        @Test
        @DisplayName("Should set fallback search")
        void shouldSetFallbackSearch() {
            configuration.setFallbackSearch(true);
            assertTrue(configuration.isFallbackSearch());

            configuration.setFallbackSearch(false);
            assertFalse(configuration.isFallbackSearch());
        }

        @Test
        @DisplayName("Should set activate first only")
        void shouldSetActivateFirstOnly() {
            configuration.setActivateFirstOnly(true);
            assertTrue(configuration.isActivateFirstOnly());

            configuration.setActivateFirstOnly(false);
            assertFalse(configuration.isActivateFirstOnly());
        }

        @ParameterizedTest
        @DisplayName("Should set startup delay")
        @ValueSource(ints = {0, 1, 5, 10, 60})
        void shouldSetStartupDelay(int delay) {
            configuration.setStartupDelay(delay);
            assertEquals(delay, configuration.getStartupDelay());
        }
    }

    @Nested
    @DisplayName("Configuration Scenarios")
    class ConfigurationScenarios {

        @Test
        @DisplayName("Should configure for simple startup")
        void shouldConfigureForSimpleStartup() {
            configuration.setVerifyInitialStates(true);
            configuration.setInitialStates(Arrays.asList("HOME"));
            configuration.setActivateFirstOnly(true);

            assertTrue(configuration.isVerifyInitialStates());
            assertEquals(1, configuration.getInitialStates().size());
            assertTrue(configuration.isActivateFirstOnly());
            assertFalse(configuration.isFallbackSearch());
        }

        @Test
        @DisplayName("Should configure for complex startup")
        void shouldConfigureForComplexStartup() {
            configuration.setVerifyInitialStates(true);
            configuration.setInitialStates(Arrays.asList("Login", "Dashboard", "Settings"));
            configuration.setFallbackSearch(true);
            configuration.setActivateFirstOnly(false);
            configuration.setStartupDelay(5);

            assertTrue(configuration.isVerifyInitialStates());
            assertEquals(3, configuration.getInitialStates().size());
            assertTrue(configuration.isFallbackSearch());
            assertFalse(configuration.isActivateFirstOnly());
            assertEquals(5, configuration.getStartupDelay());
        }

        @Test
        @DisplayName("Should configure for delayed startup")
        void shouldConfigureForDelayedStartup() {
            configuration.setVerifyInitialStates(true);
            configuration.setStartupDelay(30);
            configuration.setInitialStates(Arrays.asList("ApplicationReady"));

            assertTrue(configuration.isVerifyInitialStates());
            assertEquals(30, configuration.getStartupDelay());
            assertEquals("ApplicationReady", configuration.getInitialStates().get(0));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty initial states")
        void shouldHandleEmptyInitialStates() {
            configuration.setVerifyInitialStates(true);
            configuration.getInitialStates().clear();

            assertTrue(configuration.isVerifyInitialStates());
            assertTrue(configuration.getInitialStates().isEmpty());
        }

        @Test
        @DisplayName("Should handle null initial states list")
        void shouldHandleNullInitialStatesList() {
            configuration.setInitialStates(null);
            assertNull(configuration.getInitialStates());
        }

        @Test
        @DisplayName("Should handle negative startup delay")
        void shouldHandleNegativeStartupDelay() {
            configuration.setStartupDelay(-5);
            assertEquals(-5, configuration.getStartupDelay());
        }

        @Test
        @DisplayName("Should handle duplicate states")
        void shouldHandleDuplicateStates() {
            configuration.setInitialStates(Arrays.asList("State1", "State1", "State2"));

            assertEquals(3, configuration.getInitialStates().size());
            // Duplicates are allowed in the list
            assertEquals("State1", configuration.getInitialStates().get(0));
            assertEquals("State1", configuration.getInitialStates().get(1));
            assertEquals("State2", configuration.getInitialStates().get(2));
        }
    }

    @Nested
    @DisplayName("Property Combinations")
    class PropertyCombinations {

        @Test
        @DisplayName("Fallback search with activate first only")
        void fallbackSearchWithActivateFirstOnly() {
            configuration.setFallbackSearch(true);
            configuration.setActivateFirstOnly(true);

            // Both can be true - will search all states but only activate first found
            assertTrue(configuration.isFallbackSearch());
            assertTrue(configuration.isActivateFirstOnly());
        }

        @Test
        @DisplayName("Verification disabled with states configured")
        void verificationDisabledWithStatesConfigured() {
            configuration.setVerifyInitialStates(false);
            configuration.setInitialStates(Arrays.asList("State1", "State2"));

            // States can be configured even if verification is disabled
            assertFalse(configuration.isVerifyInitialStates());
            assertEquals(2, configuration.getInitialStates().size());
        }

        @Test
        @DisplayName("All options enabled")
        void allOptionsEnabled() {
            configuration.setVerifyInitialStates(true);
            configuration.setInitialStates(Arrays.asList("A", "B", "C"));
            configuration.setFallbackSearch(true);
            configuration.setActivateFirstOnly(true);
            configuration.setStartupDelay(10);

            assertTrue(configuration.isVerifyInitialStates());
            assertEquals(3, configuration.getInitialStates().size());
            assertTrue(configuration.isFallbackSearch());
            assertTrue(configuration.isActivateFirstOnly());
            assertEquals(10, configuration.getStartupDelay());
        }
    }
}
