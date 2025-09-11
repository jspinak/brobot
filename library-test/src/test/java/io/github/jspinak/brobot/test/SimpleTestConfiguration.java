package io.github.jspinak.brobot.test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test configuration for Spring Boot integration tests. Provides minimal configuration needed for
 * tests to run.
 */
@TestConfiguration
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = "io.github.jspinak.brobot",
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.startup\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.initialization\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*BrobotStartup.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*BrobotRunner.*")
        })
public class SimpleTestConfiguration {
    // This class provides test configuration
}
