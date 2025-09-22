package io.github.jspinak.brobot.config.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

import lombok.extern.slf4j.Slf4j;

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
        brobotLogger.builder(LogCategory.SYSTEM)
                .message("Brobot Property Verification")
                .context("saveHistory", brobotProperties.getScreenshot().isSaveHistory())
                .context("historyPath", brobotProperties.getScreenshot().getHistoryPath())
                .context("illustrationEnabled", isIllustrationEnabled())
                .log();

        // Log execution environment - use singleton instance
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        brobotLogger.builder(LogCategory.SYSTEM)
                .message("Execution Environment")
                .context("mockMode", env.isMockMode())
                .context("hasDisplay", env.hasDisplay())
                .context("canCaptureScreen", env.canCaptureScreen())
                .context("headlessMode", java.awt.GraphicsEnvironment.isHeadless())
                .context("osName", System.getProperty("os.name"))
                .context("javaAwtHeadless", System.getProperty("java.awt.headless"))
                .log();

        // Log illustration settings detail
        brobotLogger.builder(LogCategory.SYSTEM)
                .message("Illustration Settings")
                .context("drawFind", brobotProperties.getIllustration().isDrawFind())
                .context("drawClick", brobotProperties.getIllustration().isDrawClick())
                .context("drawDrag", brobotProperties.getIllustration().isDrawDrag())
                .context("drawMove", brobotProperties.getIllustration().isDrawMove())
                .context("drawHighlight", brobotProperties.getIllustration().isDrawHighlight())
                .context(
                        "drawRepeatedActions",
                        brobotProperties.getIllustration().isDrawRepeatedActions())
                .context("drawClassify", brobotProperties.getIllustration().isDrawClassify())
                .context("drawDefine", brobotProperties.getIllustration().isDrawDefine())
                .log();

        // Log framework settings that affect illustrations - removed duplicate

        // Provide diagnostic summary
        if (!brobotProperties.getScreenshot().isSaveHistory()) {
            log.warn("Illustrations are DISABLED: saveHistory is false");
            brobotLogger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.WARN)
                    .message("WARNING: Illustrations Disabled")
                    .context("reason", "saveHistory is false")
                    .context(
                            "solution",
                            "Set brobot.screenshot.save-history=true in application.properties")
                    .log();
        } else if (env.isMockMode()) {
            log.info("Illustrations may be limited in mock mode");
            brobotLogger.builder(LogCategory.SYSTEM).message("Note: Running in mock mode - illustrations use mock data").log();
        } else if (java.awt.GraphicsEnvironment.isHeadless()) {
            log.warn(
                    "Running in HEADLESS mode - illustrations will be created but may have limited"
                            + " content");
            brobotLogger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.WARN)
                    .message("WARNING: Headless Mode Active")
                    .context("headless", true)
                    .context("note", "Illustrations will be created using cached screenshots")
                    .log();
        } else {
            log.info("Illustrations are ENABLED");
            brobotLogger.builder(LogCategory.SYSTEM).message("Illustrations are enabled and configured").log();
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
        System.out.println(
                "Save history (BrobotProperties): "
                        + brobotProperties.getScreenshot().isSaveHistory());
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
