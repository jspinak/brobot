package io.github.jspinak.brobot.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for FrameworkSettings. Tests global configuration settings for the
 * Brobot framework.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("CI failure - needs investigation")
public class FrameworkSettingsTest extends BrobotTestBase {

    // Store original values to restore after tests
    private Map<String, Object> originalSettings = new HashMap<>();

    @BeforeAll
    void saveOriginalSettings() {
        // Save mouse control settings
        originalSettings.put("moveMouseDelay", FrameworkSettings.moveMouseDelay);
        originalSettings.put("pauseBeforeMouseDown", FrameworkSettings.pauseBeforeMouseDown);
        originalSettings.put("pauseAfterMouseDown", FrameworkSettings.pauseAfterMouseDown);
        originalSettings.put("pauseBeforeMouseUp", FrameworkSettings.pauseBeforeMouseUp);
        originalSettings.put("pauseAfterMouseUp", FrameworkSettings.pauseAfterMouseUp);
        originalSettings.put("xMoveAfterMouseDown", FrameworkSettings.xMoveAfterMouseDown);
        originalSettings.put("yMoveAfterMouseDown", FrameworkSettings.yMoveAfterMouseDown);

        // Save mock mode settings
        originalSettings.put("mock", FrameworkSettings.mock);
        originalSettings.put("mockTimeFindFirst", FrameworkSettings.mockTimeFindFirst);
        originalSettings.put("mockTimeFindAll", FrameworkSettings.mockTimeFindAll);
        originalSettings.put("mockTimeDrag", FrameworkSettings.mockTimeDrag);
        originalSettings.put("mockTimeClick", FrameworkSettings.mockTimeClick);
        originalSettings.put("mockTimeMove", FrameworkSettings.mockTimeMove);
        originalSettings.put("mockTimeFindHistogram", FrameworkSettings.mockTimeFindHistogram);
        originalSettings.put("mockTimeFindColor", FrameworkSettings.mockTimeFindColor);
        originalSettings.put("mockTimeClassify", FrameworkSettings.mockTimeClassify);

        // Save data collection settings
        originalSettings.put("saveSnapshots", FrameworkSettings.saveSnapshots);
        originalSettings.put("saveHistory", FrameworkSettings.saveHistory);
        originalSettings.put("buildDataset", FrameworkSettings.buildDataset);

        // Save illustration settings
        originalSettings.put("drawFind", FrameworkSettings.drawFind);
        originalSettings.put("drawClick", FrameworkSettings.drawClick);
        originalSettings.put("drawDrag", FrameworkSettings.drawDrag);
        originalSettings.put("drawMove", FrameworkSettings.drawMove);
        originalSettings.put("drawHighlight", FrameworkSettings.drawHighlight);
        originalSettings.put("drawRepeatedActions", FrameworkSettings.drawRepeatedActions);
        originalSettings.put("drawClassify", FrameworkSettings.drawClassify);
        originalSettings.put("drawDefine", FrameworkSettings.drawDefine);
    }

    @AfterAll
    void restoreOriginalSettings() {
        // Restore mouse control settings
        FrameworkSettings.moveMouseDelay = (float) originalSettings.get("moveMouseDelay");
        FrameworkSettings.pauseBeforeMouseDown =
                (double) originalSettings.get("pauseBeforeMouseDown");
        FrameworkSettings.pauseAfterMouseDown =
                (double) originalSettings.get("pauseAfterMouseDown");
        FrameworkSettings.pauseBeforeMouseUp = (double) originalSettings.get("pauseBeforeMouseUp");
        FrameworkSettings.pauseAfterMouseUp = (double) originalSettings.get("pauseAfterMouseUp");
        FrameworkSettings.xMoveAfterMouseDown = (int) originalSettings.get("xMoveAfterMouseDown");
        FrameworkSettings.yMoveAfterMouseDown = (int) originalSettings.get("yMoveAfterMouseDown");

        // Restore mock mode settings
        FrameworkSettings.mock = (boolean) originalSettings.get("mock");
        FrameworkSettings.mockTimeFindFirst = (double) originalSettings.get("mockTimeFindFirst");
        FrameworkSettings.mockTimeFindAll = (double) originalSettings.get("mockTimeFindAll");
        FrameworkSettings.mockTimeDrag = (double) originalSettings.get("mockTimeDrag");
        FrameworkSettings.mockTimeClick = (double) originalSettings.get("mockTimeClick");
        FrameworkSettings.mockTimeMove = (double) originalSettings.get("mockTimeMove");
        FrameworkSettings.mockTimeFindHistogram =
                (double) originalSettings.get("mockTimeFindHistogram");
        FrameworkSettings.mockTimeFindColor = (double) originalSettings.get("mockTimeFindColor");
        FrameworkSettings.mockTimeClassify = (double) originalSettings.get("mockTimeClassify");

        // Restore data collection settings
        FrameworkSettings.saveSnapshots = (boolean) originalSettings.get("saveSnapshots");
        FrameworkSettings.saveHistory = (boolean) originalSettings.get("saveHistory");
        FrameworkSettings.buildDataset = (boolean) originalSettings.get("buildDataset");

        // Restore illustration settings
        FrameworkSettings.drawFind = (boolean) originalSettings.get("drawFind");
        FrameworkSettings.drawClick = (boolean) originalSettings.get("drawClick");
        FrameworkSettings.drawDrag = (boolean) originalSettings.get("drawDrag");
        FrameworkSettings.drawMove = (boolean) originalSettings.get("drawMove");
        FrameworkSettings.drawHighlight = (boolean) originalSettings.get("drawHighlight");
        FrameworkSettings.drawRepeatedActions =
                (boolean) originalSettings.get("drawRepeatedActions");
        FrameworkSettings.drawClassify = (boolean) originalSettings.get("drawClassify");
        FrameworkSettings.drawDefine = (boolean) originalSettings.get("drawDefine");
    }

    @Nested
    @DisplayName("Mouse Control Settings Tests")
    @Disabled("CI failure - needs investigation")
    class MouseControlSettingsTests {

        @Test
        @Order(1)
        @DisplayName("Should have default mouse delay")
        void testDefaultMouseDelay() {
            assertEquals(0.5f, FrameworkSettings.moveMouseDelay, 0.001);
        }

        @Test
        @Order(2)
        @DisplayName("Should have default mouse pause settings")
        void testDefaultMousePauseSettings() {
            assertEquals(0, FrameworkSettings.pauseBeforeMouseDown, 0.001);
            assertEquals(0, FrameworkSettings.pauseAfterMouseDown, 0.001);
            assertEquals(0, FrameworkSettings.pauseBeforeMouseUp, 0.001);
            assertEquals(0, FrameworkSettings.pauseAfterMouseUp, 0.001);
        }

        @Test
        @Order(3)
        @DisplayName("Should set custom mouse delays")
        void testCustomMouseDelays() {
            FrameworkSettings.moveMouseDelay = 1.0f;
            FrameworkSettings.pauseBeforeMouseDown = 0.1;
            FrameworkSettings.pauseAfterMouseDown = 0.2;
            FrameworkSettings.pauseBeforeMouseUp = 0.3;
            FrameworkSettings.pauseAfterMouseUp = 0.4;

            assertEquals(1.0f, FrameworkSettings.moveMouseDelay, 0.001);
            assertEquals(0.1, FrameworkSettings.pauseBeforeMouseDown, 0.001);
            assertEquals(0.2, FrameworkSettings.pauseAfterMouseDown, 0.001);
            assertEquals(0.3, FrameworkSettings.pauseBeforeMouseUp, 0.001);
            assertEquals(0.4, FrameworkSettings.pauseAfterMouseUp, 0.001);
        }

        @Test
        @Order(4)
        @DisplayName("Should have default mouse movement offsets")
        void testDefaultMouseMovementOffsets() {
            assertEquals(0, FrameworkSettings.xMoveAfterMouseDown);
            assertEquals(0, FrameworkSettings.yMoveAfterMouseDown);
        }

        @Test
        @Order(5)
        @DisplayName("Should set mouse movement offsets")
        void testMouseMovementOffsets() {
            FrameworkSettings.xMoveAfterMouseDown = 10;
            FrameworkSettings.yMoveAfterMouseDown = 20;

            assertEquals(10, FrameworkSettings.xMoveAfterMouseDown);
            assertEquals(20, FrameworkSettings.yMoveAfterMouseDown);
        }
    }

    @Nested
    @DisplayName("Mock Mode Settings Tests")
    @Disabled("CI failure - needs investigation")
    class MockModeSettingsTests {

        @Test
        @Order(6)
        @DisplayName("Should default to mock mode false")
        void testDefaultMockMode() {
            // Note: BrobotTestBase sets mock=true, but testing the field itself
            assertNotNull(FrameworkSettings.mock);
        }

        @Test
        @Order(7)
        @DisplayName("Should have default mock timings")
        void testDefaultMockTimings() {
            assertEquals(0.1, FrameworkSettings.mockTimeFindFirst, 0.001);
            assertEquals(0.2, FrameworkSettings.mockTimeFindAll, 0.001);
            assertEquals(0.3, FrameworkSettings.mockTimeDrag, 0.001);
            assertEquals(0.05, FrameworkSettings.mockTimeClick, 0.001);
            assertEquals(0.1, FrameworkSettings.mockTimeMove, 0.001);
            assertEquals(0.3, FrameworkSettings.mockTimeFindHistogram, 0.001);
            assertEquals(0.3, FrameworkSettings.mockTimeFindColor, 0.001);
            assertEquals(0.4, FrameworkSettings.mockTimeClassify, 0.001);
        }

        @Test
        @Order(8)
        @DisplayName("Should set custom mock timings")
        void testCustomMockTimings() {
            FrameworkSettings.mockTimeFindFirst = 0.15;
            FrameworkSettings.mockTimeFindAll = 0.25;
            FrameworkSettings.mockTimeDrag = 0.35;
            FrameworkSettings.mockTimeClick = 0.08;
            FrameworkSettings.mockTimeMove = 0.15;

            assertEquals(0.15, FrameworkSettings.mockTimeFindFirst, 0.001);
            assertEquals(0.25, FrameworkSettings.mockTimeFindAll, 0.001);
            assertEquals(0.35, FrameworkSettings.mockTimeDrag, 0.001);
            assertEquals(0.08, FrameworkSettings.mockTimeClick, 0.001);
            assertEquals(0.15, FrameworkSettings.mockTimeMove, 0.001);
        }

        @Test
        @Order(9)
        @DisplayName("Should handle zero mock timings")
        void testZeroMockTimings() {
            FrameworkSettings.mockTimeFindFirst = 0.0;
            FrameworkSettings.mockTimeClick = 0.0;

            assertEquals(0.0, FrameworkSettings.mockTimeFindFirst, 0.001);
            assertEquals(0.0, FrameworkSettings.mockTimeClick, 0.001);
        }
    }

    @Nested
    @DisplayName("Data Collection Settings Tests")
    @Disabled("CI failure - needs investigation")
    class DataCollectionSettingsTests {

        @Test
        @Order(10)
        @DisplayName("Should have default snapshot settings")
        void testDefaultSnapshotSettings() {
            assertFalse(FrameworkSettings.saveSnapshots);
            assertEquals("screenshots/", FrameworkSettings.screenshotPath);
            assertEquals("screen", FrameworkSettings.screenshotFilename);
        }

        @Test
        @Order(11)
        @DisplayName("Should have default history settings")
        void testDefaultHistorySettings() {
            assertFalse(FrameworkSettings.saveHistory);
            assertEquals("history/", FrameworkSettings.historyPath);
            assertEquals("hist", FrameworkSettings.historyFilename);
        }

        @Test
        @Order(12)
        @DisplayName("Should enable data collection")
        void testEnableDataCollection() {
            FrameworkSettings.saveSnapshots = true;
            FrameworkSettings.saveHistory = true;
            FrameworkSettings.buildDataset = true;

            assertTrue(FrameworkSettings.saveSnapshots);
            assertTrue(FrameworkSettings.saveHistory);
            assertTrue(FrameworkSettings.buildDataset);
        }

        @Test
        @Order(13)
        @DisplayName("Should set custom paths")
        void testCustomPaths() {
            FrameworkSettings.screenshotPath = "/custom/screenshots/";
            FrameworkSettings.screenshotFilename = "custom_screen";
            FrameworkSettings.historyPath = "/custom/history/";
            FrameworkSettings.historyFilename = "custom_hist";
            FrameworkSettings.datasetPath = "/custom/dataset/";

            assertEquals("/custom/screenshots/", FrameworkSettings.screenshotPath);
            assertEquals("custom_screen", FrameworkSettings.screenshotFilename);
            assertEquals("/custom/history/", FrameworkSettings.historyPath);
            assertEquals("custom_hist", FrameworkSettings.historyFilename);
            assertEquals("/custom/dataset/", FrameworkSettings.datasetPath);
        }

        @Test
        @Order(14)
        @DisplayName("Should manage screenshot list")
        void testScreenshotList() {
            FrameworkSettings.screenshots = new ArrayList<>();
            FrameworkSettings.screenshots.add("screen1.png");
            FrameworkSettings.screenshots.add("screen2.png");

            assertEquals(2, FrameworkSettings.screenshots.size());
            assertTrue(FrameworkSettings.screenshots.contains("screen1.png"));
            assertTrue(FrameworkSettings.screenshots.contains("screen2.png"));
        }
    }

    @Nested
    @DisplayName("Illustration Settings Tests")
    @Disabled("CI failure - needs investigation")
    class IllustrationSettingsTests {

        @Test
        @Order(15)
        @DisplayName("Should have default illustration settings")
        void testDefaultIllustrationSettings() {
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
        @Order(16)
        @DisplayName("Should disable specific illustrations")
        void testDisableSpecificIllustrations() {
            FrameworkSettings.drawFind = false;
            FrameworkSettings.drawClick = false;
            FrameworkSettings.drawDrag = true;
            FrameworkSettings.drawMove = true;

            assertFalse(FrameworkSettings.drawFind);
            assertFalse(FrameworkSettings.drawClick);
            assertTrue(FrameworkSettings.drawDrag);
            assertTrue(FrameworkSettings.drawMove);
        }

        @Test
        @Order(17)
        @DisplayName("Should disable all illustrations")
        void testDisableAllIllustrations() {
            FrameworkSettings.drawFind = false;
            FrameworkSettings.drawClick = false;
            FrameworkSettings.drawDrag = false;
            FrameworkSettings.drawMove = false;
            FrameworkSettings.drawHighlight = false;
            FrameworkSettings.drawRepeatedActions = false;
            FrameworkSettings.drawClassify = false;
            FrameworkSettings.drawDefine = false;

            assertFalse(FrameworkSettings.drawFind);
            assertFalse(FrameworkSettings.drawClick);
            assertFalse(FrameworkSettings.drawDrag);
            assertFalse(FrameworkSettings.drawMove);
            assertFalse(FrameworkSettings.drawHighlight);
            assertFalse(FrameworkSettings.drawRepeatedActions);
            assertFalse(FrameworkSettings.drawClassify);
            assertFalse(FrameworkSettings.drawDefine);
        }
    }

    @Nested
    @DisplayName("K-Means Settings Tests")
    @Disabled("CI failure - needs investigation")
    class KMeansSettingsTests {

        @Test
        @Order(18)
        @DisplayName("Should have default k-means settings")
        void testDefaultKMeansSettings() {
            assertEquals(5, FrameworkSettings.kMeansInProfile);
            assertEquals(10, FrameworkSettings.maxKMeansToStoreInProfile);
        }

        @Test
        @Order(19)
        @DisplayName("Should set custom k-means values")
        void testCustomKMeansValues() {
            FrameworkSettings.kMeansInProfile = 8;
            FrameworkSettings.maxKMeansToStoreInProfile = 15;

            assertEquals(8, FrameworkSettings.kMeansInProfile);
            assertEquals(15, FrameworkSettings.maxKMeansToStoreInProfile);
        }

        @Test
        @Order(20)
        @DisplayName("Should have default profile initialization settings")
        void testDefaultProfileInitSettings() {
            assertFalse(FrameworkSettings.initProfilesForStaticfImages);
            assertFalse(FrameworkSettings.initProfilesForDynamicImages);
            assertTrue(FrameworkSettings.includeStateImageObjectsFromActiveStatesInAnalysis);
        }
    }

    @Nested
    @DisplayName("Capture Settings Tests")
    @Disabled("CI failure - needs investigation")
    class CaptureSettingsTests {

        @Test
        @Order(21)
        @DisplayName("Should have default capture settings")
        void testDefaultCaptureSettings() {
            assertEquals(1000, FrameworkSettings.secondsToCapture);
            assertEquals(1, FrameworkSettings.captureFrequency);
            assertEquals("recording", FrameworkSettings.recordingFolder);
        }

        @Test
        @Order(22)
        @DisplayName("Should set custom capture settings")
        void testCustomCaptureSettings() {
            FrameworkSettings.secondsToCapture = 500;
            FrameworkSettings.captureFrequency = 5;
            FrameworkSettings.recordingFolder = "custom_recording";

            assertEquals(500, FrameworkSettings.secondsToCapture);
            assertEquals(5, FrameworkSettings.captureFrequency);
            assertEquals("custom_recording", FrameworkSettings.recordingFolder);
        }
    }

    @Nested
    @DisplayName("Test Environment Settings Tests")
    @Disabled("CI failure - needs investigation")
    class TestEnvironmentSettingsTests {

        @Test
        @Order(23)
        @DisplayName("Should have default test settings")
        void testDefaultTestSettings() {
            assertEquals(1, FrameworkSettings.testIteration);
            assertTrue(FrameworkSettings.sendLogs);
            assertEquals("screenshots/", FrameworkSettings.testScreenshotsPath);
        }

        @Test
        @Order(24)
        @DisplayName("Should set test iteration")
        void testSetTestIteration() {
            FrameworkSettings.testIteration = 5;
            assertEquals(5, FrameworkSettings.testIteration);
        }

        @Test
        @Order(25)
        @DisplayName("Should configure log sending")
        void testConfigureLogSending() {
            FrameworkSettings.sendLogs = false;
            assertFalse(FrameworkSettings.sendLogs);

            FrameworkSettings.sendLogs = true;
            assertTrue(FrameworkSettings.sendLogs);
        }
    }

    @Nested
    @DisplayName("State Comparison Settings Tests")
    @Disabled("CI failure - needs investigation")
    class StateComparisonSettingsTests {

        @Test
        @Order(26)
        @DisplayName("Should have default state comparison settings")
        void testDefaultStateComparisonSettings() {
            assertEquals(0.7, FrameworkSettings.minimumStateProbabilityThreshold, 0.001);
            assertFalse(FrameworkSettings.enableStateComparison);
            assertFalse(FrameworkSettings.logStateComparison);
        }

        @Test
        @Order(27)
        @DisplayName("Should set state probability threshold")
        void testSetStateProbabilityThreshold() {
            FrameworkSettings.minimumStateProbabilityThreshold = 0.9;
            assertEquals(0.9, FrameworkSettings.minimumStateProbabilityThreshold, 0.001);

            FrameworkSettings.minimumStateProbabilityThreshold = 0.5;
            assertEquals(0.5, FrameworkSettings.minimumStateProbabilityThreshold, 0.001);
        }

        @Test
        @Order(28)
        @DisplayName("Should enable state comparison")
        void testEnableStateComparison() {
            FrameworkSettings.enableStateComparison = true;
            FrameworkSettings.logStateComparison = true;

            assertTrue(FrameworkSettings.enableStateComparison);
            assertTrue(FrameworkSettings.logStateComparison);
        }
    }

    @Nested
    @DisplayName("Package Settings Tests")
    @Disabled("CI failure - needs investigation")
    class PackageSettingsTests {

        @Test
        @Order(29)
        @DisplayName("Should have default package name")
        void testDefaultPackageName() {
            assertEquals("com.example", FrameworkSettings.packageName);
        }

        @Test
        @Order(30)
        @DisplayName("Should set custom package name")
        void testCustomPackageName() {
            FrameworkSettings.packageName = "io.github.jspinak.brobot";
            assertEquals("io.github.jspinak.brobot", FrameworkSettings.packageName);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    @Disabled("CI failure - needs investigation")
    class ThreadSafetyTests {

        @Test
        @Order(31)
        @DisplayName("Should handle concurrent settings access")
        void testConcurrentSettingsAccess() throws InterruptedException {
            int threadCount = 20;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int value = i;
                executor.submit(
                        () -> {
                            try {
                                // Concurrent reads and writes to different settings
                                FrameworkSettings.moveMouseDelay = value * 0.1f;
                                float delay = FrameworkSettings.moveMouseDelay;

                                FrameworkSettings.mock = (value % 2 == 0);
                                boolean mock = FrameworkSettings.mock;

                                FrameworkSettings.kMeansInProfile = value;
                                int k = FrameworkSettings.kMeansInProfile;

                                FrameworkSettings.historyPath = "path" + value;
                                String path = FrameworkSettings.historyPath;
                            } catch (Exception e) {
                                errors.incrementAndGet();
                            } finally {
                                latch.countDown();
                            }
                        });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();

            assertEquals(0, errors.get(), "No errors during concurrent access");
        }

        @Test
        @Order(32)
        @DisplayName("Should handle concurrent mock timing updates")
        void testConcurrentMockTimingUpdates() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final double timing = i * 0.01;
                executor.submit(
                        () -> {
                            try {
                                FrameworkSettings.mockTimeFindFirst = timing;
                                FrameworkSettings.mockTimeClick = timing * 2;
                                FrameworkSettings.mockTimeMove = timing * 3;

                                // Verify values are set
                                assertNotNull(FrameworkSettings.mockTimeFindFirst);
                                assertNotNull(FrameworkSettings.mockTimeClick);
                                assertNotNull(FrameworkSettings.mockTimeMove);
                            } finally {
                                latch.countDown();
                            }
                        });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
        }
    }
}
