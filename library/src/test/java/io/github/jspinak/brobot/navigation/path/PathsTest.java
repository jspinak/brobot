package io.github.jspinak.brobot.navigation.path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for Paths - manages collections of navigation paths. Tests path
 * collection operations, sorting, cleaning, and scoring.
 */
@DisplayName("Paths Tests")
class PathsTest extends BrobotTestBase {

    private Paths paths;

    @Mock private Path mockPath1;

    @Mock private Path mockPath2;

    @Mock private Path mockPath3;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        paths = new Paths();
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should be empty when created")
        void testEmptyOnCreation() {
            // Assert
            assertTrue(paths.isEmpty());
            assertEquals(0, paths.getPaths().size());
        }

        @Test
        @DisplayName("Should add non-empty path")
        void testAddNonEmptyPath() {
            // Arrange
            when(mockPath1.isEmpty()).thenReturn(false);

            // Act
            paths.addPath(mockPath1);

            // Assert
            assertFalse(paths.isEmpty());
            assertEquals(1, paths.getPaths().size());
            assertTrue(paths.getPaths().contains(mockPath1));
        }

        @Test
        @DisplayName("Should not add empty path")
        void testNotAddEmptyPath() {
            // Arrange
            when(mockPath1.isEmpty()).thenReturn(true);

            // Act
            paths.addPath(mockPath1);

            // Assert
            assertTrue(paths.isEmpty());
            assertEquals(0, paths.getPaths().size());
        }

        @Test
        @DisplayName("Should create with initial paths")
        void testConstructorWithPaths() {
            // Arrange
            List<Path> initialPaths = Arrays.asList(mockPath1, mockPath2);

            // Act
            Paths pathsWithInitial = new Paths(initialPaths);

            // Assert
            assertEquals(2, pathsWithInitial.getPaths().size());
            assertTrue(pathsWithInitial.getPaths().contains(mockPath1));
            assertTrue(pathsWithInitial.getPaths().contains(mockPath2));
        }
    }

    @Nested
    @DisplayName("Sorting Operations")
    class SortingOperations {

        @Test
        @DisplayName("Should sort paths by score ascending")
        void testSortByScore() {
            // Arrange
            Path path1 = new Path();
            path1.setScore(30);
            Path path2 = new Path();
            path2.setScore(10);
            Path path3 = new Path();
            path3.setScore(20);

            Paths sortablePaths = new Paths(Arrays.asList(path1, path2, path3));

            // Act
            sortablePaths.sort();

            // Assert
            List<Path> sorted = sortablePaths.getPaths();
            assertEquals(10, sorted.get(0).getScore());
            assertEquals(20, sorted.get(1).getScore());
            assertEquals(30, sorted.get(2).getScore());
        }

        @Test
        @DisplayName("Should handle sorting empty paths")
        void testSortEmptyPaths() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> paths.sort());
            assertTrue(paths.isEmpty());
        }

        @Test
        @DisplayName("Should handle sorting single path")
        void testSortSinglePath() {
            // Arrange
            Path path = new Path();
            path.setScore(15);
            Paths singlePath = new Paths(List.of(path));

            // Act
            singlePath.sort();

            // Assert
            assertEquals(1, singlePath.getPaths().size());
            assertEquals(15, singlePath.getPaths().get(0).getScore());
        }

        @Test
        @DisplayName("Should maintain order for equal scores")
        void testSortEqualScores() {
            // Arrange
            Path path1 = new Path();
            path1.setScore(10);
            path1.getStates().add(1L); // Distinguish paths

            Path path2 = new Path();
            path2.setScore(10);
            path2.getStates().add(2L);

            Paths equalScorePaths = new Paths(Arrays.asList(path1, path2));

            // Act
            equalScorePaths.sort();

            // Assert
            List<Path> sorted = equalScorePaths.getPaths();
            assertEquals(10, sorted.get(0).getScore());
            assertEquals(10, sorted.get(1).getScore());
            // Original order should be maintained (stable sort)
            assertEquals(1L, sorted.get(0).getStates().get(0));
            assertEquals(2L, sorted.get(1).getStates().get(0));
        }
    }

    @Nested
    @DisplayName("Best Score Calculation")
    class BestScoreCalculation {

        @Test
        @DisplayName("Should get best score from paths")
        void testGetBestScore() {
            // Arrange
            Path path1 = new Path();
            path1.setScore(15);
            Path path2 = new Path();
            path2.setScore(25);
            Path path3 = new Path();
            path3.setScore(20);

            Paths scoredPaths = new Paths(Arrays.asList(path1, path2, path3));

            // Act
            int bestScore = scoredPaths.getBestScore();

            // Assert
            assertEquals(25, bestScore); // Highest score
        }

        @Test
        @DisplayName("Should return 0 for empty paths")
        void testBestScoreEmptyPaths() {
            // Act
            int bestScore = paths.getBestScore();

            // Assert
            assertEquals(0, bestScore);
        }

        @Test
        @DisplayName("Should handle negative scores")
        void testBestScoreWithNegatives() {
            // Arrange
            Path path1 = new Path();
            path1.setScore(-10);
            Path path2 = new Path();
            path2.setScore(-5);
            Path path3 = new Path();
            path3.setScore(-20);

            Paths negativePaths = new Paths(Arrays.asList(path1, path2, path3));

            // Act
            int bestScore = negativePaths.getBestScore();

            // Assert
            assertEquals(-5, bestScore); // Least negative is "best"
        }
    }

    @Nested
    @DisplayName("Equality Comparison")
    class EqualityComparison {

        @Test
        @DisplayName("Should be equal for same paths in same order")
        void testEqualSamePaths() {
            // Arrange
            Path path1 = new Path();
            path1.getStates().add(1L);
            Path path2 = new Path();
            path2.getStates().add(2L);

            Paths paths1 = new Paths(Arrays.asList(path1, path2));
            Paths paths2 = new Paths(Arrays.asList(path1, path2));

            // Act & Assert
            assertTrue(paths1.equals(paths2));
        }

        @Test
        @DisplayName("Should not be equal for different sizes")
        void testNotEqualDifferentSizes() {
            // Arrange
            when(mockPath1.equals(any())).thenReturn(true);

            Paths paths1 = new Paths(List.of(mockPath1));
            Paths paths2 = new Paths(Arrays.asList(mockPath1, mockPath2));

            // Act & Assert
            assertFalse(paths1.equals(paths2));
        }

        @Test
        @DisplayName("Should not be equal for different paths")
        void testNotEqualDifferentPaths() {
            // Arrange
            when(mockPath1.equals(mockPath1)).thenReturn(true);
            when(mockPath1.equals(mockPath2)).thenReturn(false);
            when(mockPath2.equals(mockPath2)).thenReturn(true);

            Paths paths1 = new Paths(List.of(mockPath1));
            Paths paths2 = new Paths(List.of(mockPath2));

            // Act & Assert
            assertFalse(paths1.equals(paths2));
        }

        @Test
        @DisplayName("Should not be equal for different order")
        void testNotEqualDifferentOrder() {
            // Arrange
            when(mockPath1.equals(mockPath1)).thenReturn(true);
            when(mockPath2.equals(mockPath2)).thenReturn(true);
            when(mockPath1.equals(mockPath2)).thenReturn(false);
            when(mockPath2.equals(mockPath1)).thenReturn(false);

            Paths paths1 = new Paths(Arrays.asList(mockPath1, mockPath2));
            Paths paths2 = new Paths(Arrays.asList(mockPath2, mockPath1));

            // Act & Assert
            assertFalse(paths1.equals(paths2));
        }

        @Test
        @DisplayName("Should be equal for empty paths")
        void testEqualEmptyPaths() {
            // Arrange
            Paths emptyPaths1 = new Paths();
            Paths emptyPaths2 = new Paths();

            // Act & Assert
            assertTrue(emptyPaths1.equals(emptyPaths2));
        }
    }

    @Nested
    @DisplayName("Path Cleaning")
    class PathCleaning {

        @Test
        @DisplayName("Should clean paths based on active states")
        void testCleanPaths() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 3L));
            Long failedTransition = 1L;

            Path cleanedPath1 = mock(Path.class);
            Path cleanedPath2 = mock(Path.class);

            when(mockPath1.isEmpty()).thenReturn(false);
            when(mockPath2.isEmpty()).thenReturn(false);
            when(mockPath1.cleanPath(activeStates, failedTransition)).thenReturn(cleanedPath1);
            when(mockPath2.cleanPath(activeStates, failedTransition)).thenReturn(cleanedPath2);
            when(cleanedPath1.isEmpty()).thenReturn(false);
            when(cleanedPath2.isEmpty()).thenReturn(false);

            Paths originalPaths = new Paths(Arrays.asList(mockPath1, mockPath2));

            // Act
            Paths cleanedPaths = originalPaths.cleanPaths(activeStates, failedTransition);

            // Assert
            assertEquals(2, cleanedPaths.getPaths().size());
            assertTrue(cleanedPaths.getPaths().contains(cleanedPath1));
            assertTrue(cleanedPaths.getPaths().contains(cleanedPath2));
        }

        @Test
        @DisplayName("Should filter out empty cleaned paths")
        void testFilterEmptyCleanedPaths() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L));
            Long failedTransition = 1L;

            Path cleanedPath = mock(Path.class);
            Path emptyPath = mock(Path.class);

            when(mockPath1.isEmpty()).thenReturn(false);
            when(mockPath2.isEmpty()).thenReturn(false);
            when(mockPath1.cleanPath(activeStates, failedTransition)).thenReturn(cleanedPath);
            when(mockPath2.cleanPath(activeStates, failedTransition)).thenReturn(emptyPath);
            when(cleanedPath.isEmpty()).thenReturn(false);
            when(emptyPath.isEmpty()).thenReturn(true); // This one should be filtered

            Paths originalPaths = new Paths(Arrays.asList(mockPath1, mockPath2));

            // Act
            Paths cleanedPaths = originalPaths.cleanPaths(activeStates, failedTransition);

            // Assert
            assertEquals(1, cleanedPaths.getPaths().size());
            assertTrue(cleanedPaths.getPaths().contains(cleanedPath));
            assertFalse(cleanedPaths.getPaths().contains(emptyPath));
        }

        @Test
        @DisplayName("Should handle empty active states")
        void testCleanPathsEmptyActiveStates() {
            // Arrange
            Set<Long> emptyStates = new HashSet<>();
            Long failedTransition = 1L;

            Path cleanedPath = mock(Path.class);
            when(mockPath1.isEmpty()).thenReturn(false);
            when(mockPath1.cleanPath(emptyStates, failedTransition)).thenReturn(cleanedPath);
            when(cleanedPath.isEmpty()).thenReturn(false);

            Paths originalPaths = new Paths(List.of(mockPath1));

            // Act
            Paths cleanedPaths = originalPaths.cleanPaths(emptyStates, failedTransition);

            // Assert
            assertNotNull(cleanedPaths);
            verify(mockPath1).cleanPath(emptyStates, failedTransition);
        }

        @Test
        @DisplayName("Should handle null failed transition")
        void testCleanPathsNullFailedTransition() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L));
            Long failedTransition = null;

            Path cleanedPath = mock(Path.class);
            when(mockPath1.isEmpty()).thenReturn(false);
            when(mockPath1.cleanPath(activeStates, failedTransition)).thenReturn(cleanedPath);
            when(cleanedPath.isEmpty()).thenReturn(false);

            Paths originalPaths = new Paths(List.of(mockPath1));

            // Act
            Paths cleanedPaths = originalPaths.cleanPaths(activeStates, failedTransition);

            // Assert
            assertEquals(1, cleanedPaths.getPaths().size());
            verify(mockPath1).cleanPath(activeStates, null);
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle large path collection")
        void testLargePathCollection() {
            // Arrange
            List<Path> largePaths = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Path path = new Path();
                path.setScore(i);
                largePaths.add(path);
            }

            Paths largeCollection = new Paths(largePaths);

            // Act
            largeCollection.sort();
            int bestScore = largeCollection.getBestScore();

            // Assert
            assertEquals(1000, largeCollection.getPaths().size());
            assertEquals(999, bestScore);
            assertEquals(0, largeCollection.getPaths().get(0).getScore());
            assertEquals(999, largeCollection.getPaths().get(999).getScore());
        }

        @Test
        @DisplayName("Should handle mixed operations sequence")
        void testMixedOperations() {
            // Arrange
            Path path1 = new Path();
            path1.setScore(20);
            path1.getStates().add(1L); // Make non-empty
            Path path2 = new Path();
            path2.setScore(10);
            path2.getStates().add(2L); // Make non-empty
            Path path3 = new Path();
            path3.setScore(30);
            path3.getStates().add(3L); // Make non-empty

            when(mockPath1.isEmpty()).thenReturn(true); // Will be filtered

            // Act
            paths.addPath(path1);
            paths.addPath(mockPath1); // Should not be added (empty)
            paths.addPath(path2);
            paths.sort();
            paths.addPath(path3); // Added after sort

            // Assert
            List<Path> pathList = paths.getPaths();
            assertEquals(3, pathList.size());
            // First two should be sorted (10, 20), then path3 added after sort
            assertEquals(10, pathList.get(0).getScore());
            assertEquals(20, pathList.get(1).getScore());
            assertEquals(30, pathList.get(2).getScore()); // Added after sort, remains at end
        }
    }
}
