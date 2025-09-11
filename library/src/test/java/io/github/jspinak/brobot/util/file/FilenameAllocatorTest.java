package io.github.jspinak.brobot.util.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.test.ConcurrentTestBase;
import io.github.jspinak.brobot.test.annotations.Flaky;
import io.github.jspinak.brobot.test.annotations.Flaky.FlakyCause;
import io.github.jspinak.brobot.test.utils.ConcurrentTestHelper;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

/**
 * Comprehensive test suite for FilenameAllocator - filename reservation system. Tests unique
 * filename generation, reservation tracking, and collision avoidance. Uses ConcurrentTestBase for
 * thread-safe parallel execution.
 */
@DisplayName("FilenameAllocator Tests")
@ResourceLock(value = ConcurrentTestBase.ResourceLocks.FILE_SYSTEM)
public class FilenameAllocatorTest extends ConcurrentTestBase {

    private FilenameAllocator allocator;

    @Mock private ImageFileUtilities mockImageUtils;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        allocator = new FilenameAllocator(mockImageUtils);
    }

    @Nested
    @DisplayName("Basic Filename Reservation")
    class BasicFilenameReservation {

        @Test
        @DisplayName("Reserve first filename without collision")
        public void testReserveFirstFilename() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String result = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png", result);
            verify(mockImageUtils, times(1)).fileExists("/path/file.png");
        }

        @Test
        @DisplayName("Reserve filename with filesystem collision")
        public void testReserveWithFilesystemCollision() {
            when(mockImageUtils.fileExists("/path/file.png")).thenReturn(true);
            when(mockImageUtils.fileExists("/path/file.png_1")).thenReturn(false);

            String result = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png_1", result);
            verify(mockImageUtils).fileExists("/path/file.png");
            verify(mockImageUtils).fileExists("/path/file.png_1");
        }

        @Test
        @DisplayName("Reserve filename with multiple collisions")
        public void testReserveWithMultipleCollisions() {
            when(mockImageUtils.fileExists("/path/file.png")).thenReturn(true);
            when(mockImageUtils.fileExists("/path/file.png_1")).thenReturn(true);
            when(mockImageUtils.fileExists("/path/file.png_2")).thenReturn(true);
            when(mockImageUtils.fileExists("/path/file.png_3")).thenReturn(false);

            String result = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png_3", result);
        }

        @Test
        @DisplayName("Reserve same prefix multiple times")
        public void testReserveSamePrefixMultipleTimes() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String first = allocator.reserveFreePath("/path/", "file.png");
            String second = allocator.reserveFreePath("/path/", "file.png");
            String third = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png", first);
            assertEquals("/path/file.png_1", second);
            assertEquals("/path/file.png_2", third);
        }
    }

    @Nested
    @DisplayName("Manual Filename Addition")
    class ManualFilenameAddition {

        @Test
        @DisplayName("Add and check filename existence")
        public void testAddAndCheckFilename() {
            assertFalse(allocator.filenameExists("test.txt"));

            allocator.addFilename("test.txt");

            assertTrue(allocator.filenameExists("test.txt"));
        }

        @Test
        @DisplayName("Add multiple filenames")
        public void testAddMultipleFilenames() {
            allocator.addFilename("file1.txt");
            allocator.addFilename("file2.txt");
            allocator.addFilename("file3.txt");

            assertTrue(allocator.filenameExists("file1.txt"));
            assertTrue(allocator.filenameExists("file2.txt"));
            assertTrue(allocator.filenameExists("file3.txt"));
            assertFalse(allocator.filenameExists("file4.txt"));
        }

        @Test
        @DisplayName("Add duplicate filenames")
        public void testAddDuplicateFilenames() {
            allocator.addFilename("duplicate.txt");
            allocator.addFilename("duplicate.txt");

            assertTrue(allocator.filenameExists("duplicate.txt"));
        }
    }

    @Nested
    @DisplayName("Collision Avoidance")
    class CollisionAvoidance {

        @Test
        @DisplayName("Avoid collision with manually added filename")
        public void testAvoidManuallyAddedCollision() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            allocator.addFilename("/path/file.png");

            String result = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png_1", result);
        }

        @Test
        @DisplayName("Avoid collision with previously reserved filename")
        public void testAvoidPreviouslyReservedCollision() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String first = allocator.reserveFreePath("/path/", "file.png");
            String second = allocator.reserveFreePath("/path/", "file.png");

            assertNotEquals(first, second);
            assertTrue(allocator.filenameExists(first));
            assertTrue(allocator.filenameExists(second));
        }

        @Test
        @DisplayName("Mixed filesystem and reservation collisions")
        public void testMixedCollisions() {
            when(mockImageUtils.fileExists("/path/file.png")).thenReturn(true); // Filesystem
            when(mockImageUtils.fileExists("/path/file.png_1")).thenReturn(false);
            when(mockImageUtils.fileExists("/path/file.png_2")).thenReturn(true); // Filesystem
            when(mockImageUtils.fileExists("/path/file.png_3")).thenReturn(false);

            allocator.addFilename("/path/file.png_1"); // Manual reservation

            String result = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png_3", result);
        }
    }

    @Nested
    @DisplayName("Index Management")
    class IndexManagement {

        @Test
        @DisplayName("Index increments for same prefix")
        public void testIndexIncrement() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String file1 = allocator.reserveFreePath("/prefix/", "suffix.txt");
            String file2 = allocator.reserveFreePath("/prefix/", "suffix.txt");
            String file3 = allocator.reserveFreePath("/prefix/", "suffix.txt");

            assertTrue(file1.equals("/prefix/suffix.txt"));
            assertTrue(file2.endsWith("_1"));
            assertTrue(file3.endsWith("_2"));
        }

        @Test
        @DisplayName("Different prefixes have independent indices")
        public void testIndependentPrefixIndices() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String pathA1 = allocator.reserveFreePath("/pathA/", "file.txt");
            String pathB1 = allocator.reserveFreePath("/pathB/", "file.txt");
            String pathA2 = allocator.reserveFreePath("/pathA/", "file.txt");
            String pathB2 = allocator.reserveFreePath("/pathB/", "file.txt");

            assertEquals("/pathA/file.txt", pathA1);
            assertEquals("/pathB/file.txt", pathB1);
            assertEquals("/pathA/file.txt_1", pathA2);
            assertEquals("/pathB/file.txt_1", pathB2);
        }

        @Test
        @DisplayName("Index skips existing filesystem files")
        public void testIndexSkipsExistingFiles() {
            when(mockImageUtils.fileExists("/path/file.png")).thenReturn(false);
            when(mockImageUtils.fileExists("/path/file.png_1")).thenReturn(true);
            when(mockImageUtils.fileExists("/path/file.png_2")).thenReturn(true);
            when(mockImageUtils.fileExists("/path/file.png_3")).thenReturn(false);

            String first = allocator.reserveFreePath("/path/", "file.png");
            String second = allocator.reserveFreePath("/path/", "file.png");

            assertEquals("/path/file.png", first);
            assertEquals("/path/file.png_3", second);
        }
    }

    @Nested
    @DisplayName("Path and Suffix Variations")
    class PathAndSuffixVariations {

        @ParameterizedTest
        @CsvSource({
            "/path/to/, file.txt, /path/to/file.txt",
            "/path/, image.png, /path/image.png",
            "relative/, doc.pdf, relative/doc.pdf",
            ", file.ext, file.ext",
            "prefix_, suffix, prefix_suffix"
        })
        @DisplayName("Various path and suffix combinations")
        public void testVariousPathSuffixCombinations(
                String prefix, String suffix, String expected) {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String result = allocator.reserveFreePath(prefix, suffix);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Empty prefix and suffix")
        public void testEmptyPrefixAndSuffix() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String result = allocator.reserveFreePath("", "");

            assertEquals("", result);
        }

        @Test
        @DisplayName("Paths with special characters")
        public void testSpecialCharacterPaths() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String result = allocator.reserveFreePath("/path-with.special_chars/", "file[1].txt");

            assertEquals("/path-with.special_chars/file[1].txt", result);
        }

        @Test
        @DisplayName("Paths with spaces")
        public void testPathsWithSpaces() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String result = allocator.reserveFreePath("/path with spaces/", "file name.txt");

            assertEquals("/path with spaces/file name.txt", result);
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Screenshot filename generation")
        public void testScreenshotGeneration() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String timestamp = "20240115_143022";
            String screenshot1 = allocator.reserveFreePath("/screenshots/", timestamp + ".png");
            String screenshot2 = allocator.reserveFreePath("/screenshots/", timestamp + ".png");

            assertEquals("/screenshots/" + timestamp + ".png", screenshot1);
            assertEquals("/screenshots/" + timestamp + ".png_1", screenshot2);
        }

        @Test
        @DisplayName("Batch report generation")
        public void testBatchReportGeneration() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            for (int i = 0; i < 5; i++) {
                String report = allocator.reserveFreePath("/reports/daily_", "report.pdf");
                assertTrue(allocator.filenameExists(report));

                if (i == 0) {
                    assertEquals("/reports/daily_report.pdf", report);
                } else {
                    assertTrue(report.matches("/reports/daily_report\\.pdf_\\d+"));
                }
            }
        }

        @Test
        @DisplayName("Illustration file generation")
        public void testIllustrationGeneration() {
            when(mockImageUtils.fileExists("/illustrations/state_diagram.svg")).thenReturn(true);
            when(mockImageUtils.fileExists("/illustrations/state_diagram.svg_1")).thenReturn(true);
            when(mockImageUtils.fileExists("/illustrations/state_diagram.svg_2")).thenReturn(false);

            String result = allocator.reserveFreePath("/illustrations/", "state_diagram.svg");

            assertEquals("/illustrations/state_diagram.svg_2", result);
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Handle large number of reservations")
        public void testLargeNumberOfReservations() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            Set<String> reserved = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                String filename = allocator.reserveFreePath("/bulk/", "file.dat");
                assertTrue(reserved.add(filename), "Duplicate filename: " + filename);
            }

            assertEquals(1000, reserved.size());
        }

        @RepeatedTest(5)
        @DisplayName("Consistent reservation order")
        public void testConsistentReservationOrder() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String first = allocator.reserveFreePath("/test/", "file.txt");
            String second = allocator.reserveFreePath("/test/", "file.txt");
            String third = allocator.reserveFreePath("/test/", "file.txt");

            assertEquals("/test/file.txt", first);
            assertEquals("/test/file.txt_1", second);
            assertEquals("/test/file.txt_2", third);
        }

        @Test
        @DisplayName("Performance with high index numbers")
        public void testHighIndexPerformance() {
            // Simulate many existing files
            when(mockImageUtils.fileExists(anyString()))
                    .thenAnswer(
                            invocation -> {
                                String filename = invocation.getArgument(0);
                                if (filename.equals("/path/file.txt")) return true;
                                if (filename.matches("/path/file\\.txt_\\d+")) {
                                    int num =
                                            Integer.parseInt(
                                                    filename.substring(
                                                            filename.lastIndexOf('_') + 1));
                                    return num < 100; // First 100 exist
                                }
                                return false;
                            });

            String result = allocator.reserveFreePath("/path/", "file.txt");

            assertEquals("/path/file.txt_100", result);
        }
    }

    @Nested
    @DisplayName("Thread Safety Concerns")
    class ThreadSafetyConcerns {

        @Test
        @DisplayName("Concurrent reservations may cause issues")
        @Flaky(
                reason = "Race conditions in concurrent filename allocation",
                cause = FlakyCause.CONCURRENCY)
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        public void testConcurrentReservations() throws Exception {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            int threadCount = 10;
            int reservationsPerThread = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            Set<String> allReservations = ConcurrentHashMap.newKeySet();
            AtomicInteger duplicates = new AtomicInteger(0);

            // Use testExecutor from ConcurrentTestBase for automatic cleanup
            testExecutor = Executors.newFixedThreadPool(threadCount);

            for (int t = 0; t < threadCount; t++) {
                testExecutor.submit(
                        () -> {
                            try {
                                startLatch.await();
                                for (int i = 0; i < reservationsPerThread; i++) {
                                    String filename =
                                            allocator.reserveFreePath("/concurrent/", "file.txt");
                                    if (!allReservations.add(filename)) {
                                        duplicates.incrementAndGet();
                                    }
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                endLatch.countDown();
                            }
                        });
            }

            startLatch.countDown(); // Start all threads
            assertTrue(
                    ConcurrentTestHelper.awaitLatch(
                            endLatch, Duration.ofSeconds(5), "Concurrent reservation completion"));

            // Note: This test demonstrates thread safety issues
            // Duplicates may occur due to lack of synchronization
            System.out.println("Duplicates found: " + duplicates.get());
            System.out.println("Unique reservations: " + allReservations.size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Very long prefix and suffix")
        public void testVeryLongPrefixSuffix() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String longPrefix = "a".repeat(500) + "/";
            String longSuffix = "b".repeat(500) + ".ext";

            String result = allocator.reserveFreePath(longPrefix, longSuffix);

            assertEquals(longPrefix + longSuffix, result);
            assertTrue(allocator.filenameExists(result));
        }

        @Test
        @DisplayName("Unicode in filenames")
        public void testUnicodeFilenames() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            String result = allocator.reserveFreePath("/unicode/", "文件名.txt");

            assertEquals("/unicode/文件名.txt", result);
            assertTrue(allocator.filenameExists(result));
        }

        @Test
        @DisplayName("Null handling")
        public void testNullHandling() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            // Should handle nulls gracefully - both become empty strings
            String result = allocator.reserveFreePath(null, null);
            assertEquals("", result); // both null become empty strings
        }

        @Test
        @DisplayName("Check non-existent filename")
        public void testCheckNonExistent() {
            assertFalse(allocator.filenameExists("never-added.txt"));
            assertFalse(allocator.filenameExists(""));
            assertFalse(allocator.filenameExists(null));
        }
    }

    @Nested
    @DisplayName("Memory Growth")
    class MemoryGrowth {

        @Test
        @DisplayName("Filenames list grows unbounded")
        public void testUnboundedGrowth() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            // Reserve many filenames
            for (int i = 0; i < 100; i++) {
                allocator.addFilename("file" + i + ".txt");
            }

            // All should be tracked
            for (int i = 0; i < 100; i++) {
                assertTrue(allocator.filenameExists("file" + i + ".txt"));
            }

            // Note: In production, this could lead to memory issues
            // No cleanup mechanism exists
        }

        @Test
        @DisplayName("Index map growth with different prefixes")
        public void testIndexMapGrowth() {
            when(mockImageUtils.fileExists(anyString())).thenReturn(false);

            // Use many different prefixes
            for (int i = 0; i < 100; i++) {
                String prefix = "/prefix" + i + "/";
                allocator.reserveFreePath(prefix, "file.txt");
                allocator.reserveFreePath(prefix, "file.txt");
            }

            // Each prefix tracks its own index
            // Map grows with number of unique prefixes
        }
    }
}
