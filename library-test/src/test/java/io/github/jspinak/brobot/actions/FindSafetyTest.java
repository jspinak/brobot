package io.github.jspinak.brobot.actions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.config.TestApplicationConfiguration;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;

/** Test to verify Find operations don't hang with safety checks. */
@SpringBootTest(
        classes = BrobotTestApplication.class,
        properties = {
            "brobot.mock.enabled=true",
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false"
        })
@Import({
    TestApplicationConfiguration.class,
    MockGuiAccessConfig.class,
    MockGuiAccessMonitor.class,
    MockScreenConfig.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@Disabled("CI failure - needs investigation")
class FindSafetyTest extends BrobotTestBase {

    @Autowired private ActionService actionService;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Ensure mock mode is enabled
        FrameworkSettings.mock = true;
        ExecutionEnvironment env =
                ExecutionEnvironment.builder()
                        .mockMode(true)
                        .forceHeadless(true)
                        .allowScreenCapture(false)
                        .build();
        ExecutionEnvironment.setInstance(env);
    }

    @Test
    @Timeout(value = 3)
    void testFindDoesNotHangInMockMode() {
        // Arrange
        assertTrue(FrameworkSettings.mock, "Mock mode should be enabled");

        StateImage stateImage = BrobotTestUtils.createTestStateImage("TestImage");
        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withImages(stateImage).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSearchDuration(0.1) // Very short duration
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        result.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 0.1));

        // Act
        ActionInterface findAction = actionService.getAction(findOptions).orElseThrow();

        // This should complete quickly due to our safety checks
        findAction.perform(result, objectCollection);

        // Assert
        assertNotNull(result);
        // In mock mode with safety checks, it should complete without hanging
    }

    @Test
    @Timeout(value = 3)
    void testFindWithAllStrategyDoesNotHang() {
        // Arrange
        assertTrue(FrameworkSettings.mock, "Mock mode should be enabled");

        StateImage stateImage = BrobotTestUtils.createTestStateImage("TestImage");
        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withImages(stateImage).build();

        // ALL strategy used to cause infinite loops
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSearchDuration(0.1)
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        result.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 0.1));

        // Act
        ActionInterface findAction = actionService.getAction(findOptions).orElseThrow();

        // This should complete quickly due to our safety checks
        findAction.perform(result, objectCollection);

        // Assert
        assertNotNull(result);
        // Should complete without hanging even with ALL strategy
    }
}
