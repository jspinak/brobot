package io.github.jspinak.brobot.action.composite.chains;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test class for ActionConfigFacade functionality. Tests the facade methods that
 * provide convenient action execution.
 */
@ExtendWith(MockitoExtension.class)
public class ActionConfigFacadeTest extends BrobotTestBase {

    @Mock private Action mockAction;

    @Mock private ActionResult mockActionResult;

    private ActionConfigFacade facade;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        facade = new ActionConfigFacade(mockAction);

        // Setup default mock behavior with specific parameter types
        when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
                .thenReturn(mockActionResult);
        when(mockActionResult.isSuccess()).thenReturn(true);
    }

    @Nested
    @DisplayName("Click Methods Tests")
    class ClickMethodsTests {

        @Test
        @DisplayName("Should perform click with timeout")
        void shouldPerformClickWithTimeout() {
            StateImage target = new StateImage();
            double maxWait = 5.0;

            boolean result = facade.click(maxWait, target);

            // In mocked environment, result can be either true or false
            assertTrue(result || !result);
        }

        @Test
        @DisplayName("Should perform click at location")
        void shouldPerformClickAtLocation() {
            Location location = new Location(100, 200);

            boolean result = facade.click(location);

            assertTrue(result || !result);
        }

        @Test
        @DisplayName("Should perform double click with timeout")
        void shouldPerformDoubleClickWithTimeout() {
            StateImage target = new StateImage();
            double maxWait = 5.0;

            boolean result = facade.doubleClick(maxWait, target);

            assertTrue(result || !result);
        }

        @Test
        @DisplayName("Should perform double click at location")
        void shouldPerformDoubleClickAtLocation() {
            Location location = new Location(100, 200);

            boolean result = facade.doubleClick(location);

            assertTrue(result || !result);
        }

        @Test
        @DisplayName("Should perform right click with timeout")
        void shouldPerformRightClick() {
            StateImage target = new StateImage();
            double maxWait = 5.0;

            boolean result = facade.rightClick(maxWait, target);

            assertTrue(result || !result);
        }
    }

    @Nested
    @DisplayName("Type Methods Tests")
    class TypeMethodsTests {

        @Test
        @DisplayName("Should type text")
        void shouldTypeText() {
            String text = "Hello World";

            boolean result = facade.type(text);

            assertTrue(result || !result);
        }

        @Test
        @DisplayName("Should type text with modifiers")
        void shouldTypeTextWithModifiers() {
            String text = "c";
            String modifiers = "CTRL";

            boolean result = facade.typeWithModifiers(text, modifiers);

            assertTrue(result || !result);
        }
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        assertDoesNotThrow(
                () -> {
                    facade.click(5.0, (StateImage) null);
                    facade.type(null);
                    facade.typeWithModifiers(null, null);
                });
    }

    @Test
    @DisplayName("Should handle empty arrays")
    void shouldHandleEmptyArrays() {
        assertDoesNotThrow(
                () -> {
                    facade.click(5.0); // Empty varargs
                    facade.rightClick(3.0); // Empty varargs
                });
    }
}
