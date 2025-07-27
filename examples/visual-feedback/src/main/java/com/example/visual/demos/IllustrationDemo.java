package com.example.visual.demos;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates basic illustration and visual feedback capabilities.
 * Shows how to capture screenshots, highlight matches, and generate
 * step-by-step visual documentation of automation flows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IllustrationDemo {
    
    private final Action action;
    private final IllustrationController illustrationController;
    
    public void runDemos() {
        log.info("\n=== Illustration Demo ===");
        
        // Demo 1: Basic screenshot capture
        demonstrateBasicIllustration();
        
        // Demo 2: Match highlighting
        demonstrateMatchHighlighting();
        
        // Demo 3: Step-by-step workflow
        demonstrateWorkflowIllustration();
        
        // Demo 4: Comparison screenshots
        demonstrateComparisonScreenshots();
    }
    
    /**
     * Demo 1: Basic screenshot capture with logging
     */
    private void demonstrateBasicIllustration() {
        log.info("\n--- Demo 1: Basic Screenshot Capture ---");
        
        // Create action with illustration enabled
        PatternFindOptions findWithScreenshot = new PatternFindOptions.Builder()
            .withIllustration(true)
            .withVisualizationPath("basic-illustration")
            .withBeforeActionLog("Capturing initial screenshot...")
            .withSuccessLog("Element found - screenshot with highlight saved")
            .withFailureLog("Element not found - screenshot of search area saved")
            .setIllustrationFilename("search-element")
            .then(new ClickOptions.Builder()
                .withIllustration(true)
                .withBeforeActionLog("Capturing click action...")
                .withSuccessLog("Click executed - before/after screenshots saved")
                .setIllustrationFilename("click-action")
                .build())
            .build();
        
        // Create mock UI element for demonstration
        StateImage searchButton = new StateImage.Builder()
            .setName("search-button")
            .addPattern("images/search-button.png")
            .build();
        
        log.info("Basic illustration configured:");
        log.info("- Screenshots will be saved to: build/illustrations/basic-illustration/");
        log.info("- Match highlights will show confidence scores");
        log.info("- Before/after states will be captured for each action");
        
        // In a real scenario:
        // ActionResult result = action.perform(findWithScreenshot, searchButton);
        log.info("✓ Basic illustration demo configured");
    }
    
    /**
     * Demo 2: Match highlighting with confidence visualization
     */
    private void demonstrateMatchHighlighting() {
        log.info("\n--- Demo 2: Match Highlighting ---");
        
        // Create action with detailed match visualization
        PatternFindOptions highlightedSearch = new PatternFindOptions.Builder()
            .withIllustration(true)
            .withVisualizationPath("match-highlighting")
            .setSimilarityScore(0.85)
            .withBeforeActionLog("Searching with match highlighting...")
            .withSuccessLog("Found match - confidence: {confidence}%, position: ({x}, {y})")
            .withFailureLog("No match found above {similarity}% threshold")
            .setHighlightMatches(true)
            .setShowConfidenceScores(true)
            .setShowCoordinates(true)
            .setIllustrationFilename("highlighted-match")
            .build();
        
        // Multiple target search for comparison
        PatternFindOptions multiTargetSearch = new PatternFindOptions.Builder()
            .withIllustration(true)
            .withVisualizationPath("multi-target")
            .withBeforeActionLog("Searching for multiple targets...")
            .withSuccessLog("Found {matchCount} matches - all highlighted")
            .setFindAllMatches(true)
            .setHighlightAllMatches(true)
            .setMinimumMatches(1)
            .setMaximumMatches(5)
            .setIllustrationFilename("all-matches")
            .build();
        
        // Create test elements
        StateImage menuIcon = new StateImage.Builder()
            .setName("menu-icon")
            .addPattern("images/menu-icon.png")
            .build();
            
        StateImage buttonElements = new StateImage.Builder()
            .setName("button-elements")
            .addPattern("images/button.png")
            .addPattern("images/button-alt.png")
            .build();
        
        log.info("Match highlighting configured:");
        log.info("- Single target: Highlights best match with confidence score");
        log.info("- Multiple targets: Highlights all matches above threshold");
        log.info("- Coordinate display: Shows exact pixel positions");
        log.info("- Color coding: Green=high confidence, Yellow=medium, Red=low");
        
        log.info("✓ Match highlighting demo configured");
    }
    
    /**
     * Demo 3: Step-by-step workflow illustration
     */
    private void demonstrateWorkflowIllustration() {
        log.info("\n--- Demo 3: Workflow Illustration ---");
        
        // Create comprehensive workflow with step numbering
        ActionConfig workflowWithSteps = new PatternFindOptions.Builder()
            .withIllustration(true)
            .withVisualizationPath("workflow-steps")
            .withBeforeActionLog("Step 1: Opening search interface...")
            .withSuccessLog("Step 1 complete: Search interface ready")
            .setStepNumber(1)
            .setIllustrationFilename("step-01-search-interface")
            .then(new ClickOptions.Builder()
                .withIllustration(true)
                .withBeforeActionLog("Step 2: Activating search box...")
                .withSuccessLog("Step 2 complete: Search box focused")
                .setStepNumber(2)
                .setIllustrationFilename("step-02-search-focused")
                .build())
            .then(new TypeOptions.Builder()
                .withIllustration(true)
                .withBeforeActionLog("Step 3: Entering search query...")
                .withSuccessLog("Step 3 complete: Query '{text}' entered")
                .setStepNumber(3)
                .setTypeDelay(0.1) // Slower typing for better visualization
                .setIllustrationFilename("step-03-query-entered")
                .build())
            .then(new PatternFindOptions.Builder()
                .withIllustration(true)
                .withBeforeActionLog("Step 4: Locating search button...")
                .withSuccessLog("Step 4 complete: Search button found")
                .setStepNumber(4)
                .setIllustrationFilename("step-04-button-found")
                .build())
            .then(new ClickOptions.Builder()
                .withIllustration(true)
                .withBeforeActionLog("Step 5: Executing search...")
                .withSuccessLog("Step 5 complete: Search executed")
                .withAfterActionLog("Workflow completed: 5 steps in {duration}ms")
                .setStepNumber(5)
                .setIllustrationFilename("step-05-search-executed")
                .setPauseAfterEnd(1.0) // Pause to capture final state
                .build())
            .build();
        
        // Timeline generation configuration
        log.info("Workflow illustration features:");
        log.info("- Sequential step numbering in screenshots");
        log.info("- Timeline HTML report generation");
        log.info("- Before/after state capture for each step");
        log.info("- Automatic step summary creation");
        log.info("- Interactive navigation between steps");
        
        log.info("✓ Workflow illustration demo configured");
    }
    
    /**
     * Demo 4: Comparison screenshots for validation
     */
    private void demonstrateComparisonScreenshots() {
        log.info("\n--- Demo 4: Comparison Screenshots ---");
        
        // Create action with before/after comparison
        ActionConfig comparisonAction = new PatternFindOptions.Builder()
            .withIllustration(true)
            .withVisualizationPath("comparisons")
            .withBeforeActionLog("Capturing initial state for comparison...")
            .withSuccessLog("Action completed - comparison available")
            .setCaptureBeforeState(true)
            .setCaptureAfterState(true)
            .setIllustrationFilename("state-comparison")
            .then(new ClickOptions.Builder()
                .withIllustration(true)
                .withBeforeActionLog("Modifying UI state...")
                .withSuccessLog("UI state changed - comparison captured")
                .setCaptureBeforeState(true)
                .setCaptureAfterState(true)
                .setGenerateComparison(true)
                .setIllustrationFilename("click-comparison")
                .build())
            .build();
        
        // Validation action with expected vs actual comparison
        PatternFindOptions validationAction = new PatternFindOptions.Builder()
            .withIllustration(true)
            .withVisualizationPath("validation")
            .withBeforeActionLog("Validating expected UI state...")
            .withSuccessLog("Validation passed - UI matches expected state")
            .withFailureLog("Validation failed - UI differs from expected")
            .setExpectedStateImage("images/expected-result.png")
            .setGenerateValidationComparison(true)
            .setValidationThreshold(0.90)
            .setIllustrationFilename("validation-result")
            .build();
        
        log.info("Comparison screenshot features:");
        log.info("- Before/after state capture");
        log.info("- Side-by-side comparison generation");
        log.info("- Difference highlighting");
        log.info("- Expected vs actual validation");
        log.info("- Threshold-based pass/fail visualization");
        
        // Mock illustration controller usage
        log.info("Example comparison outputs:");
        log.info("- state-comparison-before.png");
        log.info("- state-comparison-after.png");
        log.info("- state-comparison-diff.png");
        log.info("- validation-expected-vs-actual.png");
        
        log.info("✓ Comparison screenshots demo configured");
    }
    
    /**
     * Helper method to demonstrate manual illustration control
     */
    private void demonstrateManualIllustration() {
        log.info("\n--- Manual Illustration Control ---");
        
        // Manual screenshot capture
        illustrationController.captureScreenshot("manual-capture", "Custom screenshot description");
        
        // Custom annotation
        illustrationController.addAnnotation("Click here for search", 450, 120);
        illustrationController.addAnnotation("Results will appear below", 400, 300);
        
        // Save annotated screenshot
        illustrationController.saveAnnotatedScreenshot("annotated-instructions");
        
        log.info("Manual illustration capabilities:");
        log.info("- Programmatic screenshot capture");
        log.info("- Custom annotations and callouts");
        log.info("- Manual step documentation");
        log.info("- Custom illustration organization");
        
        log.info("✓ Manual illustration demo configured");
    }
}