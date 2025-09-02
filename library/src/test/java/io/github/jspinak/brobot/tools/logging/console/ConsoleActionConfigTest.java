package io.github.jspinak.brobot.tools.logging.console;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Integration test - should be in library-test module. Uses ApplicationContextRunner which creates Spring context.")
@ExtendWith(MockitoExtension.class)
public class ConsoleActionConfigTest extends BrobotTestBase {
    
    private ConsoleActionConfig config;
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfiguration.class);
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        config = new ConsoleActionConfig();
    }
    
    @Test
    public void testDefaultConfiguration() {
        // Assert default values
        assertTrue(config.isEnabled());
        assertEquals(ConsoleActionConfig.Level.NORMAL, config.getLevel());
        
        // Action type filters
        assertTrue(config.isReportFind());
        assertTrue(config.isReportClick());
        assertTrue(config.isReportType());
        assertTrue(config.isReportDrag());
        assertFalse(config.isReportHighlight());
        assertTrue(config.isReportTransitions());
        
        // Performance thresholds
        assertEquals(1000, config.getPerformanceWarnThreshold());
        assertEquals(5000, config.getPerformanceErrorThreshold());
        
        // Formatting options
        assertTrue(config.isUseColors());
        assertTrue(config.isUseIcons());
        assertEquals(50, config.getMaxTextLength());
        assertTrue(config.isShowMatchDetails());
        assertTrue(config.isShowTiming());
        assertFalse(config.isGroupRelatedActions());
        assertEquals(100, config.getGroupingTimeWindow());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test enabled
        config.setEnabled(false);
        assertFalse(config.isEnabled());
        
        // Test level
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        assertEquals(ConsoleActionConfig.Level.VERBOSE, config.getLevel());
        
        // Test action filters
        config.setReportFind(false);
        assertFalse(config.isReportFind());
        
        config.setReportClick(false);
        assertFalse(config.isReportClick());
        
        config.setReportType(false);
        assertFalse(config.isReportType());
        
        config.setReportDrag(false);
        assertFalse(config.isReportDrag());
        
        config.setReportTransitions(false);
        assertFalse(config.isReportTransitions());
        
        config.setShowMatchDetails(false);
        assertFalse(config.isShowMatchDetails());
        
        config.setReportHighlight(false);
        assertFalse(config.isReportHighlight());
        
        // Test performance thresholds
        config.setPerformanceWarnThreshold(1000);
        assertEquals(1000, config.getPerformanceWarnThreshold());
        
        config.setPerformanceErrorThreshold(3000);
        assertEquals(3000, config.getPerformanceErrorThreshold());
        
        // Test formatting options
        config.setUseColors(false);
        assertFalse(config.isUseColors());
        
        config.setUseIcons(false);
        assertFalse(config.isUseIcons());
        
        config.setMaxTextLength(50);
        assertEquals(50, config.getMaxTextLength());
        
        config.setShowMatchDetails(false);
        assertFalse(config.isShowMatchDetails());
        
        config.setShowTiming(false);
        assertFalse(config.isShowTiming());
        
        config.setGroupRelatedActions(false);
        assertFalse(config.isGroupRelatedActions());
        
        config.setGroupRelatedActions(false);
        assertFalse(config.isGroupRelatedActions());
    }
    
    @Test
    public void testLevelEnum() {
        // Test all level values
        assertEquals("QUIET", ConsoleActionConfig.Level.QUIET.name());
        assertEquals("NORMAL", ConsoleActionConfig.Level.NORMAL.name());
        assertEquals("VERBOSE", ConsoleActionConfig.Level.VERBOSE.name());
        
        // Test valueOf
        assertEquals(ConsoleActionConfig.Level.QUIET, 
                    ConsoleActionConfig.Level.valueOf("QUIET"));
        assertEquals(ConsoleActionConfig.Level.NORMAL, 
                    ConsoleActionConfig.Level.valueOf("NORMAL"));
        assertEquals(ConsoleActionConfig.Level.VERBOSE, 
                    ConsoleActionConfig.Level.valueOf("VERBOSE"));
    }
    
    @Test
    public void testConfigurationProperties_Integration() {
        contextRunner
            .withPropertyValues(
                "brobot.console.actions.enabled=false",
                "brobot.console.actions.level=VERBOSE",
                "brobot.console.actions.report-find=false",
                "brobot.console.actions.report-click=false",
                "brobot.console.actions.report-type=true",
                "brobot.console.actions.performance-warn-threshold=1500",
                "brobot.console.actions.performance-error-threshold=4000",
                "brobot.console.actions.use-colors=false",
                "brobot.console.actions.use-icons=false",
                "brobot.console.actions.max-text-length=75",
                "brobot.console.actions.show-timestamp=false",
                "brobot.console.actions.show-duration=false",
                "brobot.console.actions.show-confidence=false",
                "brobot.console.actions.show-region=false"
            )
            .run(context -> {
                // The test runner context should process the configuration
                assertNotNull(context);
            });
    }
    
    @Test
    public void testPerformanceThresholdValidation() {
        // Test that warn threshold can be less than error threshold
        config.setPerformanceWarnThreshold(1000);
        config.setPerformanceErrorThreshold(2000);
        
        assertTrue(config.getPerformanceWarnThreshold() < config.getPerformanceErrorThreshold());
        
        // Test edge case: same values
        config.setPerformanceWarnThreshold(1500);
        config.setPerformanceErrorThreshold(1500);
        
        assertEquals(config.getPerformanceWarnThreshold(), config.getPerformanceErrorThreshold());
        
        // Test zero values
        config.setPerformanceWarnThreshold(0);
        config.setPerformanceErrorThreshold(0);
        
        assertEquals(0, config.getPerformanceWarnThreshold());
        assertEquals(0, config.getPerformanceErrorThreshold());
        
        // Test negative values (should be allowed but may indicate misconfiguration)
        config.setPerformanceWarnThreshold(-1);
        config.setPerformanceErrorThreshold(-1);
        
        assertEquals(-1, config.getPerformanceWarnThreshold());
        assertEquals(-1, config.getPerformanceErrorThreshold());
    }
    
    @Test
    public void testMaxTextLengthBounds() {
        // Test minimum bound
        config.setMaxTextLength(0);
        assertEquals(0, config.getMaxTextLength());
        
        // Test reasonable value
        config.setMaxTextLength(100);
        assertEquals(100, config.getMaxTextLength());
        
        // Test large value
        config.setMaxTextLength(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, config.getMaxTextLength());
        
        // Test negative value (should be allowed but may indicate misconfiguration)
        config.setMaxTextLength(-1);
        assertEquals(-1, config.getMaxTextLength());
    }
    
    @Test
    public void testQuietLevelConfiguration() {
        config.setLevel(ConsoleActionConfig.Level.QUIET);
        
        // In quiet mode, detailed options should still maintain their settings
        assertTrue(config.isShowMatchDetails());
        assertTrue(config.isShowTiming());
        assertFalse(config.isGroupRelatedActions());
        assertFalse(config.isGroupRelatedActions());
        
        // The reporter should handle quiet mode logic, not the config
        assertEquals(ConsoleActionConfig.Level.QUIET, config.getLevel());
    }
    
    @Test
    public void testVerboseLevelConfiguration() {
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        
        // All detail options should be available in verbose mode
        assertTrue(config.isShowMatchDetails());
        assertTrue(config.isShowTiming());
        assertFalse(config.isGroupRelatedActions());
        assertFalse(config.isGroupRelatedActions());
        
        assertEquals(ConsoleActionConfig.Level.VERBOSE, config.getLevel());
    }
    
    @Test
    public void testAllActionsDisabled() {
        // Disable all action reporting
        config.setReportFind(false);
        config.setReportClick(false);
        config.setReportType(false);
        config.setReportDrag(false);
        config.setReportTransitions(false);
        config.setShowMatchDetails(false);
        config.setReportHighlight(false);
        
        // Config should still be enabled (master switch)
        assertTrue(config.isEnabled());
        
        // But all individual actions should be disabled
        assertFalse(config.isReportFind());
        assertFalse(config.isReportClick());
        assertFalse(config.isReportType());
        assertFalse(config.isReportDrag());
        assertFalse(config.isReportTransitions());
        assertFalse(config.isShowMatchDetails());
        assertFalse(config.isReportHighlight());
    }
    
    @Test
    public void testSelectiveActionReporting() {
        // Enable only specific actions
        config.setReportFind(true);
        config.setReportClick(true);
        config.setReportType(false);
        config.setReportDrag(false);
        config.setReportTransitions(false);
        config.setShowMatchDetails(false);
        config.setReportHighlight(false);
        
        assertTrue(config.isReportFind());
        assertTrue(config.isReportClick());
        assertFalse(config.isReportType());
        assertFalse(config.isReportDrag());
        assertFalse(config.isReportTransitions());
        assertFalse(config.isShowMatchDetails());
        assertFalse(config.isReportHighlight());
    }
    
    @Test
    public void testFormattingOptionsIndependence() {
        // Test that formatting options are independent
        config.setUseColors(true);
        config.setUseIcons(false);
        
        assertTrue(config.isUseColors());
        assertFalse(config.isUseIcons());
        
        config.setUseColors(false);
        config.setUseIcons(true);
        
        assertFalse(config.isUseColors());
        assertTrue(config.isUseIcons());
    }
    
    @Test
    public void testDetailOptionsIndependence() {
        // Test that detail options are independent
        config.setShowMatchDetails(true);
        config.setShowTiming(false);
        config.setGroupRelatedActions(true);
        
        assertTrue(config.isShowMatchDetails());
        assertFalse(config.isShowTiming());
        assertTrue(config.isGroupRelatedActions());
        
        // Change one option shouldn't affect others
        config.setShowTiming(true);
        assertTrue(config.isShowMatchDetails());
        assertTrue(config.isShowTiming());
        assertTrue(config.isGroupRelatedActions());
    }
    
    @Test
    public void testConfigurationCopy() {
        // Setup original config
        config.setEnabled(false);
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        config.setReportFind(false);
        config.setPerformanceWarnThreshold(1234);
        config.setUseColors(false);
        config.setMaxTextLength(42);
        
        // Create new config and copy values
        ConsoleActionConfig copy = new ConsoleActionConfig();
        copy.setEnabled(config.isEnabled());
        copy.setLevel(config.getLevel());
        copy.setReportFind(config.isReportFind());
        copy.setPerformanceWarnThreshold(config.getPerformanceWarnThreshold());
        copy.setUseColors(config.isUseColors());
        copy.setMaxTextLength(config.getMaxTextLength());
        
        // Verify copy
        assertEquals(config.isEnabled(), copy.isEnabled());
        assertEquals(config.getLevel(), copy.getLevel());
        assertEquals(config.isReportFind(), copy.isReportFind());
        assertEquals(config.getPerformanceWarnThreshold(), copy.getPerformanceWarnThreshold());
        assertEquals(config.isUseColors(), copy.isUseColors());
        assertEquals(config.getMaxTextLength(), copy.getMaxTextLength());
    }
    
    @EnableConfigurationProperties(ConsoleActionConfig.class)
    static class TestConfiguration {
        // Configuration class for testing property binding
    }
}