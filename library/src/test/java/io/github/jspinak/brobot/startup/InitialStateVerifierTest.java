package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for InitialStateVerifier.
 * Verifies initial state detection and verification functionality.
 */
@DisplayName("InitialStateVerifier Tests")
public class InitialStateVerifierTest extends BrobotTestBase {
    
    @Mock
    private StateDetector stateDetector;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private StateService stateService;
    
    private InitialStateVerifier verifier;
    
    // Test state enums
    enum TestState implements StateEnum {
        HOME, LOGIN, DASHBOARD;
        
        public String getName() {
            return name();
        }
    }
    
    private State createTestState(String name, Long id) {
        State state = mock(State.class);
        when(state.getName()).thenReturn(name);
        when(state.getId()).thenReturn(id);
        // setProbabilityToBaseProbability is void, no need to mock it
        return state;
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        verifier = new InitialStateVerifier(stateDetector, stateMemory, stateService);
    }
    
    @Nested
    @DisplayName("Active State Verification")
    class ActiveStateVerification {
        
        @Test
        @DisplayName("Verify active states delegates to StateDetector")
        public void testVerifyActiveStates() {
            // Setup
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            when(stateMemory.getActiveStates()).thenReturn(activeStates);
            
            // Execute
            Set<Long> result = verifier.verifyActiveStates();
            
            // Verify
            verify(stateDetector).checkForActiveStates();
            verify(stateMemory).getActiveStates();
            assertEquals(activeStates, result);
        }
        
        @Test
        @DisplayName("Rebuild active states delegates to StateDetector")
        public void testRebuildActiveStates() {
            // Setup
            Set<Long> rebuiltStates = new HashSet<>(Arrays.asList(4L, 5L));
            when(stateMemory.getActiveStates()).thenReturn(rebuiltStates);
            
            // Execute
            Set<Long> result = verifier.rebuildActiveStates();
            
            // Verify
            verify(stateDetector).rebuildActiveStates();
            verify(stateMemory).getActiveStates();
            assertEquals(rebuiltStates, result);
        }
        
        @Test
        @DisplayName("Refresh active states returns detected states")
        public void testRefreshActiveStates() {
            // Setup
            Set<Long> refreshedStates = new HashSet<>(Arrays.asList(10L, 11L));
            when(stateDetector.refreshActiveStates()).thenReturn(refreshedStates);
            
            // Execute
            Set<Long> result = verifier.refreshActiveStates();
            
            // Verify
            verify(stateDetector).refreshActiveStates();
            assertEquals(refreshedStates, result);
        }
    }
    
    @Nested
    @DisplayName("State Verification with StateEnums")
    class StateEnumVerification {
        
        @Test
        @DisplayName("Verify with single state enum")
        public void testVerifySingleState() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateDetector.findState(1L)).thenReturn(true);
            
            // Execute
            boolean result = verifier.verify(TestState.HOME);
            
            // Verify
            assertTrue(result);
            verify(stateService).getState("HOME");
            verify(stateDetector).findState(1L);
            verify(stateMemory).addActiveState(1L);
        }
        
        @Test
        @DisplayName("Verify with multiple state enums")
        public void testVerifyMultipleStates() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            State loginState = createTestState("LOGIN", 2L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState("LOGIN")).thenReturn(Optional.of(loginState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateService.getState(2L)).thenReturn(Optional.of(loginState));
            when(stateDetector.findState(1L)).thenReturn(false);
            when(stateDetector.findState(2L)).thenReturn(true);
            
            // Execute
            boolean result = verifier.verify(TestState.HOME, TestState.LOGIN);
            
            // Verify
            assertTrue(result);
            verify(stateDetector).findState(1L);
            verify(stateDetector).findState(2L);
            verify(stateMemory).addActiveState(2L);
        }
        
        @Test
        @DisplayName("Verify returns false when no states found")
        public void testVerifyNoStatesFound() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateDetector.findState(1L)).thenReturn(false);
            
            // Execute
            boolean result = verifier.verify(TestState.HOME);
            
            // Verify
            assertFalse(result);
            verify(stateDetector).findState(1L);
            verify(stateMemory, never()).addActiveState(anyLong());
        }
        
        @Test
        @DisplayName("Handle unknown state enum")
        public void testUnknownStateEnum() {
            // Setup
            when(stateService.getState("UNKNOWN")).thenReturn(Optional.empty());
            
            // Execute
            boolean result = verifier.verify(new StateEnum() { public String getName() { return "UNKNOWN"; }});
            
            // Verify
            assertFalse(result);
            verify(stateService).getState("UNKNOWN");
            verify(stateDetector, never()).findState(anyLong());
        }
    }
    
    @Nested
    @DisplayName("Mock Mode Verification")
    class MockModeVerification {
        
        @BeforeEach
        public void setupMockMode() {
            FrameworkSettings.mock = true;
        }
        
        @Test
        @DisplayName("Mock mode uses first state with no probabilities")
        public void testMockModeDefaultBehavior() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            
            // Execute
            boolean result = verifier.verify(TestState.HOME, TestState.LOGIN);
            
            // Verify
            assertTrue(result);
            verify(stateMemory).addActiveState(eq(1L), anyBoolean());
            // In mock mode, doesn't check with stateDetector
            verify(stateDetector, never()).findState(anyLong());
        }
        
        @Test
        @DisplayName("Mock mode handles empty state list")
        public void testMockModeEmptyList() {
            // Execute
            boolean result = verifier.verify(new StateEnum[0]);
            
            // Verify
            assertFalse(result);
            verify(stateMemory, never()).addActiveState(anyLong());
        }
    }
    
    @Nested
    @DisplayName("Verification Builder")
    class VerificationBuilderTests {
        
        @Test
        @DisplayName("Builder with states verifies successfully")
        public void testBuilderWithStates() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateDetector.findState(1L)).thenReturn(true);
            
            // Execute
            InitialStateVerifier.VerificationBuilder builder = verifier.builder()
                .withStates(TestState.HOME);
            boolean result = builder.verify();
            
            // Verify
            assertTrue(result);
            verify(stateDetector).findState(1L);
        }
        
        @Test
        @DisplayName("Builder with fallback search")
        public void testBuilderWithFallback() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            State state2 = createTestState("STATE2", 2L);
            State state3 = createTestState("STATE3", 3L);
            
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            when(stateService.getAllStateIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
            
            when(stateDetector.findState(1L)).thenReturn(false);
            when(stateDetector.findState(2L)).thenReturn(true);
            when(stateDetector.findState(3L)).thenReturn(true);
            
            // Execute
            InitialStateVerifier.VerificationBuilder builder = verifier.builder()
                .withStates(TestState.HOME)
                .withFallbackSearch(true);
            boolean result = builder.verify();
            
            // Verify
            assertTrue(result);
            verify(stateDetector).findState(1L);
            verify(stateDetector).findState(2L);
            verify(stateMemory).addActiveState(2L);
        }
        
        @Test
        @DisplayName("Builder with probabilities in mock mode")
        public void testBuilderWithProbabilities() {
            // Setup
            FrameworkSettings.mock = true;
            State homeState = createTestState("HOME", 1L);
            State loginState = createTestState("LOGIN", 2L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState("LOGIN")).thenReturn(Optional.of(loginState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateService.getState(2L)).thenReturn(Optional.of(loginState));
            
            // Execute
            InitialStateVerifier.VerificationBuilder builder = verifier.builder()
                .withState(TestState.HOME, 70)
                .withState(TestState.LOGIN, 30);
            
            boolean result = builder.verify();
            
            // Verify - in mock mode it should activate based on probabilities
            assertTrue(result);
            // Should activate at least one state
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong(), anyBoolean());
        }
        
        @Test
        @DisplayName("Builder with basic configuration")
        public void testBuilderBasicConfiguration() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateDetector.findState(1L)).thenReturn(true);
            
            // Execute
            InitialStateVerifier.VerificationBuilder builder = verifier.builder()
                .withStates(TestState.HOME)
                .activateFirstOnly(true);
            boolean result = builder.verify();
            
            // Verify
            assertTrue(result);
            verify(stateDetector).findState(1L);
            verify(stateMemory).addActiveState(1L);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Handle null state enum")
        public void testNullStateEnum() {
            // Execute
            boolean result = verifier.verify((StateEnum) null);
            
            // Verify
            assertFalse(result);
            verify(stateService, never()).getState(anyString());
        }
        
        @Test
        @DisplayName("Handle mixed null and valid states")
        public void testMixedNullAndValidStates() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateDetector.findState(1L)).thenReturn(true);
            
            // Execute
            boolean result = verifier.verify(null, TestState.HOME, null);
            
            // Verify
            assertTrue(result);
            verify(stateService).getState("HOME");
            verify(stateDetector).findState(1L);
        }
        
        @Test
        @DisplayName("Verify with duplicate states")
        public void testDuplicateStates() {
            // Setup
            State homeState = createTestState("HOME", 1L);
            when(stateService.getState("HOME")).thenReturn(Optional.of(homeState));
            when(stateService.getState(1L)).thenReturn(Optional.of(homeState));
            when(stateDetector.findState(1L)).thenReturn(true);
            
            // Execute
            boolean result = verifier.verify(TestState.HOME, TestState.HOME);
            
            // Verify
            assertTrue(result);
            // Should check multiple times due to the duplicate
            verify(stateDetector, atLeastOnce()).findState(1L);
            verify(stateMemory, atLeastOnce()).addActiveState(1L);
        }
        
        @Test
        @DisplayName("Concurrent verification attempts")
        public void testConcurrentVerification() throws InterruptedException {
            // Setup
            State testState = createTestState("TEST", 1L);
            when(stateService.getState(anyString())).thenReturn(Optional.of(testState));
            when(stateService.getState(1L)).thenReturn(Optional.of(testState));
            when(stateDetector.findState(1L)).thenReturn(true);
            
            // Execute multiple verifications in parallel
            Thread t1 = new Thread(() -> verifier.verify(TestState.HOME));
            Thread t2 = new Thread(() -> verifier.verify(TestState.LOGIN));
            
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            
            // Verify - should handle concurrent access
            verify(stateDetector, atLeastOnce()).findState(anyLong());
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong());
        }
    }
}