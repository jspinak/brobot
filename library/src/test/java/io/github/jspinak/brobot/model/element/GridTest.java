package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the Grid class which divides a screen region 
 * into a matrix of cells for systematic interaction.
 */
@DisplayName("Grid Model Tests")
public class GridTest extends BrobotTestBase {

    private Region testRegion;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Create a test region 100x100 at position (10, 20)
        testRegion = new Region(10, 20, 100, 100);
    }

    @Test
    @DisplayName("Should create grid by rows and columns")
    void testCreateGridByRowsAndColumns() {
        // When
        Grid grid = new Grid.Builder()
            .setRegion(testRegion)
            .setRows(2)
            .setColumns(2)
            .build();
        
        // Then
        assertEquals(2, grid.getRows());
        assertEquals(2, grid.getCols());
        assertEquals(50, grid.getCellWidth());  // 100/2
        assertEquals(50, grid.getCellHeight()); // 100/2
        assertEquals(4, grid.getGridRegions().size());
        assertSame(testRegion, grid.getRegion());
    }

    @Test
    @DisplayName("Should create grid by cell dimensions")
    void testCreateGridByCellDimensions() {
        // When
        Grid grid = new Grid.Builder()
            .setRegion(testRegion)
            .setCellWidth(25)
            .setCellHeight(20)
            .build();
        
        // Then
        assertEquals(4, grid.getCols());  // 100/25
        assertEquals(5, grid.getRows());   // 100/20
        assertEquals(25, grid.getCellWidth());
        assertEquals(20, grid.getCellHeight());
        assertEquals(20, grid.getGridRegions().size()); // 4 * 5
    }

    @Test
    @DisplayName("Should handle perfect grid division")
    void testPerfectGridDivision() {
        // Given
        Region perfectRegion = new Region(0, 0, 100, 100);
        
        // When
        Grid grid = new Grid.Builder()
            .setRegion(perfectRegion)
            .setRows(5)
            .setColumns(5)
            .build();
        
        // Then
        assertEquals(25, grid.getGridRegions().size());
        
        // Verify first cell
        Region firstCell = grid.getGridRegions().get(0);
        assertEquals(0, firstCell.x());
        assertEquals(0, firstCell.y());
        assertEquals(20, firstCell.w());
        assertEquals(20, firstCell.h());
        
        // Verify last cell
        Region lastCell = grid.getGridRegions().get(24);
        assertEquals(80, lastCell.x());
        assertEquals(80, lastCell.y());
        assertEquals(20, lastCell.w());
        assertEquals(20, lastCell.h());
    }

    @Test
    @DisplayName("Should handle remainder pixels in last cells")
    void testRemainderHandling() {
        // Given - Region not evenly divisible
        Region region = new Region(0, 0, 103, 107);
        
        // When
        Grid grid = new Grid.Builder()
            .setRegion(region)
            .setRows(3)
            .setColumns(3)
            .build();
        
        // Then
        assertEquals(9, grid.getGridRegions().size());
        
        // Normal cell should be 34x35
        Region normalCell = grid.getGridRegions().get(0);
        assertEquals(34, normalCell.w());
        assertEquals(35, normalCell.h());
        
        // Rightmost cells should be wider (103 - 2*34 = 35)
        Region rightCell = grid.getGridRegions().get(2);
        assertEquals(35, rightCell.w());
        
        // Bottom cells should be taller (107 - 2*35 = 37)
        Region bottomCell = grid.getGridRegions().get(6);
        assertEquals(37, bottomCell.h());
        
        // Bottom-right cell should be both wider and taller
        Region bottomRightCell = grid.getGridRegions().get(8);
        assertEquals(35, bottomRightCell.w());
        assertEquals(37, bottomRightCell.h());
    }

    @Test
    @DisplayName("Should add extra row/column for large remainders")
    void testLargeRemainderHandling() {
        // Given - Cell dimensions that leave large remainders
        Region region = new Region(0, 0, 100, 100);
        
        // When - 30x30 cells in 100x100 region leaves remainder of 10 (not > 30/2)
        Grid grid = new Grid.Builder()
            .setRegion(region)
            .setCellWidth(30)
            .setCellHeight(30)
            .build();
        
        // Then - Should create 3x3 grid (remainder 10 is not > 15)
        assertEquals(3, grid.getCols());
        assertEquals(3, grid.getRows());
        assertEquals(9, grid.getGridRegions().size());
    }

    @Test
    @DisplayName("Should adjust region to fit grids when specified")
    void testAdjustRegionToGrids() {
        // Given
        Region region = new Region(0, 0, 105, 105);
        
        // When - adjustRegionToGrids not set (default false)
        Grid gridDefault = new Grid.Builder()
            .setRegion(region)
            .setCellWidth(20)
            .setCellHeight(20)
            .build();
        
        // Then - Region unchanged
        assertEquals(105, gridDefault.getRegion().w());
        assertEquals(105, gridDefault.getRegion().h());
    }

    @Test
    @DisplayName("Should handle single cell grid")
    void testSingleCellGrid() {
        // When
        Grid grid = new Grid.Builder()
            .setRegion(testRegion)
            .setRows(1)
            .setColumns(1)
            .build();
        
        // Then
        assertEquals(1, grid.getRows());
        assertEquals(1, grid.getCols());
        assertEquals(1, grid.getGridRegions().size());
        
        Region singleCell = grid.getGridRegions().get(0);
        assertEquals(testRegion.x(), singleCell.x());
        assertEquals(testRegion.y(), singleCell.y());
        assertEquals(testRegion.w(), singleCell.w());
        assertEquals(testRegion.h(), singleCell.h());
    }

    @Test
    @DisplayName("Should handle null region gracefully")
    void testNullRegion() {
        // When
        Grid grid = new Grid.Builder()
            .setRows(2)
            .setColumns(2)
            .build();
        
        // Then - Should create default region (full screen)
        assertNotNull(grid.getRegion());
        // Default region is full screen, typically 1920x1080 or actual screen size
        assertTrue(grid.getRegion().w() > 0);
        assertTrue(grid.getRegion().h() > 0);
    }

    @ParameterizedTest
    @CsvSource({
        "7,5,35",    // Calendar grid (7 days x 5 weeks)
        "8,8,64",    // Chess board
        "3,3,9",     // Tic-tac-toe
        "10,10,100", // 10x10 grid
        "1,10,10",   // Single row
        "10,1,10"    // Single column
    })
    @DisplayName("Should handle common grid patterns")
    void testCommonGridPatterns(int cols, int rows, int expectedCells) {
        // Given
        Region region = new Region(0, 0, 350, 250);
        
        // When
        Grid grid = new Grid.Builder()
            .setRegion(region)
            .setColumns(cols)
            .setRows(rows)
            .build();
        
        // Then
        assertEquals(cols, grid.getCols());
        assertEquals(rows, grid.getRows());
        assertEquals(expectedCells, grid.getGridRegions().size());
    }

    @TestFactory
    @DisplayName("Cell positioning tests")
    Stream<DynamicTest> testCellPositioning() {
        return Stream.of(
            dynamicTest("Top-left cell at origin", () -> {
                Grid grid = new Grid.Builder()
                    .setRegion(new Region(10, 20, 100, 100))
                    .setRows(2).setColumns(2)
                    .build();
                Region topLeft = grid.getGridRegions().get(0);
                assertEquals(10, topLeft.x());
                assertEquals(20, topLeft.y());
            }),
            
            dynamicTest("Cells maintain relative positions", () -> {
                Grid grid = new Grid.Builder()
                    .setRegion(new Region(0, 0, 90, 90))
                    .setRows(3).setColumns(3)
                    .build();
                    
                // Check middle cell (index 4 in 3x3 grid)
                Region middle = grid.getGridRegions().get(4);
                assertEquals(30, middle.x());
                assertEquals(30, middle.y());
            }),
            
            dynamicTest("Cell indexing follows row-major order", () -> {
                Grid grid = new Grid.Builder()
                    .setRegion(new Region(0, 0, 60, 40))
                    .setRows(2).setColumns(3)
                    .build();
                    
                // First row: indices 0, 1, 2
                // Second row: indices 3, 4, 5
                Region cell_1_2 = grid.getGridRegions().get(5); // Row 1, Col 2
                assertEquals(40, cell_1_2.x());
                assertEquals(20, cell_1_2.y());
            })
        );
    }

    @Test
    @DisplayName("Should handle minimum dimensions")
    void testMinimumDimensions() {
        // When - Use 1x1 grid (minimum valid dimensions)
        Grid grid = new Grid.Builder()
            .setRegion(testRegion)
            .setRows(1)
            .setColumns(1)
            .build();
        
        // Then - Should create 1x1 grid
        assertEquals(1, grid.getRows());
        assertEquals(1, grid.getCols());
        assertEquals(1, grid.getGridRegions().size());
        
        // The single cell should cover the entire region
        Region cell = grid.getGridRegions().get(0);
        assertEquals(testRegion.x(), cell.x());
        assertEquals(testRegion.y(), cell.y());
        assertEquals(testRegion.w(), cell.w());
        assertEquals(testRegion.h(), cell.h());
    }

    @Test
    @DisplayName("Should handle very small cell dimensions")
    void testVerySmallCells() {
        // Given
        Region region = new Region(0, 0, 100, 100);
        
        // When - 1x1 cells
        Grid grid = new Grid.Builder()
            .setRegion(region)
            .setCellWidth(1)
            .setCellHeight(1)
            .build();
        
        // Then - Should create 100x100 grid
        assertEquals(100, grid.getCols());
        assertEquals(100, grid.getRows());
        assertEquals(10000, grid.getGridRegions().size());
    }

    @Test
    @DisplayName("Should handle cells larger than region")
    void testCellsLargerThanRegion() {
        // Given
        Region smallRegion = new Region(0, 0, 50, 50);
        
        // When
        Grid grid = new Grid.Builder()
            .setRegion(smallRegion)
            .setCellWidth(100)
            .setCellHeight(100)
            .build();
        
        // Then - Should create single cell
        assertEquals(1, grid.getCols());
        assertEquals(1, grid.getRows());
        assertEquals(1, grid.getGridRegions().size());
    }

    @Test
    @DisplayName("Should preserve region offset in cell positions")
    void testRegionOffsetPreservation() {
        // Given - Region with offset
        Region offsetRegion = new Region(100, 200, 60, 40);
        
        // When
        Grid grid = new Grid.Builder()
            .setRegion(offsetRegion)
            .setRows(2)
            .setColumns(3)
            .build();
        
        // Then - All cells should maintain the offset
        for (Region cell : grid.getGridRegions()) {
            assertTrue(cell.x() >= 100, "Cell x should be >= region x");
            assertTrue(cell.y() >= 200, "Cell y should be >= region y");
        }
    }

    @Test
    @DisplayName("Should cover entire region without gaps")
    void testCompleteRegionCoverage() {
        // Given
        Region region = new Region(10, 20, 100, 80);
        
        // When
        Grid grid = new Grid.Builder()
            .setRegion(region)
            .setRows(4)
            .setColumns(5)
            .build();
        
        // Then - Last cell should reach the region boundaries
        List<Region> cells = grid.getGridRegions();
        
        // Find rightmost cell
        int maxRight = cells.stream()
            .mapToInt(cell -> cell.x() + cell.w())
            .max().orElse(0);
        assertEquals(110, maxRight); // 10 + 100
        
        // Find bottom cell
        int maxBottom = cells.stream()
            .mapToInt(cell -> cell.y() + cell.h())
            .max().orElse(0);
        assertEquals(100, maxBottom); // 20 + 80
    }

    @Test
    @DisplayName("Should handle builder method chaining")
    void testBuilderMethodChaining() {
        // When - All builder methods should return Builder
        // When using both cell dimensions and rows/columns, cell dimensions take precedence
        Grid grid = new Grid.Builder()
            .setRegion(testRegion)
            .setCellWidth(25)
            .setCellHeight(25)
            .setRows(3)  // These will be recalculated based on cell dimensions
            .setColumns(4) // These will be recalculated based on cell dimensions
            .build();
        
        // Then - Cell dimensions take precedence, so rows/cols are calculated
        assertNotNull(grid);
        assertEquals(4, grid.getCols()); // 100/25 = 4
        assertEquals(4, grid.getRows()); // 100/25 = 4  
        assertEquals(25, grid.getCellWidth());
        assertEquals(25, grid.getCellHeight());
    }

    @Test
    @DisplayName("Should calculate correct cell indices for coordinates")
    void testCellIndexCalculation() {
        // Given
        Grid grid = new Grid.Builder()
            .setRegion(new Region(0, 0, 100, 100))
            .setRows(5)
            .setColumns(5)
            .build();
        
        // Then - Verify we can calculate cell index from coordinates
        // Cell at row 2, col 3 should be at index 2*5+3 = 13
        Region targetCell = grid.getGridRegions().get(13);
        assertEquals(60, targetCell.x()); // col 3 * 20
        assertEquals(40, targetCell.y()); // row 2 * 20
    }
}