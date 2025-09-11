package com.example.basics;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.basics.automation.BasicAutomation;

import io.github.jspinak.brobot.statemanagement.StateMemory;

import lombok.extern.slf4j.Slf4j;

/** Runs the tutorial basics examples. */
@Component
@Slf4j
public class TutorialRunner implements CommandLineRunner {

    private final BasicAutomation basicAutomation;
    private final StateMemory stateMemory;

    public TutorialRunner(BasicAutomation basicAutomation, StateMemory stateMemory) {
        this.basicAutomation = basicAutomation;
        this.stateMemory = stateMemory;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("Brobot Tutorial Basics");
        log.info("================================================");
        log.info("");

        // Show initial state
        log.info("Starting tutorial...");
        log.info("Initial state: {}", stateMemory.getActiveStateNamesAsString());
        log.info("");

        // Verify states are registered
        log.info(">>> Verifying State Registration <<<");
        basicAutomation.verifyStates();
        log.info("");

        Thread.sleep(1000);

        // Navigate through all states
        log.info(">>> Navigating Through States <<<");
        basicAutomation.navigateAllStates();
        log.info("");

        Thread.sleep(1000);

        // Demonstrate error recovery
        log.info(">>> Error Recovery <<<");
        basicAutomation.errorRecoveryExample();
        log.info("");

        log.info("================================================");
        log.info("Tutorial completed!");
        log.info("================================================");
        log.info("");

        log.info("Key concepts demonstrated:");
        log.info("✓ State management with @State annotation");
        log.info("✓ Automatic state registration");
        log.info("✓ Transitions with @Transition annotation");
        log.info("✓ StateObject with images and regions");
        log.info("✓ Pattern finding and text extraction");
        log.info("✓ Navigation between states");
        log.info("✓ Error recovery");

        log.info("");
        log.info("Next steps:");
        log.info("- Add your own states and transitions");
        log.info("- Experiment with different action configurations");
        log.info("- Try running with real UI (mock: false)");
        log.info("- Explore advanced features like cross-state dependencies");
    }
}
