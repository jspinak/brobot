package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for the action framework.
 * 
 * Note: The comment about circular dependency (ActionService -> BasicActionRegistry -> 
 * Drag -> ActionChainExecutor -> ActionService) was incorrect. 
 * Analysis shows Drag does NOT depend on ActionChainExecutor.
 * 
 * This configuration now follows proper architecture without @Lazy:
 * - ActionChainExecutor depends on ActionService (one-way dependency)
 * - No circular dependencies exist in the action framework
 */
@TestConfiguration
public class TestActionConfig {
    
    /**
     * Provides ActionChainExecutor bean for tests.
     * This is a clean one-way dependency with no cycles.
     * 
     * Single Responsibility: Wire ActionChainExecutor with its dependencies.
     */
    @Bean
    @Primary
    public ActionChainExecutor actionChainExecutor(
            ActionExecution actionExecution,
            ActionService actionService) {
        return new ActionChainExecutor(actionExecution, actionService);
    }
}