package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickType;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MouseDownWrapper;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MouseUpWrapper;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ClickIntegrationTest {

    @Autowired
    private Action action;

    @SpyBean
    private Find find;

    @SpyBean
    private MoveMouseWrapper moveMouseWrapper;

    @SpyBean
    private MouseDownWrapper mouseDownWrapper;

    @SpyBean
    private MouseUpWrapper mouseUpWrapper;

    private boolean originalMockState;

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        // ** FIX: Force the test to run in a non-mocked state **
        BrobotSettings.mock = false;

        // Since find.perform is a void method that modifies its arguments,
        // we use doAnswer to simulate this behavior.
        doAnswer(invocation -> {
            Matches matches = invocation.getArgument(0); // Get the Matches object passed to find.perform
            matches.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setSimScore(0.9)
                    .build());
            matches.setSuccess(true);
            return null; // void methods must return null
        }).when(find).perform(any(Matches.class), any(ObjectCollection[].class));
    }

    @Test
    void perform_simpleClick_shouldMoveAndPressDownAndUp() {
        // Setup
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        ObjectCollection objectCollection = new ObjectCollection();

        // Action
        Matches result = action.perform(actionOptions, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());
        verify(moveMouseWrapper).move(any(Location.class));
        verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
    }

    @Test
    void perform_doubleClick_shouldResultInTwoMouseDownAndUpEvents() {
        // Setup
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickType(ClickType.Type.DOUBLE_LEFT)
                .setPauseAfterMouseDown(0.1) // A pause forces two distinct clicks
                .build();
        ObjectCollection objectCollection = new ObjectCollection();

        // Action
        action.perform(actionOptions, objectCollection);

        // Verification
        verify(moveMouseWrapper).move(any(Location.class));
        verify(mouseDownWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
        verify(mouseUpWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
    }

    @Test
    void perform_clickWithMoveAfter_shouldMoveTwice() {
        // Setup
        Location moveLocation = new Location(100, 100);
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionTo(moveLocation)
                .build();
        ObjectCollection objectCollection = new ObjectCollection();

        // Action
        action.perform(actionOptions, objectCollection);

        // Verification
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        verify(moveMouseWrapper, times(2)).move(locationCaptor.capture());

        List<Location> capturedLocations = locationCaptor.getAllValues();
        Assertions.assertEquals(moveLocation, capturedLocations.get(1));

        verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
    }
}
