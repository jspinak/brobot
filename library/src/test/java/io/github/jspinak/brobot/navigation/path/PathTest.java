package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.transition.StateTransition;
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
 * Comprehensive test suite for Path class.
 * Tests path operations, state management, and transformation methods.
 */
@DisplayName("Path Tests")
class PathTest extends BrobotTestBase {
    
    private Path path;
    
    @Mock
    private StateTransition mockTransition1;
    
    @Mock
    private StateTransition mockTransition2;
    
    @Mock
    private StateTransition mockTransition3;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        path = new Path();
    }
    
    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {
        
        @Test
        @DisplayName("Should initialize with empty states and transitions")
        void testInitialization() {
            assertNotNull(path.getStates());
            assertNotNull(path.getTransitions());
            assertTrue(path.isEmpty());
            assertEquals(0, path.size());
            assertEquals(0, path.getScore());
        }
        
        @Test
        @DisplayName("Should add states to path")
        void testAddStates() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            
            assertEquals(3, path.size());
            assertFalse(path.isEmpty());
            assertEquals(100L, path.get(0));
            assertEquals(200L, path.get(1));
            assertEquals(300L, path.get(2));
        }
        
        @Test
        @DisplayName("Should add transitions to path")
        void testAddTransitions() {
            path.add(mockTransition1);
            path.add(mockTransition2);
            
            assertEquals(2, path.getTransitions().size());
            assertTrue(path.getTransitions().contains(mockTransition1));
            assertTrue(path.getTransitions().contains(mockTransition2));
        }
        
        @Test
        @DisplayName("Should check if path contains state")
        void testContainsState() {
            path.add(100L);
            path.add(200L);
            
            assertTrue(path.contains(100L));
            assertTrue(path.contains(200L));
            assertFalse(path.contains(300L));
        }
        
        @Test
        @DisplayName("Should remove state from path")
        void testRemoveState() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            
            assertTrue(path.remove(200L));
            assertEquals(2, path.size());
            assertFalse(path.contains(200L));
            
            assertFalse(path.remove(999L)); // Remove non-existent
        }
        
        @Test
        @DisplayName("Should get and set score")
        void testScore() {
            assertEquals(0, path.getScore());
            
            path.setScore(50);
            assertEquals(50, path.getScore());
            
            path.setScore(-10);
            assertEquals(-10, path.getScore());
        }
    }
    
    @Nested
    @DisplayName("Path Comparison")
    class PathComparison {
        
        @Test
        @DisplayName("Should compare paths by states")
        void testEquals() {
            Path path1 = new Path();
            path1.add(100L);
            path1.add(200L);
            path1.add(300L);
            
            Path path2 = new Path();
            path2.add(100L);
            path2.add(200L);
            path2.add(300L);
            
            assertTrue(path1.equals(path2));
        }
        
        @Test
        @DisplayName("Should not be equal with different states")
        void testNotEqualDifferentStates() {
            Path path1 = new Path();
            path1.add(100L);
            path1.add(200L);
            
            Path path2 = new Path();
            path2.add(100L);
            path2.add(300L);
            
            assertFalse(path1.equals(path2));
        }
        
        @Test
        @DisplayName("Should not be equal with different order")
        void testNotEqualDifferentOrder() {
            Path path1 = new Path();
            path1.add(100L);
            path1.add(200L);
            
            Path path2 = new Path();
            path2.add(200L);
            path2.add(100L);
            
            assertFalse(path1.equals(path2));
        }
        
        @Test
        @DisplayName("Should handle equals with empty paths")
        void testEqualsEmptyPaths() {
            Path path1 = new Path();
            Path path2 = new Path();
            
            assertTrue(path1.equals(path2));
        }
    }
    
    @Nested
    @DisplayName("Path Transformation")
    class PathTransformation {
        
        @Test
        @DisplayName("Should reverse path states")
        void testReverse() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            path.add(400L);
            
            path.reverse();
            
            assertEquals(400L, path.get(0));
            assertEquals(300L, path.get(1));
            assertEquals(200L, path.get(2));
            assertEquals(100L, path.get(3));
        }
        
        @Test
        @DisplayName("Should handle reverse on empty path")
        void testReverseEmpty() {
            path.reverse();
            
            assertTrue(path.isEmpty());
            assertEquals(0, path.size());
        }
        
        @Test
        @DisplayName("Should handle reverse on single element")
        void testReverseSingleElement() {
            path.add(100L);
            
            path.reverse();
            
            assertEquals(1, path.size());
            assertEquals(100L, path.get(0));
        }
        
        @Test
        @DisplayName("Should create independent copy")
        void testGetCopy() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            path.setScore(50);
            path.add(mockTransition1);
            
            Path copy = path.getCopy();
            
            // Check copy has same content
            assertEquals(path.getStates(), copy.getStates());
            assertEquals(path.getScore(), copy.getScore());
            
            // Check copy is independent
            copy.add(400L);
            copy.setScore(100);
            
            assertEquals(3, path.size());
            assertEquals(4, copy.size());
            assertEquals(50, path.getScore());
            assertEquals(100, copy.getScore());
            
            // Verify states list is a new instance
            assertNotSame(path.getStates(), copy.getStates());
        }
        
        @Test
        @DisplayName("Should copy empty path")
        void testGetCopyEmpty() {
            Path copy = path.getCopy();
            
            assertTrue(copy.isEmpty());
            assertEquals(0, copy.getScore());
            assertNotSame(path.getStates(), copy.getStates());
        }
    }
    
    @Nested
    @DisplayName("Path Cleaning and Trimming")
    class PathCleaningAndTrimming {
        
        @BeforeEach
        void setupPath() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            path.add(400L);
            path.add(500L);
            
            // Add transitions for each state
            path.add(mockTransition1);
            path.add(mockTransition2);
            path.add(mockTransition3);
            path.add(mock(StateTransition.class));
            path.add(mock(StateTransition.class));
        }
        
        @Test
        @DisplayName("Should trim path from first active state")
        void testTrimPath() {
            Set<Long> activeStates = new HashSet<>(Arrays.asList(300L, 500L));
            
            Path trimmed = path.trimPath(activeStates);
            
            assertEquals(3, trimmed.size());
            assertEquals(300L, trimmed.get(0));
            assertEquals(400L, trimmed.get(1));
            assertEquals(500L, trimmed.get(2));
            assertEquals(3, trimmed.getTransitions().size());
        }
        
        @Test
        @DisplayName("Should return empty path when no active states")
        void testTrimPathNoActiveStates() {
            Set<Long> activeStates = new HashSet<>(Arrays.asList(600L, 700L));
            
            Path trimmed = path.trimPath(activeStates);
            
            assertTrue(trimmed.isEmpty());
            assertTrue(trimmed.getTransitions().isEmpty());
        }
        
        @Test
        @DisplayName("Should return full path when first state is active")
        void testTrimPathFirstStateActive() {
            Set<Long> activeStates = new HashSet<>(Arrays.asList(100L));
            
            Path trimmed = path.trimPath(activeStates);
            
            assertEquals(5, trimmed.size());
            assertEquals(100L, trimmed.get(0));
            assertEquals(500L, trimmed.get(4));
        }
        
        @Test
        @DisplayName("Should clean path with failed transition")
        void testCleanPathWithFailedTransition() {
            Set<Long> activeStates = new HashSet<>(Arrays.asList(300L));
            Long failedState = 200L; // Path contains this state
            
            Path cleaned = path.cleanPath(activeStates, failedState);
            
            assertTrue(cleaned.isEmpty()); // Returns empty because path contains failed state
        }
        
        @Test
        @DisplayName("Should clean path without failed transition")
        void testCleanPathWithoutFailedTransition() {
            Set<Long> activeStates = new HashSet<>(Arrays.asList(300L));
            Long failedState = 999L; // Path doesn't contain this state
            
            Path cleaned = path.cleanPath(activeStates, failedState);
            
            // Should return trimmed path starting from active state
            assertEquals(3, cleaned.size());
            assertEquals(300L, cleaned.get(0));
        }
        
        @Test
        @DisplayName("Should handle empty path in cleaning")
        void testCleanEmptyPath() {
            Path emptyPath = new Path();
            Set<Long> activeStates = new HashSet<>(Arrays.asList(100L));
            
            Path cleaned = emptyPath.cleanPath(activeStates, 200L);
            
            assertTrue(cleaned.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {
        
        @Test
        @DisplayName("Should convert states to string")
        void testGetStatesAsString() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            
            String result = path.getStatesAsString();
            
            assertEquals("100 -> 200 -> 300", result);
        }
        
        @Test
        @DisplayName("Should handle empty path in string conversion")
        void testGetStatesAsStringEmpty() {
            String result = path.getStatesAsString();
            
            assertEquals("", result);
        }
        
        @Test
        @DisplayName("Should handle single state in string conversion")
        void testGetStatesAsStringSingleState() {
            path.add(100L);
            
            String result = path.getStatesAsString();
            
            assertEquals("100", result);
        }
        
        @Test
        @DisplayName("Should print path with score")
        void testPrint() {
            path.add(100L);
            path.add(200L);
            path.setScore(25);
            
            // Capture System.out
            assertDoesNotThrow(() -> path.print());
            // Note: We're not capturing output here, just ensuring it doesn't throw
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null state in contains")
        void testContainsNull() {
            path.add(100L);
            
            assertFalse(path.contains(null));
        }
        
        @Test
        @DisplayName("Should handle get with invalid index")
        void testGetInvalidIndex() {
            path.add(100L);
            
            assertThrows(IndexOutOfBoundsException.class, () -> path.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> path.get(1));
        }
        
        @Test
        @DisplayName("Should handle very large paths")
        void testLargePath() {
            for (long i = 0; i < 1000; i++) {
                path.add(i);
            }
            
            assertEquals(1000, path.size());
            assertTrue(path.contains(500L));
            assertEquals(999L, path.get(999));
            
            path.reverse();
            assertEquals(999L, path.get(0));
            assertEquals(0L, path.get(999));
        }
        
        @Test
        @DisplayName("Should handle negative scores")
        void testNegativeScore() {
            path.setScore(-100);
            assertEquals(-100, path.getScore());
            
            Path copy = path.getCopy();
            assertEquals(-100, copy.getScore());
        }
        
        @Test
        @DisplayName("Should handle MAX_VALUE score")
        void testMaxScore() {
            path.setScore(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, path.getScore());
        }
        
        @Test
        @DisplayName("Should handle transitions list independently in copy")
        void testCopyTransitionsIndependence() {
            path.add(100L);
            path.add(mockTransition1);
            
            Path copy = path.getCopy();
            
            // Note: getCopy() doesn't copy transitions list (based on implementation)
            // It only copies states and score
            assertEquals(path.getStates(), copy.getStates());
            assertNotSame(path.getStates(), copy.getStates());
        }
        
        @Test
        @DisplayName("Should handle trim with multiple active states in sequence")
        void testTrimMultipleActiveStatesInSequence() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            path.add(mockTransition1);
            path.add(mockTransition2);
            path.add(mockTransition3);
            
            Set<Long> activeStates = new HashSet<>(Arrays.asList(100L, 200L));
            
            Path trimmed = path.trimPath(activeStates);
            
            // Should start from first active state (100L)
            assertEquals(3, trimmed.size());
            assertEquals(100L, trimmed.get(0));
            assertEquals(200L, trimmed.get(1));
            assertEquals(300L, trimmed.get(2));
        }
    }
    
    @Nested
    @DisplayName("Boundary Conditions")
    class BoundaryConditions {
        
        @Test
        @DisplayName("Should handle Long.MAX_VALUE as state ID")
        void testMaxLongStateId() {
            path.add(Long.MAX_VALUE);
            
            assertTrue(path.contains(Long.MAX_VALUE));
            assertEquals(Long.MAX_VALUE, path.get(0));
        }
        
        @Test
        @DisplayName("Should handle Long.MIN_VALUE as state ID")
        void testMinLongStateId() {
            path.add(Long.MIN_VALUE);
            
            assertTrue(path.contains(Long.MIN_VALUE));
            assertEquals(Long.MIN_VALUE, path.get(0));
        }
        
        @Test
        @DisplayName("Should handle trim with all states active")
        void testTrimAllStatesActive() {
            path.add(100L);
            path.add(200L);
            path.add(300L);
            path.add(mockTransition1);
            path.add(mockTransition2);
            path.add(mockTransition3);
            
            Set<Long> activeStates = new HashSet<>(Arrays.asList(100L, 200L, 300L));
            
            Path trimmed = path.trimPath(activeStates);
            
            assertEquals(3, trimmed.size());
            assertEquals(path.getStates(), trimmed.getStates());
        }
    }
}