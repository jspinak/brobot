package io.github.jspinak.brobot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized configuration properties for the Brobot framework.
 * 
 * <p>This class provides a modern, Spring-based approach to framework configuration,
 * replacing static fields with configurable properties that can be set via
 * application.properties, application.yml, or environment variables.</p>
 * 
 * <p>Default values are loaded from brobot-defaults.properties in the library.
 * Applications can override any of these defaults in their own configuration files.</p>
 * 
 * <p>Properties are organized into logical groups:
 * <ul>
 *   <li><b>Core</b>: Essential framework settings like image paths and mock mode</li>
 *   <li><b>Mouse</b>: Mouse action timing and behavior configuration</li>
 *   <li><b>Mock</b>: Simulated execution timings for testing</li>
 *   <li><b>Screenshot</b>: Screen capture and history settings</li>
 *   <li><b>Illustration</b>: Visual feedback and annotation settings</li>
 *   <li><b>Analysis</b>: Color profiling and k-means clustering settings</li>
 *   <li><b>Recording</b>: Screen recording configuration</li>
 *   <li><b>Dataset</b>: AI training data generation settings</li>
 *   <li><b>Testing</b>: Test execution configuration</li>
 * </ul>
 * </p>
 * 
 * <p>Example configuration in application.yml:
 * <pre>{@code
 * brobot:
 *   core:
 *     image-path: classpath:images/
 *     mock: false
 *     headless: false
 *   mouse:
 *     move-delay: 0.5
 *     pause-before-down: 0.0
 *   screenshot:
 *     save-snapshots: true
 *     path: screenshots/
 * }</pre>
 * </p>
 * 
 * @since 1.1.0
 */
@ConfigurationProperties(prefix = "brobot")
@Getter
@Setter
public class BrobotProperties {
    
    /**
     * Core framework settings
     */
    private Core core = new Core();
    
    /**
     * Mouse action configuration
     */
    private Mouse mouse = new Mouse();
    
    /**
     * Mock mode timing configuration
     */
    private Mock mock = new Mock();
    
    /**
     * Screenshot and history settings
     */
    private Screenshot screenshot = new Screenshot();
    
    /**
     * Action illustration settings
     */
    private Illustration illustration = new Illustration();
    
    /**
     * Color analysis settings
     */
    private Analysis analysis = new Analysis();
    
    /**
     * Screen recording settings
     */
    private Recording recording = new Recording();
    
    /**
     * AI dataset generation settings
     */
    private Dataset dataset = new Dataset();
    
    /**
     * Test execution settings
     */
    private Testing testing = new Testing();
    
    @Getter
    @Setter
    public static class Core {
        /**
         * Path to image resources. Supports:
         * - Classpath: "classpath:images/"
         * - Absolute: "/home/user/images/"
         * - Relative: "images/"
         */
        private String imagePath = "images";
        
        /**
         * Enable mock mode for simulated execution
         */
        private boolean mock = false;
        
        /**
         * Run in headless mode (no GUI)
         */
        private boolean headless = false;
        
        /**
         * Default package name for generated code
         */
        private String packageName = "com.example";
    }
    
    @Getter
    @Setter
    public static class Mouse {
        /**
         * Delay in seconds for mouse movement
         */
        private float moveDelay = 0.5f;
        
        /**
         * Pause before mouse down action (seconds)
         */
        private double pauseBeforeDown = 0.0;
        
        /**
         * Pause after mouse down action (seconds)
         */
        private double pauseAfterDown = 0.0;
        
        /**
         * Pause before mouse up action (seconds)
         */
        private double pauseBeforeUp = 0.0;
        
        /**
         * Pause after mouse up action (seconds)
         */
        private double pauseAfterUp = 0.0;
        
        /**
         * X offset after mouse down (pixels)
         */
        private int xMoveAfterDown = 0;
        
        /**
         * Y offset after mouse down (pixels)
         */
        private int yMoveAfterDown = 0;
    }
    
    @Getter
    @Setter
    public static class Mock {
        /**
         * Simulated time for find first operation (seconds)
         */
        private double timeFindFirst = 0.1;
        
        /**
         * Simulated time for find all operation (seconds)
         */
        private double timeFindAll = 0.2;
        
        /**
         * Simulated time for drag operation (seconds)
         */
        private double timeDrag = 0.3;
        
        /**
         * Simulated time for click operation (seconds)
         */
        private double timeClick = 0.05;
        
        /**
         * Simulated time for move operation (seconds)
         */
        private double timeMove = 0.1;
        
        /**
         * Simulated time for histogram find (seconds)
         */
        private double timeFindHistogram = 0.3;
        
        /**
         * Simulated time for color find (seconds)
         */
        private double timeFindColor = 0.3;
        
        /**
         * Simulated time for classify operation (seconds)
         */
        private double timeClassify = 0.4;
    }
    
    @Getter
    @Setter
    public static class Screenshot {
        /**
         * Enable screenshot saving
         */
        private boolean saveSnapshots = false;
        
        /**
         * Enable history saving with illustrations
         */
        private boolean saveHistory = false;
        
        /**
         * Path for screenshots
         */
        private String path = "screenshots/";
        
        /**
         * Filename prefix for screenshots
         */
        private String filename = "screen";
        
        /**
         * Path for history screenshots
         */
        private String historyPath = "history/";
        
        /**
         * Filename prefix for history
         */
        private String historyFilename = "hist";
        
        /**
         * Test screenshots for unit tests
         */
        private List<String> testScreenshots = new ArrayList<>();
        
        /**
         * Path for test screenshots
         */
        private String testPath = "screenshots/";
    }
    
    @Getter
    @Setter
    public static class Illustration {
        /**
         * Draw find results
         */
        private boolean drawFind = true;
        
        /**
         * Draw click locations
         */
        private boolean drawClick = true;
        
        /**
         * Draw drag paths
         */
        private boolean drawDrag = true;
        
        /**
         * Draw move paths
         */
        private boolean drawMove = true;
        
        /**
         * Draw highlight regions
         */
        private boolean drawHighlight = true;
        
        /**
         * Draw repeated actions
         */
        private boolean drawRepeatedActions = true;
        
        /**
         * Draw classify results
         */
        private boolean drawClassify = true;
        
        /**
         * Draw define regions
         */
        private boolean drawDefine = true;
    }
    
    @Getter
    @Setter
    public static class Analysis {
        /**
         * Default k value for k-means clustering
         */
        private int kMeansInProfile = 5;
        
        /**
         * Maximum k value to store
         */
        private int maxKMeansToStore = 10;
        
        /**
         * Initialize profiles for static images
         */
        private boolean initStaticProfiles = false;
        
        /**
         * Initialize profiles for dynamic images
         */
        private boolean initDynamicProfiles = false;
        
        /**
         * Include state objects in scene analysis
         */
        private boolean includeStateObjects = true;
    }
    
    @Getter
    @Setter
    public static class Recording {
        /**
         * Maximum recording duration (seconds)
         */
        private int secondsToCapture = 1000;
        
        /**
         * Capture frequency (frames per second)
         */
        private int captureFrequency = 1;
        
        /**
         * Recording folder path
         */
        private String folder = "recording";
    }
    
    @Getter
    @Setter
    public static class Dataset {
        /**
         * Enable dataset building
         */
        private boolean build = false;
        
        /**
         * Dataset storage path
         */
        private String path = "dataset/";
    }
    
    @Getter
    @Setter
    public static class Testing {
        /**
         * Current test iteration
         */
        private int iteration = 1;
        
        /**
         * Send logs to external systems
         */
        private boolean sendLogs = true;
    }
    
    /**
     * Updates the static FrameworkSettings with values from this configuration.
     * This provides backward compatibility during migration.
     */
    public void applyToFrameworkSettings() {
        // Core settings
        FrameworkSettings.mock = core.mock;
        FrameworkSettings.packageName = core.packageName;
        
        // Mouse settings
        FrameworkSettings.moveMouseDelay = mouse.moveDelay;
        FrameworkSettings.pauseBeforeMouseDown = mouse.pauseBeforeDown;
        FrameworkSettings.pauseAfterMouseDown = mouse.pauseAfterDown;
        FrameworkSettings.pauseBeforeMouseUp = mouse.pauseBeforeUp;
        FrameworkSettings.pauseAfterMouseUp = mouse.pauseAfterUp;
        FrameworkSettings.xMoveAfterMouseDown = mouse.xMoveAfterDown;
        FrameworkSettings.yMoveAfterMouseDown = mouse.yMoveAfterDown;
        
        // Mock timings
        FrameworkSettings.mockTimeFindFirst = mock.timeFindFirst;
        FrameworkSettings.mockTimeFindAll = mock.timeFindAll;
        FrameworkSettings.mockTimeDrag = mock.timeDrag;
        FrameworkSettings.mockTimeClick = mock.timeClick;
        FrameworkSettings.mockTimeMove = mock.timeMove;
        FrameworkSettings.mockTimeFindHistogram = mock.timeFindHistogram;
        FrameworkSettings.mockTimeFindColor = mock.timeFindColor;
        FrameworkSettings.mockTimeClassify = mock.timeClassify;
        
        // Screenshot settings
        FrameworkSettings.saveSnapshots = screenshot.saveSnapshots;
        FrameworkSettings.saveHistory = screenshot.saveHistory;
        FrameworkSettings.screenshotPath = screenshot.path;
        FrameworkSettings.screenshotFilename = screenshot.filename;
        FrameworkSettings.historyPath = screenshot.historyPath;
        FrameworkSettings.historyFilename = screenshot.historyFilename;
        FrameworkSettings.screenshots = screenshot.testScreenshots;
        FrameworkSettings.testScreenshotsPath = screenshot.testPath;
        
        // Illustration settings
        FrameworkSettings.drawFind = illustration.drawFind;
        FrameworkSettings.drawClick = illustration.drawClick;
        FrameworkSettings.drawDrag = illustration.drawDrag;
        FrameworkSettings.drawMove = illustration.drawMove;
        FrameworkSettings.drawHighlight = illustration.drawHighlight;
        FrameworkSettings.drawRepeatedActions = illustration.drawRepeatedActions;
        FrameworkSettings.drawClassify = illustration.drawClassify;
        FrameworkSettings.drawDefine = illustration.drawDefine;
        
        // Analysis settings
        FrameworkSettings.kMeansInProfile = analysis.kMeansInProfile;
        FrameworkSettings.maxKMeansToStoreInProfile = analysis.maxKMeansToStore;
        FrameworkSettings.initProfilesForStaticfImages = analysis.initStaticProfiles;
        FrameworkSettings.initProfilesForDynamicImages = analysis.initDynamicProfiles;
        FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis = analysis.includeStateObjects;
        
        // Recording settings
        FrameworkSettings.secondsToCapture = recording.secondsToCapture;
        FrameworkSettings.captureFrequency = recording.captureFrequency;
        FrameworkSettings.recordingFolder = recording.folder;
        
        // Dataset settings
        FrameworkSettings.buildDataset = dataset.build;
        FrameworkSettings.datasetPath = dataset.path;
        
        // Testing settings
        FrameworkSettings.testIteration = testing.iteration;
        FrameworkSettings.sendLogs = testing.sendLogs;
    }
}