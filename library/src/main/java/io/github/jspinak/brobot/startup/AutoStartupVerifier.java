package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatic startup verifier that handles both image and state verification
 * in a two-phase approach without requiring application-specific code.
 * 
 * <p>Phase 1 (ApplicationRunner): Verifies images from configured states</p>
 * <p>Phase 2 (StatesRegisteredEvent): Verifies states are active on screen</p>
 * 
 * <p>Enable with: {@code brobot.startup.auto-verify=true}</p>
 * 
 * <p>Configuration:
 * <pre>
 * brobot:
 *   startup:
 *     auto-verify: true
 *     verify-states: "Working,Prompt"  # States to verify (comma-separated)
 *     image-path: images
 *     fallback-paths:
 *       - "${user.home}/images"
 *       - "/opt/app/images"
 *     clear-states-before-verify: true
 *     ui-stabilization-delay: 2.0
 *     throw-on-failure: false
 * </pre>
 * </p>
 * 
 * @since 1.1.0
 */
@Component
@ConditionalOnProperty(name = "brobot.startup.auto-verify", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run early
public class AutoStartupVerifier implements ApplicationRunner {
    
    private final ApplicationStartupVerifier applicationStartupVerifier;
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
    // Configuration properties with sensible defaults
    @Value("${brobot.startup.verify-states:}")
    private String verifyStatesConfig;
    
    @Value("${brobot.startup.image-path:images}")
    private String imagePath;
    
    @Value("${brobot.startup.fallback-paths:}")
    private List<String> fallbackPaths;
    
    @Value("${brobot.startup.clear-states-before-verify:true}")
    private boolean clearStatesBeforeVerify;
    
    @Value("${brobot.startup.ui-stabilization-delay:2.0}")
    private double uiStabilizationDelay;
    
    @Value("${brobot.startup.throw-on-failure:false}")
    private boolean throwOnFailure;
    
    @Value("${brobot.startup.run-diagnostics-on-failure:true}")
    private boolean runDiagnosticsOnFailure;
    
    private boolean imageVerificationCompleted = false;
    private List<String> statesToVerify;
    
    /**
     * Phase 1: Image verification at application startup
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Brobot Auto Startup Verification: Phase 1 (Images) ===");
        
        // Parse states to verify
        statesToVerify = parseStatesToVerify();
        if (statesToVerify.isEmpty()) {
            log.info("No states configured for verification. Skipping auto-verification.");
            return;
        }
        
        try {
            if (brobotLogger != null) {
                try (var operation = brobotLogger.operation("AutoStartupImageVerification")) {
                    performImageVerification();
                }
            } else {
                performImageVerification();
            }
        } catch (Exception e) {
            log.error("Error during image verification", e);
            if (throwOnFailure) {
                throw e;
            }
        }
    }
    
    /**
     * Phase 2: State verification after states are registered
     */
    @EventListener(StatesRegisteredEvent.class)
    @Order(10)
    public void onStatesRegistered(StatesRegisteredEvent event) {
        if (statesToVerify == null || statesToVerify.isEmpty()) {
            return; // Auto-verification not configured
        }
        
        log.info("=== Brobot Auto Startup Verification: Phase 2 (States) ===");
        log.info("States registered - verifying active states... (states: {}, transitions: {})", 
                event.getStateCount(), event.getTransitionCount());
        
        try {
            if (brobotLogger != null) {
                try (var operation = brobotLogger.operation("AutoStartupStateVerification")) {
                    performStateVerification();
                }
            } else {
                performStateVerification();
            }
        } catch (Exception e) {
            log.error("Error during state verification", e);
            if (throwOnFailure) {
                throw new IllegalStateException("State verification failed", e);
            }
        }
    }
    
    private void performImageVerification() {
        logMetadata("Starting automatic image verification", 
            "statesToVerify", statesToVerify,
            "primaryImagePath", imagePath,
            "fallbackPathCount", fallbackPaths.size()
        );
        
        // Build configuration with automatic fallback paths
        List<String> enhancedFallbackPaths = buildEnhancedFallbackPaths();
        
        ApplicationStartupVerifier.StartupConfig config = ApplicationStartupVerifier.StartupConfig.builder()
                .primaryImagePath(imagePath)
                .fallbackImagePaths(enhancedFallbackPaths)
                .requiredImages(new ArrayList<>()) // Will be auto-discovered
                .clearStatesBeforeVerification(false) // Don't clear yet
                .runDiagnosticsOnFailure(runDiagnosticsOnFailure)
                .throwOnFailure(false) // Handle internally
                .build();
        
        // Run image verification with auto-discovery
        ApplicationStartupVerifier.StartupResult result = 
            applicationStartupVerifier.verifyFromStateNames(statesToVerify, config);
        
        imageVerificationCompleted = result.isImageVerificationPassed();
        
        if (result.isSuccess()) {
            logMetadata("Image verification completed successfully",
                "verifiedImages", result.getVerifiedImages().size()
            );
        } else {
            logMetadata("Image verification completed with errors",
                "missingImages", result.getMissingImages(),
                "errors", result.getErrorMessages()
            );
            
            if (throwOnFailure) {
                throw new IllegalStateException("Image verification failed: " + 
                    String.join("; ", result.getErrorMessages()));
            }
        }
    }
    
    private void performStateVerification() {
        logMetadata("Starting automatic state verification",
            "imageVerificationCompleted", imageVerificationCompleted,
            "clearStatesBeforeVerify", clearStatesBeforeVerify,
            "uiStabilizationDelay", uiStabilizationDelay
        );
        
        ApplicationStartupVerifier.StartupConfig config = ApplicationStartupVerifier.StartupConfig.builder()
                .primaryImagePath(imagePath) // Already configured
                .clearStatesBeforeVerification(clearStatesBeforeVerify)
                .uiStabilizationDelay(uiStabilizationDelay)
                .expectedStates(statesToVerify)
                .runDiagnosticsOnFailure(false) // Already ran if needed
                .throwOnFailure(false)
                .build();
        
        ApplicationStartupVerifier.StartupResult result = applicationStartupVerifier.verify(config);
        
        if (result.isStateVerificationPassed()) {
            logMetadata("State verification completed successfully",
                "activeStates", result.getActiveStateNames(),
                "activeStateCount", result.getActiveStates().size()
            );
            log.info("Application ready. Active states: {}", result.getActiveStateNames());
        } else {
            logMetadata("State verification completed with warnings",
                "activeStates", result.getActiveStateNames(),
                "errors", result.getErrorMessages()
            );
            
            if (throwOnFailure) {
                throw new IllegalStateException("State verification failed: " + 
                    String.join("; ", result.getErrorMessages()));
            }
        }
    }
    
    private List<String> parseStatesToVerify() {
        List<String> states = new ArrayList<>();
        if (verifyStatesConfig != null && !verifyStatesConfig.trim().isEmpty()) {
            for (String state : verifyStatesConfig.split(",")) {
                String trimmed = state.trim();
                if (!trimmed.isEmpty()) {
                    states.add(trimmed);
                }
            }
        }
        return states;
    }
    
    private List<String> buildEnhancedFallbackPaths() {
        List<String> enhanced = new ArrayList<>(fallbackPaths);
        
        // Add common fallback patterns if not already present
        String userHome = System.getProperty("user.home");
        String appName = System.getProperty("spring.application.name", "brobot-app");
        
        // Common patterns
        enhanced.add(userHome + "/" + appName + "/images");
        enhanced.add(userHome + "/Documents/" + appName + "/images");
        enhanced.add(userHome + "/.config/" + appName + "/images");
        enhanced.add("/opt/" + appName + "/images");
        enhanced.add("/usr/local/" + appName + "/images");
        
        return enhanced;
    }
    
    private void logMetadata(String message, Object... keyValuePairs) {
        if (brobotLogger != null) {
            var logBuilder = brobotLogger.log().observation(message);
            for (int i = 0; i < keyValuePairs.length; i += 2) {
                if (i + 1 < keyValuePairs.length) {
                    logBuilder.metadata(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
                }
            }
            logBuilder.log();
        } else {
            log.info("{}", message);
        }
    }
}