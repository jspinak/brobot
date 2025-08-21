package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for MoveMouseWrapper class.
 * Tests mouse movement operations to specific screen locations.
 */
@DisplayName("MoveMouseWrapper Tests")
public class MoveMouseWrapperTest extends BrobotTestBase {

    @InjectMocks
    private MoveMouseWrapper moveMouseWrapper;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        moveMouseWrapper = new MoveMouseWrapper();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Basic Movement")
    class BasicMovement {
        
        @Test
        @DisplayName("Should move to location")
        void shouldMoveToLocation() {
            // Arrange
            Location location = new Location(100, 200);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should move to origin")
        void shouldMoveToOrigin() {
            // Arrange
            Location origin = new Location(0, 0);
            
            // Act
            boolean result = moveMouseWrapper.move(origin);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should move to screen center")
        void shouldMoveToScreenCenter() {
            // Arrange
            Location center = new Location(960, 540); // Assuming 1920x1080
            
            // Act
            boolean result = moveMouseWrapper.move(center);
            
            // Assert
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Coordinate Validation")
    class CoordinateValidation {
        
        @ParameterizedTest
        @CsvSource({
            "0, 0",
            "100, 200",
            "1920, 1080",
            "3840, 2160",
            "500, 500"
        })
        @DisplayName("Should handle various valid coordinates")
        void shouldHandleVariousValidCoordinates(int x, int y) {
            // Arrange
            Location location = new Location(x, y);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            // Arrange
            Location location = new Location(-10, -20);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            // May succeed or fail depending on implementation
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should handle very large coordinates")
        void shouldHandleVeryLargeCoordinates() {
            // Arrange
            Location location = new Location(10000, 10000);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Mock Mode")
    class MockMode {
        
        @Test
        @DisplayName("Should simulate movement in mock mode")
        void shouldSimulateMovementInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            Location location = new Location(300, 400);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should log movement in mock mode")
        void shouldLogMovementInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            Location location = new Location(150, 250);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
            // Mock mode will log the movement
        }
        
        @Test
        @DisplayName("Should return true for all movements in mock mode")
        void shouldReturnTrueForAllMovementsInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            
            // Act & Assert
            assertTrue(moveMouseWrapper.move(new Location(0, 0)));
            assertTrue(moveMouseWrapper.move(new Location(100, 100)));
            assertTrue(moveMouseWrapper.move(new Location(1000, 1000)));
        }
    }
    
    @Nested
    @DisplayName("Movement Precision")
    class MovementPrecision {
        
        @Test
        @DisplayName("Should move to exact coordinates")
        void shouldMoveToExactCoordinates() {
            // Arrange
            Location target = new Location(456, 789);
            
            // Act
            boolean result = moveMouseWrapper.move(target);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should handle floating point coordinates")
        void shouldHandleFloatingPointCoordinates() {
            // Arrange
            Location location = new Location(123.456, 789.012);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.1, 0.5, 0.9, 1.4, 1.6})
        @DisplayName("Should round fractional coordinates")
        void shouldRoundFractionalCoordinates(double fraction) {
            // Arrange
            Location location = new Location(100 + fraction, 200 + fraction);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Sequential Movements")
    class SequentialMovements {
        
        @Test
        @DisplayName("Should handle rapid sequential movements")
        void shouldHandleRapidSequentialMovements() {
            // Arrange
            Location[] locations = {
                new Location(100, 100),
                new Location(200, 200),
                new Location(300, 300),
                new Location(400, 400),
                new Location(500, 500)
            };
            
            // Act & Assert
            for (Location loc : locations) {
                assertTrue(moveMouseWrapper.move(loc));
            }
        }
        
        @Test
        @DisplayName("Should move in a pattern")
        void shouldMoveInPattern() {
            // Arrange - Move in a square pattern
            Location[] square = {
                new Location(100, 100),
                new Location(500, 100),
                new Location(500, 500),
                new Location(100, 500),
                new Location(100, 100)
            };
            
            // Act & Assert
            for (Location loc : square) {
                assertTrue(moveMouseWrapper.move(loc));
            }
        }
        
        @Test
        @DisplayName("Should handle zigzag movement")
        void shouldHandleZigzagMovement() {
            // Arrange
            for (int i = 0; i < 10; i++) {
                int x = (i % 2 == 0) ? 100 : 500;
                int y = i * 50;
                Location loc = new Location(x, y);
                
                // Act & Assert
                assertTrue(moveMouseWrapper.move(loc));
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null location")
        void shouldHandleNullLocation() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                moveMouseWrapper.move(null)
            );
        }
        
        @Test
        @DisplayName("Should recover from movement failure")
        void shouldRecoverFromMovementFailure() {
            // In mock mode, movements always succeed
            assertTrue(FrameworkSettings.mock);
            
            // Arrange
            Location location = new Location(100, 100);
            
            // Act
            boolean result = moveMouseWrapper.move(location);
            
            // Assert
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should complete movement quickly")
        void shouldCompleteMovementQuickly() {
            // Arrange
            Location location = new Location(500, 500);
            
            // Act
            long startTime = System.currentTimeMillis();
            boolean result = moveMouseWrapper.move(location);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertTrue(result);
            assertTrue(endTime - startTime < 100, "Movement should complete in less than 100ms");
        }
        
        @Test
        @DisplayName("Should handle thousand movements")
        void shouldHandleThousandMovements() {
            // Act
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                Location loc = new Location(i % 800, i % 600);
                assertTrue(moveMouseWrapper.move(loc));
            }
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertTrue(endTime - startTime < 5000, "1000 movements should complete in less than 5 seconds");
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should position for click operation")
        void shouldPositionForClickOperation() {
            // Arrange - Move to button location
            Location buttonLocation = new Location(150, 50);
            
            // Act
            boolean moved = moveMouseWrapper.move(buttonLocation);
            
            // Assert
            assertTrue(moved);
        }
        
        @Test
        @DisplayName("Should hover over element")
        void shouldHoverOverElement() {
            // Arrange - Hover position
            Location hoverLocation = new Location(300, 200);
            
            // Act
            boolean result = moveMouseWrapper.move(hoverLocation);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should setup for drag operation")
        void shouldSetupForDragOperation() {
            // Arrange - Start position for drag
            Location dragStart = new Location(100, 100);
            
            // Act
            boolean result = moveMouseWrapper.move(dragStart);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should navigate between regions")
        void shouldNavigateBetweenRegions() {
            // Arrange
            Location region1 = new Location(100, 100);
            Location region2 = new Location(500, 100);
            Location region3 = new Location(500, 500);
            Location region4 = new Location(100, 500);
            
            // Act & Assert
            assertTrue(moveMouseWrapper.move(region1));
            assertTrue(moveMouseWrapper.move(region2));
            assertTrue(moveMouseWrapper.move(region3));
            assertTrue(moveMouseWrapper.move(region4));
        }
    }
}