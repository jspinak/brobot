package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Verifies and logs Brobot property configuration after Spring Boot initialization.
 * This component helps diagnose configuration issues by logging the actual runtime values.
 */
@Slf4j
@Component
public class BrobotPropertyVerifier {
    
    @Autowired
    private BrobotProperties properties;
    
    @Autowired
    private BrobotLogger brobotLogger;
    
    @Autowired
    private ExecutionEnvironment executionEnvironment;
    
    /**
     * Logs property verification after application is ready.
     * This ensures all properties have been properly initialized.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void verifyProperties() {
        // Log screenshot and illustration settings
        brobotLogger.log()
            .observation("Brobot Property Verification")
            .metadata("saveHistory", properties.getScreenshot().isSaveHistory())
            .metadata("historyPath", FrameworkSettings.historyPath)
            .metadata("saveHistoryFrameworkSettings", FrameworkSettings.saveHistory)
            .metadata("illustrationEnabled", isIllustrationEnabled())
            .log();
        
        // Log execution environment
        brobotLogger.log()
            .observation("Execution Environment")
            .metadata("mockMode", executionEnvironment.isMockMode())
            .metadata("hasDisplay", executionEnvironment.hasDisplay())
            .metadata("canCaptureScreen", executionEnvironment.canCaptureScreen())
            .metadata("headlessMode", java.awt.GraphicsEnvironment.isHeadless())
            .metadata("osName", System.getProperty("os.name"))
            .metadata("javaAwtHeadless", System.getProperty("java.awt.headless"))
            .log();
        
        // Log illustration settings detail
        brobotLogger.log()
            .observation("Illustration Settings")
            .metadata("drawFind", properties.getIllustration().isDrawFind())
            .metadata("drawClick", properties.getIllustration().isDrawClick())
            .metadata("drawDrag", properties.getIllustration().isDrawDrag())
            .metadata("drawMove", properties.getIllustration().isDrawMove())
            .metadata("drawHighlight", properties.getIllustration().isDrawHighlight())
            .metadata("drawRepeatedActions", properties.getIllustration().isDrawRepeatedActions())
            .metadata("drawClassify", properties.getIllustration().isDrawClassify())
            .metadata("drawDefine", properties.getIllustration().isDrawDefine())
            .log();
        
        // Log framework settings that affect illustrations
        brobotLogger.log()
            .observation("Framework Settings (Illustration Related)")
            .metadata("drawFind", FrameworkSettings.drawFind)
            .metadata("drawClick", FrameworkSettings.drawClick)
            .metadata("drawDrag", FrameworkSettings.drawDrag)
            .metadata("drawMove", FrameworkSettings.drawMove)
            .metadata("drawHighlight", FrameworkSettings.drawHighlight)
            .metadata("drawRepeatedActions", FrameworkSettings.drawRepeatedActions)
            .metadata("drawClassify", FrameworkSettings.drawClassify)
            .metadata("drawDefine", FrameworkSettings.drawDefine)
            .log();
        
        // Provide diagnostic summary
        if (!FrameworkSettings.saveHistory) {
            log.warn("Illustrations are DISABLED: saveHistory is false");
            brobotLogger.log()
                .observation("WARNING: Illustrations Disabled")
                .metadata("reason", "saveHistory is false")
                .metadata("solution", "Set brobot.screenshot.save-history=true in application.properties")
                .log();
        } else if (executionEnvironment.isMockMode()) {
            log.info("Illustrations may be limited in mock mode");
            brobotLogger.observation("Note: Running in mock mode - illustrations use mock data");
        } else if (java.awt.GraphicsEnvironment.isHeadless()) {
            log.warn("Running in HEADLESS mode - illustrations will be created but may have limited content");
            brobotLogger.log()
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
     * Provides a simple console output for critical settings.
     * This method can be called manually for debugging.
     */
    public void printVerification() {
        System.out.println("\n=== Brobot Property Verification ===");
        System.out.println("Save history: " + properties.getScreenshot().isSaveHistory());
        System.out.println("History path: " + FrameworkSettings.historyPath);
        System.out.println("Save history (FrameworkSettings): " + FrameworkSettings.saveHistory);
        System.out.println("Mock mode: " + executionEnvironment.isMockMode());
        System.out.println("Has display: " + executionEnvironment.hasDisplay());
        System.out.println("Can capture screen: " + executionEnvironment.canCaptureScreen());
        System.out.println("===================================\n");
    }
    
    private boolean isIllustrationEnabled() {
        return properties.getScreenshot().isSaveHistory() &&
               (properties.getIllustration().isDrawFind() ||
                properties.getIllustration().isDrawClick() ||
                properties.getIllustration().isDrawDrag() ||
                properties.getIllustration().isDrawMove() ||
                properties.getIllustration().isDrawHighlight() ||
                properties.getIllustration().isDrawClassify() ||
                properties.getIllustration().isDrawDefine());
    }
}