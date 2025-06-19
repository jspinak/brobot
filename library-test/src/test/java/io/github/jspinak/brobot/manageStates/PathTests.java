package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.BrobotEnvironment;
import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.test.HeadlessTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        BrobotEnvironment env = BrobotEnvironment.builder()
                .mockMode(true)  // Use mock mode since we don't need real images
                .forceHeadless(true)
                .allowScreenCapture(false)
                .verboseLogging(false)
                .build();
        BrobotEnvironment.setInstance(env);
        BrobotSettings.mock = true;
    }

    @Autowired
    StateTransitionsManagement stateTransitionsManagement;

    @Autowired
    QuickStateStructureBuilder quickStateStructureBuilder;

    @Autowired
    StateTransitionsInProjectService stateTransitionsInProjectService;

    @Test
    void pathTest() {
        // build simple state structure
        BrobotSettings.mock = true;
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
