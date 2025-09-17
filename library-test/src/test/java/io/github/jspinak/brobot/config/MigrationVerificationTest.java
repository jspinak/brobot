package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test to verify BrobotProperties configuration.
 *
 * This test ensures:
 * 1. BrobotProperties is properly loaded and configured
 * 2. Mock mode configuration works correctly
 * 3. All property groups are accessible
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.core.headless=true"
})
public class MigrationVerificationTest extends BrobotTestBase {

    private BrobotProperties brobotProperties;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Create a test instance of BrobotProperties
        brobotProperties = new BrobotProperties();

        // Set test values
        brobotProperties.getCore().setImagePath("test/images");
        brobotProperties.getCore().setMock(true);
        brobotProperties.getMouse().setMoveDelay(0.5f);
        brobotProperties.getMock().setTimeFindFirst(0.1);
        brobotProperties.getMock().setTimeClick(0.05);
        brobotProperties.getScreenshot().setSaveSnapshots(true);

    }

    @Test
    public void testBrobotPropertiesLoaded() {
        assertNotNull(brobotProperties, "BrobotProperties should be loaded");
        
        // Verify core properties
        assertEquals("test/images", brobotProperties.getCore().getImagePath());
        assertTrue(brobotProperties.getCore().isMock());
        
        // Verify mouse properties
        assertEquals(0.5f, brobotProperties.getMouse().getMoveDelay(), 0.001);
        
        // Verify mock properties
        assertEquals(0.1, brobotProperties.getMock().getTimeFindFirst(), 0.001);
        assertEquals(0.05, brobotProperties.getMock().getTimeClick(), 0.001);
        
        // Verify screenshot properties
        assertTrue(brobotProperties.getScreenshot().isSaveSnapshots());
    }


    @Test
    public void testMockModeConfiguration() {
        // Mock mode should be properly set
        assertTrue(brobotProperties.getCore().isMock(),
            "Mock mode should be enabled in test");
    }


    @Test  
    public void testDefaultValues() {
        // Verify default values are set correctly
        assertNotNull(brobotProperties.getCore().getPackageName());
        assertTrue(brobotProperties.getMouse().getPauseBeforeDown() >= 0);
        assertTrue(brobotProperties.getMouse().getPauseAfterDown() >= 0);
        assertTrue(brobotProperties.getMock().getTimeFindAll() > 0);
        assertNotNull(brobotProperties.getScreenshot().getPath());
        assertNotNull(brobotProperties.getIllustration());
        assertNotNull(brobotProperties.getAnalysis());
    }

    @Test
    public void testMonitorConfiguration() {
        // Test monitor settings
        assertNotNull(brobotProperties.getMonitor());
        assertEquals(-1, brobotProperties.getMonitor().getDefaultScreenIndex());
        assertFalse(brobotProperties.getMonitor().isMultiMonitorEnabled());
        assertFalse(brobotProperties.getMonitor().isSearchAllMonitors());
        assertTrue(brobotProperties.getMonitor().isLogMonitorInfo());
    }

    @Test
    public void testNestedPropertyAccess() {
        // Ensure nested properties can be accessed without NPE
        assertDoesNotThrow(() -> {
            brobotProperties.getCore().getImagePath();
            brobotProperties.getMouse().getMoveDelay();
            brobotProperties.getMock().getTimeClick();
            brobotProperties.getScreenshot().getPath();
            brobotProperties.getIllustration().isDrawFind();
            brobotProperties.getAnalysis().getKMeansInProfile();
            brobotProperties.getRecording().getSecondsToCapture();
            brobotProperties.getDataset().getPath();
            brobotProperties.getTesting().getIteration();
        });
    }
}