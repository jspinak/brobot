package io.github.jspinak.brobot.startup;

import jakarta.annotation.PostConstruct;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Ensures compatibility between SikuliX and JavaCV by configuring SikuliX to use its bundled OpenCV
 * libraries instead of JavaCV's.
 *
 * <p>This prevents version conflicts between SikuliX (OpenCV 4.3.0) and JavaCV (which we've
 * downgraded to match).
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 1) // Run before OpenCVNativeLibraryInitializer
public class SikuliXCompatibilityInitializer {

    static {
        configureSikuliXOpenCV();
    }

    @PostConstruct
    public void init() {
        configureSikuliXOpenCV();
    }

    private static void configureSikuliXOpenCV() {
        try {
            // Tell SikuliX to use its own OpenCV
            System.setProperty("sikuli.opencv_core", "430");
            System.setProperty("sikuli.opencv_version", "430");
            System.setProperty("sikuli.useJavaCV", "false");

            // Disable SikuliX's JavaCV integration
            // Settings.useJavaCV = false; // This field doesn't exist in current SikuliX version

            // Force SikuliX to load its bundled libraries
            System.setProperty("sikuli.libsDir", "sikulixlibs");
            System.setProperty("sikuli.libPath", "sikulixlibs");

            log.info("Configured SikuliX to use its bundled OpenCV 4.3.0 libraries");

        } catch (Exception e) {
            log.warn("Could not configure SikuliX OpenCV settings: {}", e.getMessage());
        }
    }
}
