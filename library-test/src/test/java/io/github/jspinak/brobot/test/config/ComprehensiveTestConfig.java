package io.github.jspinak.brobot.test.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.core.BrobotConfiguration;
import io.github.jspinak.brobot.logging.modular.VerboseFormatter;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;

/**
 * Comprehensive test configuration that provides all commonly needed beans for testing. This
 * configuration ensures tests can run without full Spring Boot context.
 */
@TestConfiguration
@ComponentScan(
        basePackages = {"io.github.jspinak.brobot"},
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.startup\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.initialization\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*BrobotStartup.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*BrobotRunner.*")
        })
public class ComprehensiveTestConfig {

    static {
        // Ensure mock mode is enabled before any beans are created
        // Mock mode is enabled via BrobotTestBase
        System.setProperty("brobot.core.mockMode", "true");
        System.setProperty("java.awt.headless", "true");
        System.setProperty("brobot.test.mode", "true");
        System.setProperty("brobot.test.type", "unit");
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public BrobotConfiguration brobotConfiguration() {
        BrobotConfiguration config = new BrobotConfiguration();
        BrobotConfiguration.CoreConfig coreConfig = new BrobotConfiguration.CoreConfig();
        coreConfig.setMockMode(true);
        coreConfig.setForceHeadless(true);
        coreConfig.setAllowScreenCapture(false);
        config.setCore(coreConfig);
        return config;
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
    public Action action(
            ActionExecution actionExecution,
            ActionService actionService,
            ActionChainExecutor actionChainExecutor) {
        // Return a simple implementation that works in tests
        return new Action(actionExecution, actionService, actionChainExecutor);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateMemory stateMemory() {
        return mock(StateMemory.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateDetector stateDetector() {
        return mock(StateDetector.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateService stateService() {
        return mock(StateService.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StateNavigator stateNavigator() {
        return mock(StateNavigator.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public InitialStates initialStates(
            BrobotProperties brobotProperties,
            StateDetector stateDetector,
            StateMemory stateMemory,
            StateService stateService) {
        return new InitialStates(brobotProperties, stateDetector, stateMemory, stateService);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public VerboseFormatter verboseFormatter() {
        return mock(VerboseFormatter.class);
    }
}
