package io.github.jspinak.brobot.config.mock;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.action.internal.text.GetTextWrapper;
import io.github.jspinak.brobot.analysis.histogram.SingleRegionHistogramExtractor;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.environment.ExecutionMode;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.testing.mock.action.*;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import io.github.jspinak.brobot.tools.testing.mock.time.*;
import io.github.jspinak.brobot.tools.testing.wrapper.*;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for mock and wrapper beans to ensure proper initialization order. This
 * configuration breaks circular dependencies by defining beans explicitly.
 *
 * <p>When running with the "test" profile, additional optimizations are applied to speed up test
 * execution and provide deterministic behavior.
 */
@Configuration
@Slf4j
public class MockConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ActionDurations actionDurations() {
        return new ActionDurations();
    }

    @Bean
    @ConditionalOnMissingBean
    public MockTime mockTime(ActionDurations actionDurations) {
        return new MockTime(actionDurations);
    }

    @Bean
    @ConditionalOnMissingBean
    public MockFind mockFind(
            ActionDurations actionDurations,
            StateMemory stateMemory,
            StateService stateService,
            MockTime mockTime) {
        return new MockFind(actionDurations, stateMemory, stateService, mockTime);
    }

    @Bean
    @ConditionalOnMissingBean
    public MockText mockText(MockTime mockTime) {
        return new MockText(mockTime);
    }

    @Bean
    @ConditionalOnMissingBean
    public MockHistogram mockHistogram() {
        return new MockHistogram();
    }

    @Bean
    @ConditionalOnMissingBean
    public MockColor mockColor(ImageLoader imageLoader) {
        return new MockColor(imageLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public MockDrag mockDrag(MockTime mockTime) {
        return new MockDrag(mockTime);
    }

    @Bean
    public FindWrapper findWrapper(
            ExecutionMode executionMode,
            MockFind mockFind,
            ScenePatternMatcher scenePatternMatcher) {
        return new FindWrapper(executionMode, mockFind, scenePatternMatcher);
    }

    @Bean
    public TextWrapper textWrapper(
            ExecutionMode executionMode, MockText mockText, GetTextWrapper getTextWrapper) {
        return new TextWrapper(executionMode, mockText, getTextWrapper);
    }

    @Bean
    public HistogramWrapper histogramWrapper(
            ExecutionMode executionMode,
            MockHistogram mockHistogram,
            SingleRegionHistogramExtractor histogramExtractor) {
        return new HistogramWrapper(executionMode, mockHistogram, histogramExtractor);
    }

    @Bean
    public TimeWrapper timeWrapper(ExecutionMode executionMode, MockTime mockTime) {
        return new TimeWrapper(executionMode, mockTime);
    }

    @Bean
    public ExecutionModeController executionModeController(
            FindWrapper findWrapper,
            TextWrapper textWrapper,
            HistogramWrapper histogramWrapper,
            TimeWrapper timeWrapper) {
        return new ExecutionModeController(findWrapper, textWrapper, histogramWrapper, timeWrapper);
    }

    /**
     * Test profile configuration that optimizes settings for fast test execution. This
     * configuration automatically activates when running with the "test" profile.
     */
    @Configuration
    @Profile("test")
    @Order(1) // High priority to override defaults
    public static class TestProfileOptimization {

        @Autowired(required = false)
        private MockStateManagement mockStateManagement;

        @PostConstruct
        public void optimizeForTesting() {
            log.info("════════════════════════════════════════════════════════");
            log.info("  TEST PROFILE OPTIMIZATION ACTIVATED");
            log.info("════════════════════════════════════════════════════════");

            // Force mock mode for test profile
            FrameworkSettings.mock = true;
            FrameworkSettings.mockActionSuccessProbability = 1.0; // 100% success for tests
            log.info("✅ Mock mode enabled for test profile with 100% action success");

            // Configure test-optimized settings
            configureTestSettings();

            // Configure mock state management if available
            if (mockStateManagement != null) {
                configureMockStates();
            }

            log.info("════════════════════════════════════════════════════════");
        }

        private void configureTestSettings() {
            // Fast mock execution times for rapid test execution
            FrameworkSettings.mockTimeFindFirst = 0.01;
            FrameworkSettings.mockTimeFindAll = 0.02;
            FrameworkSettings.mockTimeClick = 0.005;
            FrameworkSettings.mockTimeDrag = 0.01;
            FrameworkSettings.mockTimeMove = 0.01;

            // Disable visual elements for headless test execution
            FrameworkSettings.drawFind = false;
            FrameworkSettings.drawClick = false;
            FrameworkSettings.drawDrag = false;
            FrameworkSettings.drawMove = false;
            FrameworkSettings.drawHighlight = false;
            FrameworkSettings.saveSnapshots = false;
            FrameworkSettings.saveHistory = false;

            // Remove pauses for maximum speed
            FrameworkSettings.pauseBeforeMouseDown = 0;
            FrameworkSettings.pauseAfterMouseDown = 0;
            FrameworkSettings.pauseBeforeMouseUp = 0;
            FrameworkSettings.pauseAfterMouseUp = 0;
            FrameworkSettings.moveMouseDelay = 0;

            log.info("✅ Test optimization settings applied:");
            log.info("  - Mock operation times: 0.005-0.02 seconds");
            log.info("  - Visual elements: DISABLED");
            log.info("  - Mouse pauses: REMOVED");
        }

        private void configureMockStates() {
            // Set deterministic probabilities for reliable testing
            // Applications can override these for specific states
            log.info("✅ MockStateManagement available for deterministic state testing");
        }
    }
}
