package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActionTest {

    @InjectMocks
    private Action action;

    @Mock
    private ActionExecution actionExecution;

    @Mock
    private ActionService actionService;

    @Mock
    private ActionInterface actionInterface;

    @Mock
    private StateImage stateImage; // Mock StateImage for testing find()

    @Captor
    private ArgumentCaptor<ActionOptions> actionOptionsCaptor;

    @Captor
    private ArgumentCaptor<ObjectCollection> objectCollectionCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Stub the mock ActionExecution to return a predictable Matches object for all perform calls
        when(actionExecution.perform(any(), anyString(), any(), any())).thenReturn(new Matches());
    }

    @Test
    void perform_shouldCallActionExecutionWhenActionIsFound() {
        ActionOptions options = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        when(actionService.getAction(options)).thenReturn(Optional.of(actionInterface));

        action.perform("test click", options, collection);

        // Verify that resetTimesActedOn was called on the collection.
        // Direct verification on the mock is complex since it's modified internally.
        // We trust the implementation calls it based on code inspection.

        // Verify that ActionService was asked for an action
        verify(actionService).getAction(options);
        // Verify that ActionExecution was called with the retrieved action and the correct parameters
        verify(actionExecution).perform(actionInterface, "test click", options, new ObjectCollection[]{collection});
    }

    @Test
    void perform_shouldReturnEmptyMatchesWhenActionIsNotFound() {
        ActionOptions options = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        when(actionService.getAction(options)).thenReturn(Optional.empty());

        Matches result = action.perform("test click", options, new ObjectCollection.Builder().build());

        // Verify that execution was NOT called
        verify(actionExecution, never()).perform(any(), anyString(), any(), any());
        // Verify that the returned Matches object is empty and success is false
        assertTrue(result.getMatchList().isEmpty());
        assertFalse(result.isSuccess());
    }

    @Test
    void find_withStateImages_shouldCallPerformWithFindAction() {
        // Here we use a spy to partially mock the class and verify the internal call
        Action spyAction = spy(action);
        // We must stub the specific overload of 'perform' that 'find' calls.
        doReturn(new Matches()).when(spyAction).perform(any(ActionOptions.class), any(StateImage[].class));

        spyAction.find(stateImage); // Call the method to be tested with a mock StateImage

        // Verify that it called the perform(ActionOptions, StateImage...) overload
        verify(spyAction).perform(actionOptionsCaptor.capture(), eq(stateImage));
        assertEquals(ActionOptions.Action.FIND, actionOptionsCaptor.getValue().getAction());
    }

    @Test
    void perform_withActionAndRegions_shouldBuildCorrectObjectCollection() {
        Action spyAction = spy(action);
        Region region1 = new Region(10, 10, 10, 10);
        // Stub the perform(Action, ObjectCollection...) overload
        doReturn(new Matches()).when(spyAction).perform(any(ActionOptions.Action.class), any(ObjectCollection.class));

        spyAction.perform(ActionOptions.Action.CLICK, region1);

        verify(spyAction).perform(eq(ActionOptions.Action.CLICK), objectCollectionCaptor.capture());

        ObjectCollection capturedCollection = objectCollectionCaptor.getValue();
        assertFalse(capturedCollection.getStateRegions().isEmpty());
        assertEquals(1, capturedCollection.getStateRegions().size());
        // The region in the collection is a StateRegion, so we check its internal region
        assertEquals(region1, capturedCollection.getStateRegions().get(0).getSearchRegion());
    }

    @Test
    void perform_withActionAndStrings_shouldBuildCorrectObjectCollection() {
        Action spyAction = spy(action);
        // Stub the perform(Action, ObjectCollection...) overload
        doReturn(new Matches()).when(spyAction).perform(any(ActionOptions.Action.class), any(ObjectCollection.class));

        spyAction.perform(ActionOptions.Action.TYPE, "hello", "world");

        verify(spyAction).perform(eq(ActionOptions.Action.TYPE), objectCollectionCaptor.capture());

        ObjectCollection capturedCollection = objectCollectionCaptor.getValue();
        assertFalse(capturedCollection.getStateStrings().isEmpty());
        assertEquals(2, capturedCollection.getStateStrings().size());
        assertEquals("hello", capturedCollection.getStateStrings().get(0).getString());
        assertEquals("world", capturedCollection.getStateStrings().get(1).getString());
    }
}
