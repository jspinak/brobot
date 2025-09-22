package io.github.jspinak.brobot.action.internal.mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;

/**
 * Unit tests for MoveMouseWrapper.
 * Tests focus on mock mode behavior since MoveMouseWrapper's main responsibility
 * is to return true in mock mode and delegate to sikuliMove in real mode.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MoveMouseWrapper Tests")
public class MoveMouseWrapperTest extends BrobotTestBase {

    private MoveMouseWrapper moveMouseWrapper;

    @Mock private BrobotProperties brobotProperties;
    @Mock private BrobotProperties.Core core;
    @Mock private CoordinateScaler coordinateScaler;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Setup mock mode = true (Brobot mock mode)
        when(core.isMock()).thenReturn(true);
        when(brobotProperties.getCore()).thenReturn(core);

        moveMouseWrapper = new MoveMouseWrapper(brobotProperties, coordinateScaler);
    }

    @Test
    @DisplayName("Should return true in mock mode")
    public void testMouseMoveInMockMode() {
        // Given - any location
        Location location = new Location(100, 200);

        // When - move is called in mock mode
        boolean result = moveMouseWrapper.move(location);

        // Then - should always return true
        assertTrue(result, "Mouse movement should always succeed in mock mode");

        // Verify that coordinateScaler was not called (no real movement in mock mode)
        verifyNoInteractions(coordinateScaler);
    }

    @Test
    @DisplayName("Should return true for any location in mock mode")
    public void testMouseMoveVariousLocations() {
        // Test with different types of locations
        Location explicitLocation = new Location(50, 75);
        Location zeroLocation = new Location(0, 0);
        Location negativeLocation = new Location(-10, -20);

        // All moves should succeed in mock mode
        assertTrue(moveMouseWrapper.move(explicitLocation), "Should succeed for explicit coordinates");
        assertTrue(moveMouseWrapper.move(zeroLocation), "Should succeed for zero coordinates");
        assertTrue(moveMouseWrapper.move(negativeLocation), "Should succeed for negative coordinates");

        // No interaction with coordinate scaler in mock mode
        verifyNoInteractions(coordinateScaler);
    }

    @Test
    @DisplayName("Should not call sikuliMove in mock mode")
    public void testNoRealMovementInMockMode() {
        // Given
        Location location = new Location(300, 400);

        // When
        boolean result = moveMouseWrapper.move(location);

        // Then
        assertTrue(result);
        // Verify brobotProperties was checked for mock mode
        verify(brobotProperties.getCore()).isMock();
        // Verify no coordinate scaling happened (would only happen in sikuliMove)
        verifyNoInteractions(coordinateScaler);
    }

    @Test
    @DisplayName("Should call sikuliMove in real mode")
    public void testRealModeCallsSikuliMove() {
        // Setup for real mode (not mock)
        when(core.isMock()).thenReturn(false);

        // Setup coordinate scaler to return a sikuli location
        Location location = new Location(100, 200);
        org.sikuli.script.Location sikuliLocation = new org.sikuli.script.Location(100, 200);
        when(coordinateScaler.scaleLocationToLogical(location)).thenReturn(sikuliLocation);

        // Note: In a real test, sikuliLocation.hover() would be called, but we can't mock
        // that easily since it's a final method. The test would fail in real mode without
        // a display, which is why these tests focus on mock mode.

        // This test just verifies the mock mode check happens
        try {
            moveMouseWrapper.move(location);
        } catch (Exception e) {
            // Expected in test environment without display
        }

        // Verify mock mode was checked
        verify(brobotProperties.getCore()).isMock();
        // Verify coordinate scaler was called in real mode
        verify(coordinateScaler).scaleLocationToLogical(location);
    }
}
