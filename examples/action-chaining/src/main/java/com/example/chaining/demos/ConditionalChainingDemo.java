package com.example.chaining.demos;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Demonstrates conditional action chaining patterns
 * where chain execution depends on runtime conditions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionalChainingDemo {
    
    private final Action action;
    private final Random random = new Random();
    
    public void runDemos() {
        log.info("\n=== Conditional Action Chaining Demos ===");
        
        // Demo 1: Login flow with conditional paths
        demonstrateConditionalLogin();
        
        // Demo 2: Feature detection chain
        demonstrateFeatureDetection();
        
        // Demo 3: Multi-path navigation
        demonstrateMultiPathNavigation();
        
        // Demo 4: Retry with fallback
        demonstrateRetryWithFallback();
    }
    
    /**
     * Demo 1: Conditional login flow
     * Shows different paths based on login state
     */
    private void demonstrateConditionalLogin() {
        log.info("\n--- Demo 1: Conditional Login Flow ---");
        
        // Check if already logged in
        PatternFindOptions checkLoggedIn = new PatternFindOptions.Builder()
            .withBeforeActionLog("Checking login status...")
            .withSuccessLog("User already logged in")
            .withFailureLog("Login required")
            .setSuccessCriteria(result -> {
                if (result.isSuccess()) {
                    log.info("Already logged in - skipping login flow");
                    return true;
                }
                return false;
            })
            .build();
        
        // Login flow if not logged in
        ActionConfig loginFlow = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for login button...")
            .withSuccessLog("Login button found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Opening login dialog...")
                .withSuccessLog("Login dialog opened")
                .setPauseAfterEnd(0.5)
                .build())
            // Check for remember me option
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for 'Remember Me' option...")
                .withSuccessLog("'Remember Me' found - will enable")
                .withFailureLog("No 'Remember Me' option")
                .setSuccessCriteria(result -> {
                    if (result.isSuccess()) {
                        // Would click remember me
                        log.info("Enabling 'Remember Me' for future sessions");
                    }
                    return true; // Continue regardless
                })
                .build())
            // Enter credentials
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for username field...")
                .withSuccessLog("Username field found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing username field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering username...")
                .withSuccessLog("Username entered")
                .build())
            // Check for 2FA
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for 2FA requirement...")
                .withSuccessLog("2FA required - preparing for additional step")
                .withFailureLog("No 2FA required")
                .setPauseBeforeBegin(2.0) // Wait for possible 2FA prompt
                .setSuccessCriteria(result -> {
                    if (result.isSuccess()) {
                        log.info("2FA detected - would handle 2FA flow");
                        // Would chain 2FA handling actions
                    }
                    return true;
                })
                .build())
            .build();
        
        log.info("Conditional login chain created - adapts to login state and features");
    }
    
    /**
     * Demo 2: Feature detection chain
     * Shows how to detect and use available features
     */
    private void demonstrateFeatureDetection() {
        log.info("\n--- Demo 2: Feature Detection Chain ---");
        
        // Create feature detection chain
        ActionConfig featureChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("Detecting available features...")
            .withSuccessLog("Advanced search available")
            .withFailureLog("Basic search only")
            // Branch based on advanced search availability
            .then(createAdvancedSearchChain())
            .setFailureChain(createBasicSearchChain())
            .build();
        
        // Alternative: Check multiple features
        ActionConfig multiFeatureChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("Checking for filter panel...")
            .withSuccessLog("Filters available")
            .setSuccessCriteria(result -> {
                if (result.isSuccess()) {
                    log.info("Advanced filtering detected - enabling filter options");
                    return true;
                }
                return false;
            })
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for sort options...")
                .withSuccessLog("Sorting available")
                .setSuccessCriteria(result -> {
                    if (result.isSuccess()) {
                        log.info("Sort functionality detected");
                    }
                    return true; // Continue regardless
                })
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for export button...")
                .withSuccessLog("Export feature available")
                .withFailureLog("No export option")
                .setSuccessCriteria(result -> {
                    if (result.isSuccess()) {
                        log.info("Export functionality available for results");
                    }
                    return true;
                })
                .build())
            .build();
        
        log.info("Feature detection chains created - adapt UI based on available features");
    }
    
    /**
     * Demo 3: Multi-path navigation
     * Shows navigation that adapts to UI state
     */
    private void demonstrateMultiPathNavigation() {
        log.info("\n--- Demo 3: Multi-Path Navigation ---");
        
        // Navigation that tries multiple paths to reach destination
        ActionConfig navigationChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("Attempting direct navigation to settings...")
            .withSuccessLog("Settings accessible directly")
            .withFailureLog("Direct path not available - trying menu")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Clicking settings...")
                .withSuccessLog("Settings opened")
                .build())
            // Fallback path through menu
            .setFailureChain(
                new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for menu button...")
                    .withSuccessLog("Menu button found")
                    .then(new ClickOptions.Builder()
                        .withBeforeActionLog("Opening menu...")
                        .withSuccessLog("Menu opened")
                        .setPauseAfterEnd(0.5)
                        .build())
                    .then(new PatternFindOptions.Builder()
                        .withBeforeActionLog("Looking for settings in menu...")
                        .withSuccessLog("Settings found in menu")
                        .withFailureLog("Settings not in menu - trying toolbar")
                        .build())
                    .then(new ClickOptions.Builder()
                        .withBeforeActionLog("Clicking settings from menu...")
                        .withSuccessLog("Settings opened via menu")
                        .build())
                    // Third path through toolbar
                    .setFailureChain(
                        new PatternFindOptions.Builder()
                            .withBeforeActionLog("Looking for toolbar...")
                            .withSuccessLog("Toolbar found")
                            .then(new PatternFindOptions.Builder()
                                .withBeforeActionLog("Looking for settings icon in toolbar...")
                                .withSuccessLog("Settings icon found")
                                .build())
                            .then(new ClickOptions.Builder()
                                .withBeforeActionLog("Clicking settings icon...")
                                .withSuccessLog("Settings opened via toolbar")
                                .build())
                            .build()
                    )
                    .build()
            )
            .build();
        
        log.info("Multi-path navigation created - tries: direct -> menu -> toolbar");
    }
    
    /**
     * Demo 4: Retry with fallback
     * Shows retry logic with degradation
     */
    private void demonstrateRetryWithFallback() {
        log.info("\n--- Demo 4: Retry with Fallback ---");
        
        // Create retry chain with degrading quality
        ActionConfig retryChain = new PatternFindOptions.Builder()
            .setSimilarityScore(0.95)
            .withBeforeActionLog("Looking for high-quality match (95%)...")
            .withSuccessLog("High-quality match found")
            .withFailureLog("No high-quality match - trying medium quality")
            .setRepetition(new RepetitionOptions.Builder()
                .setMaxTimesToRepeatActionSequence(2)
                .setPauseBetweenActionSequences(1)
                .build())
            // Fallback to medium quality
            .setFailureChain(
                new PatternFindOptions.Builder()
                    .setSimilarityScore(0.80)
                    .withBeforeActionLog("Looking for medium-quality match (80%)...")
                    .withSuccessLog("Medium-quality match found")
                    .withFailureLog("No medium match - trying low quality")
                    .setRepetition(new RepetitionOptions.Builder()
                        .setMaxTimesToRepeatActionSequence(2)
                        .build())
                    // Fallback to low quality
                    .setFailureChain(
                        new PatternFindOptions.Builder()
                            .setSimilarityScore(0.65)
                            .withBeforeActionLog("Looking for any match (65%)...")
                            .withSuccessLog("Match found with reduced confidence")
                            .withFailureLog("Target not found at any quality level")
                            .setRepetition(new RepetitionOptions.Builder()
                                .setMaxTimesToRepeatActionSequence(3)
                                .build())
                            .build()
                    )
                    .build()
            )
            .build();
        
        // Alternative: Retry with wait conditions
        ActionConfig waitRetryChain = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for dynamic content...")
            .withFailureLog("Content not ready - waiting...")
            .setSuccessCriteria(result -> {
                if (!result.isSuccess()) {
                    log.info("Content not loaded yet - will retry");
                    return false;
                }
                return true;
            })
            // Wait and retry
            .setFailureChain(
                new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(2.0)  // Wait for content to load
                    .withBeforeActionLog("Waiting for content to load...")
                    .withAfterActionLog("Wait completed, retrying...")
                    .withSuccessLog("Content found after waiting")
                    .withFailureLog("Content still not available")
                    .setRepetition(new RepetitionOptions.Builder()
                        .setMaxTimesToRepeatActionSequence(3)
                        .setPauseBetweenActionSequences(2)
                        .build())
                    .build()
            )
            .build();
        
        log.info("Retry chains created - demonstrate graceful degradation and patience");
    }
    
    /**
     * Helper: Create advanced search chain
     */
    private ActionConfig createAdvancedSearchChain() {
        return new PatternFindOptions.Builder()
            .withBeforeActionLog("Using advanced search interface...")
            .withSuccessLog("Advanced search ready")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Opening filter panel...")
                .withSuccessLog("Filters available")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Setting date range filter...")
                .withSuccessLog("Date filter applied")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Setting category filter...")
                .withSuccessLog("Category filter applied")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Applying advanced filters...")
                .withSuccessLog("Advanced search executed")
                .withAfterActionLog("Found {matchCount} results with filters")
                .build())
            .build();
    }
    
    /**
     * Helper: Create basic search chain
     */
    private ActionConfig createBasicSearchChain() {
        return new TypeOptions.Builder()
            .withBeforeActionLog("Using basic search (advanced not available)...")
            .withSuccessLog("Search query entered")
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for search button...")
                .withSuccessLog("Search button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Executing basic search...")
                .withSuccessLog("Basic search completed")
                .withAfterActionLog("Found {matchCount} results (no filtering available)")
                .build())
            .build();
    }
}