package io.github.jspinak.brobot.config.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.action.internal.text.GetTextWrapper;
import io.github.jspinak.brobot.analysis.histogram.SingleRegionHistogramExtractor;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.environment.ExecutionMode;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.testing.mock.action.*;
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

    @Autowired private BrobotProperties brobotProperties;

    @Bean
    @ConditionalOnMissingBean
    public ActionDurations actionDurations() {
        return new ActionDurations(brobotProperties);
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
        return new MockColor(brobotProperties, imageLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public MockDrag mockDrag(MockTime mockTime) {
        return new MockDrag(brobotProperties, mockTime);
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
}
