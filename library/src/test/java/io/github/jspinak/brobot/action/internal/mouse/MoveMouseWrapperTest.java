package io.github.jspinak.brobot.action.internal.mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.MockedStatic;

import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for MoveMouseWrapper coordinate scaling functionality. */
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class MoveMouseWrapperTest extends BrobotTestBase {

    private MoveMouseWrapper moveMouseWrapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        moveMouseWrapper = new MoveMouseWrapper();
    }

    @Test
    public void testMouseMovementWithCoordinateScaling() {
        // Initialize with physical resolution (FFmpeg capture)
        ScreenDimensions.initialize("JAVACV_FFMPEG", 1920, 1080);

        // Mock logical resolution (125% DPI scaling)
        try (MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
            Toolkit mockToolkit = mock(Toolkit.class);
            when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1536, 864));
            toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);

            // Create a location at the center of the screen
            Location centerLocation = new Location(Positions.Name.MIDDLEMIDDLE);

            // In mock mode, move should succeed without actual mouse movement
            assertTrue(FrameworkSettings.mock, "Should be in mock mode");
            boolean result = moveMouseWrapper.move(centerLocation);

            assertTrue(result, "Mouse movement should succeed in mock mode");

            // Verify the location is calculated correctly
            // Physical center: 960, 540
            // With proper implementation, these should be scaled to logical coordinates
            assertEquals(960, centerLocation.getCalculatedX(), "Physical X should be 960");
            assertEquals(540, centerLocation.getCalculatedY(), "Physical Y should be 540");
        }
    }

    @Test
    public void testMouseMovementWithoutScaling() {
        // Initialize with logical resolution (SikuliX capture)
        ScreenDimensions.initialize("SIKULIX", 1536, 864);

        // Mock logical resolution (same as capture)
        try (MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
            Toolkit mockToolkit = mock(Toolkit.class);
            when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1536, 864));
            toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);

            // Create a location at the center of the screen
            Location centerLocation = new Location(Positions.Name.MIDDLEMIDDLE);

            // In mock mode, move should succeed without actual mouse movement
            assertTrue(FrameworkSettings.mock, "Should be in mock mode");
            boolean result = moveMouseWrapper.move(centerLocation);

            assertTrue(result, "Mouse movement should succeed in mock mode");

            // Verify the location is calculated correctly
            // Logical center: 768, 432
            assertEquals(768, centerLocation.getCalculatedX(), "Logical X should be 768");
            assertEquals(432, centerLocation.getCalculatedY(), "Logical Y should be 432");
        }
    }

    @Test
    public void testExplicitCoordinates() {
        // Initialize with physical resolution
        ScreenDimensions.initialize("JAVACV_FFMPEG", 1920, 1080);

        // Create a location with explicit coordinates
        Location explicitLocation = new Location(100, 200);

        // In mock mode, move should succeed
        assertTrue(FrameworkSettings.mock, "Should be in mock mode");
        boolean result = moveMouseWrapper.move(explicitLocation);

        assertTrue(result, "Mouse movement should succeed in mock mode");

        // Explicit coordinates should remain unchanged
        assertEquals(100, explicitLocation.getCalculatedX(), "X should be 100");
        assertEquals(200, explicitLocation.getCalculatedY(), "Y should be 200");
    }
}
