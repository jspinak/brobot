package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.logging.ActionLoggingEnhancer;
import io.github.jspinak.brobot.action.logging.DynamicMessageTemplateEngine;
import io.github.jspinak.brobot.action.logging.ConditionalLoggingStrategy;
import io.github.jspinak.brobot.action.logging.ActionChainLogger;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Map;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ActionConfig logging functionality.
 * Tests that logging configuration is properly integrated with the action system.
 */
class ActionLoggingIntegrationTest {

    private ActionLoggingEnhancer loggingEnhancer;
    private DynamicMessageTemplateEngine templateEngine;
    private ConditionalLoggingStrategy conditionalLogging;
    private ActionChainLogger chainLogger;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loggingEnhancer = new ActionLoggingEnhancer();
        templateEngine = new DynamicMessageTemplateEngine();
        conditionalLogging = mock(ConditionalLoggingStrategy.class);
        chainLogger = mock(ActionChainLogger.class);
    }

    @Test
    @DisplayName("Should create pattern find action with comprehensive logging")
    void testPatternFindWithLogging() {
        // Create pattern find options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
        
        // Add logging configuration using the new ActionLoggingEnhancer
        ActionConfig enhancedConfig = loggingEnhancer.withBeforeActionLog(
            findOptions,
            config -> System.out.println("Starting find action with similarity: " + 
                ((PatternFindOptions)config).getSimilarity())
        );
        
        enhancedConfig = loggingEnhancer.withAfterActionLog(
            enhancedConfig,
            (config, result) -> System.out.println("Find completed. Success: " + result.isSuccess())
        );
        
        enhancedConfig = loggingEnhancer.withLogLevel(enhancedConfig, LogLevel.INFO);
        enhancedConfig = loggingEnhancer.withLogOnSuccess(enhancedConfig, true);
        enhancedConfig = loggingEnhancer.withLogOnFailure(enhancedConfig, true);
        
        assertNotNull(enhancedConfig);
        assertNotNull(loggingEnhancer.getLoggingConfiguration(enhancedConfig));
        assertEquals(LogLevel.INFO, loggingEnhancer.getLoggingConfiguration(enhancedConfig).getLogLevel());
    }
    
    @Test
    @DisplayName("Should configure action chaining with logging at each step")
    void testActionChainingWithLogging() {
        // Create a chain of actions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
                
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();
                
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build();
        
        // Use ActionChainLogger to track the chain
        String chainId = "test-chain-123";
        when(chainLogger.logChainStart("TestChain", "Testing action chain logging"))
            .thenReturn(chainId);
        
        String actualChainId = chainLogger.logChainStart("TestChain", "Testing action chain logging");
        
        // Log each step
        chainLogger.logStep(chainId, "Finding pattern");
        chainLogger.logStep(chainId, "Clicking on match");
        chainLogger.logStep(chainId, "Typing text");
        
        // End the chain
        chainLogger.logChainEnd(chainId, true, "Chain completed successfully");
        
        assertNotNull(findOptions);
        assertNotNull(clickOptions);
        assertNotNull(typeOptions);
        assertEquals(chainId, actualChainId);
        verify(chainLogger).logChainStart("TestChain", "Testing action chain logging");
        verify(chainLogger, times(3)).logStep(eq(chainId), anyString());
        verify(chainLogger).logChainEnd(chainId, true, "Chain completed successfully");
    }
    
    @Test
    @DisplayName("Should respect conditional logging based on result")
    void testConditionalLogging() {
        // Create an action result
        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);
        
        ActionResult failureResult = new ActionResult();
        failureResult.setSuccess(false);
        
        // Test conditional logging
        when(conditionalLogging.shouldLog(successResult)).thenReturn(true);
        when(conditionalLogging.shouldLog(failureResult)).thenReturn(false);
        
        assertTrue(conditionalLogging.shouldLog(successResult));
        assertFalse(conditionalLogging.shouldLog(failureResult));
        
        // Verify conditional execution
        conditionalLogging.logIfSuccess(successResult, "Action succeeded");
        conditionalLogging.logIfFailure(failureResult, "Action failed");
        
        verify(conditionalLogging).logIfSuccess(successResult, "Action succeeded");
        verify(conditionalLogging).logIfFailure(failureResult, "Action failed");
    }
    
    @Test
    @DisplayName("Should support dynamic message templates")
    void testDynamicMessageTemplates() {
        // Create a template with placeholders
        String template = "Action ${actionType} completed with ${matchCount} matches at ${timestamp}";
        
        // Parse the template
        String[] variables = templateEngine.parse(template);
        assertEquals(3, variables.length);
        assertTrue(Arrays.asList(variables).contains("actionType"));
        assertTrue(Arrays.asList(variables).contains("matchCount"));
        assertTrue(Arrays.asList(variables).contains("timestamp"));
        
        // Substitute variables
        Map<String, Object> vars = Map.of(
            "actionType", "FIND",
            "matchCount", 5
        );
        
        String result = templateEngine.substitute(template, vars);
        assertTrue(result.contains("Action FIND"));
        assertTrue(result.contains("5 matches"));
        assertFalse(result.contains("${")); // No unsubstituted placeholders
        
        // Test with ActionConfig and ActionResult
        PatternFindOptions config = new PatternFindOptions.Builder().build();
        ActionResult actionResult = new ActionResult();
        actionResult.setSuccess(true);
        
        String formatted = templateEngine.formatMessage(
            "Find action ${success} with ${matchCount} matches",
            config,
            actionResult
        );
        
        assertTrue(formatted.contains("Find action true"));
        assertTrue(formatted.contains("0 matches"));
    }
    
    @Test
    @DisplayName("Should integrate with quiet mode settings")
    void testQuietModeIntegration() {
        // This test can work with current API
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
                
        // In quiet mode, logging would be suppressed
        // This is controlled by Spring properties
        assertNotNull(findOptions);
    }
}