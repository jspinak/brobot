package io.github.jspinak.brobot.config.environment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.github.jspinak.brobot.diagnostics.ImageLoadingDiagnosticsRunner;

/**
 * Configuration for diagnostic tools that can be enabled via properties.
 *
 * <p>Enable diagnostics by setting: - brobot.diagnostics.image-loading.enabled=true
 */
@Configuration
@ConditionalOnProperty(
        prefix = "brobot.diagnostics",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
@ComponentScan(basePackageClasses = ImageLoadingDiagnosticsRunner.class)
public class DiagnosticsConfiguration {}
