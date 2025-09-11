package io.github.jspinak.brobot.patterncapture;

import javax.swing.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import io.github.jspinak.brobot.patterncapture.ui.MainWindow;

/**
 * Main application class for the Brobot Pattern Capture Tool.
 *
 * <p>This is a standalone Spring Boot application with Swing UI that provides SikuliX-like image
 * capture functionality using Brobot's capture system.
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {
            "io.github.jspinak.brobot.patterncapture",
            "io.github.jspinak.brobot.capture" // Include Brobot capture components
        })
public class BrobotPatternCaptureTool {

    public static void main(String[] args) {
        // Set system properties for Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        // Start Spring context
        ConfigurableApplicationContext context =
                SpringApplication.run(BrobotPatternCaptureTool.class, args);

        // Initialize UI on EDT
        SwingUtilities.invokeLater(
                () -> {
                    MainWindow mainWindow = context.getBean(MainWindow.class);
                    mainWindow.initialize();
                    mainWindow.setupHotkeys();
                });
    }
}
