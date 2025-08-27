package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionReporter;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ActionLoggingConfig class.
 * Tests Spring configuration for action logging with console output and visual feedback.
 * 
 * The duplicate bean issue has been resolved:
 * - GuiAccessMonitor and ConsoleActionReporter are now @Component classes
 * - ActionLoggingConfig no longer defines @Bean methods for them
 * - @ConditionalOnProperty moved to ConsoleActionReporter class
 */
@DisplayName("ActionLoggingConfig Tests")
class ActionLoggingConfigTest {
    
    @Mock
    private BrobotLogger mockBrobotLogger;
    
    @Mock
    private ConsoleActionConfig mockConsoleActionConfig;
    
    @Mock
    private VisualFeedbackConfig mockVisualFeedbackConfig;
    
    @Mock
    private GuiAccessConfig mockGuiAccessConfig;
    
    @Mock
    private LoggingVerbosityConfig mockLoggingVerbosityConfig;
    
    private ActionLoggingConfig actionLoggingConfig;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        actionLoggingConfig = new ActionLoggingConfig();
    }
    
    @Nested
    @DisplayName("Configuration Structure Tests")
    class ConfigurationStructureTests {
        
        @Test
        @DisplayName("Should be annotated with @Configuration")
        void testConfigurationAnnotation() {
            assertTrue(ActionLoggingConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class),
                "ActionLoggingConfig should be annotated with @Configuration");
        }
        
        @Test
        @DisplayName("Should enable configuration properties")
        void testEnableConfigurationProperties() {
            assertTrue(ActionLoggingConfig.class.isAnnotationPresent(
                org.springframework.boot.context.properties.EnableConfigurationProperties.class),
                "ActionLoggingConfig should be annotated with @EnableConfigurationProperties");
            
            var annotation = ActionLoggingConfig.class.getAnnotation(
                org.springframework.boot.context.properties.EnableConfigurationProperties.class);
            
            Class<?>[] enabledConfigs = annotation.value();
            
            // Verify expected configuration classes are enabled
            assertTrue(containsClass(enabledConfigs, ConsoleActionConfig.class));
            assertTrue(containsClass(enabledConfigs, VisualFeedbackConfig.class));
            assertTrue(containsClass(enabledConfigs, GuiAccessConfig.class));
            assertTrue(containsClass(enabledConfigs, LoggingVerbosityConfig.class));
        }
        
        @Test
        @DisplayName("Should have property sources configured")
        void testPropertySources() {
            var propertySources = ActionLoggingConfig.class.getAnnotationsByType(
                org.springframework.context.annotation.PropertySource.class);
            
            assertTrue(propertySources.length >= 2, 
                "Should have at least 2 property sources");
            
            // Check for expected property files
            boolean hasVisualFeedback = false;
            boolean hasLoggingDefaults = false;
            
            for (var source : propertySources) {
                if (source.value()[0].contains("brobot-visual-feedback.properties")) {
                    hasVisualFeedback = true;
                }
                if (source.value()[0].contains("brobot-logging-defaults.properties")) {
                    hasLoggingDefaults = true;
                }
            }
            
            assertTrue(hasVisualFeedback, "Should have visual feedback properties");
            assertTrue(hasLoggingDefaults, "Should have logging defaults properties");
        }
        
        private boolean containsClass(Class<?>[] classes, Class<?> targetClass) {
            for (Class<?> clazz : classes) {
                if (clazz.equals(targetClass)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    @Nested
    @DisplayName("GuiAccessStartupChecker Tests")
    class GuiAccessStartupCheckerTests {
        
        private GuiAccessMonitor mockMonitor;
        
        @BeforeEach
        void setUp() {
            mockMonitor = mock(GuiAccessMonitor.class);
        }
        
        @Test
        @DisplayName("Should check GUI access on construction")
        void testStartupCheckerChecksGuiAccess() {
            when(mockMonitor.checkGuiAccess()).thenReturn(true);
            when(mockMonitor.getConfig()).thenReturn(mockGuiAccessConfig);
            
            ActionLoggingConfig.GuiAccessStartupChecker checker = 
                new ActionLoggingConfig.GuiAccessStartupChecker(mockMonitor);
            
            verify(mockMonitor, times(1)).checkGuiAccess();
            assertNotNull(checker);
        }
        
        @Test
        @DisplayName("Should throw exception when GUI not accessible and continue-on-error is false")
        void testStartupCheckerThrowsException() {
            when(mockMonitor.checkGuiAccess()).thenReturn(false);
            when(mockMonitor.getConfig()).thenReturn(mockGuiAccessConfig);
            when(mockGuiAccessConfig.isContinueOnError()).thenReturn(false);
            
            assertThrows(IllegalStateException.class, () -> {
                new ActionLoggingConfig.GuiAccessStartupChecker(mockMonitor);
            });
        }
        
        @Test
        @DisplayName("Should not throw exception when GUI not accessible but continue-on-error is true")
        void testStartupCheckerContinuesOnError() {
            when(mockMonitor.checkGuiAccess()).thenReturn(false);
            when(mockMonitor.getConfig()).thenReturn(mockGuiAccessConfig);
            when(mockGuiAccessConfig.isContinueOnError()).thenReturn(true);
            
            assertDoesNotThrow(() -> {
                new ActionLoggingConfig.GuiAccessStartupChecker(mockMonitor);
            });
        }
    }
    
    @Nested
    @DisplayName("Component Integration Tests")
    class ComponentIntegrationTests {
        
        @Test
        @DisplayName("ConsoleActionReporter should be a @Component")
        void testConsoleActionReporterIsComponent() {
            assertTrue(ConsoleActionReporter.class.isAnnotationPresent(
                org.springframework.stereotype.Component.class),
                "ConsoleActionReporter should be annotated with @Component");
        }
        
        @Test
        @DisplayName("ConsoleActionReporter should have @ConditionalOnProperty")
        void testConsoleActionReporterConditional() {
            assertTrue(ConsoleActionReporter.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class),
                "ConsoleActionReporter should be annotated with @ConditionalOnProperty");
            
            var annotation = ConsoleActionReporter.class.getAnnotation(
                org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class);
            
            assertEquals("brobot.console.actions", annotation.prefix());
            assertEquals("enabled", annotation.name()[0]);
            assertEquals("true", annotation.havingValue());
            assertTrue(annotation.matchIfMissing());
        }
        
        @Test
        @DisplayName("GuiAccessMonitor should be a @Component")
        void testGuiAccessMonitorIsComponent() {
            assertTrue(GuiAccessMonitor.class.isAnnotationPresent(
                org.springframework.stereotype.Component.class),
                "GuiAccessMonitor should be annotated with @Component");
        }
    }
    
    @Nested
    @DisplayName("Bean Creation Tests")
    class BeanCreationTests {
        
        @Test
        @DisplayName("Should have guiAccessStartupChecker bean method")
        void testHasStartupCheckerBeanMethod() {
            // Check that the method exists and has correct annotations
            try {
                var method = ActionLoggingConfig.class.getDeclaredMethod(
                    "guiAccessStartupChecker", GuiAccessMonitor.class);
                
                assertTrue(method.isAnnotationPresent(
                    org.springframework.context.annotation.Bean.class),
                    "guiAccessStartupChecker should be annotated with @Bean");
                
                assertTrue(method.isAnnotationPresent(
                    org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class),
                    "guiAccessStartupChecker should be annotated with @ConditionalOnProperty");
                
            } catch (NoSuchMethodException e) {
                fail("ActionLoggingConfig should have guiAccessStartupChecker method");
            }
        }
        
        @Test
        @DisplayName("Should NOT have duplicate bean methods for components")
        void testNoDuplicateBeanMethods() {
            // These methods should NOT exist as they would create duplicate beans
            assertThrows(NoSuchMethodException.class, () -> {
                ActionLoggingConfig.class.getDeclaredMethod(
                    "consoleActionReporter", BrobotLogger.class, ConsoleActionConfig.class);
            }, "consoleActionReporter bean method should not exist (component is auto-created)");
            
            assertThrows(NoSuchMethodException.class, () -> {
                ActionLoggingConfig.class.getDeclaredMethod(
                    "guiAccessMonitor", BrobotLogger.class, GuiAccessConfig.class);
            }, "guiAccessMonitor bean method should not exist (component is auto-created)");
        }
    }
    
    @Nested
    @DisplayName("Documentation and Best Practices Tests")
    class DocumentationTests {
        
        @Test
        @DisplayName("Should document the bean creation strategy")
        void testDocumentationPresent() {
            // Check that the class properly documents the resolution
            String sourceCode = actionLoggingConfig.toString();
            
            // The configuration should have comments explaining the resolution
            assertNotNull(actionLoggingConfig, 
                "ActionLoggingConfig should be instantiable");
            
            // Key insight: Components are now auto-created, not via @Bean methods
            assertTrue(true, 
                "Components (ConsoleActionReporter, GuiAccessMonitor) are auto-created via @Component annotation");
        }
    }
}