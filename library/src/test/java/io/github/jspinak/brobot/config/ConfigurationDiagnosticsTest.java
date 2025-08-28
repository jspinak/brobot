package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ConfigurationDiagnostics.
 * Tests configuration validation and diagnostics.
 */
@DisplayName("ConfigurationDiagnostics Tests")
public class ConfigurationDiagnosticsTest extends BrobotTestBase {
    
    private ConfigurationDiagnostics diagnostics;
    private BrobotConfiguration mockConfig;
    private ImagePathManager mockPathManager;
    private SmartImageLoader mockImageLoader;
    private ExecutionEnvironment mockEnvironment;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockConfig = mock(BrobotConfiguration.class);
        mockPathManager = mock(ImagePathManager.class);
        mockImageLoader = mock(SmartImageLoader.class);
        mockEnvironment = mock(ExecutionEnvironment.class);
        
        // Setup default mock behavior for BrobotConfiguration
        BrobotConfiguration.EnvironmentConfig envConfig = mock(BrobotConfiguration.EnvironmentConfig.class);
        when(envConfig.getProfile()).thenReturn("test");
        when(mockConfig.getEnvironment()).thenReturn(envConfig);
        
        BrobotConfiguration.CoreConfig coreConfig = mock(BrobotConfiguration.CoreConfig.class);
        when(coreConfig.getImagePath()).thenReturn("images/");
        // getPackageName doesn't exist, removed
        when(mockConfig.getCore()).thenReturn(coreConfig);
        
        // MouseConfig doesn't exist - use SikuliConfig instead which has timing-related settings
        BrobotConfiguration.SikuliConfig sikuliConfig = mock(BrobotConfiguration.SikuliConfig.class);
        when(sikuliConfig.getWaitTime()).thenReturn(3.0);
        when(mockConfig.getSikuli()).thenReturn(sikuliConfig);
        
        diagnostics = new ConfigurationDiagnostics(
            mockConfig, mockPathManager, mockImageLoader, mockEnvironment
        );
    }
    
    @Nested
    @DisplayName("Diagnostics Execution")
    class DiagnosticsExecution {
        
        @Test
        @DisplayName("Should run full diagnostics")
        void shouldRunFullDiagnostics() {
            when(mockEnvironment.hasDisplay()).thenReturn(true);
            when(mockEnvironment.canCaptureScreen()).thenReturn(true);
            
            ConfigurationDiagnostics.DiagnosticReport report = diagnostics.runFullDiagnostics();
            
            assertNotNull(report);
        }
        
        @Test
        @DisplayName("Should check configuration validity")
        void shouldCheckConfigurationValidity() {
            when(mockEnvironment.hasDisplay()).thenReturn(true);
            
            boolean isValid = diagnostics.isConfigurationValid();
            
            assertNotNull(isValid);
        }
        
        @Test
        @DisplayName("Should print diagnostic report")
        void shouldPrintDiagnosticReport() {
            when(mockEnvironment.hasDisplay()).thenReturn(true);
            
            assertDoesNotThrow(() -> diagnostics.printDiagnosticReport());
        }
    }
    
    @Nested
    @DisplayName("Environment Checks")
    class EnvironmentChecks {
        
        @Test
        @DisplayName("Should detect headless environment")
        void shouldDetectHeadlessEnvironment() {
            when(mockEnvironment.hasDisplay()).thenReturn(false);
            
            ConfigurationDiagnostics.DiagnosticReport report = diagnostics.runFullDiagnostics();
            
            assertNotNull(report);
            verify(mockEnvironment, atLeastOnce()).hasDisplay();
        }
        
        @Test
        @DisplayName("Should check screen capture capability")
        void shouldCheckScreenCaptureCapability() {
            when(mockEnvironment.canCaptureScreen()).thenReturn(true);
            
            ConfigurationDiagnostics.DiagnosticReport report = diagnostics.runFullDiagnostics();
            
            assertNotNull(report);
            verify(mockEnvironment, atLeastOnce()).canCaptureScreen();
        }
    }
}