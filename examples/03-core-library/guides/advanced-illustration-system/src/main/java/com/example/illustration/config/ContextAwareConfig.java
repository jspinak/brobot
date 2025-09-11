package com.example.illustration.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.tools.history.IllustrationController;

import lombok.extern.slf4j.Slf4j;

/**
 * Context-aware illustration strategies for Brobot v1.1.0.
 *
 * <p>In v1.1.0, context-aware illustration is achieved through: - ActionConfig.illustrate property
 * (YES, NO, USE_GLOBAL) - Custom wrappers around IllustrationController - Application-level logic
 * for filtering
 */
@Configuration
@Slf4j
public class ContextAwareConfig {

    /** Wrapper that only illustrates failures for debugging. */
    @Bean
    public FailureOnlyIllustrator failureOnlyIllustrator(
            IllustrationController illustrationController) {
        return new FailureOnlyIllustrator(illustrationController);
    }

    /** Wrapper that implements smart sampling strategies. */
    @Bean
    public SmartSamplingIllustrator smartSamplingIllustrator(
            IllustrationController illustrationController) {
        return new SmartSamplingIllustrator(illustrationController);
    }

    /** Wrapper that adapts to system performance. */
    @Bean
    public PerformanceAwareIllustrator performanceAwareIllustrator(
            IllustrationController illustrationController) {
        return new PerformanceAwareIllustrator(illustrationController);
    }

    /** Illustrator that only captures failures. */
    @Component
    public static class FailureOnlyIllustrator {
        private final IllustrationController illustrationController;

        public FailureOnlyIllustrator(IllustrationController illustrationController) {
            this.illustrationController = illustrationController;
        }

        public void illustrateIfFailed(ActionResult result, ObjectCollection collection) {
            if (!result.isSuccess()) {
                log.debug("Action failed - illustrating for debugging");
                // Need to provide proper parameters for v1.1.0
                illustrationController.illustrateWhenAllowed(
                        result,
                        new ArrayList<>(),
                        new PatternFindOptions.Builder().build(),
                        collection);
            }
        }
    }

    /** Illustrator with smart sampling logic. */
    @Component
    public static class SmartSamplingIllustrator {
        private final IllustrationController illustrationController;
        private int findCount = 0;
        private int clickCount = 0;
        private int consecutiveFailures = 0;

        public SmartSamplingIllustrator(IllustrationController illustrationController) {
            this.illustrationController = illustrationController;
        }

        public void illustrateWithSampling(
                ActionResult result, ActionConfig config, ObjectCollection collection) {
            boolean shouldIllustrate = false;

            // Always illustrate failures
            if (!result.isSuccess()) {
                consecutiveFailures++;
                shouldIllustrate = true;
            } else {
                consecutiveFailures = 0;
            }

            // Sample based on action type
            if (config instanceof PatternFindOptions) {
                findCount++;
                // Illustrate first find and every 10th
                if (findCount == 1 || findCount % 10 == 0) {
                    shouldIllustrate = true;
                }
            } else if (config instanceof ClickOptions) {
                clickCount++;
                // Illustrate first click and every 5th
                if (clickCount == 1 || clickCount % 5 == 0) {
                    shouldIllustrate = true;
                }
            }

            // Always illustrate after multiple failures
            if (consecutiveFailures >= 3) {
                shouldIllustrate = true;
            }

            if (shouldIllustrate && illustrationController.okToIllustrate(config, collection)) {
                illustrationController.illustrateWhenAllowed(
                        result, new ArrayList<>(), config, collection);
            }
        }
    }

    /** Illustrator that adapts to system performance. */
    @Component
    public static class PerformanceAwareIllustrator {
        private final IllustrationController illustrationController;
        private long lastIllustrationTime = 0;
        private static final long MIN_INTERVAL_MS = 1000; // Minimum 1 second between illustrations

        public PerformanceAwareIllustrator(IllustrationController illustrationController) {
            this.illustrationController = illustrationController;
        }

        public void illustrateIfPerformanceAllows(
                ActionResult result, ActionConfig config, ObjectCollection collection) {
            long currentTime = System.currentTimeMillis();

            // Always illustrate critical failures immediately
            if (!result.isSuccess() && isCriticalAction(config)) {
                illustrationController.illustrateWhenAllowed(
                        result, new ArrayList<>(), config, collection);
                lastIllustrationTime = currentTime;
                return;
            }

            // Rate limit non-critical illustrations
            if (currentTime - lastIllustrationTime >= MIN_INTERVAL_MS) {
                if (illustrationController.okToIllustrate(config, collection)) {
                    illustrationController.illustrateWhenAllowed(
                            result, new ArrayList<>(), config, collection);
                    lastIllustrationTime = currentTime;
                }
            }
        }

        private boolean isCriticalAction(ActionConfig config) {
            // Define your critical actions here
            // For example, all clicks might be considered critical
            return config instanceof ClickOptions;
        }
    }

    /** Example helper for creating ActionConfig with explicit illustration settings. */
    @Bean
    public IllustrationHelper illustrationHelper() {
        return new IllustrationHelper();
    }

    public static class IllustrationHelper {
        /** Create PatternFindOptions that always illustrates. */
        public PatternFindOptions alwaysIllustrateFindOptions() {
            return new PatternFindOptions.Builder()
                    .setIllustrate(ActionConfig.Illustrate.YES)
                    .build();
        }

        /** Create ClickOptions that never illustrates. */
        public ClickOptions neverIllustrateClickOptions() {
            return new ClickOptions.Builder().setIllustrate(ActionConfig.Illustrate.NO).build();
        }

        /** Create options that use global settings. */
        public PatternFindOptions useGlobalSettingsFindOptions() {
            return new PatternFindOptions.Builder()
                    .setIllustrate(ActionConfig.Illustrate.USE_GLOBAL)
                    .build();
        }
    }
}
