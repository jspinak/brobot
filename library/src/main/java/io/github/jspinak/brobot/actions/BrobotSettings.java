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

    public static boolean saveSnapshots = false; // should be initialized to false in Brobot 1.0

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
     * Capture Settings
     */
    public static String screenshotPath = "screenshots/";
    public static String screenshotFilename = "screen";

    /**
     * Write Settings
     */
    public static String packageName = "com.example";
}
