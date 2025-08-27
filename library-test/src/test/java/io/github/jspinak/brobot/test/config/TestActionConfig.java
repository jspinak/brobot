package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration to resolve circular dependencies in the action framework.
 * 
 * The circular dependency is:
 * ActionService -> BasicActionRegistry -> Drag -> ActionChainExecutor -> ActionService
 * 
 * We break this cycle by using @Lazy injection for ActionService in ActionChainExecutor.
 */
@TestConfiguration
public class TestActionConfig {
    
    @Bean
    @Primary
    public ActionChainExecutor actionChainExecutor(
            ActionExecution actionExecution,
            @Lazy ActionService actionService) {
        return new ActionChainExecutor(actionExecution, actionService);
    }
}