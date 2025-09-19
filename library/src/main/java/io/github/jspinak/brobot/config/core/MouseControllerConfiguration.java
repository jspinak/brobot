package io.github.jspinak.brobot.config.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.core.services.MouseController;
import io.github.jspinak.brobot.core.services.SikuliMouseController;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for MouseController in the Brobot framework.
 *
 * <p>Following the Brobot 1.0.7 pattern, this configuration only uses SikuliMouseController. Direct
 * Robot usage has been removed to simplify the codebase and avoid early GraphicsEnvironment
 * initialization issues.
 *
 * <p>SikuliMouseController provides:
 *
 * <ul>
 *   <li>Better integration with SikuliX pattern matching
 *   <li>Lazy Robot initialization (avoids headless detection issues)
 *   <li>Consistent behavior across platforms
 *   <li>Battle-tested implementation
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
@Configuration
public class MouseControllerConfiguration {

    /**
     * Provides the primary MouseController bean.
     *
     * <p>Uses SikuliMouseController exclusively. This avoids the complexity of maintaining our own
     * Robot wrapper and the associated headless detection issues.
     *
     * @param sikuliMouseController The SikuliX-based mouse controller
     * @return The primary MouseController implementation
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "primaryMouseController")
    public MouseController primaryMouseController(SikuliMouseController sikuliMouseController) {
        log.debug("Using SikuliMouseController as primary MouseController");
        return sikuliMouseController;
    }
}
