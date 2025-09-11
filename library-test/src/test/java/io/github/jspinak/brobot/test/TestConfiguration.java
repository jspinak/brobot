package io.github.jspinak.brobot.test;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.control.ExecutionController;

/**
 * Test configuration for Spring Boot integration tests. This configuration ensures that Spring can
 * find and load all necessary components during test execution.
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {
            "io.github.jspinak.brobot",
            "io.github.jspinak.brobot.action",
            "io.github.jspinak.brobot.config",
            "io.github.jspinak.brobot.model",
            "io.github.jspinak.brobot.navigation",
            "io.github.jspinak.brobot.statemanagement",
            "io.github.jspinak.brobot.analysis",
            "io.github.jspinak.brobot.runner"
        })
public class TestConfiguration {

    /**
     * Provide a primary ExecutionController bean to resolve the conflict between
     * threadSafeExecutionController and reactiveAutomator. For tests, we'll use the thread-safe
     * controller by default.
     */
    @Bean
    @Primary
    public ExecutionController primaryExecutionController(
            @Qualifier("threadSafeExecutionController") ExecutionController threadSafeController) {
        return threadSafeController;
    }
}
