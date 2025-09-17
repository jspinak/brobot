package io.github.jspinak.brobot.config.dpi;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for automatic pattern scaling detection. This enables Brobot to work across
 * different DPI settings without manual configuration.
 */
@Configuration
public class AutoScalingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AutoScalingConfiguration.class);

    @Value("${brobot.autoscaling.enabled:true}")
    private boolean autoScalingEnabled;

    @Value("${brobot.autoscaling.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${brobot.autoscaling.global.learning:true}")
    private boolean globalLearningEnabled;

    @Value("${brobot.autoscaling.min.confidence:0.85}")
    private double minConfidence;

    @PostConstruct
    public void init() {
        if (autoScalingEnabled) {
            log.info("Auto-scaling pattern matching is ENABLED");
            log.info("  Cache: {}", cacheEnabled ? "ENABLED" : "DISABLED");
            log.info("  Global learning: {}", globalLearningEnabled ? "ENABLED" : "DISABLED");
            log.info("  Min confidence: {}", minConfidence);
            log.info("");
            log.info("Brobot will automatically detect the correct scaling for patterns.");
            log.info("This solves DPI/scaling mismatches between pattern capture and runtime.");
        } else {
            log.info("Auto-scaling pattern matching is DISABLED");
            log.info("Patterns will be matched at their original scale.");
        }
    }

    /** Bean configuration for auto-scaling settings. */
    @Bean
    public AutoScalingProperties autoScalingProperties() {
        return new AutoScalingProperties(
                autoScalingEnabled, cacheEnabled, globalLearningEnabled, minConfidence);
    }

    /** Properties holder for auto-scaling configuration. */
    public static class AutoScalingProperties {
        private final boolean enabled;
        private final boolean cacheEnabled;
        private final boolean globalLearningEnabled;
        private final double minConfidence;

        public AutoScalingProperties(
                boolean enabled,
                boolean cacheEnabled,
                boolean globalLearningEnabled,
                double minConfidence) {
            this.enabled = enabled;
            this.cacheEnabled = cacheEnabled;
            this.globalLearningEnabled = globalLearningEnabled;
            this.minConfidence = minConfidence;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isCacheEnabled() {
            return cacheEnabled;
        }

        public boolean isGlobalLearningEnabled() {
            return globalLearningEnabled;
        }

        public double getMinConfidence() {
            return minConfidence;
        }
    }
}
