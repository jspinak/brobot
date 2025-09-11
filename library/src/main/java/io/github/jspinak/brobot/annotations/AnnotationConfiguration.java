package io.github.jspinak.brobot.annotations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Brobot annotations support. This ensures all annotation-related beans are
 * properly scanned and registered.
 */
@Configuration
@ComponentScan(basePackageClasses = AnnotationProcessor.class)
@Slf4j
public class AnnotationConfiguration {

    public AnnotationConfiguration() {
        log.info("AnnotationConfiguration loaded - annotation processing enabled");
    }
}
