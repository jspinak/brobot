package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.datatypes.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Global configuration settings for the Brobot model-based GUI automation framework.
 * 
 * <p>BrobotSettings serves as the central configuration repository for the entire framework, 
 * controlling behavior across all components from action execution to data collection. These 
 * settings enable customization of the framework's operation without modifying code, supporting 
 * different deployment scenarios from development to production.</p>
 * 
 * <p>Setting categories:
 * <ul>
 *   <li><b>Mouse Control</b>: Fine-tune mouse action timing and movement for different 
 *       application response characteristics</li>
 *   <li><b>Mock Mode</b>: Configure simulated execution for testing without GUI interaction</li>
 *   <li><b>Data Collection</b>: Control screenshot capture, history recording, and dataset 
 *       building for analysis and machine learning</li>
 *   <li><b>Visual Analysis</b>: Configure color profiling, k-means clustering, and image 
 *       processing parameters</li>
 *   <li><b>Testing Support</b>: Settings for unit tests and application testing scenarios</li>
 * </ul>
 * </p>
 * 
 * <p>Key configuration areas:
 * <ul>
 *   <li><b>Execution Timing</b>: Pause durations before/after mouse actions to accommodate 
 *       varying GUI response times</li>
 *   <li><b>Mock Timings</b>: Simulated execution times for different action types when 
 *       running in mock mode</li>
 *   <li><b>File Paths</b>: Locations for screenshots, history, datasets, and recordings</li>
 *   <li><b>Visualization</b>: Control which actions are illustrated in history screenshots</li>
 *   <li><b>Machine Learning</b>: Settings for building training datasets from automation runs</li>
 * </ul>
 * </p>
 * 
 * <p>Usage patterns:
 * <ul>
 *   <li>Configure once at application startup based on environment</li>
 *   <li>Override specific settings for testing scenarios</li>
 *   <li>Some settings override underlying SikuliX configurations</li>
 *   <li>Settings are static and affect all automation instances globally</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, BrobotSettings enables the framework to adapt to different 
 * environments and use cases without code changes. This configurability is essential for 
 * creating robust automation that can handle variations in application behavior, system 
 * performance, and deployment requirements.</p>
 * 
 * @since 1.0
 * @see ActionOptions
 * @see Project
 */
public class BrobotSettings {

    public static double pauseBeforeMouseDown = 0;
    public static double pauseAfterMouseDown = 0;
    public static double pauseBeforeMouseUp = 0;
    public static double pauseAfterMouseUp = 0;
    public static int xMoveAfterMouseDown = 0;
    public static int yMoveAfterMouseDown = 0;

    public static boolean saveSnapshots = false; // should be initialized to false in Brobot 1.xx (any version not using a database)
    public static boolean saveHistory = false; // when set to true, make sure you have a folder corresponding to BrobotSettings.historyPath

    /**
     * Mock Settings
     */
    public static boolean mock = false;
    public static double mockTimeFindFirst = 0.1;
    public static double mockTimeFindAll = 0.2;
    public static double mockTimeDrag = 0.3;
    public static double mockTimeClick = 0.05;
    public static double mockTimeMove = 0.1;
    public static double mockTimeFindHistogram = 0.3;
    public static double mockTimeFindColor = 0.3;
    public static double mockTimeClassify = 0.4;
    /**
     * Unit Tests
     */
    /*
    When this has a value, it is used instead of the screen for Find and Highlight operations.
    It uses the screenshotPath and then this value as the filename.
    The name does not have to start with the value in 'screenshotFilename'.
     */
    public static List<String> screenshots = new ArrayList<>();

    /**
     * Screenshot Capture Settings
     */
    public static String screenshotPath = "screenshots/"; // screenshots for the StateStructure builder
    /*
    Files that do not start with this variable's value will not be used for the StateStructure builder.
     */
    public static String screenshotFilename = "screen";

    /**
     * Write Settings
     */
    public static String packageName = "com.example";

    /**
     * Illustrated Screenshots Settings (for displaying Action results)
     */
    public static String historyPath = "history/"; // where screenshots are saved for the illustrated history
    public static String historyFilename = "hist";
    public static boolean drawFind = true;
    public static boolean drawClick = true;
    public static boolean drawDrag = true;
    public static boolean drawMove = true;
    public static boolean drawHighlight = true;
    public static boolean drawRepeatedActions = true;
    public static boolean drawClassify = true;
    public static boolean drawDefine = true;

    /**
     * kMeans Settings
     */
    public static int kMeansInProfile = 5;

    /*
    When a Brobot application is run, kMeans profiles for means = {2, ..., maxKMeansToStoreInProfile} are stored
    with StateImages that have DynamicImages.
     */
    public static int maxKMeansToStoreInProfile = 10;

    /**
     * Color Settings
     */
    public static boolean initProfilesForStaticfImages = false;
    public static boolean initProfilesForDynamicImages = false;

    public static boolean includeStateImageObjectsFromActiveStatesInAnalysis = true;

    /**
     * Capture Settings
     * These are used for both capture and replay, so it's best to have them in a central location.
     */
    public static int secondsToCapture = 1000;
    public static int captureFrequency = 1;
    public static String recordingFolder = "recording";

    /**
     * AI Settings
     */
    /*
    Setting buildDataset to true will save
    - the 'before' screenshot
    - the action as a vector
    - a text description of the action
    - the 'after' screenshot
     */
    public static boolean buildDataset = false;
    public static String datasetPath = "dataset/";

    /**
     * AUT Testing
     */
    public static int testIteration = 1;
    public static boolean sendLogs = true;
    
    /**
     * Test Screenshots
     */
    public static String testScreenshotsPath = "screenshots/"; // base path for test screenshots used in unit tests

}
