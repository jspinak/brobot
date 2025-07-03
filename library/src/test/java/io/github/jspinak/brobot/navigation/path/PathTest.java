package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PathTest {
    
    private Path path;
    private StateTransition transition1;
    private StateTransition transition2;
    
    @BeforeEach
    void setUp() {
        path = new Path();
        
        transition1 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate("State2")
                .setScore(5)
                .build();
                
        transition2 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate("State3")
                .setScore(10)
                .build();
    }
    
    @Test
    void testAddState() {
        path.add(1L);
        path.add(2L);
        
        assertEquals(2, path.size());
        assertEquals(Arrays.asList(1L, 2L), path.getStates());
    }
    
    @Test
    void testAddTransition() {
        path.add(transition1);
        path.add(transition2);
        
        assertEquals(2, path.getTransitions().size());
        assertEquals(transition1, path.getTransitions().get(0));
        assertEquals(transition2, path.getTransitions().get(1));
    }
    
    @Test
    void testContains() {
        path.add(1L);
        path.add(2L);
        
        assertTrue(path.contains(1L));
        assertTrue(path.contains(2L));
        assertFalse(path.contains(3L));
    }
    
    @Test
    void testGet() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        assertEquals(1L, path.get(0));
        assertEquals(2L, path.get(1));
        assertEquals(3L, path.get(2));
    }
    
    @Test
    void testIsEmpty() {
        assertTrue(path.isEmpty());
        
        path.add(1L);
        assertFalse(path.isEmpty());
    }
    
    @Test
    void testReverse() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        path.reverse();
        
        assertEquals(Arrays.asList(3L, 2L, 1L), path.getStates());
    }
    
    @Test
    void testRemove() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        assertTrue(path.remove(2L));
        assertEquals(Arrays.asList(1L, 3L), path.getStates());
        
        assertFalse(path.remove(4L));
    }
    
    @Test
    void testGetCopy() {
        path.add(1L);
        path.add(2L);
        path.setScore(50);
        
        Path copy = path.getCopy();
        
        assertEquals(path.getStates(), copy.getStates());
        assertEquals(path.getScore(), copy.getScore());
        
        // Ensure it's a real copy
        copy.add(3L);
        assertEquals(2, path.size());
        assertEquals(3, copy.size());
    }
    
    @Test
    void testEquals() {
        Path path1 = new Path();
        path1.add(1L);
        path1.add(2L);
        
        Path path2 = new Path();
        path2.add(1L);
        path2.add(2L);
        
        assertTrue(path1.equals(path2));
        
        path2.add(3L);
        assertFalse(path1.equals(path2));
    }
    
    @Test
    void testCleanPath_WithFailedTransition() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        path.add(4L);
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 3L));
        Long failedTransitionStartState = 3L;
        
        Path cleanedPath = path.cleanPath(activeStates, failedTransitionStartState);
        
        assertTrue(cleanedPath.isEmpty());
    }
    
    @Test
    void testCleanPath_NoFailedTransition() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        path.add(4L);
        
        // Need transitions for each state in the path
        path.add(transition1);
        path.add(transition2);
        path.add(transition1);
        path.add(transition2);
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 3L));
        Long failedTransitionStartState = 5L; // Not in path
        
        Path cleanedPath = path.cleanPath(activeStates, failedTransitionStartState);
        
        // Should trim from first active state
        assertEquals(3, cleanedPath.getStates().size());
        assertEquals(Arrays.asList(2L, 3L, 4L), cleanedPath.getStates());
    }
    
    @Test
    void testTrimPath() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        path.add(4L);
        
        // Add transitions for each state
        path.add(transition1);
        path.add(transition2);
        path.add(transition1); // Reuse for testing
        path.add(transition2);
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(3L));
        
        Path trimmedPath = path.trimPath(activeStates);
        
        assertEquals(2, trimmedPath.getStates().size());
        assertEquals(Arrays.asList(3L, 4L), trimmedPath.getStates());
        assertEquals(2, trimmedPath.getTransitions().size());
    }
    
    @Test
    void testTrimPath_FirstStateActive() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        path.add(transition1);
        path.add(transition2);
        path.add(transition1);
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L));
        
        Path trimmedPath = path.trimPath(activeStates);
        
        assertEquals(3, trimmedPath.getStates().size());
        assertEquals(Arrays.asList(1L, 2L, 3L), trimmedPath.getStates());
    }
    
    @Test
    void testTrimPath_NoActiveStates() {
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        Set<Long> activeStates = new HashSet<>();
        
        Path trimmedPath = path.trimPath(activeStates);
        
        assertTrue(trimmedPath.isEmpty());
    }
    
    @Test
    void testGetStatesAsString() {
        assertEquals("", path.getStatesAsString());
        
        path.add(1L);
        assertEquals("1", path.getStatesAsString());
        
        path.add(2L);
        path.add(3L);
        assertEquals("1 -> 2 -> 3", path.getStatesAsString());
    }
    
    @Test
    void testPrint() {
        path.add(1L);
        path.add(2L);
        path.setScore(100);
        
        // Just verify it doesn't throw exception
        assertDoesNotThrow(() -> path.print());
    }
    
    @Test
    void testScoreGetterSetter() {
        assertEquals(0, path.getScore());
        
        path.setScore(50);
        assertEquals(50, path.getScore());
        
        path.setScore(-10);
        assertEquals(-10, path.getScore());
    }
}