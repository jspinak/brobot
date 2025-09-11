package io.github.jspinak.brobot.config.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.core.services.MouseController;
import io.github.jspinak.brobot.core.services.SikuliMouseController;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for MouseController selection in the Brobot framework.
 *
 * <p>This configuration resolves the ambiguity between multiple MouseController implementations:
 *
 * <ul>
 *   <li>SikuliMouseController - Uses SikuliX mouse control (default)
 *   <li>RobotMouseController - Uses Java Robot API
 * </ul>
 *
 * <p>The controller can be selected via configuration:
 *
 * <pre>
 * brobot.mouse.controller=sikuli  # Use SikuliX (default)
 * brobot.mouse.controller=robot   # Use Java Robot
 * </pre>
 *
 * <p>SikuliMouseController is the default because:
 *
 * <ul>
 *   <li>Better integration with SikuliX pattern matching
 *   <li>More accurate coordinate handling
 *   <li>Consistent behavior across platforms
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
     * <p>By default, uses SikuliMouseController for consistency with SikuliX operations. This can
     * be overridden by setting brobot.mouse.controller=robot in application properties.
     *
     * @param sikuliMouseController The SikuliX-based mouse controller
     * @return The primary MouseController implementation
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "primaryMouseController")
    @ConditionalOnProperty(
            name = "brobot.mouse.controller",
            havingValue = "sikuli",
            matchIfMissing = true // Default to sikuli if not specified
            )
    public MouseController primaryMouseController(SikuliMouseController sikuliMouseController) {
        log.debug("Using SikuliMouseController as primary MouseController");
        return sikuliMouseController;
    }

    /**
     * Alternative configuration to use RobotMouseController as primary.
     *
     * <p>Activated when brobot.mouse.controller=robot is set in application properties.
     *
     * @param robotMouseController The Java Robot-based mouse controller
     * @return The primary MouseController implementation
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "primaryMouseController")
    @ConditionalOnProperty(name = "brobot.mouse.controller", havingValue = "robot")
    public MouseController primaryRobotMouseController(
            @org.springframework.beans.factory.annotation.Qualifier("robotMouseController") MouseController robotMouseController) {
        log.debug("Using RobotMouseController as primary MouseController");
        return robotMouseController;
    }
}
