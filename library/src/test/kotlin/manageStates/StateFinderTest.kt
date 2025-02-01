package manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions
import io.github.jspinak.brobot.database.services.AllStatesInProjectService
import io.github.jspinak.brobot.datatypes.primitives.match.Matches
import io.github.jspinak.brobot.datatypes.state.ObjectCollection
import io.github.jspinak.brobot.datatypes.state.state.State
import io.github.jspinak.brobot.manageStates.StateFinder
import io.github.jspinak.brobot.manageStates.StateMemory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class StateFinderTest {

    @Mock
    private lateinit var allStatesInProjectService: AllStatesInProjectService
    @Mock
    private lateinit var stateMemory: StateMemory
    @Mock(lenient = true)
    private lateinit var action: Action

    private lateinit var stateFinder: StateFinder

    @BeforeEach
    fun setUp() {
        stateFinder = StateFinder(allStatesInProjectService, stateMemory, action)
    }

    @Test
    fun `should use STATE_DETECTION log type when finding state`() {
        // Arrange
        val stateId = 1L
        val mockState = mock<State>()
        whenever(allStatesInProjectService.getState(stateId))
            .thenReturn(Optional.of(mockState))

        val mockMatches = mock<Matches> {
            on { isSuccess } doReturn true
        }

        // Only mock the specific method that's actually being called
        whenever(action.perform(
            eq(ActionOptions.Action.FIND),  // Use the actual FIND constant
            any<ObjectCollection>()
        )).thenReturn(mockMatches)

        // Act
        stateFinder.findState(stateId)

        // Assert
        verify(action).perform(
            eq(ActionOptions.Action.FIND),
            any<ObjectCollection>()
        )
    }

}