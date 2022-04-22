package io.github.jspinak.brobot.actions;

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
    public static double mockTimeGetText = 0.1;
    /**
     * Unit Tests
     */
    /*
    When this has a value, it is used instead of the screen for Find and Highlight operations.
    It uses the screenshotPath and then this value as the filename.
    The name does not have to start with the value in 'screenshotFilename'.
     */
    public static String screenshot = "";

    /**
     * Capture Settings
     */
    public static String screenshotPath = "screenshots/"; // screenshots for the StateStructure builder
    public static String historyPath = "history/"; // where screenshots are saved for the illustrated history
    /*
    Files that do not start with this variable's value will not be used for the StateStructure builder.
    This field is also used for the filenames in the illustrated history.
     */
    public static String screenshotFilename = "screen";
    public static String historyFilename = "hist";

    /**
     * Write Settings
     */
    public static String packageName = "com.example";
}
