package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.mouse.MouseDown;
import io.github.jspinak.brobot.action.basic.mouse.MouseUp;
import io.github.jspinak.brobot.action.basic.mouse.MoveMouse;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test for refactored Drag class that no longer depends on ActionChainExecutor.
 * This verifies the pure action implementation works correctly.
 */
public class DragRefactoredTest extends BrobotTestBase {

    @Mock
    private MouseDown mockMouseDown;
    
    @Mock
    private MoveMouse mockMoveMouse;
    
    @Mock
    private MouseUp mockMouseUp;
    
    private Drag drag;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        drag = new Drag(mockMouseDown, mockMoveMouse, mockMouseUp);
    }
    
    @Test
    public void testDragBetweenLocations() {
        // Arrange
        Location sourceLocation = new Location(100, 100);
        Location targetLocation = new Location(300, 300);
        
        StateLocation sourceStateLocation = new StateLocation();
        sourceStateLocation.setLocation(sourceLocation);
        
        StateLocation targetStateLocation = new StateLocation();
        targetStateLocation.setLocation(targetLocation);
        
        ObjectCollection sourceCollection = new ObjectCollection.Builder()
                .withLocations(sourceStateLocation)
                .build();
                
        ObjectCollection targetCollection = new ObjectCollection.Builder()
                .withLocations(targetStateLocation)
                .build();
        
        ActionResult actionResult = new ActionResult();
        DragOptions dragOptions = new DragOptions.Builder().build();
        actionResult.setActionConfig(dragOptions);
        
        // Set up mocks to succeed
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockMoveMouse).perform(any(), any());
        
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockMouseDown).perform(any(), any());
        
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockMouseUp).perform(any(), any());
        
        // Act
        drag.perform(actionResult, sourceCollection, targetCollection);
        
        // Assert
        assertTrue(actionResult.isSuccess(), "Drag operation should succeed");
        assertNotNull(actionResult.getMatchList(), "Should have match list");
        assertEquals(2, actionResult.getMatchList().size(), "Should have source and target matches");
        
        // Verify the correct sequence of operations
        verify(mockMoveMouse, times(2)).perform(any(), any()); // Move to source, then to target
        verify(mockMouseDown, times(1)).perform(any(), any());
        verify(mockMouseUp, times(1)).perform(any(), any());
    }
    
    @Test
    public void testDragWithNoCollections() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        
        // Act
        drag.perform(actionResult);
        
        // Assert
        assertFalse(actionResult.isSuccess(), "Should fail with no collections");
    }
    
    @Test
    public void testDragWithInsufficientCollections() {
        // Arrange
        Location sourceLocation = new Location(100, 100);
        StateLocation sourceStateLocation = new StateLocation();
        sourceStateLocation.setLocation(sourceLocation);
        
        ObjectCollection sourceCollection = new ObjectCollection.Builder()
                .withLocations(sourceStateLocation)
                .build();
        
        ActionResult actionResult = new ActionResult();
        
        // Act
        drag.perform(actionResult, sourceCollection);
        
        // Assert
        assertFalse(actionResult.isSuccess(), "Should fail with only one collection");
    }
    
    @Test
    public void testDragWithEmptyCollections() {
        // Arrange
        ObjectCollection emptyCollection1 = new ObjectCollection.Builder().build();
        ObjectCollection emptyCollection2 = new ObjectCollection.Builder().build();
        
        ActionResult actionResult = new ActionResult();
        
        // Act
        drag.perform(actionResult, emptyCollection1, emptyCollection2);
        
        // Assert
        assertFalse(actionResult.isSuccess(), "Should fail with empty collections");
    }
}