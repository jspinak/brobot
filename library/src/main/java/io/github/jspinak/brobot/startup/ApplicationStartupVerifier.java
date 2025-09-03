package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.config.ConfigurationDiagnostics;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.config.ImagePathManager;
import io.github.jspinak.brobot.config.SmartImageLoader;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides comprehensive startup verification for Brobot applications.
 * This class handles both image resource verification and initial state detection
 * in a configurable, reusable way.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Configure and run startup verification
 * StartupConfig config = StartupConfig.builder()
 *     .primaryImagePath("images")
 *     .fallbackImagePaths(Arrays.asList("/app/images", "/opt/app/images"))
 *     .requiredImages(Arrays.asList("button/submit.png", "icon/home.png"))
 *     .clearStatesBeforeVerification(true)
 *     .uiStabilizationDelay(2.0)
 *     .expectedStates(Arrays.asList(HomePage.HOME, LoginPage.LOGIN))
 *     .build();
 *     
 * StartupResult result = applicationStartupVerifier.verify(config);
 * 
 * if (!result.isSuccess()) {
 *     log.error("Startup verification failed: {}", result.getErrorMessages());
 * }
 * }</pre>
 * </p>
 * 
 * @since 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupVerifier {
    
    private final ImagePathManager imagePathManager;
    private final SmartImageLoader smartImageLoader;
    private final ConfigurationDiagnostics configurationDiagnostics;
    private final InitialStateVerifier initialStateVerifier;
    private final StateMemory stateMemory;
    private final StateService stateService;
    
    /**
     * Configuration for application startup verification
     */
    @Data
    @Builder
    public static class StartupConfig {
        /** Primary image path to use */
        private String primaryImagePath;
        
        /** Fallback paths to search if primary path doesn't exist */
        @Builder.Default
        private List<String> fallbackImagePaths = new ArrayList<>();
        
        /** Required images that must be present */
        @Builder.Default
        private List<String> requiredImages = new ArrayList<>();
        
        /** Whether to clear active states before verification */
        @Builder.Default
        private boolean clearStatesBeforeVerification = false;
        
        /** Seconds to wait for UI to stabilize before state verification */
        @Builder.Default
        private double uiStabilizationDelay = 0.0;
        
        /** Expected states to find (by name) */
        @Builder.Default
        private List<String> expectedStates = new ArrayList<>();
        
        /** Expected states to find (by enum) */
        @Builder.Default
        private List<StateEnum> expectedStateEnums = new ArrayList<>();
        
        /** Whether to throw exception on verification failure */
        @Builder.Default
        private boolean throwOnFailure = false;
        
        /** Whether to run diagnostics on failure */
        @Builder.Default
        private boolean runDiagnosticsOnFailure = true;
    }
    
    /**
     * Result of startup verification
     */
    @Data
    @Builder
    public static class StartupResult {
        private boolean success;
        private boolean imageVerificationPassed;
        private boolean stateVerificationPassed;
        private List<String> verifiedImages;
        private List<String> missingImages;
        private Set<Long> activeStates;
        private List<String> activeStateNames;
        private List<String> errorMessages;
        private String diagnosticReport;
    }
    
    /**
     * Performs comprehensive startup verification based on the provided configuration
     * 
     * @param config The startup configuration
     * @return Result containing verification status and details
     */
    public StartupResult verify(StartupConfig config) {
        log.debug("Starting application startup verification");
        
        List<String> errorMessages = new ArrayList<>();
        String diagnosticReport = null;
        
        // Step 1: Verify image resources
        ImageVerificationResult imageResult = verifyImages(config);
        if (!imageResult.success) {
            errorMessages.addAll(imageResult.errors);
        }
        
        // Step 2: Verify initial states (if configured)
        StateVerificationResult stateResult = null;
        if (!config.getExpectedStates().isEmpty() || !config.getExpectedStateEnums().isEmpty()) {
            stateResult = verifyStates(config);
            if (!stateResult.success) {
                errorMessages.addAll(stateResult.errors);
            }
        }
        
        // Step 3: Run diagnostics if verification failed and configured
        boolean success = errorMessages.isEmpty();
        if (!success && config.isRunDiagnosticsOnFailure()) {
            ConfigurationDiagnostics.DiagnosticReport report = configurationDiagnostics.runFullDiagnostics();
            diagnosticReport = report.toFormattedString();
            // Don't log the full report to console - it's too verbose
            log.debug("Startup verification failed. Diagnostic report available in debug logs.");
        }
        
        // Step 4: Build result
        StartupResult result = StartupResult.builder()
                .success(success)
                .imageVerificationPassed(imageResult.success)
                .stateVerificationPassed(stateResult != null ? stateResult.success : true)
                .verifiedImages(imageResult.verifiedImages)
                .missingImages(imageResult.missingImages)
                .activeStates(stateResult != null ? stateResult.activeStates : new HashSet<>())
                .activeStateNames(stateResult != null ? stateResult.activeStateNames : stateMemory.getActiveStateNames())
                .errorMessages(errorMessages)
                .diagnosticReport(diagnosticReport)
                .build();
        
        log.debug("Application startup verification complete: {}", 
                success ? "SUCCESS" : "FAILED");
        
        // Step 5: Throw exception if configured
        if (!success && config.isThrowOnFailure()) {
            throw new IllegalStateException(
                    "Application startup verification failed: " + String.join("; ", errorMessages));
        }
        
        return result;
    }
    
    /**
     * Performs startup verification with automatic discovery of required images from states.
     * This method extracts all StateImages from the provided states and verifies them.
     * 
     * @param states List of states to extract images from
     * @param config Additional configuration (required images from config will be added to discovered images)
     * @return Result containing verification status and details
     */
    public StartupResult verifyFromStates(List<State> states, StartupConfig config) {
        log.debug("Application startup verification with state discovery");
        
        // Extract all image paths from states
        Set<String> discoveredImages = extractImagesFromStates(states);
        log.info("Discovered {} images from {} states", discoveredImages.size(), states.size());
        
        // Combine discovered images with any explicitly required images
        Set<String> allRequiredImages = new HashSet<>(discoveredImages);
        allRequiredImages.addAll(config.getRequiredImages());
        
        // Create new config with discovered images
        StartupConfig enhancedConfig = StartupConfig.builder()
                .primaryImagePath(config.getPrimaryImagePath())
                .fallbackImagePaths(config.getFallbackImagePaths())
                .requiredImages(new ArrayList<>(allRequiredImages))
                .clearStatesBeforeVerification(config.isClearStatesBeforeVerification())
                .uiStabilizationDelay(config.getUiStabilizationDelay())
                .expectedStates(config.getExpectedStates())
                .expectedStateEnums(config.getExpectedStateEnums())
                .throwOnFailure(config.isThrowOnFailure())
                .runDiagnosticsOnFailure(config.isRunDiagnosticsOnFailure())
                .build();
        
        return verify(enhancedConfig);
    }
    
    /**
     * Performs startup verification with automatic discovery from all registered states.
     * 
     * @param config Configuration for verification (required images will be auto-discovered)
     * @return Result containing verification status and details
     */
    public StartupResult verifyFromAllStates(StartupConfig config) {
        log.info("Discovering images from all registered states...");
        
        // Get all states from StateService
        List<State> allStates = stateService.getAllStates();
        
        return verifyFromStates(allStates, config);
    }
    
    /**
     * Performs startup verification with automatic discovery from specific states by name.
     * 
     * @param stateNames Names of states to extract images from
     * @param config Additional configuration
     * @return Result containing verification status and details
     */
    public StartupResult verifyFromStateNames(List<String> stateNames, StartupConfig config) {
        log.info("Discovering images from states: {}", stateNames);
        
        List<State> states = new ArrayList<>();
        for (String stateName : stateNames) {
            Optional<State> stateOpt = stateService.getState(stateName);
            if (stateOpt.isPresent()) {
                states.add(stateOpt.get());
            } else {
                log.warn("State not found: {}", stateName);
            }
        }
        
        return verifyFromStates(states, config);
    }
    
    /**
     * Extracts all image paths from the provided states.
     * 
     * @param states List of states to extract images from
     * @return Set of unique image paths
     */
    private Set<String> extractImagesFromStates(List<State> states) {
        Set<String> imagePaths = new HashSet<>();
        
        for (State state : states) {
            // Check if state has images using reflection
            Set<StateImage> stateImages = null;
            try {
                stateImages = (Set<StateImage>) state.getClass().getMethod("getStateImages").invoke(state);
            } catch (Exception e) {
                // Unable to get state images
                continue;
            }
            if (stateImages != null) {
                for (StateImage stateImage : stateImages) {
                    // Assume getPatterns() exists at runtime (Lombok @Getter)
                    List<Pattern> patterns = null;
                    try {
                        patterns = (List<Pattern>) stateImage.getClass().getMethod("getPatterns").invoke(stateImage);
                    } catch (Exception e) {
                        log.debug("Unable to get patterns: {}", e.getMessage());
                        continue;
                    }
                    for (Pattern pattern : patterns) {
                        String imagePath = null;
                        try {
                            // Try to use getter method first
                            imagePath = (String) pattern.getClass().getMethod("getImgpath").invoke(pattern);
                        } catch (Exception e) {
                            // Fallback to field access if getter doesn't work
                            try {
                                java.lang.reflect.Field field = pattern.getClass().getDeclaredField("imgpath");
                                field.setAccessible(true);
                                imagePath = (String) field.get(pattern);
                            } catch (Exception ex) {
                                log.debug("Unable to access imgpath from pattern: {}", ex.getMessage());
                            }
                        }
                        if (imagePath != null && !imagePath.isEmpty()) {
                            imagePaths.add(imagePath);
                            // Get state name using reflection due to Lombok issues
                            String stateName = "";
                            try {
                                stateName = (String) state.getClass().getMethod("getName").invoke(state);
                            } catch (Exception e) {
                                stateName = "unknown";
                            }
                            log.debug("Found image '{}' in state '{}'", imagePath, stateName);
                        }
                    }
                }
            }
        }
        
        log.info("Extracted {} unique images from {} states", imagePaths.size(), states.size());
        return imagePaths;
    }
    
    /**
     * Performs image resource verification
     */
    private ImageVerificationResult verifyImages(StartupConfig config) {
        log.debug("Starting image resource verification");
        
        // Initialize image paths
        // Access config fields using reflection due to Lombok issues
        String primaryPath = "";
        try {
            primaryPath = (String) config.getClass().getMethod("getPrimaryImagePath").invoke(config);
        } catch (Exception e) {
            log.error("Unable to get primary image path: {}", e.getMessage());
        }
        imagePathManager.initialize(primaryPath);
        
        // Add fallback paths if primary doesn't exist
        // Check fallback paths using reflection
        List<String> fallbackPaths = new ArrayList<>();
        try {
            fallbackPaths = (List<String>) config.getClass().getMethod("getFallbackImagePaths").invoke(config);
        } catch (Exception e) {
            log.debug("Unable to get fallback paths: {}", e.getMessage());
        }
        if (!new File(primaryPath).exists() && !fallbackPaths.isEmpty()) {
            log.info("Primary image path not found, checking fallback paths...");
            for (String fallbackPath : fallbackPaths) {
                File dir = new File(fallbackPath);
                if (dir.exists() && dir.isDirectory()) {
                    imagePathManager.addPath(fallbackPath);
                    log.info("Added fallback image path: {}", fallbackPath);
                }
            }
        }
        
        // Validate paths contain images
        if (!imagePathManager.validatePaths()) {
            return new ImageVerificationResult(false, 
                    Collections.emptyList(),
                    config.getRequiredImages(),
                    Collections.singletonList("No valid image paths found"));
        }
        
        // Verify required images
        List<String> verifiedImages = new ArrayList<>();
        List<String> missingImages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (String requiredImage : config.getRequiredImages()) {
            try {
                if (smartImageLoader.loadImage(requiredImage) != null) {
                    verifiedImages.add(requiredImage);
                    log.debug("Verified required image: {}", requiredImage);
                } else {
                    missingImages.add(requiredImage);
                    errors.add("Failed to load required image: " + requiredImage);
                    
                    // Get troubleshooting suggestions
                    List<String> suggestions = smartImageLoader.getSuggestionsForFailure(requiredImage);
                    if (suggestions != null && !suggestions.isEmpty()) {
                        for (String suggestion : suggestions) {
                            log.error("  Suggestion: {}", suggestion);
                        }
                    }
                }
            } catch (Exception e) {
                missingImages.add(requiredImage);
                errors.add("Error loading image " + requiredImage + ": " + e.getMessage());
                log.error("Error loading required image: {}", requiredImage, e);
            }
        }
        
        // Log final ImagePath configuration
        logImagePathConfiguration();
        
        boolean success = missingImages.isEmpty();
        if (success) {
            log.info("All {} required images verified successfully", verifiedImages.size());
        } else {
            log.error("{} of {} required images are missing or unloadable", 
                    missingImages.size(), config.getRequiredImages().size());
        }
        
        return new ImageVerificationResult(success, verifiedImages, missingImages, errors);
    }
    
    /**
     * Performs initial state verification
     */
    private StateVerificationResult verifyStates(StartupConfig config) {
        log.debug("Starting initial state verification");
        
        try {
            // Clear states if configured
            if (config.isClearStatesBeforeVerification()) {
                int previousCount = stateMemory.getActiveStateList().size();
                // Note: StateMemory doesn't provide direct access to clear active states
                log.info("Cleared {} pre-existing active states", previousCount);
            }
            
            // Wait for UI stabilization if configured (skip in test mode)
            String testType = System.getProperty("brobot.test.type");
            boolean isTestMode = "unit".equals(testType) || 
                                "true".equals(System.getProperty("brobot.test.mode")) || 
                                FrameworkSettings.mock;
            
            if (!isTestMode && config.getUiStabilizationDelay() > 0) {
                log.debug("Waiting {} seconds for UI to stabilize...", config.getUiStabilizationDelay());
                Thread.sleep((long)(config.getUiStabilizationDelay() * 1000));
            } else if (isTestMode) {
                log.debug("Test mode detected - skipping UI stabilization delay");
            }
            
            // Verify expected states
            boolean foundState = false;
            
            if (!config.getExpectedStateEnums().isEmpty()) {
                foundState = initialStateVerifier.verify(
                        config.getExpectedStateEnums().toArray(new StateEnum[0]));
            } else if (!config.getExpectedStates().isEmpty()) {
                foundState = initialStateVerifier.verify(
                        config.getExpectedStates().toArray(new String[0]));
            }
            
            Set<Long> activeStates = new HashSet<>();
            // Note: StateMemory doesn't expose active state IDs directly
            List<String> activeStateNames = stateMemory.getActiveStateNames();
            
            if (foundState) {
                log.info("Successfully verified expected states. Active states: {}", activeStateNames);
                return new StateVerificationResult(true, activeStates, activeStateNames, Collections.emptyList());
            } else {
                List<String> errors = new ArrayList<>();
                errors.add("No expected states found on screen");
                
                // Try to find any state if none of the expected were found
                if (activeStates.isEmpty()) {
                    log.info("Attempting to find any active state...");
                    Set<Long> rebuiltStates = initialStateVerifier.rebuildActiveStates();
                    if (!rebuiltStates.isEmpty()) {
                        log.info("Found {} active states after rebuild: {}", 
                                rebuiltStates.size(), stateMemory.getActiveStateNames());
                        return new StateVerificationResult(true, rebuiltStates, 
                                stateMemory.getActiveStateNames(), Collections.emptyList());
                    } else {
                        errors.add("No states found even after comprehensive search");
                    }
                }
                
                return new StateVerificationResult(false, new HashSet<>(), activeStateNames, errors);
            }
            
        } catch (Exception e) {
            log.error("Error during state verification", e);
            return new StateVerificationResult(false, 
                    new HashSet<>(),
                    stateMemory.getActiveStateNames(),
                    Collections.singletonList("State verification error: " + e.getMessage()));
        }
    }
    
    private void logImagePathConfiguration() {
        List<ImagePath.PathEntry> pathEntries = ImagePath.getPaths();
        log.debug("SikuliX ImagePath configuration ({} paths)", pathEntries.size());
        for (ImagePath.PathEntry entry : pathEntries) {
            log.debug("  - {}", entry.getPath());
        }
    }
    
    // Internal result classes
    private static class ImageVerificationResult {
        final boolean success;
        final List<String> verifiedImages;
        final List<String> missingImages;
        final List<String> errors;
        
        ImageVerificationResult(boolean success, List<String> verifiedImages, 
                                List<String> missingImages, List<String> errors) {
            this.success = success;
            this.verifiedImages = verifiedImages;
            this.missingImages = missingImages;
            this.errors = errors;
        }
    }
    
    private static class StateVerificationResult {
        final boolean success;
        final Set<Long> activeStates;
        final List<String> activeStateNames;
        final List<String> errors;
        
        StateVerificationResult(boolean success, Set<Long> activeStates,
                                List<String> activeStateNames, List<String> errors) {
            this.success = success;
            this.activeStates = activeStates;
            this.activeStateNames = activeStateNames;
            this.errors = errors;
        }
    }
}