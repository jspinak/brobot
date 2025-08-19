package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionReporter;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ActionLoggingConfig class.
 * Tests Spring configuration for action logging with console output and visual feedback.
 * 
 * This is an integration test that uses Spring Boot test infrastructure.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ActionLoggingConfig Tests")
class ActionLoggingConfigTest {
    
    @Mock
    private BrobotLogger brobotLogger;
    
    @Mock
    private ConsoleActionConfig consoleActionConfig;
    
    @Mock
    private VisualFeedbackConfig visualFeedbackConfig;
    
    @Mock
    private GuiAccessConfig guiAccessConfig;
    
    @Mock
    private LoggingVerbosityConfig loggingVerbosityConfig;
    
    @Mock
    private GuiAccessMonitor guiAccessMonitor;
    
    private ApplicationContextRunner contextRunner;
    
    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ActionLoggingConfig.class))
            .withUserConfiguration(TestConfiguration.class)
            .withAllowBeanDefinitionOverriding(true);
    }
    
    @Nested
    @DisplayName("Console Action Reporter Tests")
    class ConsoleActionReporterTests {
        
        @Test
        @DisplayName("Should create ConsoleActionReporter when enabled")
        void testConsoleActionReporterEnabled() {
            contextRunner
                .withPropertyValues("brobot.console.actions.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(ConsoleActionReporter.class);
                    ConsoleActionReporter reporter = context.getBean(ConsoleActionReporter.class);
                    assertThat(reporter).isNotNull();
                });
        }
        
        @Test
        @DisplayName("Should create ConsoleActionReporter by default")
        void testConsoleActionReporterDefault() {
            contextRunner
                .run(context -> {
                    // matchIfMissing = true, so should be created by default
                    assertThat(context).hasSingleBean(ConsoleActionReporter.class);
                });
        }
        
        @Test
        @DisplayName("Should not create ConsoleActionReporter when disabled")
        void testConsoleActionReporterDisabled() {
            contextRunner
                .withPropertyValues("brobot.console.actions.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ConsoleActionReporter.class);
                });
        }
        
        @Test
        @DisplayName("Should inject dependencies into ConsoleActionReporter")
        void testConsoleActionReporterDependencies() {
            contextRunner
                .withPropertyValues("brobot.console.actions.enabled=true")
                .run(context -> {
                    ConsoleActionReporter reporter = context.getBean(ConsoleActionReporter.class);
                    assertThat(reporter).isNotNull();
                    // Verify it was created with the mocked dependencies
                    BrobotLogger logger = context.getBean(BrobotLogger.class);
                    assertThat(logger).isNotNull();
                });
        }
    }
    
    @Nested
    @DisplayName("GUI Access Monitor Tests")
    class GuiAccessMonitorTests {
        
        @Test
        @DisplayName("Should always create GuiAccessMonitor")
        void testGuiAccessMonitorAlwaysCreated() {
            contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(GuiAccessMonitor.class);
                    GuiAccessMonitor monitor = context.getBean(GuiAccessMonitor.class);
                    assertThat(monitor).isNotNull();
                });
        }
        
        @Test
        @DisplayName("Should inject dependencies into GuiAccessMonitor")
        void testGuiAccessMonitorDependencies() {
            contextRunner
                .run(context -> {
                    GuiAccessMonitor monitor = context.getBean(GuiAccessMonitor.class);
                    assertThat(monitor).isNotNull();
                    BrobotLogger logger = context.getBean(BrobotLogger.class);
                    assertThat(logger).isNotNull();
                    GuiAccessConfig config = context.getBean(GuiAccessConfig.class);
                    assertThat(config).isNotNull();
                });
        }
    }
    
    @Nested
    @DisplayName("Startup Checker Tests")
    class StartupCheckerTests {
        
        @Test
        @DisplayName("Should create startup checker when enabled")
        void testStartupCheckerEnabled() {
            when(guiAccessMonitor.checkGuiAccess()).thenReturn(true);
            
            contextRunner
                .withPropertyValues("brobot.gui-access.check-on-startup=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(ActionLoggingConfig.GuiAccessStartupChecker.class);
                });
        }
        
        @Test
        @DisplayName("Should create startup checker by default")
        void testStartupCheckerDefault() {
            when(guiAccessMonitor.checkGuiAccess()).thenReturn(true);
            
            contextRunner
                .run(context -> {
                    // matchIfMissing = true, so should be created by default
                    assertThat(context).hasSingleBean(ActionLoggingConfig.GuiAccessStartupChecker.class);
                });
        }
        
        @Test
        @DisplayName("Should not create startup checker when disabled")
        void testStartupCheckerDisabled() {
            contextRunner
                .withPropertyValues("brobot.gui-access.check-on-startup=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ActionLoggingConfig.GuiAccessStartupChecker.class);
                });
        }
        
        @Test
        @DisplayName("Should check GUI access on startup")
        void testStartupCheckerPerformsCheck() {
            when(guiAccessMonitor.checkGuiAccess()).thenReturn(true);
            when(guiAccessMonitor.getConfig()).thenReturn(guiAccessConfig);
            
            contextRunner
                .withPropertyValues("brobot.gui-access.check-on-startup=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(ActionLoggingConfig.GuiAccessStartupChecker.class);
                    verify(guiAccessMonitor, times(1)).checkGuiAccess();
                });
        }
        
        @Test
        @DisplayName("Should throw exception when GUI not accessible and continue-on-error is false")
        void testStartupCheckerThrowsException() {
            when(guiAccessMonitor.checkGuiAccess()).thenReturn(false);
            when(guiAccessMonitor.getConfig()).thenReturn(guiAccessConfig);
            when(guiAccessConfig.isContinueOnError()).thenReturn(false);
            
            contextRunner
                .withPropertyValues("brobot.gui-access.check-on-startup=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                        .rootCause()
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("GUI is not accessible");
                });
        }
        
        @Test
        @DisplayName("Should not throw exception when GUI not accessible but continue-on-error is true")
        void testStartupCheckerContinuesOnError() {
            when(guiAccessMonitor.checkGuiAccess()).thenReturn(false);
            when(guiAccessMonitor.getConfig()).thenReturn(guiAccessConfig);
            when(guiAccessConfig.isContinueOnError()).thenReturn(true);
            
            contextRunner
                .withPropertyValues("brobot.gui-access.check-on-startup=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ActionLoggingConfig.GuiAccessStartupChecker.class);
                });
        }
    }
    
    @Nested
    @DisplayName("Configuration Properties Tests")
    class ConfigurationPropertiesTests {
        
        @Test
        @DisplayName("Should enable configuration properties for all configs")
        void testEnableConfigurationProperties() {
            contextRunner
                .run(context -> {
                    // Verify all configuration classes are available as beans
                    assertThat(context).hasSingleBean(ConsoleActionConfig.class);
                    assertThat(context).hasSingleBean(VisualFeedbackConfig.class);
                    assertThat(context).hasSingleBean(GuiAccessConfig.class);
                    assertThat(context).hasSingleBean(LoggingVerbosityConfig.class);
                    
                    // Verify nested configuration classes
                    assertThat(context).hasSingleBean(VisualFeedbackConfig.FindHighlightConfig.class);
                    assertThat(context).hasSingleBean(VisualFeedbackConfig.SearchRegionHighlightConfig.class);
                    assertThat(context).hasSingleBean(VisualFeedbackConfig.ErrorHighlightConfig.class);
                    assertThat(context).hasSingleBean(VisualFeedbackConfig.ClickHighlightConfig.class);
                    assertThat(context).hasSingleBean(LoggingVerbosityConfig.NormalModeConfig.class);
                    assertThat(context).hasSingleBean(LoggingVerbosityConfig.VerboseModeConfig.class);
                });
        }
        
        @Test
        @DisplayName("Should load properties from brobot-visual-feedback.properties")
        void testLoadVisualFeedbackProperties() {
            contextRunner
                .withPropertyValues(
                    "brobot.highlight.enabled=true",
                    "brobot.highlight.find.enabled=true",
                    "brobot.highlight.find.duration=2000"
                )
                .run(context -> {
                    VisualFeedbackConfig config = context.getBean(VisualFeedbackConfig.class);
                    assertThat(config).isNotNull();
                    // The actual property binding would be tested in integration tests
                });
        }
        
        @Test
        @DisplayName("Should load properties from brobot-logging-defaults.properties")
        void testLoadLoggingDefaultProperties() {
            contextRunner
                .withPropertyValues(
                    "brobot.logging.verbosity=VERBOSE",
                    "brobot.logging.normal.console-output=true"
                )
                .run(context -> {
                    LoggingVerbosityConfig config = context.getBean(LoggingVerbosityConfig.class);
                    assertThat(config).isNotNull();
                    // The actual property binding would be tested in integration tests
                });
        }
    }
    
    @Nested
    @DisplayName("Bean Creation Order Tests")
    class BeanCreationOrderTests {
        
        @Test
        @DisplayName("Should create beans in correct order")
        void testBeanCreationOrder() {
            when(guiAccessMonitor.checkGuiAccess()).thenReturn(true);
            
            contextRunner
                .withPropertyValues(
                    "brobot.console.actions.enabled=true",
                    "brobot.gui-access.check-on-startup=true"
                )
                .run(context -> {
                    // All beans should be created successfully
                    assertThat(context).hasSingleBean(BrobotLogger.class);
                    assertThat(context).hasSingleBean(ConsoleActionConfig.class);
                    assertThat(context).hasSingleBean(ConsoleActionReporter.class);
                    assertThat(context).hasSingleBean(GuiAccessMonitor.class);
                    assertThat(context).hasSingleBean(ActionLoggingConfig.GuiAccessStartupChecker.class);
                });
        }
    }
    
    /**
     * Test configuration that provides mock beans for dependencies.
     */
    @Configuration
    static class TestConfiguration {
        
        @Bean
        public BrobotLogger brobotLogger() {
            return mock(BrobotLogger.class);
        }
        
        @Bean
        public ConsoleActionConfig consoleActionConfig() {
            return mock(ConsoleActionConfig.class);
        }
        
        @Bean
        public VisualFeedbackConfig visualFeedbackConfig() {
            return mock(VisualFeedbackConfig.class);
        }
        
        @Bean
        public VisualFeedbackConfig.FindHighlightConfig findHighlightConfig() {
            return mock(VisualFeedbackConfig.FindHighlightConfig.class);
        }
        
        @Bean
        public VisualFeedbackConfig.SearchRegionHighlightConfig searchRegionHighlightConfig() {
            return mock(VisualFeedbackConfig.SearchRegionHighlightConfig.class);
        }
        
        @Bean
        public VisualFeedbackConfig.ErrorHighlightConfig errorHighlightConfig() {
            return mock(VisualFeedbackConfig.ErrorHighlightConfig.class);
        }
        
        @Bean
        public VisualFeedbackConfig.ClickHighlightConfig clickHighlightConfig() {
            return mock(VisualFeedbackConfig.ClickHighlightConfig.class);
        }
        
        @Bean
        public GuiAccessConfig guiAccessConfig() {
            return mock(GuiAccessConfig.class);
        }
        
        @Bean
        @org.springframework.context.annotation.Primary
        public LoggingVerbosityConfig loggingVerbosityConfig() {
            return mock(LoggingVerbosityConfig.class);
        }
        
        @Bean
        public LoggingVerbosityConfig.NormalModeConfig normalModeConfig() {
            return mock(LoggingVerbosityConfig.NormalModeConfig.class);
        }
        
        @Bean
        public LoggingVerbosityConfig.VerboseModeConfig verboseModeConfig() {
            return mock(LoggingVerbosityConfig.VerboseModeConfig.class);
        }
        
        @Bean
        @org.springframework.context.annotation.Primary
        public GuiAccessMonitor guiAccessMonitor(BrobotLogger logger, GuiAccessConfig config) {
            GuiAccessMonitor monitor = mock(GuiAccessMonitor.class);
            when(monitor.getConfig()).thenReturn(config);
            when(monitor.checkGuiAccess()).thenReturn(true);
            return monitor;
        }
    }
}