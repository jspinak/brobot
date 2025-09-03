package io.github.jspinak.brobot.tools.testing.wrapper;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.testing.mock.action.MockHistogram;
import io.github.jspinak.brobot.analysis.histogram.SingleRegionHistogramExtractor;
import io.github.jspinak.brobot.config.environment.ExecutionMode;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Wrapper for Histogram operations that routes to mock or live implementation.
 * 
 * This wrapper provides a stable API for histogram analysis operations while
 * allowing the underlying implementation to switch between mock and live modes.
 */
@Component
public class HistogramWrapper {
    
    private final ExecutionMode executionMode;
    private final MockHistogram mockHistogram;
    private final SingleRegionHistogramExtractor histogramExtractor;
    
    public HistogramWrapper(ExecutionMode executionMode,
                           MockHistogram mockHistogram,
                           SingleRegionHistogramExtractor histogramExtractor) {
        this.executionMode = executionMode;
        this.mockHistogram = mockHistogram;
        this.histogramExtractor = histogramExtractor;
    }
    
    /**
     * Performs histogram-based pattern matching.
     */
    public List<Match> findHistogram(StateImage stateImage, Mat sceneHSV, List<Region> regions) {
        if (executionMode.isMock()) {
            return mockHistogram.getMockHistogramMatches(stateImage, regions);
        }
        return histogramExtractor.findAll(regions, stateImage, sceneHSV);
    }
}