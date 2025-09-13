package io.github.jspinak.brobot.config.core;

import java.util.ArrayList;
import java.util.List;

import io.github.jspinak.brobot.runner.project.AutomationProject;

/**
 * Global configuration settings for the Brobot model-based GUI automation framework.
 *
 * <p>FrameworkSettings serves as the central configuration repository for the entire framework,
 * controlling behavior across all components from action execution to data collection. These
 * settings enable customization of the framework's operation without modifying code, supporting
 * different deployment scenarios from development to production.
 *
 * <p>Setting categories:
 *
 * <ul>
 *   <li><b>Mouse Control</b>: Fine-tune mouse action timing and movement for different application
 *       response characteristics
 *   <li><b>Mock Mode</b>: Configure simulated execution for testing without GUI interaction
 *   <li><b>Data Collection</b>: Control screenshot capture, history recording, and dataset building
 *       for analysis and machine learning
 *   <li><b>Visual Analysis</b>: Configure color profiling, k-means clustering, and image processing
 *       parameters
 *   <li><b>Testing Support</b>: Settings for unit tests and application testing scenarios
 * </ul>
 *
 * <p>Key configuration areas:
 *
 * <ul>
 *   <li><b>Execution Timing</b>: Pause durations before/after mouse actions to accommodate varying
 *       GUI response times
 *   <li><b>Mock Timings</b>: Simulated execution times for different action types when running in
 *       mock mode
 *   <li><b>File Paths</b>: Locations for screenshots, history, datasets, and recordings
 *   <li><b>Visualization</b>: Control which actions are illustrated in history screenshots
 *   <li><b>Machine Learning</b>: Settings for building training datasets from automation runs
 * </ul>
 *
 * <p>Usage patterns:
 *
 * <ul>
 *   <li>Configure once at application startup based on environment
 *   <li>Override specific settings for testing scenarios
 *   <li>Some settings override underlying SikuliX configurations
 *   <li>Settings are static and affect all automation instances globally
 * </ul>
 *
 * <p>In the model-based approach, FrameworkSettings enables the framework to adapt to different
 * environments and use cases without code changes. This configurability is essential for creating
 * robust automation that can handle variations in application behavior, system performance, and
 * deployment requirements.
 *
 * @since 1.0
 * @see ActionOptions
 * @see AutomationProject
 */
public class FrameworkSettings {

    /** Mouse Control - Configuration for mouse action timing and behavior */
    public static float moveMouseDelay = 0.5f;

    /**
     * Delay in seconds before initiating a mouse down action. Allows GUI elements to stabilize
     * before interaction begins.
     */
    public static double pauseBeforeMouseDown = 0;

    /**
     * Delay in seconds after completing a mouse down action. Provides time for the GUI to react to
     * the press event.
     */
    public static double pauseAfterMouseDown = 0;

    /**
     * Delay in seconds before initiating a mouse up action. Used in drag operations to ensure
     * stable drag state.
     */
    public static double pauseBeforeMouseUp = 0;

    /**
     * Delay in seconds after completing a mouse up action. Allows GUI to process the release event
     * before continuing.
     */
    public static double pauseAfterMouseUp = 0;

    /**
     * Horizontal offset in pixels to move the mouse after pressing down. Used for drag initiation
     * or gesture recognition.
     */
    public static int xMoveAfterMouseDown = 0;

    /**
     * Vertical offset in pixels to move the mouse after pressing down. Used for drag initiation or
     * gesture recognition.
     */
    public static int yMoveAfterMouseDown = 0;

    /**
     * Controls whether screenshots are saved during automation execution.
     *
     * <p>When true, captures are stored for later analysis or dataset building. Should be
     * initialized to false in Brobot 1.x versions (not using a database). Screenshots are saved to
     * the path specified by {@link #screenshotPath}.
     */
    public static boolean saveSnapshots = false;

    /**
     * Controls whether action history with illustrated screenshots is saved.
     *
     * <p>When true, saves annotated screenshots showing action results to the path specified by
     * {@link #historyPath}. Ensure the history folder exists before enabling this setting to avoid
     * runtime errors.
     *
     * @see #historyPath
     * @see #historyFilename
     */
    public static boolean saveHistory = false;

    /** Mock Settings - Control simulated execution for testing without GUI interaction */

    /**
     * Enables mock mode for simulated action execution.
     *
     * <p>When true, actions are simulated rather than performed on the actual GUI. Useful for unit
     * testing, development, and scenarios where GUI interaction is not possible or desired.
     */
    public static boolean mock = false;

    /**
     * Simulated execution time in seconds for finding the first match. Used in mock mode to
     * approximate real find operation timing.
     */
    public static double mockTimeFindFirst = 0.1;

    /**
     * Simulated execution time in seconds for finding all matches. Typically longer than findFirst
     * to reflect scanning entire search area.
     */
    public static double mockTimeFindAll = 0.2;

    /**
     * Simulated execution time in seconds for drag operations. Reflects the combined time for
     * press, move, and release actions.
     */
    public static double mockTimeDrag = 0.3;

    /**
     * Simulated execution time in seconds for click operations. Represents a simple mouse click
     * action timing.
     */
    public static double mockTimeClick = 0.05;

    /**
     * Simulated execution time in seconds for mouse move operations. Used when moving the mouse
     * without clicking.
     */
    public static double mockTimeMove = 0.1;

    /**
     * Simulated execution time in seconds for histogram-based find operations. Reflects the
     * additional processing time for color histogram analysis.
     */
    public static double mockTimeFindHistogram = 0.3;

    /**
     * Simulated execution time in seconds for color-based find operations. Accounts for
     * pixel-by-pixel color matching algorithms.
     */
    public static double mockTimeFindColor = 0.3;

    /**
     * Simulated execution time in seconds for classification operations. Represents machine
     * learning inference timing.
     */
    public static double mockTimeClassify = 0.4;

    /**
     * Probability of action success in mock mode (0.0 to 1.0).
     *
     * <p>Controls how often simulated actions succeed:
     *
     * <ul>
     *   <li>1.0 = All actions always succeed (default)
     *   <li>0.95 = 95% success rate (realistic simulation)
     *   <li>0.5 = 50% success rate (stress testing)
     *   <li>0.0 = All actions always fail (failure testing)
     * </ul>
     *
     * <p>This applies to actions like click, type, and drag. Find operations still require
     * ActionSnapshots for proper match simulation.
     *
     * @since 1.2.0
     */
    public static double mockActionSuccessProbability = 1.0;

    /** Unit Tests - Configuration for test execution */

    /**
     * List of screenshot filenames to use instead of live screen capture during tests.
     *
     * <p>When populated, Find and Highlight operations will search within these images rather than
     * capturing the current screen. Images are loaded from {@link #screenshotPath}. The filenames
     * do not need to start with the value in {@link #screenshotFilename}.
     *
     * <p>This enables deterministic testing by using known screen states.
     *
     * @see #screenshotPath
     */
    public static List<String> screenshots = new ArrayList<>();

    /** Screenshot Capture Settings - Control where and how screenshots are saved */

    /**
     * Base directory path for storing screenshots used by the StateStructure builder.
     *
     * <p>This path is relative to the application's working directory. Ensure the directory exists
     * or can be created before capturing screenshots.
     */
    public static String screenshotPath = "screenshots/";

    /**
     * Filename prefix for StateStructure builder screenshots.
     *
     * <p>Only files starting with this prefix will be processed by the StateStructure builder. This
     * allows separation of builder screenshots from other images in the same directory.
     */
    public static String screenshotFilename = "screen";

    /** Write Settings - Configuration for code generation */

    /**
     * Default package name for generated Java code.
     *
     * <p>Used when the framework generates code files, such as state definitions or automation
     * scripts. Should follow Java package naming conventions.
     */
    public static String packageName = "com.example";

    /** Illustrated Screenshots Settings - Control visualization of action results */

    /**
     * Directory path for saving illustrated history screenshots.
     *
     * <p>These screenshots are annotated with visual indicators showing where actions were
     * performed. Path is relative to the application directory.
     *
     * @see #saveHistory
     */
    public static String historyPath = "history/";

    /** Filename prefix for history screenshots. Files are typically named as: hist_timestamp.png */
    public static String historyFilename = "hist";

    /**
     * Controls whether Find action results are drawn on history screenshots. When true, highlights
     * regions where patterns were found.
     */
    public static boolean drawFind = true;

    /**
     * Controls whether Click action locations are marked on history screenshots. When true, shows
     * click points with visual indicators.
     */
    public static boolean drawClick = true;

    /**
     * Controls whether Drag action paths are illustrated on history screenshots. When true, draws
     * lines showing drag start and end points.
     */
    public static boolean drawDrag = true;

    /**
     * Controls whether Move action paths are shown on history screenshots. When true, illustrates
     * mouse movement trajectories.
     */
    public static boolean drawMove = true;

    /**
     * Controls whether Highlight action regions are shown on history screenshots. When true,
     * preserves the highlight rectangles in the saved image.
     */
    public static boolean drawHighlight = true;

    /**
     * Controls whether repeated actions are drawn multiple times. When false, only the first
     * occurrence of repeated actions is illustrated.
     */
    public static boolean drawRepeatedActions = true;

    /**
     * Controls whether Classify action results are visualized. When true, shows classification
     * regions and labels.
     */
    public static boolean drawClassify = true;

    /**
     * Controls whether Define action regions are illustrated. When true, shows the newly defined
     * regions with boundaries.
     */
    public static boolean drawDefine = true;

    /** K-Means Settings - Configure color clustering for image analysis */

    /**
     * Default number of clusters (k) for k-means color profiling.
     *
     * <p>Used when analyzing image color distributions. Higher values provide more detailed color
     * profiles but increase processing time.
     */
    public static int kMeansInProfile = 5;

    /**
     * Maximum k value for which to pre-compute and store k-means profiles.
     *
     * <p>When processing DynamicImages, k-means profiles for k values from 2 to this maximum are
     * computed and stored. This enables fast color-based matching with different clustering
     * granularities.
     *
     * <p>Higher values increase memory usage but provide more matching options.
     */
    public static int maxKMeansToStoreInProfile = 10;

    /** Color Settings - Control color profile initialization and analysis */

    /**
     * Controls whether color profiles are pre-computed for static images.
     *
     * <p>When true, color analysis is performed on static images during initialization. This
     * increases startup time but improves runtime performance for color-based matching.
     */
    public static boolean initProfilesForStaticfImages = false;

    /**
     * Controls whether color profiles are pre-computed for dynamic images.
     *
     * <p>When true, generates k-means profiles for dynamic images that change during execution.
     * Enables adaptive color-based matching for variable content.
     */
    public static boolean initProfilesForDynamicImages = false;

    /**
     * Controls whether images from active states are included in scene analysis.
     *
     * <p>When true, the analysis considers all visible state objects, not just the primary search
     * targets. This provides more comprehensive scene understanding but may increase processing
     * time.
     */
    public static boolean includeStateImageObjectsFromActiveStatesInAnalysis = true;

    /**
     * Capture Settings - Configure screen recording for capture and replay
     *
     * <p>These settings are used for both capture and replay operations, ensuring consistency
     * between recording and playback.
     */

    /**
     * Maximum duration in seconds for screen capture sessions. Acts as a safety limit to prevent
     * indefinite recording.
     */
    public static int secondsToCapture = 1000;

    /**
     * Frequency of screen captures per second (frames per second). Higher values provide smoother
     * replay but increase storage requirements.
     */
    public static int captureFrequency = 1;

    /**
     * Directory name for storing screen recordings. Created relative to the application's working
     * directory.
     */
    public static String recordingFolder = "recording";

    /** AI Settings - Configure machine learning dataset generation */

    /**
     * Controls whether to build training datasets during automation execution.
     *
     * <p>When true, captures and saves:
     *
     * <ul>
     *   <li>Screenshot before each action
     *   <li>Action representation as a feature vector
     *   <li>Human-readable text description of the action
     *   <li>Screenshot after action completion
     * </ul>
     *
     * <p>This data can be used to train machine learning models for automated GUI interaction or
     * action prediction.
     */
    public static boolean buildDataset = false;

    /**
     * Directory path for storing generated training datasets. Dataset files are organized by
     * timestamp and action type.
     */
    public static String datasetPath = "dataset/";

    /** Application Under Test (AUT) Settings - Configure test execution */

    /**
     * Current iteration number for test runs.
     *
     * <p>Used to track multiple test executions and can be incorporated into log files and reports
     * for test run identification.
     */
    public static int testIteration = 1;

    /**
     * Controls whether test execution logs are sent to external systems.
     *
     * <p>When true, test results and execution logs can be transmitted to monitoring systems or
     * test management platforms.
     */
    public static boolean sendLogs = true;

    /** Test Screenshots - Configuration for unit test image resources */

    /**
     * Base directory path for screenshots used in unit tests.
     *
     * <p>Test screenshots provide consistent, reproducible screen states for automated testing.
     * Path is relative to the test resources directory.
     */
    public static String testScreenshotsPath = "screenshots/";

    /** State Comparison Settings - Control state probability and comparison */

    /**
     * Minimum probability threshold for state transitions.
     *
     * <p>States with probability below this threshold are not considered valid for transitions.
     * Range: 0.0 to 1.0, where 1.0 requires absolute certainty.
     */
    public static double minimumStateProbabilityThreshold = 0.7;

    /**
     * Controls whether state comparison is performed during action execution.
     *
     * <p>When true, compares expected and actual states to verify successful transitions. Useful
     * for debugging state management but adds overhead to execution.
     */
    public static boolean enableStateComparison = false;

    /**
     * Controls whether state comparison results are logged.
     *
     * <p>When true, outputs detailed state comparison information to logs. Only effective when
     * enableStateComparison is also true.
     */
    public static boolean logStateComparison = false;
}
