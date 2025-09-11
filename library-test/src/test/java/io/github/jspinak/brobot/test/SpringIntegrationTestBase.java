package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.test.config.ComprehensiveTestConfig;

/**
 * Base class for Spring integration tests that require Spring context. Extends BrobotTestBase and
 * adds Spring configuration.
 */
@SpringBootTest(classes = ComprehensiveTestConfig.class)
@Import(ComprehensiveTestConfig.class)
@TestPropertySource(
        properties = {
            "brobot.core.mockMode=true",
            "brobot.startup.enabled=false",
            "java.awt.headless=true",
            "brobot.test.mode=true",
            "brobot.test.type=integration",
            "spring.main.lazy-initialization=true",
            "spring.main.banner-mode=off"
        })
public abstract class SpringIntegrationTestBase extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Additional setup for Spring tests if needed
    }
}
