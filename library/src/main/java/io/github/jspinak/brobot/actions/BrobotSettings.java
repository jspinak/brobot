package io.github.jspinak.brobot.actions;

import java.util.ArrayList;
import java.util.List;

/**
 * Global settings
 * Some Brobot settings override Sikuli settings.
 */
public class BrobotSettings {

    public static double delayAfterMouseDown = 0;
    public static double delayBeforeMouseUp = 0;
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
    public static boolean sendLogsToElasticContainer = false;
}
