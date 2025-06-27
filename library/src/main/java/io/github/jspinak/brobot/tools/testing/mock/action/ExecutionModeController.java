package io.github.jspinak.brobot.tools.testing.mock.action;

import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.action.internal.text.GetTextWrapper;
import io.github.jspinak.brobot.analysis.histogram.SingleRegionHistogramExtractor;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.ExecutionMode;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Central coordinator for seamlessly switching between mock and live execution modes.
 * 
 * <p>ExecutionModeController serves as the bridge between Brobot's testing infrastructure and its
 * live automation capabilities, providing a unified interface that transparently routes
 * operations to either mock implementations or actual GUI interactions based on the
 * current execution mode. This design enables the same test code to run in both
 * simulated and real environments without modification.</p>
 * 
 * <p>The class embodies the principle of execution mode transparency, where high-level
 * automation logic remains independent of whether it's running against a real GUI or
 * simulated data. This separation is crucial for:
 * <ul>
 *   <li>Fast, deterministic unit testing without GUI dependencies</li>
 *   <li>Integration testing with real applications</li>
 *   <li>Gradual migration from mock to live testing</li>
 *   <li>Debugging automation logic without environmental setup</li>
 * </ul>
 * </p>
 * 
 * <h2>Architecture Overview:</h2>
 * <p>ExecutionModeController acts as a facade that:
 * <ol>
 *   <li>Checks the current execution mode via {@link ExecutionMode}</li>
 *   <li>Routes operations to appropriate mock or live implementations</li>
 *   <li>Maintains consistent APIs across both execution modes</li>
 *   <li>Handles mode-specific error conditions gracefully</li>
 * </ol>
 * </p>
 * 
 * <h2>Supported Operations:</h2>
 * <ul>
 *   <li><b>Pattern Finding:</b> Image pattern matching via computer vision or historical data</li>
 *   <li><b>Text Detection:</b> OCR operations or pre-recorded text retrieval</li>
 *   <li><b>Text Extraction:</b> Getting text content from specific matches</li>
 *   <li><b>Time Management:</b> Real or simulated time progression</li>
 *   <li><b>Wait Operations:</b> Actual delays or simulated time advancement</li>
 *   <li><b>Histogram Analysis:</b> Color distribution analysis or mock results</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * <pre>
 * // Finding patterns - same code works in mock or live mode
 * Pattern submitButton = new Pattern("submit.png");
 * List&lt;Match&gt; matches = executionModeController.findAll(submitButton, currentScene);
 * 
 * // Text extraction - automatically uses OCR or historical data
 * Match textField = matches.get(0);
 * executionModeController.setText(textField);
 * String extractedText = textField.getText();
 * 
 * // Time operations - real time or simulated
 * LocalDateTime startTime = executionModeController.now();
 * executionModeController.wait(2.5); // Waits 2.5 seconds or simulates passage of time
 * </pre>
 * 
 * <h2>Configuration:</h2>
 * <p>The execution mode is determined by {@link ExecutionMode#isMock()} and can be
 * configured through:
 * <ul>
 *   <li>System properties</li>
 *   <li>Configuration files</li>
 *   <li>Runtime API calls</li>
 *   <li>Test framework annotations</li>
 * </ul>
 * </p>
 * 
 * <h2>Design Benefits:</h2>
 * <ul>
 *   <li><b>Test Speed:</b> Mock mode runs orders of magnitude faster</li>
 *   <li><b>Reliability:</b> Mock tests are deterministic and environment-independent</li>
 *   <li><b>Flexibility:</b> Easy switching between modes for different test phases</li>
 *   <li><b>Debugging:</b> Mock mode allows testing without GUI setup</li>
 *   <li><b>CI/CD:</b> Mock tests can run in headless environments</li>
 * </ul>
 * 
 * @see ExecutionMode
 * @see MockFind
 * @see MockText
 * @see MockTime
 * @see ScenePatternMatcher
 * @see GetTextWrapper
 * @since 1.0
 */
@Component
public class ExecutionModeController {
    private final ExecutionMode executionMode;
    private final MockFind mockFind;
    private final MockText mockText;
    private final MockColor mockColor; 
    private final MockHistogram mockHistogram;
    private final MockTime mockTime;
    private final ScenePatternMatcher findInScene;
    private final GetTextWrapper getTextWrapper;
    private final SingleRegionHistogramExtractor findHistogramsOneRegionOneImage;

    /**
     * Constructs an ExecutionModeController with all required mock and live implementations.
     * 
     * @param executionMode service determining current execution mode (mock vs live)
     * @param mockFind mock implementation for pattern finding operations
     * @param mockText mock implementation for text extraction operations
     * @param mockColor mock implementation for color-based operations (TODO: implement)
     * @param mockHistogram mock implementation for histogram analysis
     * @param mockTime mock implementation for time operations
     * @param findInScene live implementation for pattern finding
     * @param getTextWrapper live implementation for text extraction
     * @param findHistogramsOneRegionOneImage live implementation for histogram analysis
     */
    public ExecutionModeController(ExecutionMode executionMode,
                      MockFind mockFind, MockText mockText, MockColor mockColor, MockHistogram mockHistogram,
                      MockTime mockTime,
                      ScenePatternMatcher findInScene, GetTextWrapper getTextWrapper,
                      SingleRegionHistogramExtractor findHistogramsOneRegionOneImage) {
        this.executionMode = executionMode;
        this.mockFind = mockFind;
        this.mockText = mockText;
        this.mockColor = mockColor;
        this.mockHistogram = mockHistogram;
        this.mockTime = mockTime;
        this.findInScene = findInScene;
        this.getTextWrapper = getTextWrapper;
        this.findHistogramsOneRegionOneImage = findHistogramsOneRegionOneImage;
    }

    /**
     * Finds all instances of a pattern in the current scene, using mock or live execution.
     * 
     * <p>This method provides transparent pattern finding that works identically in both
     * mock and live modes. In mock mode, it returns historically recorded matches from
     * previous live executions. In live mode, it performs actual computer vision operations
     * to locate the pattern in the current GUI state.</p>
     * 
     * <h3>Mock Mode Behavior:</h3>
     * <ul>
     *   <li>Returns matches from historical snapshots</li>
     *   <li>Respects state context - only finds patterns in active states</li>
     *   <li>Extremely fast execution (milliseconds)</li>
     *   <li>Deterministic results based on recorded data</li>
     * </ul>
     * 
     * <h3>Live Mode Behavior:</h3>
     * <ul>
     *   <li>Performs actual image recognition using computer vision</li>
     *   <li>Searches within the provided scene boundaries</li>
     *   <li>Returns real-time match locations</li>
     *   <li>Execution time depends on scene size and pattern complexity</li>
     * </ul>
     * 
     * @param pattern the image pattern to find, containing the target image and search parameters
     * @param scene the screen region to search within (used in live mode only)
     * @return a list of Match objects representing found instances, empty if none found
     * @see MockFind#getMatches(Pattern)
     * @see ScenePatternMatcher#findAllInScene(Pattern, Scene)
     */
    public List<Match> findAll(Pattern pattern, Scene scene) {
        if (executionMode.isMock()) return mockFind.getMatches(pattern);
        return findInScene.findAllInScene(pattern, scene);
    }

    /**
     * Finds all text/words in the current scene, using mock or live OCR.
     * 
     * <p>Provides unified text detection across mock and live modes. Mock mode returns
     * pre-recorded text matches from active states, while live mode performs actual
     * OCR on the current screen content.</p>
     * 
     * @param scene the screen region to search for text (used in live mode only)
     * @return a list of Match objects containing detected text and locations
     * @see MockFind#getWordMatches()
     * @see ScenePatternMatcher#getWordMatches(Scene)
     */
    public List<Match> findAllWords(Scene scene) {
        if (executionMode.isMock()) return mockFind.getWordMatches();
        return findInScene.getWordMatches(scene);
    }

    /**
     * Extracts and sets text content for a match object, using mock or live OCR.
     * 
     * <p>This method populates the text field of a Match object by either retrieving
     * pre-recorded text (mock mode) or performing OCR on the match region (live mode).
     * The text is stored directly in the provided Match object.</p>
     * 
     * <h3>Implementation Note:</h3>
     * <p>This method modifies the provided Match object by calling its setText() method.
     * In mock mode, it retrieves historically recorded text. In live mode, it performs
     * OCR on the screen region defined by the match.</p>
     * 
     * @param match the Match object to populate with text content
     * @see Match#setText(String)
     * @see MockText#getString(Match)
     * @see GetTextWrapper#setText(Match)
     */
    public void setText(Match match) {
        if (executionMode.isMock()) match.setText(mockText.getString(match));
        else getTextWrapper.setText(match);
    }

    /**
     * Returns the current time, using either simulated or real system time.
     * 
     * <p>In mock mode, returns the simulated time managed by MockTime, which can be
     * controlled for testing time-dependent behavior. In live mode, returns the actual
     * system time. This abstraction allows tests to verify time-based logic without
     * depending on real time passage.</p>
     * 
     * <h3>Mock Time Benefits:</h3>
     * <ul>
     *   <li>Test timeout behavior without actual waiting</li>
     *   <li>Verify time-based state transitions</li>
     *   <li>Ensure consistent timing in test scenarios</li>
     *   <li>Test edge cases around time boundaries</li>
     * </ul>
     * 
     * <p>Note: LocalDateTime is immutable, so the returned value is effectively a snapshot
     * of the current time and won't change even if time advances.</p>
     * 
     * @return the current LocalDateTime from either mock or system clock
     * @see MockTime#now()
     * @see LocalDateTime#now()
     */
    public LocalDateTime now() {
        if (executionMode.isMock()) return mockTime.now();
        return LocalDateTime.now();
    }

    /**
     * Pauses execution for the specified duration, using mock or real waiting.
     * 
     * <p>This method provides unified waiting behavior across execution modes:
     * <ul>
     *   <li><b>Mock mode:</b> Simulates time passage without actual delay</li>
     *   <li><b>Live mode:</b> Performs actual waiting using available mechanisms</li>
     * </ul>
     * </p>
     * 
     * <h3>Live Mode Implementation:</h3>
     * <p>In live mode, the method attempts to use SikuliX's wait mechanism if available,
     * which can be interrupted by GUI events. If SikuliX is not available or disabled,
     * it falls back to Thread.sleep for compatibility.</p>
     * 
     * <h3>Mock Mode Benefits:</h3>
     * <ul>
     *   <li>Tests run without actual delays</li>
     *   <li>Time-dependent logic can be tested instantly</li>
     *   <li>Consistent test execution times</li>
     * </ul>
     * 
     * @param seconds the duration to wait in seconds (can be fractional)
     * @see MockTime#wait(double)
     * @see org.sikuli.script.Region#wait(double)
     */
    public void wait(double seconds) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        if (executionMode.isMock() || env.shouldSkipSikuliX()) {
            mockTime.wait(seconds);
        } else {
            org.sikuli.script.Region sikuliRegion = new Region().sikuli();
            if (sikuliRegion != null) {
                sikuliRegion.wait(seconds);
            } else {
                // Fallback to Thread.sleep when SikuliX not available
                try {
                    Thread.sleep((long) (seconds * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Performs histogram-based pattern matching using color distribution analysis.
     * 
     * <p>Histogram matching is useful for finding images based on color distribution
     * rather than exact pixel matching, making it more robust to minor variations
     * in lighting or rendering. This method routes to either mock histogram data
     * or live histogram analysis based on execution mode.</p>
     * 
     * <h3>Use Cases:</h3>
     * <ul>
     *   <li>Finding UI elements with consistent color schemes</li>
     *   <li>Detecting color-based state changes</li>
     *   <li>Robust matching despite rendering variations</li>
     * </ul>
     * 
     * @param stateImage the reference image containing the target histogram
     * @param sceneHSV the scene image in HSV color space (used in live mode)
     * @param regions the screen regions to search within
     * @return a list of Match objects where histogram similarity exceeds threshold
     * @see MockHistogram#getMockHistogramMatches(StateImage, List)
     * @see SingleRegionHistogramExtractor#findAll(List, StateImage, Mat)
     */
    public List<Match> findHistogram(StateImage stateImage, Mat sceneHSV, List<Region> regions) {
        if (executionMode.isMock()) return mockHistogram.getMockHistogramMatches(stateImage, regions);
        return findHistogramsOneRegionOneImage.findAll(regions, stateImage, sceneHSV);
    }

}
