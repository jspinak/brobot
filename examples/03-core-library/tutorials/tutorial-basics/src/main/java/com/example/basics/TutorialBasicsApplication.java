package com.example.basics;

import static com.example.basics.StateNames.*;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.example.basics.automation.SaveLabeledImages;

import io.github.jspinak.brobot.statemanagement.InitialStates;

/**
 * Tutorial Basics - Introduction to Brobot fundamentals.
 *
 * <p>This example demonstrates: - State management with @State annotation - Transitions between
 * states with @Transition - StateImage and StateRegion objects - Basic action execution - Mock mode
 * for testing - Proper Spring Boot configuration for GUI automation
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.basics", "io.github.jspinak.brobot"})
public class TutorialBasicsApplication {

    public static void main(String[] args) {
        // Configure Spring to automate the GUI
        SpringApplicationBuilder builder =
                new SpringApplicationBuilder(TutorialBasicsApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        // Setup Brobot
        // Configuration would go here in a real app
        // For example: setting image paths, mock mode, etc.

        // Find initial active States
        InitialStates initialStates = context.getBean(InitialStates.class);
        initialStates.addStateSet(90, WORLD);
        initialStates.addStateSet(10, HOME);
        initialStates.findIntialStates();

        // get and save labeled images
        SaveLabeledImages saveLabeledImages = context.getBean(SaveLabeledImages.class);
        saveLabeledImages.saveImages(100);
    }
}
