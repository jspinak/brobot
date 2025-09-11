package io.github.jspinak.brobot.test.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;

/**
 * Simple test configuration that provides mock beans for testing. This configuration ensures tests
 * can run without full Spring Boot context.
 */
@TestConfiguration
public class SimpleMockTestConfig {

    static {
        // Ensure mock mode is enabled before any beans are created
        FrameworkSettings.mock = true;
        System.setProperty("brobot.core.mockMode", "true");
        System.setProperty("java.awt.headless", "true");
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ActionExecution mockActionExecution() {
        return mock(ActionExecution.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ActionService mockActionService() {
        return mock(ActionService.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ActionChainExecutor mockActionChainExecutor() {
        return mock(ActionChainExecutor.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Action mockAction(
            ActionExecution actionExecution,
            ActionService actionService,
            ActionChainExecutor actionChainExecutor) {
        return new Action(actionExecution, actionService, actionChainExecutor);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateMemory mockStateMemory() {
        return mock(StateMemory.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateDetector mockStateDetector() {
        return mock(StateDetector.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateService mockStateService() {
        return mock(StateService.class);
    }
}
