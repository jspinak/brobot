package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.action.Action;
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
    private ArgumentCaptor<ActionConfig> actionConfigCaptor;

    @Captor
    private ArgumentCaptor<ObjectCollection> objectCollectionCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Stub the mock ActionExecution to return a predictable Matches object for all perform calls
        when(actionExecution.perform(any(ActionInterface.class), anyString(), any(ActionOptions.class), any(ObjectCollection[].class)))
                .thenReturn(new ActionResult());
        when(actionExecution.perform(any(ActionInterface.class), anyString(), any(ActionConfig.class), any(ObjectCollection[].class)))
                .thenReturn(new ActionResult());
    }

    @Test
    void perform_shouldCallActionExecutionWhenActionIsFound() {
        ActionOptions options = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        when(actionService.getAction(options)).thenReturn(Optional.of(actionInterface));

        action.perform("test click", options, collection);

        // Verify that ActionService was asked for an action
        verify(actionService).getAction(options);
        // Verify that ActionExecution was called with the retrieved action and the correct parameters
        verify(actionExecution).perform(eq(actionInterface), eq("test click"), eq(options), any(ObjectCollection[].class));
    }
    
    @Test
    void perform_withActionConfig_shouldCallActionExecutionWhenActionIsFound() {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        when(actionService.getAction(clickOptions)).thenReturn(Optional.of(actionInterface));

        action.perform("test click", clickOptions, collection);

        // Verify that ActionService was asked for an action
        verify(actionService).getAction(clickOptions);
        // Verify that ActionExecution was called with the retrieved action and the correct parameters
        verify(actionExecution).perform(eq(actionInterface), eq("test click"), eq(clickOptions), any(ObjectCollection[].class));
    }

    @Test
    void perform_shouldReturnEmptyMatchesWhenActionIsNotFound() {
        ActionOptions options = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        when(actionService.getAction(options)).thenReturn(Optional.empty());

        ActionResult result = action.perform("test click", options, new ObjectCollection.Builder().build());

        // Verify that execution was NOT called
        verify(actionExecution, never()).perform(any(), anyString(), any(ActionOptions.class), any());
        // Verify that the returned Matches object is empty and success is false
        assertTrue(result.getMatchList().isEmpty());
        assertFalse(result.isSuccess());
    }
    
    @Test
    void perform_withActionConfig_shouldReturnEmptyMatchesWhenActionIsNotFound() {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        when(actionService.getAction(clickOptions)).thenReturn(Optional.empty());

        ActionResult result = action.perform("test click", clickOptions, new ObjectCollection.Builder().build());

        // Verify that execution was NOT called
        verify(actionExecution, never()).perform(any(), anyString(), any(ActionConfig.class), any());
        // Verify that the returned result is empty and success is false
        assertTrue(result.getMatchList().isEmpty());
        assertFalse(result.isSuccess());
    }

    @Test
    void find_withStateImages_shouldCallPerformWithFindAction() {
        // Here we use a spy to partially mock the class and verify the internal call
        Action spyAction = spy(action);
        // We must stub the specific overload of 'perform' that 'find' calls.
        doReturn(new ActionResult()).when(spyAction).perform(any(PatternFindOptions.class), any(StateImage[].class));

        spyAction.find(stateImage); // Call the method to be tested with a mock StateImage

        // Verify that it called the perform(PatternFindOptions, StateImage...) overload
        verify(spyAction).perform(actionConfigCaptor.capture(), eq(stateImage));
        assertTrue(actionConfigCaptor.getValue() instanceof PatternFindOptions);
    }

    @Test
    void perform_withActionAndRegions_shouldBuildCorrectObjectCollection() {
        Action spyAction = spy(action);
        Region region1 = new Region(10, 10, 10, 10);
        // Stub the perform(Action, ObjectCollection...) overload
        doReturn(new ActionResult()).when(spyAction).perform(any(ActionOptions.Action.class), any(ObjectCollection.class));

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
        doReturn(new ActionResult()).when(spyAction).perform(any(ActionOptions.Action.class), any(ObjectCollection.class));

        spyAction.perform(ActionOptions.Action.TYPE, "hello", "world");

        verify(spyAction).perform(eq(ActionOptions.Action.TYPE), objectCollectionCaptor.capture());

        ObjectCollection capturedCollection = objectCollectionCaptor.getValue();
        assertFalse(capturedCollection.getStateStrings().isEmpty());
        assertEquals(2, capturedCollection.getStateStrings().size());
        assertEquals("hello", capturedCollection.getStateStrings().get(0).getString());
        assertEquals("world", capturedCollection.getStateStrings().get(1).getString());
    }
    
    @Test
    void perform_withTypeOptions_shouldExecuteTypeAction() {
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build();
        ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(new io.github.jspinak.brobot.model.state.StateString.Builder()
                        .setName("testString")
                        .setString("Hello World").build())
                .build();
        when(actionService.getAction(typeOptions)).thenReturn(Optional.of(actionInterface));

        action.perform("type test", typeOptions, collection);

        verify(actionService).getAction(typeOptions);
        verify(actionExecution).perform(eq(actionInterface), eq("type test"), eq(typeOptions), any(ObjectCollection[].class));
    }
}
