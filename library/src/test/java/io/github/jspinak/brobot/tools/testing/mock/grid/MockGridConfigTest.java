package io.github.jspinak.brobot.tools.testing.mock.grid;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MockGridConfig and grid operations in mock mode.
 */
@DisplayName("MockGridConfig Tests")
public class MockGridConfigTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Reset to default 3x3 grid before each test
        MockGridConfig.reset();
    }
    
    @AfterEach
    public void tearDown() {
        // Always reset after tests to avoid affecting other tests
        MockGridConfig.reset();
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Default configuration is 3x3")
        public void testDefaultConfiguration() {
            assertEquals(3, MockGridConfig.getDefaultRows());
            assertEquals(3, MockGridConfig.getDefaultCols());
            assertEquals(9, MockGridConfig.getTotalCells());
        }
        
        @Test
        @DisplayName("Can set custom grid dimensions")
        public void testSetCustomDimensions() {
            MockGridConfig.setDefaultGrid(4, 5);
            
            assertEquals(4, MockGridConfig.getDefaultRows());
            assertEquals(5, MockGridConfig.getDefaultCols());
            assertEquals(20, MockGridConfig.getTotalCells());
        }
        
        @Test
        @DisplayName("Validates positive dimensions")
        public void testValidatesPositiveDimensions() {
            assertThrows(IllegalArgumentException.class, 
                () -> MockGridConfig.setDefaultGrid(0, 3));
            assertThrows(IllegalArgumentException.class, 
                () -> MockGridConfig.setDefaultGrid(3, 0));
            assertThrows(IllegalArgumentException.class, 
                () -> MockGridConfig.setDefaultGrid(-1, 3));
        }
        
        @Test
        @DisplayName("Reset restores default 3x3")
        public void testReset() {
            MockGridConfig.setDefaultGrid(5, 5);
            assertEquals(25, MockGridConfig.getTotalCells());
            
            MockGridConfig.reset();
            
            assertEquals(3, MockGridConfig.getDefaultRows());
            assertEquals(3, MockGridConfig.getDefaultCols());
            assertEquals(9, MockGridConfig.getTotalCells());
        }
        
        @Test
        @DisplayName("Validates grid numbers")
        public void testValidateGridNumbers() {
            // Default 3x3 grid has cells 0-8
            assertTrue(MockGridConfig.isValidGridNumber(0));
            assertTrue(MockGridConfig.isValidGridNumber(4));
            assertTrue(MockGridConfig.isValidGridNumber(8));
            
            assertFalse(MockGridConfig.isValidGridNumber(-1));
            assertFalse(MockGridConfig.isValidGridNumber(9));
            assertFalse(MockGridConfig.isValidGridNumber(100));
        }
    }
    
    @Nested
    @DisplayName("Grid Operations with Mock Config")
    class GridOperationsWithMockConfig {
        
        @Test
        @DisplayName("Grid operations use configured dimensions")
        public void testGridOperationsUseConfiguredDimensions() {
            // Set a 2x2 grid
            MockGridConfig.setDefaultGrid(2, 2);
            
            Region r = new Region(0, 0, 100, 100);
            
            // Test corners of 2x2 grid
            Optional<Integer> topLeft = r.getGridNumber(new Location(25, 25));
            assertTrue(topLeft.isPresent());
            assertEquals(0, topLeft.get());
            
            Optional<Integer> topRight = r.getGridNumber(new Location(75, 25));
            assertTrue(topRight.isPresent());
            assertEquals(1, topRight.get());
            
            Optional<Integer> bottomLeft = r.getGridNumber(new Location(25, 75));
            assertTrue(bottomLeft.isPresent());
            assertEquals(2, bottomLeft.get());
            
            Optional<Integer> bottomRight = r.getGridNumber(new Location(75, 75));
            assertTrue(bottomRight.isPresent());
            assertEquals(3, bottomRight.get());
        }
        
        @Test
        @DisplayName("Grid operations with 4x3 configuration")
        public void testGridOperationsWith4x3() {
            // Set a 4x3 grid (4 rows, 3 columns)
            MockGridConfig.setDefaultGrid(4, 3);
            
            Region r = new Region(0, 0, 120, 120);
            
            // Each cell is 40x30 (120/3 = 40 width, 120/4 = 30 height)
            
            // Test cell (0,0) - top left
            Optional<Integer> cell00 = r.getGridNumber(new Location(20, 15));
            assertTrue(cell00.isPresent());
            assertEquals(0, cell00.get());
            
            // Test cell (1,1) - second row, second column
            Optional<Integer> cell11 = r.getGridNumber(new Location(60, 45));
            assertTrue(cell11.isPresent());
            assertEquals(4, cell11.get()); // row 1 * 3 cols + col 1 = 4
            
            // Test cell (3,2) - bottom right
            Optional<Integer> cell32 = r.getGridNumber(new Location(100, 100));
            assertTrue(cell32.isPresent());
            assertEquals(11, cell32.get()); // row 3 * 3 cols + col 2 = 11
        }
        
        @Test
        @DisplayName("getGridRegion uses mock configuration")
        public void testGetGridRegionWithMockConfig() {
            // Set a 2x2 grid
            MockGridConfig.setDefaultGrid(2, 2);
            
            Region r = new Region(0, 0, 100, 100);
            
            // Get grid region 0 (top-left)
            Region grid0 = r.getGridRegion(0);
            assertNotNull(grid0);
            assertEquals(0, grid0.getX());
            assertEquals(0, grid0.getY());
            assertEquals(50, grid0.getW()); // 100/2
            assertEquals(50, grid0.getH()); // 100/2
            
            // Get grid region 3 (bottom-right)
            Region grid3 = r.getGridRegion(3);
            assertNotNull(grid3);
            assertEquals(50, grid3.getX());
            assertEquals(50, grid3.getY());
            assertEquals(50, grid3.getW());
            assertEquals(50, grid3.getH());
        }
        
        @Test
        @DisplayName("Grid operations handle edge cases")
        public void testGridOperationsEdgeCases() {
            Region r = new Region(10, 20, 90, 90); // Offset region
            
            // Location outside region
            Optional<Integer> outside = r.getGridNumber(new Location(5, 5));
            assertFalse(outside.isPresent());
            
            // Location on exact boundary
            Optional<Integer> boundary = r.getGridNumber(new Location(100, 110));
            assertFalse(boundary.isPresent()); // 100 = 10 + 90 (just outside)
            
            // Location at exact corner
            Optional<Integer> corner = r.getGridNumber(new Location(10, 20));
            assertTrue(corner.isPresent());
            assertEquals(0, corner.get());
        }
    }
    
    @Nested
    @DisplayName("Integration with Region")
    class IntegrationWithRegion {
        
        @Test
        @DisplayName("Multiple regions can use different grid configs")
        public void testMultipleRegionsWithDifferentConfigs() {
            Region r1 = new Region(0, 0, 90, 90);
            
            // Test with default 3x3
            Optional<Integer> grid1 = r1.getGridNumber(new Location(45, 45));
            assertTrue(grid1.isPresent());
            assertEquals(4, grid1.get()); // Center of 3x3
            
            // Change config
            MockGridConfig.setDefaultGrid(2, 2);
            
            Region r2 = new Region(100, 100, 80, 80);
            Optional<Integer> grid2 = r2.getGridNumber(new Location(120, 120));
            assertTrue(grid2.isPresent());
            assertEquals(0, grid2.get()); // Top-left of 2x2 (20,20 relative to region start)
            
            // Note: r1's grid operations would now also use 2x2
            // This is expected behavior - MockGridConfig is global
            Optional<Integer> grid1After = r1.getGridNumber(new Location(45, 45));
            assertTrue(grid1After.isPresent());
            assertEquals(3, grid1After.get()); // Bottom-right in 2x2 (row 1, col 1)
        }
    }
}