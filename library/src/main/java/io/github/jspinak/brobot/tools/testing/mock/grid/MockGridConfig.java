package io.github.jspinak.brobot.tools.testing.mock.grid;

/**
 * Configuration for grid operations in mock mode. Provides default grid dimensions when SikuliX is
 * not available.
 *
 * <p>The default 3x3 grid matches SikuliX's default raster configuration. Tests can override these
 * values for specific test scenarios.
 */
public class MockGridConfig {

    private static int defaultRows = 3;
    private static int defaultCols = 3;

    /**
     * Sets the default grid dimensions for mock mode. These dimensions are used when calculating
     * grid operations without SikuliX support.
     *
     * @param rows Number of rows in the grid (minimum 1)
     * @param cols Number of columns in the grid (minimum 1)
     * @throws IllegalArgumentException if rows or cols is less than 1
     */
    public static void setDefaultGrid(int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException(
                    String.format(
                            "Grid dimensions must be positive: rows=%d, cols=%d", rows, cols));
        }
        defaultRows = rows;
        defaultCols = cols;
    }

    /**
     * @return The default number of rows for mock grid operations
     */
    public static int getDefaultRows() {
        return defaultRows;
    }

    /**
     * @return The default number of columns for mock grid operations
     */
    public static int getDefaultCols() {
        return defaultCols;
    }

    /** Resets grid dimensions to the default 3x3 configuration. Useful for test cleanup. */
    public static void reset() {
        defaultRows = 3;
        defaultCols = 3;
    }

    /**
     * Calculates the total number of cells in the default grid.
     *
     * @return rows * columns
     */
    public static int getTotalCells() {
        return defaultRows * defaultCols;
    }

    /**
     * Validates if a grid number is valid for the current configuration.
     *
     * @param gridNumber The grid number to validate (0-based)
     * @return true if the grid number is within valid bounds
     */
    public static boolean isValidGridNumber(int gridNumber) {
        return gridNumber >= 0 && gridNumber < getTotalCells();
    }
}
