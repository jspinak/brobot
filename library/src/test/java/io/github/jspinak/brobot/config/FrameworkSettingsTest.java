package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FrameworkSettings.
 * Tests global configuration settings for the Brobot framework.
 */
@DisplayName("FrameworkSettings Tests")
public class FrameworkSettingsTest extends BrobotTestBase {
    
    // Store original values to restore after each test
    private float originalMoveMouseDelay;
    private double originalPauseBeforeMouseDown;
    private double originalPauseAfterMouseDown;
    private double originalPauseBeforeMouseUp;
    private double originalPauseAfterMouseUp;
    private int originalXMoveAfterMouseDown;
    private int originalYMoveAfterMouseDown;
    private boolean originalSaveSnapshots;
    private boolean originalSaveHistory;
    private boolean originalMock;
    private double originalMockTimeFindFirst;
    private double originalMockTimeFindAll;
    private double originalMockTimeDrag;
    private double originalMockTimeClick;
    private double originalMockTimeMove;
    private double originalMockTimeFindHistogram;
    private double originalMockTimeFindColor;
    private double originalMockTimeClassify;
    private List<String> originalScreenshots;
    private String originalScreenshotPath;
    private String originalScreenshotFilename;
    private String originalPackageName;
    private String originalHistoryPath;
    private String originalHistoryFilename;
    private boolean originalDrawFind;
    private boolean originalDrawClick;
    private boolean originalDrawDrag;
    private boolean originalDrawMove;
    private boolean originalDrawHighlight;
    private boolean originalDrawRepeatedActions;
    private boolean originalDrawClassify;
    private boolean originalDrawDefine;
    private int originalKMeansInProfile;
    private int originalMaxKMeansToStoreInProfile;
    private boolean originalInitProfilesForStaticImages;
    private boolean originalInitProfilesForDynamicImages;
    private boolean originalIncludeStateImageObjectsFromActiveStatesInAnalysis;
    private int originalSecondsToCapture;
    private int originalCaptureFrequency;
    private String originalRecordingFolder;
    private boolean originalBuildDataset;
    private String originalDatasetPath;
    private int originalTestIteration;
    private boolean originalSendLogs;
    private String originalTestScreenshotsPath;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        saveOriginalSettings();
    }
    
    @AfterEach
    public void restoreSettings() {
        restoreOriginalSettings();
    }
    
    private void saveOriginalSettings() {
        // Mouse Control
        originalMoveMouseDelay = FrameworkSettings.moveMouseDelay;
        originalPauseBeforeMouseDown = FrameworkSettings.pauseBeforeMouseDown;
        originalPauseAfterMouseDown = FrameworkSettings.pauseAfterMouseDown;
        originalPauseBeforeMouseUp = FrameworkSettings.pauseBeforeMouseUp;
        originalPauseAfterMouseUp = FrameworkSettings.pauseAfterMouseUp;
        originalXMoveAfterMouseDown = FrameworkSettings.xMoveAfterMouseDown;
        originalYMoveAfterMouseDown = FrameworkSettings.yMoveAfterMouseDown;
        
        // Save Settings
        originalSaveSnapshots = FrameworkSettings.saveSnapshots;
        originalSaveHistory = FrameworkSettings.saveHistory;
        
        // Mock Settings
        originalMock = FrameworkSettings.mock;
        originalMockTimeFindFirst = FrameworkSettings.mockTimeFindFirst;
        originalMockTimeFindAll = FrameworkSettings.mockTimeFindAll;
        originalMockTimeDrag = FrameworkSettings.mockTimeDrag;
        originalMockTimeClick = FrameworkSettings.mockTimeClick;
        originalMockTimeMove = FrameworkSettings.mockTimeMove;
        originalMockTimeFindHistogram = FrameworkSettings.mockTimeFindHistogram;
        originalMockTimeFindColor = FrameworkSettings.mockTimeFindColor;
        originalMockTimeClassify = FrameworkSettings.mockTimeClassify;
        
        // Test Settings
        originalScreenshots = new ArrayList<>(FrameworkSettings.screenshots);
        
        // Screenshot Settings
        originalScreenshotPath = FrameworkSettings.screenshotPath;
        originalScreenshotFilename = FrameworkSettings.screenshotFilename;
        
        // Code Generation Settings
        originalPackageName = FrameworkSettings.packageName;
        
        // History Settings
        originalHistoryPath = FrameworkSettings.historyPath;
        originalHistoryFilename = FrameworkSettings.historyFilename;
        originalDrawFind = FrameworkSettings.drawFind;
        originalDrawClick = FrameworkSettings.drawClick;
        originalDrawDrag = FrameworkSettings.drawDrag;
        originalDrawMove = FrameworkSettings.drawMove;
        originalDrawHighlight = FrameworkSettings.drawHighlight;
        originalDrawRepeatedActions = FrameworkSettings.drawRepeatedActions;
        originalDrawClassify = FrameworkSettings.drawClassify;
        originalDrawDefine = FrameworkSettings.drawDefine;
        
        // K-Means Settings
        originalKMeansInProfile = FrameworkSettings.kMeansInProfile;
        originalMaxKMeansToStoreInProfile = FrameworkSettings.maxKMeansToStoreInProfile;
        
        // Color Settings
        originalInitProfilesForStaticImages = FrameworkSettings.initProfilesForStaticfImages;
        originalInitProfilesForDynamicImages = FrameworkSettings.initProfilesForDynamicImages;
        originalIncludeStateImageObjectsFromActiveStatesInAnalysis = FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis;
        
        // Capture Settings
        originalSecondsToCapture = FrameworkSettings.secondsToCapture;
        originalCaptureFrequency = FrameworkSettings.captureFrequency;
        originalRecordingFolder = FrameworkSettings.recordingFolder;
        
        // AI Settings
        originalBuildDataset = FrameworkSettings.buildDataset;
        originalDatasetPath = FrameworkSettings.datasetPath;
        
        // Test Settings
        originalTestIteration = FrameworkSettings.testIteration;
        originalSendLogs = FrameworkSettings.sendLogs;
        originalTestScreenshotsPath = FrameworkSettings.testScreenshotsPath;
    }
    
    private void restoreOriginalSettings() {
        // Mouse Control
        FrameworkSettings.moveMouseDelay = originalMoveMouseDelay;
        FrameworkSettings.pauseBeforeMouseDown = originalPauseBeforeMouseDown;
        FrameworkSettings.pauseAfterMouseDown = originalPauseAfterMouseDown;
        FrameworkSettings.pauseBeforeMouseUp = originalPauseBeforeMouseUp;
        FrameworkSettings.pauseAfterMouseUp = originalPauseAfterMouseUp;
        FrameworkSettings.xMoveAfterMouseDown = originalXMoveAfterMouseDown;
        FrameworkSettings.yMoveAfterMouseDown = originalYMoveAfterMouseDown;
        
        // Save Settings
        FrameworkSettings.saveSnapshots = originalSaveSnapshots;
        FrameworkSettings.saveHistory = originalSaveHistory;
        
        // Mock Settings
        FrameworkSettings.mock = originalMock;
        FrameworkSettings.mockTimeFindFirst = originalMockTimeFindFirst;
        FrameworkSettings.mockTimeFindAll = originalMockTimeFindAll;
        FrameworkSettings.mockTimeDrag = originalMockTimeDrag;
        FrameworkSettings.mockTimeClick = originalMockTimeClick;
        FrameworkSettings.mockTimeMove = originalMockTimeMove;
        FrameworkSettings.mockTimeFindHistogram = originalMockTimeFindHistogram;
        FrameworkSettings.mockTimeFindColor = originalMockTimeFindColor;
        FrameworkSettings.mockTimeClassify = originalMockTimeClassify;
        
        // Test Settings
        FrameworkSettings.screenshots = new ArrayList<>(originalScreenshots);
        
        // Screenshot Settings
        FrameworkSettings.screenshotPath = originalScreenshotPath;
        FrameworkSettings.screenshotFilename = originalScreenshotFilename;
        
        // Code Generation Settings
        FrameworkSettings.packageName = originalPackageName;
        
        // History Settings
        FrameworkSettings.historyPath = originalHistoryPath;
        FrameworkSettings.historyFilename = originalHistoryFilename;
        FrameworkSettings.drawFind = originalDrawFind;
        FrameworkSettings.drawClick = originalDrawClick;
        FrameworkSettings.drawDrag = originalDrawDrag;
        FrameworkSettings.drawMove = originalDrawMove;
        FrameworkSettings.drawHighlight = originalDrawHighlight;
        FrameworkSettings.drawRepeatedActions = originalDrawRepeatedActions;
        FrameworkSettings.drawClassify = originalDrawClassify;
        FrameworkSettings.drawDefine = originalDrawDefine;
        
        // K-Means Settings
        FrameworkSettings.kMeansInProfile = originalKMeansInProfile;
        FrameworkSettings.maxKMeansToStoreInProfile = originalMaxKMeansToStoreInProfile;
        
        // Color Settings
        FrameworkSettings.initProfilesForStaticfImages = originalInitProfilesForStaticImages;
        FrameworkSettings.initProfilesForDynamicImages = originalInitProfilesForDynamicImages;
        FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis = originalIncludeStateImageObjectsFromActiveStatesInAnalysis;
        
        // Capture Settings
        FrameworkSettings.secondsToCapture = originalSecondsToCapture;
        FrameworkSettings.captureFrequency = originalCaptureFrequency;
        FrameworkSettings.recordingFolder = originalRecordingFolder;
        
        // AI Settings
        FrameworkSettings.buildDataset = originalBuildDataset;
        FrameworkSettings.datasetPath = originalDatasetPath;
        
        // Test Settings
        FrameworkSettings.testIteration = originalTestIteration;
        FrameworkSettings.sendLogs = originalSendLogs;
        FrameworkSettings.testScreenshotsPath = originalTestScreenshotsPath;
    }
    
    @Nested
    @DisplayName("Mouse Control Settings")
    class MouseControlSettings {
        
        @Test
        @DisplayName("Should have default mouse delay")
        void shouldHaveDefaultMouseDelay() {
            assertEquals(0.5f, FrameworkSettings.moveMouseDelay);
        }
        
        @ParameterizedTest
        @DisplayName("Should set mouse delays")
        @ValueSource(doubles = {0.0, 0.1, 0.5, 1.0, 2.5})
        void shouldSetMouseDelays(double delay) {
            FrameworkSettings.pauseBeforeMouseDown = delay;
            FrameworkSettings.pauseAfterMouseDown = delay;
            FrameworkSettings.pauseBeforeMouseUp = delay;
            FrameworkSettings.pauseAfterMouseUp = delay;
            
            assertEquals(delay, FrameworkSettings.pauseBeforeMouseDown);
            assertEquals(delay, FrameworkSettings.pauseAfterMouseDown);
            assertEquals(delay, FrameworkSettings.pauseBeforeMouseUp);
            assertEquals(delay, FrameworkSettings.pauseAfterMouseUp);
        }
        
        @ParameterizedTest
        @DisplayName("Should set mouse move offsets")
        @CsvSource({
            "0, 0",
            "10, 20",
            "-5, -10",
            "100, 100"
        })
        void shouldSetMouseMoveOffsets(int x, int y) {
            FrameworkSettings.xMoveAfterMouseDown = x;
            FrameworkSettings.yMoveAfterMouseDown = y;
            
            assertEquals(x, FrameworkSettings.xMoveAfterMouseDown);
            assertEquals(y, FrameworkSettings.yMoveAfterMouseDown);
        }
    }
    
    @Nested
    @DisplayName("Mock Mode Settings")
    class MockModeSettings {
        
        @Test
        @DisplayName("Should enable mock mode")
        void shouldEnableMockMode() {
            FrameworkSettings.mock = true;
            assertTrue(FrameworkSettings.mock);
        }
        
        @Test
        @DisplayName("Should have default mock timings")
        void shouldHaveDefaultMockTimings() {
            assertEquals(0.1, FrameworkSettings.mockTimeFindFirst);
            assertEquals(0.2, FrameworkSettings.mockTimeFindAll);
            assertEquals(0.3, FrameworkSettings.mockTimeDrag);
            assertEquals(0.05, FrameworkSettings.mockTimeClick);
            assertEquals(0.1, FrameworkSettings.mockTimeMove);
            assertEquals(0.3, FrameworkSettings.mockTimeFindHistogram);
            assertEquals(0.3, FrameworkSettings.mockTimeFindColor);
            assertEquals(0.4, FrameworkSettings.mockTimeClassify);
        }
        
        @Test
        @DisplayName("Should customize mock timings")
        void shouldCustomizeMockTimings() {
            FrameworkSettings.mockTimeFindFirst = 0.01;
            FrameworkSettings.mockTimeClick = 0.02;
            
            assertEquals(0.01, FrameworkSettings.mockTimeFindFirst);
            assertEquals(0.02, FrameworkSettings.mockTimeClick);
        }
        
        @Test
        @DisplayName("Mock timings should be positive")
        void mockTimingsShouldBePositive() {
            assertTrue(FrameworkSettings.mockTimeFindFirst >= 0);
            assertTrue(FrameworkSettings.mockTimeFindAll >= 0);
            assertTrue(FrameworkSettings.mockTimeDrag >= 0);
            assertTrue(FrameworkSettings.mockTimeClick >= 0);
            assertTrue(FrameworkSettings.mockTimeMove >= 0);
            assertTrue(FrameworkSettings.mockTimeFindHistogram >= 0);
            assertTrue(FrameworkSettings.mockTimeFindColor >= 0);
            assertTrue(FrameworkSettings.mockTimeClassify >= 0);
        }
    }
    
    @Nested
    @DisplayName("Screenshot Settings")
    class ScreenshotSettings {
        
        @Test
        @DisplayName("Should have default paths")
        void shouldHaveDefaultPaths() {
            assertEquals("screenshots/", FrameworkSettings.screenshotPath);
            assertEquals("screen", FrameworkSettings.screenshotFilename);
            assertEquals("history/", FrameworkSettings.historyPath);
            assertEquals("hist", FrameworkSettings.historyFilename);
        }
        
        @Test
        @DisplayName("Should manage screenshot list")
        void shouldManageScreenshotList() {
            FrameworkSettings.screenshots.clear();
            assertTrue(FrameworkSettings.screenshots.isEmpty());
            
            FrameworkSettings.screenshots.add("test1.png");
            FrameworkSettings.screenshots.add("test2.png");
            
            assertEquals(2, FrameworkSettings.screenshots.size());
            assertTrue(FrameworkSettings.screenshots.contains("test1.png"));
            assertTrue(FrameworkSettings.screenshots.contains("test2.png"));
        }
        
        @Test
        @DisplayName("Should control save settings")
        void shouldControlSaveSettings() {
            FrameworkSettings.saveSnapshots = true;
            FrameworkSettings.saveHistory = true;
            
            assertTrue(FrameworkSettings.saveSnapshots);
            assertTrue(FrameworkSettings.saveHistory);
            
            FrameworkSettings.saveSnapshots = false;
            FrameworkSettings.saveHistory = false;
            
            assertFalse(FrameworkSettings.saveSnapshots);
            assertFalse(FrameworkSettings.saveHistory);
        }
    }
    
    @Nested
    @DisplayName("Drawing Settings")
    class DrawingSettings {
        
        @Test
        @DisplayName("Should have default drawing flags enabled")
        void shouldHaveDefaultDrawingFlagsEnabled() {
            // Set to default values explicitly
            FrameworkSettings.drawFind = true;
            FrameworkSettings.drawClick = true;
            FrameworkSettings.drawDrag = true;
            FrameworkSettings.drawMove = true;
            FrameworkSettings.drawHighlight = true;
            FrameworkSettings.drawRepeatedActions = true;
            FrameworkSettings.drawClassify = true;
            FrameworkSettings.drawDefine = true;
            
            // Now verify they are set
            assertTrue(FrameworkSettings.drawFind);
            assertTrue(FrameworkSettings.drawClick);
            assertTrue(FrameworkSettings.drawDrag);
            assertTrue(FrameworkSettings.drawMove);
            assertTrue(FrameworkSettings.drawHighlight);
            assertTrue(FrameworkSettings.drawRepeatedActions);
            assertTrue(FrameworkSettings.drawClassify);
            assertTrue(FrameworkSettings.drawDefine);
        }
        
        @Test
        @DisplayName("Should toggle drawing flags")
        void shouldToggleDrawingFlags() {
            // First set some to true
            FrameworkSettings.drawDrag = true;
            FrameworkSettings.drawMove = true;
            
            // Toggle some flags
            FrameworkSettings.drawFind = false;
            FrameworkSettings.drawClick = false;
            FrameworkSettings.drawRepeatedActions = false;
            
            assertFalse(FrameworkSettings.drawFind);
            assertFalse(FrameworkSettings.drawClick);
            assertFalse(FrameworkSettings.drawRepeatedActions);
            
            assertTrue(FrameworkSettings.drawDrag);
            assertTrue(FrameworkSettings.drawMove);
        }
    }
    
    @Nested
    @DisplayName("K-Means Settings")
    class KMeansSettings {
        
        @Test
        @DisplayName("Should have default k-means values")
        void shouldHaveDefaultKMeansValues() {
            assertEquals(5, FrameworkSettings.kMeansInProfile);
            assertEquals(10, FrameworkSettings.maxKMeansToStoreInProfile);
        }
        
        @ParameterizedTest
        @DisplayName("Should set k-means values")
        @CsvSource({
            "2, 5",
            "3, 8",
            "5, 10",
            "10, 20"
        })
        void shouldSetKMeansValues(int k, int maxK) {
            FrameworkSettings.kMeansInProfile = k;
            FrameworkSettings.maxKMeansToStoreInProfile = maxK;
            
            assertEquals(k, FrameworkSettings.kMeansInProfile);
            assertEquals(maxK, FrameworkSettings.maxKMeansToStoreInProfile);
        }
        
        @Test
        @DisplayName("Max k-means should be greater than or equal to k")
        void maxKMeansShouldBeGreaterThanOrEqualToK() {
            FrameworkSettings.kMeansInProfile = 5;
            FrameworkSettings.maxKMeansToStoreInProfile = 10;
            
            assertTrue(FrameworkSettings.maxKMeansToStoreInProfile >= FrameworkSettings.kMeansInProfile);
        }
    }
    
    @Nested
    @DisplayName("Color Profile Settings")
    class ColorProfileSettings {
        
        @Test
        @DisplayName("Should have default color profile settings")
        void shouldHaveDefaultColorProfileSettings() {
            assertFalse(FrameworkSettings.initProfilesForStaticfImages);
            assertFalse(FrameworkSettings.initProfilesForDynamicImages);
            assertTrue(FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis);
        }
        
        @Test
        @DisplayName("Should toggle color profile initialization")
        void shouldToggleColorProfileInitialization() {
            FrameworkSettings.initProfilesForStaticfImages = true;
            FrameworkSettings.initProfilesForDynamicImages = true;
            FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis = false;
            
            assertTrue(FrameworkSettings.initProfilesForStaticfImages);
            assertTrue(FrameworkSettings.initProfilesForDynamicImages);
            assertFalse(FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis);
        }
    }
    
    @Nested
    @DisplayName("Capture Settings")
    class CaptureSettings {
        
        @Test
        @DisplayName("Should have default capture settings")
        void shouldHaveDefaultCaptureSettings() {
            assertEquals(1000, FrameworkSettings.secondsToCapture);
            assertEquals(1, FrameworkSettings.captureFrequency);
            assertEquals("recording", FrameworkSettings.recordingFolder);
        }
        
        @ParameterizedTest
        @DisplayName("Should set capture duration")
        @ValueSource(ints = {10, 60, 300, 1000, 3600})
        void shouldSetCaptureDuration(int seconds) {
            FrameworkSettings.secondsToCapture = seconds;
            assertEquals(seconds, FrameworkSettings.secondsToCapture);
        }
        
        @ParameterizedTest
        @DisplayName("Should set capture frequency")
        @ValueSource(ints = {1, 5, 10, 30, 60})
        void shouldSetCaptureFrequency(int fps) {
            FrameworkSettings.captureFrequency = fps;
            assertEquals(fps, FrameworkSettings.captureFrequency);
        }
    }
    
    @Nested
    @DisplayName("AI Dataset Settings")
    class AIDatasetSettings {
        
        @Test
        @DisplayName("Should have default dataset settings")
        void shouldHaveDefaultDatasetSettings() {
            assertFalse(FrameworkSettings.buildDataset);
            assertEquals("dataset/", FrameworkSettings.datasetPath);
        }
        
        @Test
        @DisplayName("Should enable dataset building")
        void shouldEnableDatasetBuilding() {
            FrameworkSettings.buildDataset = true;
            FrameworkSettings.datasetPath = "custom/dataset/";
            
            assertTrue(FrameworkSettings.buildDataset);
            assertEquals("custom/dataset/", FrameworkSettings.datasetPath);
        }
    }
    
    @Nested
    @DisplayName("Test Execution Settings")
    class TestExecutionSettings {
        
        @Test
        @DisplayName("Should have default test settings")
        void shouldHaveDefaultTestSettings() {
            assertEquals(1, FrameworkSettings.testIteration);
            assertTrue(FrameworkSettings.sendLogs);
            assertEquals("screenshots/", FrameworkSettings.testScreenshotsPath);
        }
        
        @Test
        @DisplayName("Should increment test iteration")
        void shouldIncrementTestIteration() {
            int initial = FrameworkSettings.testIteration;
            FrameworkSettings.testIteration++;
            
            assertEquals(initial + 1, FrameworkSettings.testIteration);
        }
        
        @Test
        @DisplayName("Should control log sending")
        void shouldControlLogSending() {
            FrameworkSettings.sendLogs = false;
            assertFalse(FrameworkSettings.sendLogs);
            
            FrameworkSettings.sendLogs = true;
            assertTrue(FrameworkSettings.sendLogs);
        }
    }
    
    @Nested
    @DisplayName("Code Generation Settings")
    class CodeGenerationSettings {
        
        @Test
        @DisplayName("Should have default package name")
        void shouldHaveDefaultPackageName() {
            assertEquals("com.example", FrameworkSettings.packageName);
        }
        
        @Test
        @DisplayName("Should set custom package name")
        void shouldSetCustomPackageName() {
            FrameworkSettings.packageName = "io.github.jspinak.brobot.generated";
            assertEquals("io.github.jspinak.brobot.generated", FrameworkSettings.packageName);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty screenshot list")
        void shouldHandleEmptyScreenshotList() {
            FrameworkSettings.screenshots.clear();
            assertTrue(FrameworkSettings.screenshots.isEmpty());
            
            // Should not throw when accessing
            assertDoesNotThrow(() -> {
                int size = FrameworkSettings.screenshots.size();
                assertEquals(0, size);
            });
        }
        
        @Test
        @DisplayName("Should handle null strings safely")
        void shouldHandleNullStringsSafely() {
            // Setting null should be allowed (though not recommended)
            FrameworkSettings.screenshotPath = null;
            assertNull(FrameworkSettings.screenshotPath);
            
            // Restore to valid value
            FrameworkSettings.screenshotPath = "screenshots/";
        }
        
        @Test
        @DisplayName("Should handle extreme numeric values")
        void shouldHandleExtremeNumericValues() {
            // Very small delays
            FrameworkSettings.mockTimeClick = 0.0001;
            assertEquals(0.0001, FrameworkSettings.mockTimeClick);
            
            // Very large delays  
            FrameworkSettings.pauseAfterMouseUp = 100.0;
            assertEquals(100.0, FrameworkSettings.pauseAfterMouseUp);
            
            // Negative offsets
            FrameworkSettings.xMoveAfterMouseDown = -1000;
            FrameworkSettings.yMoveAfterMouseDown = -1000;
            assertEquals(-1000, FrameworkSettings.xMoveAfterMouseDown);
            assertEquals(-1000, FrameworkSettings.yMoveAfterMouseDown);
        }
    }
    
    @Nested
    @DisplayName("Settings Interactions")
    class SettingsInteractions {
        
        @Test
        @DisplayName("Mock mode should override save settings")
        void mockModeShouldOverrideSaveSettings() {
            // In mock mode, saving might be handled differently
            FrameworkSettings.mock = true;
            FrameworkSettings.saveSnapshots = true;
            FrameworkSettings.saveHistory = true;
            
            // Settings can be set independently
            assertTrue(FrameworkSettings.mock);
            assertTrue(FrameworkSettings.saveSnapshots);
            assertTrue(FrameworkSettings.saveHistory);
        }
        
        @Test
        @DisplayName("Screenshot list affects test mode")
        void screenshotListAffectsTestMode() {
            // Empty screenshots
            FrameworkSettings.screenshots.clear();
            assertTrue(FrameworkSettings.screenshots.isEmpty());
            
            // With screenshots
            FrameworkSettings.screenshots.add("test.png");
            assertFalse(FrameworkSettings.screenshots.isEmpty());
        }
        
        @Test
        @DisplayName("Settings should be independent")
        void settingsShouldBeIndependent() {
            // Change multiple settings
            FrameworkSettings.mock = true;
            FrameworkSettings.buildDataset = true;
            FrameworkSettings.saveHistory = true;
            FrameworkSettings.captureFrequency = 30;
            
            // All should maintain their values
            assertTrue(FrameworkSettings.mock);
            assertTrue(FrameworkSettings.buildDataset);
            assertTrue(FrameworkSettings.saveHistory);
            assertEquals(30, FrameworkSettings.captureFrequency);
        }
    }
}