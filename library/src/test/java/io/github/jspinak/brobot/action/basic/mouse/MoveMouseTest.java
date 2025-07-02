package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoveMouseTest {

    @Mock private Find find;
    @Mock private MoveMouseWrapper moveMouseWrapper;
    @Mock private TimeProvider timeProvider;
    @Mock private ActionConfig actionConfig;
    
    private MoveMouse moveMouse;
    private ObjectCollection objectCollection;

    @BeforeEach
    void setUp() {
        moveMouse = new MoveMouse(find, moveMouseWrapper, timeProvider);
        when(actionConfig.getPauseAfterEnd()).thenReturn(0.5); // 500ms default
    }

    @Test
    void testGetActionType_ReturnsMove() {
        assertEquals(ActionInterface.Type.MOVE, moveMouse.getActionType());
    }

    @Test
    void testPerform_CallsFindAndMovesToLocations() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
        
        Location location1 = new Location(100, 200);
        Location location2 = new Location(300, 400);
        
        objectCollection = new ObjectCollection.Builder()
                .withLocations(
                    new StateLocation.Builder().setLocation(location1).build(),
                    new StateLocation.Builder().setLocation(location2).build()
                )
                .build();

        // Mock Find to populate match locations
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            // Create matches for the locations
            Match match1 = new Match.Builder()
                    .setRegion(new Region(location1.getX(), location1.getY(), 1, 1))
                    .build();
            Match match2 = new Match.Builder()
                    .setRegion(new Region(location2.getX(), location2.getY(), 1, 1))
                    .build();
            result.add(match1, match2);
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection.class));

        // Act
        moveMouse.perform(actionResult, objectCollection);

        // Assert
        verify(find).perform(eq(actionResult), eq(objectCollection));
        verify(moveMouseWrapper).move(location1);
        verify(moveMouseWrapper).move(location2);
        verifyNoInteractions(timeProvider); // No pause after last collection
    }

    @Test
    void testPerform_WithMultipleCollections_PausesBetweenCollections() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
        
        Location location1 = new Location(100, 100);
        Location location2 = new Location(200, 200);
        
        ObjectCollection collection1 = new ObjectCollection.Builder()
                .withLocations(new StateLocation.Builder().setLocation(location1).build())
                .build();
                
        ObjectCollection collection2 = new ObjectCollection.Builder()
                .withLocations(new StateLocation.Builder().setLocation(location2).build())
                .build();

        // Mock Find to populate match locations for each collection
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            ObjectCollection coll = invocation.getArgument(1);
            // Clear previous matches
            result.getMatchList().clear();
            if (coll == collection1) {
                Match match = new Match.Builder()
                        .setRegion(new Region(location1.getX(), location1.getY(), 1, 1))
                        .build();
                result.add(match);
            } else {
                Match match = new Match.Builder()
                        .setRegion(new Region(location2.getX(), location2.getY(), 1, 1))
                        .build();
                result.add(match);
            }
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection.class));

        // Act
        moveMouse.perform(actionResult, collection1, collection2);

        // Assert
        verify(find).perform(eq(actionResult), eq(collection1));
        verify(find).perform(eq(actionResult), eq(collection2));
        verify(moveMouseWrapper).move(location1);
        verify(moveMouseWrapper).move(location2);
        verify(timeProvider).wait(0.5); // Pause between collections
    }

    @Test
    void testPerform_WithNoPauseConfiguration_DoesNotPause() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
        when(actionConfig.getPauseAfterEnd()).thenReturn(0.0);
        
        ObjectCollection collection1 = new ObjectCollection.Builder().build();
        ObjectCollection collection2 = new ObjectCollection.Builder().build();

        // Act
        moveMouse.perform(actionResult, collection1, collection2);

        // Assert
        verify(find, times(2)).perform(any(), any());
        verifyNoInteractions(timeProvider); // No pause when duration is 0
    }

    @Test
    void testPerform_WithEmptyMatchLocations_StillCallsFind() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
        
        objectCollection = new ObjectCollection.Builder().build();

        // Mock Find to not add any match locations
        doNothing().when(find).perform(any(ActionResult.class), any(ObjectCollection.class));

        // Act
        moveMouse.perform(actionResult, objectCollection);

        // Assert
        verify(find).perform(eq(actionResult), eq(objectCollection));
        verifyNoInteractions(moveMouseWrapper); // No moves when no matches
    }

    @Test
    void testPerform_ProcessesAllLocationsFromFind() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
        
        objectCollection = new ObjectCollection.Builder().build();

        List<Location> locations = Arrays.asList(
            new Location(10, 20),
            new Location(30, 40),
            new Location(50, 60)
        );

        // Mock Find to populate multiple match locations
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            locations.forEach(loc -> {
                Match match = new Match.Builder()
                        .setRegion(new Region(loc.getX(), loc.getY(), 1, 1))
                        .build();
                result.add(match);
            });
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection.class));

        // Act
        moveMouse.perform(actionResult, objectCollection);

        // Assert
        locations.forEach(loc -> verify(moveMouseWrapper).move(loc));
        verify(moveMouseWrapper, times(3)).move(any(Location.class));
    }

    @Test
    void testPerform_WithThreeCollections_PausesTwice() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
        
        ObjectCollection collection1 = new ObjectCollection.Builder().build();
        ObjectCollection collection2 = new ObjectCollection.Builder().build();
        ObjectCollection collection3 = new ObjectCollection.Builder().build();

        // Act
        moveMouse.perform(actionResult, collection1, collection2, collection3);

        // Assert
        verify(find, times(3)).perform(any(), any());
        verify(timeProvider, times(2)).wait(0.5); // Pause after first and second, not third
    }
}