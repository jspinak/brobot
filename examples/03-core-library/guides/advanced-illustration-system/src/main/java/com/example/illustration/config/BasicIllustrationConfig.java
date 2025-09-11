package com.example.illustration.config;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

/**
 * Basic illustration configuration examples for Brobot v1.1.0.
 *
 * <p>In Brobot v1.1.0, illustration configuration is done through: - Application properties
 * (brobot.framework.* settings) - ActionConfig with Illustrate enum (YES, NO, USE_GLOBAL)
 *
 * <p>This class demonstrates different configuration profiles for various use cases.
 */
@Configuration
@Slf4j
public class BasicIllustrationConfig {

    /**
     * Log the current illustration configuration settings. In v1.1.0, settings are managed through
     * application properties.
     */
    @PostConstruct
    public void logIllustrationSettings() {
        log.info("=== Brobot v1.1.0 Illustration Configuration ===");
        log.info("Illustration settings are configured in application properties:");
        log.info("- brobot.framework.saveHistory: Enable/disable all illustrations");
        log.info("- brobot.framework.historyPath: Directory for saving illustrations");
        log.info("- brobot.framework.drawFind: Enable FIND action illustrations");
        log.info("- brobot.framework.drawClick: Enable CLICK action illustrations");
        log.info("- brobot.framework.drawMove: Enable MOVE action illustrations");
        log.info("- brobot.framework.drawDrag: Enable DRAG action illustrations");
        log.info("- brobot.framework.drawHighlight: Enable HIGHLIGHT action illustrations");
        log.info("- brobot.framework.drawClassify: Enable CLASSIFY action illustrations");
        log.info("- brobot.framework.drawDefine: Enable DEFINE action illustrations");
        log.info("- brobot.framework.drawRepeatedActions: Allow repeated action illustrations");
    }

    /**
     * Basic configuration profile - balanced illustration settings. Configure in
     * application-basic.properties:
     *
     * <p>brobot.framework.saveHistory=true brobot.framework.historyPath=illustrations/basic
     * brobot.framework.drawFind=true brobot.framework.drawClick=true
     * brobot.framework.drawMove=false brobot.framework.drawDrag=true
     * brobot.framework.drawHighlight=true brobot.framework.drawClassify=true
     * brobot.framework.drawDefine=true brobot.framework.drawRepeatedActions=false
     */
    @Bean
    @Profile("basic")
    public String basicConfigMessage() {
        log.info("Using BASIC illustration profile - balanced settings");
        return "basic-illustration-profile";
    }

    /**
     * Performance configuration profile - minimal illustrations. Configure in
     * application-performance.properties:
     *
     * <p>brobot.framework.saveHistory=true brobot.framework.historyPath=illustrations/performance
     * brobot.framework.drawFind=false brobot.framework.drawClick=true
     * brobot.framework.drawMove=false brobot.framework.drawDrag=false
     * brobot.framework.drawHighlight=false brobot.framework.drawClassify=false
     * brobot.framework.drawDefine=false brobot.framework.drawRepeatedActions=false
     */
    @Bean
    @Profile("performance")
    public String performanceConfigMessage() {
        log.info("Using PERFORMANCE illustration profile - minimal illustrations");
        return "performance-illustration-profile";
    }

    /**
     * Debug configuration profile - all illustrations enabled. Configure in
     * application-debug.properties:
     *
     * <p>brobot.framework.saveHistory=true brobot.framework.historyPath=illustrations/debug
     * brobot.framework.drawFind=true brobot.framework.drawClick=true brobot.framework.drawMove=true
     * brobot.framework.drawDrag=true brobot.framework.drawHighlight=true
     * brobot.framework.drawClassify=true brobot.framework.drawDefine=true
     * brobot.framework.drawRepeatedActions=true
     */
    @Bean
    @Profile("debug")
    public String debugConfigMessage() {
        log.info("Using DEBUG illustration profile - all illustrations enabled");
        return "debug-illustration-profile";
    }

    /**
     * Example bean showing how to programmatically check illustration settings. This is useful for
     * conditional logic based on configuration.
     */
    @Bean
    public IllustrationSettingsChecker illustrationChecker() {
        return new IllustrationSettingsChecker();
    }

    /** Helper class to check illustration settings at runtime. */
    public static class IllustrationSettingsChecker {
        public void logCurrentSettings() {
            // In v1.1.0, settings are accessed through FrameworkSettings
            log.info("Current illustration settings would be checked via FrameworkSettings");
        }
    }
}
