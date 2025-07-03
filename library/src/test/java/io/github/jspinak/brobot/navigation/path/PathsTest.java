package io.github.jspinak.brobot.navigation.path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PathsTest {
    
    private Paths paths;
    private Path path1;
    private Path path2;
    private Path path3;
    
    @BeforeEach
    void setUp() {
        paths = new Paths();
        
        // Create test paths
        path1 = new Path();
        path1.add(1L);
        path1.add(2L);
        path1.setScore(50);
        
        path2 = new Path();
        path2.add(1L);
        path2.add(3L);
        path2.add(2L);
        path2.setScore(30);
        
        path3 = new Path();
        path3.add(1L);
        path3.add(4L);
        path3.add(2L);
        path3.setScore(70);
    }
    
    @Test
    void testConstructorWithList() {
        List<Path> pathList = Arrays.asList(path1, path2);
        Paths pathsWithList = new Paths(pathList);
        
        assertEquals(2, pathsWithList.getPaths().size());
        assertEquals(path1, pathsWithList.getPaths().get(0));
        assertEquals(path2, pathsWithList.getPaths().get(1));
    }
    
    @Test
    void testIsEmpty() {
        assertTrue(paths.isEmpty());
        
        paths.addPath(path1);
        assertFalse(paths.isEmpty());
    }
    
    @Test
    void testAddPath() {
        paths.addPath(path1);
        paths.addPath(path2);
        
        assertEquals(2, paths.getPaths().size());
        assertTrue(paths.getPaths().contains(path1));
        assertTrue(paths.getPaths().contains(path2));
    }
    
    @Test
    void testAddPath_EmptyPath() {
        Path emptyPath = new Path();
        paths.addPath(emptyPath);
        
        // Empty paths should not be added
        assertTrue(paths.isEmpty());
    }
    
    @Test
    void testSort() {
        paths.addPath(path1); // score 50
        paths.addPath(path2); // score 30
        paths.addPath(path3); // score 70
        
        paths.sort();
        
        // Should be sorted by score ascending: 30, 50, 70
        assertEquals(30, paths.getPaths().get(0).getScore());
        assertEquals(50, paths.getPaths().get(1).getScore());
        assertEquals(70, paths.getPaths().get(2).getScore());
    }
    
    @Test
    void testEquals_SamePaths() {
        Paths paths1 = new Paths();
        paths1.addPath(path1);
        paths1.addPath(path2);
        
        Paths paths2 = new Paths();
        paths2.addPath(path1);
        paths2.addPath(path2);
        
        assertTrue(paths1.equals(paths2));
    }
    
    @Test
    void testEquals_DifferentSize() {
        Paths paths1 = new Paths();
        paths1.addPath(path1);
        
        Paths paths2 = new Paths();
        paths2.addPath(path1);
        paths2.addPath(path2);
        
        assertFalse(paths1.equals(paths2));
    }
    
    @Test
    void testEquals_DifferentPaths() {
        Paths paths1 = new Paths();
        paths1.addPath(path1);
        paths1.addPath(path2);
        
        Paths paths2 = new Paths();
        paths2.addPath(path1);
        paths2.addPath(path3);
        
        assertFalse(paths1.equals(paths2));
    }
    
    @Test
    void testEquals_DifferentOrder() {
        Paths paths1 = new Paths();
        paths1.addPath(path1);
        paths1.addPath(path2);
        
        Paths paths2 = new Paths();
        paths2.addPath(path2);
        paths2.addPath(path1);
        
        assertFalse(paths1.equals(paths2));
    }
    
    @Test
    void testCleanPaths() {
        // Create a simple path
        Path simplePath = new Path();
        simplePath.add(1L);
        simplePath.add(2L);
        
        paths.addPath(simplePath);
        
        // Clean with no active states - should result in empty paths
        Set<Long> activeStates = new HashSet<>();
        Long failedTransitionStartState = 10L; // Not in any path
        
        Paths cleanedPaths = paths.cleanPaths(activeStates, failedTransitionStartState);
        
        // Path should be empty because no active states
        assertTrue(cleanedPaths.isEmpty());
    }
    
    @Test
    void testCleanPaths_WithFailedState() {
        // Create path with failed state
        Path pathWithFailedState = new Path();
        pathWithFailedState.add(1L);
        pathWithFailedState.add(5L); // Will be marked as failed
        pathWithFailedState.add(3L);
        
        paths.addPath(pathWithFailedState);
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 3L));
        Long failedTransitionStartState = 5L;
        
        Paths cleanedPaths = paths.cleanPaths(activeStates, failedTransitionStartState);
        
        // Path contains failed state so should be empty
        assertTrue(cleanedPaths.isEmpty());
    }
    
    @Test
    void testGetBestScore_EmptyPaths() {
        assertEquals(0, paths.getBestScore());
    }
    
    @Test
    void testGetBestScore_SinglePath() {
        paths.addPath(path1);
        assertEquals(50, paths.getBestScore());
    }
    
    @Test
    void testGetBestScore_MultiplePaths() {
        paths.addPath(path1); // score 50
        paths.addPath(path2); // score 30
        paths.addPath(path3); // score 70
        
        assertEquals(70, paths.getBestScore());
    }
    
    @Test
    void testGetBestScore_NegativeScores() {
        Path negativePath = new Path();
        negativePath.add(1L);
        negativePath.setScore(-100);
        
        paths.addPath(negativePath);
        paths.addPath(path1); // score 50
        
        assertEquals(50, paths.getBestScore());
    }
    
    @Test
    void testPrint_EmptyPaths() {
        // Should not throw exception
        assertDoesNotThrow(() -> paths.print());
    }
    
    @Test
    void testPrint_WithPaths() {
        paths.addPath(path1);
        paths.addPath(path2);
        
        // Should not throw exception
        assertDoesNotThrow(() -> paths.print());
    }
    
    @Test
    void testPathsWithDuplicates() {
        paths.addPath(path1);
        paths.addPath(path1); // Add same path again
        
        assertEquals(2, paths.getPaths().size());
        // Both should reference the same path
        assertSame(paths.getPaths().get(0), paths.getPaths().get(1));
    }
}