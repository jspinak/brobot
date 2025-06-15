package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class AfterClickTest {

    @InjectMocks
    private AfterClick afterClick;

    @Mock
    private MoveMouseWrapper moveMouseWrapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void moveMouseAfterClick_shouldDoNothingWhenOptionIsFalse() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setMoveMouseAfterAction(false)
                .build();

        afterClick.moveMouseAfterClick(actionOptions);

        verify(moveMouseWrapper, never()).move(any(Location.class));
    }

    @Test
    void moveMouseAfterClick_shouldMoveByOffsetWhenDefined() {
        Location offset = mock(Location.class);
        when(offset.defined()).thenReturn(true);

        ActionOptions actionOptions = new ActionOptions.Builder()
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionBy(0,0) // A stub, the mock 'offset' is what's used
                .build();
        // Manually set the mocked location since the builder creates a new one
        actionOptions.setMoveMouseAfterActionBy(offset);

        afterClick.moveMouseAfterClick(actionOptions);

        verify(moveMouseWrapper).move(offset);
    }

    @Test
    void moveMouseAfterClick_shouldMoveToLocationWhenOffsetNotDefined() {
        Location offset = mock(Location.class);
        when(offset.defined()).thenReturn(false);

        Location destination = mock(Location.class);

        ActionOptions actionOptions = new ActionOptions.Builder()
                .setMoveMouseAfterAction(true)
                .build();
        actionOptions.setMoveMouseAfterActionBy(offset); // Set the undefined offset
        actionOptions.setMoveMouseAfterActionTo(destination); // Set the destination

        afterClick.moveMouseAfterClick(actionOptions);

        verify(moveMouseWrapper).move(destination);
        verify(moveMouseWrapper, never()).move(offset);
    }
}
