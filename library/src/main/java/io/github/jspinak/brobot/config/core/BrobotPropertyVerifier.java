package io.github.jspinak.brobot.config.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;

import lombok.extern.slf4j.Slf4j;
import io.github.jspinak.brobot.config.core.BrobotProperties;

/**
 * Verifies and logs Brobot property configuration after Spring Boot initialization. This component
 * helps diagnose configuration issues by logging the actual runtime values.
 */
@Slf4j
@Component
public class BrobotPropertyVerifier {

    private final BrobotProperties brobotProperties;
    private final BrobotLogger brobotLogger;

    // Flag to ensure properties are only verified once
    private static boolean propertiesVerified = false;

    @Autowired
    public BrobotPropertyVerifier(BrobotProperties brobotProperties, BrobotLogger brobotLogger) {
        this.brobotProperties = brobotProperties;
        this.brobotLogger = brobotLogger;
    }

    /**
     * Logs property verification after application is ready. This ensures all properties have been
     * properly initialized.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void verifyProperties() {
        // Only verify properties once per application lifecycle
        if (propertiesVerified) {
            return;
        }
        propertiesVerified = true;

        // Log screenshot and illustration settings
        brobotLogger
                .log()
                .observation("Brobot Property Verification")
                .metadata("saveHistory", brobotProperties.getScreenshot().isSaveHistory())
                .metadata("historyPath", brobotProperties.getScreenshot().getHistoryPath())
                .metadata("illustrationEnabled", isIllustrationEnabled())
                .log();

        // Log execution environment - use singleton instance
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        brobotLogger
                .log()
                .observation("Execution Environment")
                .metadata("mockMode", env.isMockMode())
                .metadata("hasDisplay", env.hasDisplay())
                .metadata("canCaptureScreen", env.canCaptureScreen())
                .metadata("headlessMode", java.awt.GraphicsEnvironment.isHeadless())
                .metadata("osName", System.getProperty("os.name"))
                .metadata("javaAwtHeadless", System.getProperty("java.awt.headless"))
                .log();

        // Log illustration settings detail
        brobotLogger
                .log()
                .observation("Illustration Settings")
                .metadata("drawFind", brobotProperties.getIllustration().isDrawFind())
                .metadata("drawClick", brobotProperties.getIllustration().isDrawClick())
                .metadata("drawDrag", brobotProperties.getIllustration().isDrawDrag())
                .metadata("drawMove", brobotProperties.getIllustration().isDrawMove())
                .metadata("drawHighlight", brobotProperties.getIllustration().isDrawHighlight())
                .metadata(
                        "drawRepeatedActions", brobotProperties.getIllustration().isDrawRepeatedActions())
                .metadata("drawClassify", brobotProperties.getIllustration().isDrawClassify())
                .metadata("drawDefine", brobotProperties.getIllustration().isDrawDefine())
                .log();

        // Log framework settings that affect illustrations
        brobotLogger
                .log()
                .observation("Framework Settings (Illustration Related)")
                .metadata("drawFind", brobotProperties.getIllustration().isDrawFind())
                .metadata("drawClick", brobotProperties.getIllustration().isDrawClick())
                .metadata("drawDrag", brobotProperties.getIllustration().isDrawDrag())
                .metadata("drawMove", brobotProperties.getIllustration().isDrawMove())
                .metadata("drawHighlight", brobotProperties.getIllustration().isDrawHighlight())
                .metadata("drawRepeatedActions", brobotProperties.getIllustration().isDrawRepeatedActions())
                .metadata("drawClassify", brobotProperties.getIllustration().isDrawClassify())
                .metadata("drawDefine", brobotProperties.getIllustration().isDrawDefine())
                .log();

        // Provide diagnostic summary
        if (!brobotProperties.getScreenshot().isSaveHistory()) {
            log.warn("Illustrations are DISABLED: saveHistory is false");
            brobotLogger
                    .log()
                    .observation("WARNING: Illustrations Disabled")
                    .metadata("reason", "saveHistory is false")
                    .metadata(
                            "solution",
                            "Set brobot.screenshot.save-history=true in application.properties")
                    .log();
        } else if (env.isMockMode()) {
            log.info("Illustrations may be limited in mock mode");
            brobotLogger.observation("Note: Running in mock mode - illustrations use mock data");
        } else if (java.awt.GraphicsEnvironment.isHeadless()) {
            log.warn(
                    "Running in HEADLESS mode - illustrations will be created but may have limited"
                            + " content");
            brobotLogger
                    .log()
                    .observation("WARNING: Headless Mode Active")
                    .metadata("headless", true)
                    .metadata("note", "Illustrations will be created using cached screenshots")
                    .log();
        } else {
            log.info("Illustrations are ENABLED");
            brobotLogger.observation("Illustrations are enabled and configured");
        }
    }

    /**
     * Provides a simple console output for critical settings. This method can be called manually
     * for debugging.
     */
    public void printVerification() {
        System.out.println("\n=== Brobot Property Verification ===");
        System.out.println("Save history: " + brobotProperties.getScreenshot().isSaveHistory());
        System.out.println("History path: " + brobotProperties.getScreenshot().getHistoryPath());
        System.out.println("Save history (BrobotProperties): " + brobotProperties.getScreenshot().isSaveHistory());
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        System.out.println("Mock mode: " + env.isMockMode());
        System.out.println("Has display: " + env.hasDisplay());
        System.out.println("Can capture screen: " + env.canCaptureScreen());
        System.out.println("===================================\n");
    }

    private boolean isIllustrationEnabled() {
        return brobotProperties.getScreenshot().isSaveHistory()
                && (brobotProperties.getIllustration().isDrawFind()
                        || brobotProperties.getIllustration().isDrawClick()
                        || brobotProperties.getIllustration().isDrawDrag()
                        || brobotProperties.getIllustration().isDrawMove()
                        || brobotProperties.getIllustration().isDrawHighlight()
                        || brobotProperties.getIllustration().isDrawClassify()
                        || brobotProperties.getIllustration().isDrawDefine());
    }
}
