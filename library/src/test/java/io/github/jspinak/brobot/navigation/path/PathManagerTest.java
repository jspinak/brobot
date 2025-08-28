package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for PathManager - manages path scoring and recovery.
 * Tests score calculation, path validation, and failure recovery scenarios.
 */
@DisplayName("PathManager Tests")
class PathManagerTest extends BrobotTestBase {
    
    private PathManager pathManager;
    
    @Mock
    private StateService mockStateService;
    
    @Mock
    private State mockState1;
    
    @Mock
    private State mockState2;
    
    @Mock
    private State mockState3;
    
    @Mock
    private State mockState4;
    
    @Mock
    private Paths mockPaths;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        pathManager = new PathManager(mockStateService);
        
        // Setup mock states with scores
        when(mockState1.getPathScore()).thenReturn(10);
        when(mockState2.getPathScore()).thenReturn(20);
        when(mockState3.getPathScore()).thenReturn(15);
        when(mockState4.getPathScore()).thenReturn(25);
        
        when(mockStateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(mockStateService.getState(2L)).thenReturn(Optional.of(mockState2));
        when(mockStateService.getState(3L)).thenReturn(Optional.of(mockState3));
        when(mockStateService.getState(4L)).thenReturn(Optional.of(mockState4));
    }
    
    @Nested
    @DisplayName("Score Calculation")
    class ScoreCalculation {
        
        @Test
        @DisplayName("Should calculate path score as sum of state scores")
        void testCalculatePathScore() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(45, path.getScore()); // 10 + 20 + 15
        }
        
        @Test
        @DisplayName("Should handle empty path")
        void testEmptyPathScore() {
            // Arrange
            Path path = new Path();
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(0, path.getScore());
        }
        
        @Test
        @DisplayName("Should handle path with single state")
        void testSingleStatePathScore() {
            // Arrange
            Path path = new Path();
            path.getStates().add(2L);
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(20, path.getScore());
        }
        
        @Test
        @DisplayName("Should skip non-existent states in score calculation")
        void testScoreWithNonExistentStates() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(999L); // Non-existent
            path.getStates().add(3L);
            
            when(mockStateService.getState(999L)).thenReturn(Optional.empty());
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(25, path.getScore()); // 10 + 0 + 15
        }
        
        @Test
        @DisplayName("Should handle path with duplicate states")
        void testDuplicateStatesScore() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(1L); // Duplicate
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(40, path.getScore()); // 10 + 20 + 10
        }
    }
    
    @Nested
    @DisplayName("Batch Score Updates")
    class BatchScoreUpdates {
        
        @Test
        @DisplayName("Should update scores for all paths")
        void testUpdateAllPathScores() {
            // Arrange
            Path path1 = new Path();
            path1.getStates().add(1L);
            path1.getStates().add(2L);
            
            Path path2 = new Path();
            path2.getStates().add(3L);
            path2.getStates().add(4L);
            
            List<Path> pathList = Arrays.asList(path1, path2);
            
            Paths paths = new Paths();
            paths.getPaths().addAll(pathList);
            
            // Act
            pathManager.updateScores(paths);
            
            // Assert
            assertEquals(30, path1.getScore()); // 10 + 20
            assertEquals(40, path2.getScore()); // 15 + 25
            verify(mockStateService, times(4)).getState(anyLong());
        }
        
        @Test
        @DisplayName("Should sort paths after updating scores")
        void testSortAfterUpdate() {
            // Arrange
            Paths paths = mock(Paths.class);
            List<Path> pathList = new ArrayList<>();
            when(paths.getPaths()).thenReturn(pathList);
            
            // Act
            pathManager.updateScores(paths);
            
            // Assert
            verify(paths).sort();
        }
        
        @Test
        @DisplayName("Should handle empty paths collection")
        void testUpdateEmptyPaths() {
            // Arrange
            Paths paths = new Paths();
            
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> pathManager.updateScores(paths));
        }
    }
    
    @Nested
    @DisplayName("Clean Paths Recovery")
    class CleanPathsRecovery {
        
        @Test
        @DisplayName("Should get clean paths after failure")
        void testGetCleanPaths() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 3L));
            Long failedTransitionStart = 1L;
            
            Paths originalPaths = mock(Paths.class);
            Paths cleanedPaths = new Paths();
            
            Path cleanPath1 = new Path();
            cleanPath1.getStates().add(2L);
            cleanPath1.getStates().add(3L);
            cleanedPaths.getPaths().add(cleanPath1);
            
            when(originalPaths.cleanPaths(activeStates, failedTransitionStart))
                .thenReturn(cleanedPaths);
            
            // Act
            Paths result = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getPaths().size());
            assertEquals(35, cleanPath1.getScore()); // 20 + 15
        }
        
        @Test
        @DisplayName("Should update scores in cleaned paths")
        void testUpdateScoresInCleanedPaths() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L));
            Long failedTransitionStart = 1L;
            
            Paths originalPaths = mock(Paths.class);
            Paths cleanedPaths = new Paths();
            
            Path cleanPath = new Path();
            cleanPath.getStates().add(2L);
            cleanPath.getStates().add(4L);
            cleanedPaths.getPaths().add(cleanPath);
            
            when(originalPaths.cleanPaths(activeStates, failedTransitionStart))
                .thenReturn(cleanedPaths);
            
            // Act
            Paths result = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
            
            // Assert
            assertEquals(45, cleanPath.getScore()); // 20 + 25
            assertTrue(result == cleanedPaths);
        }
        
        @Test
        @DisplayName("Should handle no active states")
        void testCleanPathsNoActiveStates() {
            // Arrange
            Set<Long> activeStates = new HashSet<>();
            Long failedTransitionStart = 1L;
            
            Paths originalPaths = mock(Paths.class);
            Paths cleanedPaths = new Paths();
            
            when(originalPaths.cleanPaths(activeStates, failedTransitionStart))
                .thenReturn(cleanedPaths);
            
            // Act
            Paths result = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.getPaths().size());
        }
        
        @Test
        @DisplayName("Should handle null failed transition")
        void testCleanPathsNullFailedTransition() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L));
            Long failedTransitionStart = null;
            
            Paths originalPaths = mock(Paths.class);
            Paths cleanedPaths = new Paths();
            
            Path path = new Path();
            path.getStates().addAll(Arrays.asList(1L, 2L, 3L));
            cleanedPaths.getPaths().add(path);
            
            when(originalPaths.cleanPaths(activeStates, failedTransitionStart))
                .thenReturn(cleanedPaths);
            
            // Act
            Paths result = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
            
            // Assert
            assertNotNull(result);
            assertEquals(45, path.getScore()); // 10 + 20 + 15
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle multiple path recovery")
        void testMultiplePathRecovery() {
            // Arrange
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 3L));
            Long failedTransitionStart = 1L;
            
            Paths originalPaths = mock(Paths.class);
            Paths cleanedPaths = new Paths();
            
            // Multiple alternative paths
            Path path1 = new Path();
            path1.getStates().addAll(Arrays.asList(2L, 3L, 4L));
            
            Path path2 = new Path();
            path2.getStates().addAll(Arrays.asList(3L, 4L));
            
            Path path3 = new Path();
            path3.getStates().addAll(Arrays.asList(2L, 4L));
            
            cleanedPaths.getPaths().addAll(Arrays.asList(path1, path2, path3));
            
            when(originalPaths.cleanPaths(activeStates, failedTransitionStart))
                .thenReturn(cleanedPaths);
            
            // Act
            Paths result = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
            
            // Assert
            assertEquals(3, result.getPaths().size());
            assertEquals(60, path1.getScore()); // 20 + 15 + 25
            assertEquals(40, path2.getScore()); // 15 + 25
            assertEquals(45, path3.getScore()); // 20 + 25
        }
        
        @Test
        @DisplayName("Should handle cyclic paths")
        void testCyclicPaths() {
            // Arrange
            Path cyclicPath = new Path();
            cyclicPath.getStates().addAll(Arrays.asList(1L, 2L, 3L, 1L)); // Cycle back to state 1
            
            // Act
            pathManager.updateScore(cyclicPath);
            
            // Assert
            assertEquals(55, cyclicPath.getScore()); // 10 + 20 + 15 + 10
        }
        
        @Test
        @DisplayName("Should handle large paths efficiently")
        void testLargePaths() {
            // Arrange
            Path largePath = new Path();
            for (int i = 0; i < 100; i++) {
                largePath.getStates().add((long) (i % 4 + 1)); // Cycle through states 1-4
            }
            
            // Act
            long startTime = System.currentTimeMillis();
            pathManager.updateScore(largePath);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertTrue((endTime - startTime) < 100, "Score calculation should be fast");
            assertTrue(largePath.getScore() > 0);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle state with zero score")
        void testStateWithZeroScore() {
            // Arrange
            when(mockState1.getPathScore()).thenReturn(0);
            
            Path path = new Path();
            path.getStates().add(1L);
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(0, path.getScore());
        }
        
        @Test
        @DisplayName("Should handle state with negative score")
        void testStateWithNegativeScore() {
            // Arrange
            when(mockState1.getPathScore()).thenReturn(-10);
            
            Path path = new Path();
            path.getStates().addAll(Arrays.asList(1L, 2L));
            
            // Act
            pathManager.updateScore(path);
            
            // Assert
            assertEquals(10, path.getScore()); // -10 + 20
        }
        
        @Test
        @DisplayName("Should handle paths with null state service")
        void testNullStateService() {
            // Arrange
            PathManager pathManagerWithNull = new PathManager(null);
            Path path = new Path();
            path.getStates().add(1L);
            
            // Act & Assert
            assertThrows(NullPointerException.class, () -> pathManagerWithNull.updateScore(path));
        }
    }
}