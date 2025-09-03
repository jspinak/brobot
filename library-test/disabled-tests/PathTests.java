package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.test.HeadlessTestConfiguration;
import io.github.jspinak.brobot.tools.builder.FluentStateBuilder;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(HeadlessTestConfiguration.class)
public class PathTests extends BrobotIntegrationTestBase {
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    @Override
    protected void setUpBrobotEnvironment() {
        // Configure for mock mode since we don't need real images for path testing
        ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(true) // Use mock mode since we don't need real images
                .forceHeadless(true)
                .allowScreenCapture(false)
                .verboseLogging(false)
                .build();
        ExecutionEnvironment.setInstance(env);
        FrameworkSettings.mock = true;
    }

    @Autowired
    StateNavigator stateTransitionsManagement;

    @Autowired
    FluentStateBuilder quickStateStructureBuilder;

    @Autowired
    StateTransitionService stateTransitionsInProjectService;

    @Test
    void pathTest() {
        // build simple state structure
        FrameworkSettings.mock = true;
        quickStateStructureBuilder
                .newState("a", "topLeft", "b")
                .newState("b", "topLeft2", "c")
                .newState("c", "bottomRight")
                .setStartStates("a")
                .build();
        stateTransitionsInProjectService.printAllTransitions();
        stateTransitionsManagement.openState("c");
    }
}
