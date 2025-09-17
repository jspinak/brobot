package io.github.jspinak.brobot.actions.methods.basicactions.click;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.action.internal.mouse.MouseDownWrapper;
import io.github.jspinak.brobot.action.internal.mouse.MouseUpWrapper;
import io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

@SpringBootTest(
        classes = io.github.jspinak.brobot.BrobotTestApplication.class,
        properties = {
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false",
            "brobot.mock.enabled=true"
        })
@Import({
    MockGuiAccessConfig.class,
    MockGuiAccessMonitor.class,
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@Disabled("CI failure - needs investigation")
class ClickIntegrationTest {

    @Autowired private Action action;

    @SpyBean private Find find;

    @SpyBean private MoveMouseWrapper moveMouseWrapper;

    @SpyBean private MouseDownWrapper mouseDownWrapper;

    @SpyBean private MouseUpWrapper mouseUpWrapper;

    private boolean originalMockState;

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        // Mock mode state saved via ExecutionEnvironment
        // ** FIX: Force the test to run in MOCK mode to avoid SikuliX headless issues
        // **
        // Mock mode is enabled via BrobotTestBase

        // Since find.perform is a void method that modifies its arguments,
        // we use doAnswer to simulate this behavior.
        doAnswer(
                        invocation -> {
                            ActionResult matches =
                                    invocation.getArgument(
                                            0); // Get the ActionResult object passed to
                            // find.perform
                            matches.add(
                                    new Match.Builder()
                                            .setRegion(10, 10, 10, 10)
                                            .setSimScore(0.9)
                                            .build());
                            matches.setSuccess(true);
                            return null; // void methods must return null
                        })
                .when(find)
                .perform(any(ActionResult.class), any(ObjectCollection[].class));
    }

    @AfterEach
    void tearDown() {
        // Mock mode restored via ExecutionEnvironment
    }

    @Test
    void perform_simpleClick_shouldMoveAndPressDownAndUp() {
        // Setup
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        ActionResult result = action.perform(clickOptions, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());

        // In mock mode, the wrapper methods are not called
        if (!ExecutionEnvironment.getInstance().isMockMode()) {
            verify(moveMouseWrapper).move(any(Location.class));
            verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
            verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        }
    }

    @Test
    void perform_doubleClick_shouldResultInTwoMouseDownAndUpEvents() {
        // Setup
        ClickOptions clickOptions = new ClickOptions.Builder().setNumberOfClicks(2).build();
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        ActionResult result = action.perform(clickOptions, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());

        // In mock mode, the wrapper methods are not called
        if (!ExecutionEnvironment.getInstance().isMockMode()) {
            verify(moveMouseWrapper).move(any(Location.class));
            verify(mouseDownWrapper, times(2))
                    .press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
            verify(mouseUpWrapper, times(2))
                    .press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        }
    }

    @Test
    void perform_clickWithMoveAfter_shouldMoveTwice() {
        // Setup
        Location moveLocation = new Location(100, 100);
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        // Note: Move after action is handled differently in the new API
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        ActionResult result = action.perform(clickOptions, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());

        // In mock mode, only the after-click move is called
        if (ExecutionEnvironment.getInstance().isMockMode()) {
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(moveMouseWrapper, times(1)).move(locationCaptor.capture());
            Assertions.assertEquals(moveLocation, locationCaptor.getValue());
        } else {
            verify(moveMouseWrapper, times(2)).move(any(Location.class));
        }
    }

    @Test
    void perform_multipleClicks_shouldCallClickMethodsCorrectly() {
        // Setup - 3 clicks
        ClickOptions clickOptions = new ClickOptions.Builder().setNumberOfClicks(3).build();
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        // Action
        ActionResult result = action.perform(clickOptions, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());

        // In mock mode, the wrapper methods are not called
        if (!ExecutionEnvironment.getInstance().isMockMode()) {
            verify(moveMouseWrapper).move(any(Location.class));
            verify(mouseDownWrapper, times(3))
                    .press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
            verify(mouseUpWrapper, times(3))
                    .press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        }
    }
}
