package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import io.github.jspinak.brobot.startup.orchestration.StartupRunner;
import io.github.jspinak.brobot.startup.verification.InitialStateVerifier;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for BrobotStartupRunner.
 * Tests application startup state verification.
 */
@DisplayName("BrobotStartupRunner Tests")
public class BrobotStartupRunnerTest extends BrobotTestBase {
    
    @Mock
    private InitialStateVerifier mockStateVerifier;
    
    @Mock
    private InitialStateVerifier.VerificationBuilder mockBuilder;
    
    @Mock
    private StartupConfiguration mockConfiguration;
    
    @Mock
    private ApplicationArguments mockArguments;
    
    private StartupRunner runner;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        // Setup mock builder chain
        when(mockStateVerifier.builder()).thenReturn(mockBuilder);
        when(mockBuilder.withStates(any(String[].class))).thenReturn(mockBuilder);
        when(mockBuilder.withFallbackSearch(anyBoolean())).thenReturn(mockBuilder);
        when(mockBuilder.activateFirstOnly(anyBoolean())).thenReturn(mockBuilder);
        when(mockBuilder.verify()).thenReturn(true);
        
        runner = new StartupRunner(mockStateVerifier, mockConfiguration);
    }
    
    @Nested
    @DisplayName("Initial State Verification")
    class InitialStateVerification {
        
        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Should skip verification when no states configured")
        void shouldSkipVerificationWhenNoStatesConfigured() throws Exception {
            when(mockConfiguration.getInitialStates()).thenReturn(Collections.emptyList());
            
            runner.run(mockArguments);
            
            verify(mockStateVerifier, never()).builder();
        }
        
        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Should verify configured states")
        void shouldVerifyConfiguredStates() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1", "State2"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            when(mockConfiguration.isFallbackSearch()).thenReturn(true);
            when(mockConfiguration.isActivateFirstOnly()).thenReturn(false);
            
            runner.run(mockArguments);
            
            verify(mockStateVerifier).builder();
            verify(mockBuilder).withStates("State1", "State2");
            verify(mockBuilder).withFallbackSearch(true);
            verify(mockBuilder).activateFirstOnly(false);
            verify(mockBuilder).verify();
        }
        
        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle verification failure")
        void shouldHandleVerificationFailure() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("InvalidState"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            when(mockBuilder.verify()).thenReturn(false);
            
            // Should not throw exception even on failure
            assertDoesNotThrow(() -> runner.run(mockArguments));
            
            verify(mockBuilder).verify();
        }
    }
    
    @Nested
    @DisplayName("Startup Delay")
    class StartupDelay {
        
        @Test
        @DisplayName("Should apply startup delay")
        void shouldApplyStartupDelay() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1"));
            when(mockConfiguration.getStartupDelay()).thenReturn(1);
            
            long startTime = System.currentTimeMillis();
            runner.run(mockArguments);
            long endTime = System.currentTimeMillis();
            
            // Should have waited at least 1 second
            assertTrue((endTime - startTime) >= 1000);
        }
        
        @Test
        @DisplayName("Should skip delay when zero")
        void shouldSkipDelayWhenZero() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            
            long startTime = System.currentTimeMillis();
            runner.run(mockArguments);
            long endTime = System.currentTimeMillis();
            
            // Should complete quickly (less than 500ms)
            assertTrue((endTime - startTime) < 500);
        }
        
        @Test
        @DisplayName("Should handle negative delay")
        void shouldHandleNegativeDelay() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1"));
            when(mockConfiguration.getStartupDelay()).thenReturn(-5);
            
            // Should not throw and should not wait
            long startTime = System.currentTimeMillis();
            assertDoesNotThrow(() -> runner.run(mockArguments));
            long endTime = System.currentTimeMillis();
            
            assertTrue((endTime - startTime) < 500);
        }
    }
    
    @Nested
    @DisplayName("Configuration Options")
    class ConfigurationOptions {
        
        @Test
        @DisplayName("Should use fallback search when enabled")
        void shouldUseFallbackSearchWhenEnabled() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            when(mockConfiguration.isFallbackSearch()).thenReturn(true);
            
            runner.run(mockArguments);
            
            verify(mockBuilder).withFallbackSearch(true);
        }
        
        @Test
        @DisplayName("Should activate first only when enabled")
        void shouldActivateFirstOnlyWhenEnabled() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1", "State2"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            when(mockConfiguration.isActivateFirstOnly()).thenReturn(true);
            
            runner.run(mockArguments);
            
            verify(mockBuilder).activateFirstOnly(true);
        }
        
        @Test
        @DisplayName("Should pass multiple states to verifier")
        void shouldPassMultipleStatesToVerifier() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("Login", "Dashboard", "MainMenu"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            
            runner.run(mockArguments);
            
            verify(mockBuilder).withStates("Login", "Dashboard", "MainMenu");
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null configuration gracefully")
        void shouldHandleNullConfigurationGracefully() throws Exception {
            when(mockConfiguration.getInitialStates()).thenReturn(null);
            
            assertThrows(NullPointerException.class, () -> runner.run(mockArguments));
        }
        
        @Test
        @DisplayName("Should handle verifier exceptions")
        void shouldHandleVerifierExceptions() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1"));
            when(mockConfiguration.getStartupDelay()).thenReturn(0);
            when(mockBuilder.verify()).thenThrow(new RuntimeException("Verification error"));
            
            assertThrows(RuntimeException.class, () -> runner.run(mockArguments));
        }
        
        @Test
        @DisplayName("Should handle interrupted exception during delay")
        void shouldHandleInterruptedExceptionDuringDelay() throws Exception {
            when(mockConfiguration.getInitialStates())
                .thenReturn(Arrays.asList("State1"));
            when(mockConfiguration.getStartupDelay()).thenReturn(10);
            
            Thread testThread = new Thread(() -> {
                try {
                    runner.run(mockArguments);
                } catch (Exception e) {
                    // Expected
                }
            });
            
            testThread.start();
            Thread.sleep(100); // Let it start waiting
            testThread.interrupt();
            testThread.join(1000);
            
            assertFalse(testThread.isAlive());
        }
    }
}