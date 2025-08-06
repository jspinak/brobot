package com.example.conditionalchains.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.ConditionalActionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates custom logic patterns with ConditionalActionChain.
 * Shows how to integrate complex conditional behaviors.
 */
@Component
public class CustomLogicExample {
    private static final Logger log = LoggerFactory.getLogger(CustomLogicExample.class);

    @Autowired
    private ConditionalActionWrapper conditionalWrapper;
    
    @Autowired
    private Action action;
    
    private StateImage searchField;
    private StateImage searchButton;
    private StateImage resultItem;
    private StateImage loadingSpinner;
    private StateImage noResultsMessage;
    
    public CustomLogicExample() {
        initializeObjects();
    }
    
    private void initializeObjects() {
        // In a real application, these would come from your State classes
        searchField = new StateImage();
        searchField.setName("SearchField");
        searchField.addPatterns("search-field.png");
        
        searchButton = new StateImage();
        searchButton.setName("SearchButton");
        searchButton.addPatterns("search-button.png");
        
        resultItem = new StateImage();
        resultItem.setName("ResultItem");
        resultItem.addPatterns("result-item.png");
        
        loadingSpinner = new StateImage();
        loadingSpinner.setName("LoadingSpinner");
        loadingSpinner.addPatterns("loading-spinner.png");
        
        noResultsMessage = new StateImage();
        noResultsMessage.setName("NoResultsMessage");
        noResultsMessage.addPatterns("no-results-message.png");
    }
    
    /**
     * Search with retry logic and timing measurements
     */
    public void searchWithRetryAndTiming() {
        log.info("=== Search with Retry and Timing Example ===");
        
        final AtomicInteger retryCount = new AtomicInteger(0);
        final int maxRetries = 3;
        final Map<String, Long> timings = new HashMap<>();
        
        performSearchWithRetry("test query", retryCount, maxRetries, timings);
        
        // Report timings
        log.info("Operation timings:");
        timings.forEach((operation, duration) -> 
            log.info("  {} took {} ms", operation, duration)
        );
    }
    
    private void performSearchWithRetry(String query, AtomicInteger retryCount, 
                                       int maxRetries, Map<String, Long> timings) {
        long startTime = System.currentTimeMillis();
        
        // Click search field and type query
        ActionResult clickResult = conditionalWrapper.findAndClick(searchField);
        
        if (clickResult.isSuccess()) {
            conditionalWrapper.findAndType(searchField, query);
            
            // Click search button
            long searchStart = System.currentTimeMillis();
            ActionResult searchResult = conditionalWrapper.findAndClick(searchButton);
            
            if (searchResult.isSuccess()) {
                timings.put("search_initiation", System.currentTimeMillis() - searchStart);
                
                // Wait for results with loading check
                waitForResultsWithLoadingCheck(timings);
            } else {
                log.warn("Search button click failed");
                
                if (retryCount.incrementAndGet() < maxRetries) {
                    log.info("Retrying search (attempt {}/{})", retryCount.get(), maxRetries);
                    performSearchWithRetry(query, retryCount, maxRetries, timings);
                } else {
                    log.error("Max retries reached. Search failed.");
                }
            }
        } else {
            log.error("Failed to click search field");
        }
        
        timings.put("total_search_time", System.currentTimeMillis() - startTime);
    }
    
    private void waitForResultsWithLoadingCheck(Map<String, Long> timings) {
        long loadStart = System.currentTimeMillis();
        
        // Check for loading spinner
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ObjectCollection spinnerCollection = new ObjectCollection.Builder()
            .withImages(loadingSpinner)
            .build();
        
        ActionResult spinnerResult = action.perform(findOptions, spinnerCollection);
        
        if (spinnerResult.isSuccess()) {
            log.info("Loading spinner detected, waiting for results...");
            
            // Wait for spinner to disappear (with timeout)
            waitForSpinnerToDisappear(timings, loadStart);
        } else {
            log.info("No loading spinner, checking for immediate results...");
            checkSearchResults(timings);
        }
    }
    
    private void waitForSpinnerToDisappear(Map<String, Long> timings, long loadStart) {
        int waitAttempts = 0;
        int maxWaitAttempts = 10;
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ObjectCollection spinnerCollection = new ObjectCollection.Builder()
            .withImages(loadingSpinner)
            .build();
        
        while (waitAttempts < maxWaitAttempts) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            
            // Check if spinner is still visible
            ActionResult spinnerCheck = action.perform(findOptions, spinnerCollection);
            
            if (!spinnerCheck.isSuccess()) {
                timings.put("loading_time", System.currentTimeMillis() - loadStart);
                log.info("Loading complete after {} ms", timings.get("loading_time"));
                checkSearchResults(timings);
                return;
            }
            
            waitAttempts++;
        }
        
        log.warn("Loading timeout - spinner still visible after {} attempts", maxWaitAttempts);
    }
    
    private void checkSearchResults(Map<String, Long> timings) {
        long resultsCheckStart = System.currentTimeMillis();
        
        // Use ConditionalActionChain for complex result checking
        ConditionalActionChain resultsChain = ConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFoundLog("✓ Search results found!")
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifFoundLog("No results message displayed - search returned empty")
            .ifNotFoundLog("Neither results nor no-results message found");
        
        ObjectCollection searchCollection = new ObjectCollection.Builder()
            .withImages(resultItem, noResultsMessage)
            .build();
        
        ActionResult result = resultsChain.perform(action, searchCollection);
        
        if (result.isSuccess() && result.getMatchList().size() > 0) {
            // Count results
            int resultCount = result.getMatchList().size();
            log.info("Found {} results", resultCount);
            
            // Process first result
            processFirstResult();
        }
        
        timings.put("results_check_time", System.currentTimeMillis() - resultsCheckStart);
    }
    
    private void processFirstResult() {
        log.info("Processing first search result...");
        // Processing logic here
    }
    
    /**
     * Dynamic action selection based on application state
     */
    public void dynamicActionSelection() {
        log.info("=== Dynamic Action Selection Example ===");
        
        // Determine current time-based action
        LocalDateTime now = LocalDateTime.now();
        boolean isBusinessHours = now.getHour() >= 9 && now.getHour() < 17;
        
        StateImage normalModeButton = new StateImage();
        normalModeButton.setName("NormalMode");
        normalModeButton.addPatterns("normal-mode.png");
        
        StateImage afterHoursButton = new StateImage();
        afterHoursButton.setName("AfterHoursMode");
        afterHoursButton.addPatterns("after-hours-mode.png");
        
        // Choose action based on time
        StateImage targetButton = isBusinessHours ? normalModeButton : afterHoursButton;
        String modeName = isBusinessHours ? "Normal" : "After-Hours";
        
        log.info("Current time: {}, selecting {} mode", now.getHour(), modeName);
        
        ActionResult modeResult = conditionalWrapper.findAndClick(targetButton);
        
        if (modeResult.isSuccess()) {
            log.info("{} mode activated successfully", modeName);
            
            // Perform mode-specific actions
            if (isBusinessHours) {
                performNormalModeActions();
            } else {
                performAfterHoursActions();
            }
        } else {
            log.warn("{} mode button not found", modeName);
        }
    }
    
    private void performNormalModeActions() {
        log.info("Executing normal business hours workflow...");
        // Normal mode logic
    }
    
    private void performAfterHoursActions() {
        log.info("Executing after-hours workflow...");
        // After-hours logic
    }
    
    /**
     * Pattern matching with custom validators
     */
    public void patternMatchingExample() {
        log.info("=== Pattern Matching Example ===");
        
        StateImage priceLabel = new StateImage();
        priceLabel.setName("PriceLabel");
        priceLabel.addPatterns("price-label.png");
        
        // Find all price labels
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        
        ObjectCollection priceCollection = new ObjectCollection.Builder()
            .withImages(priceLabel)
            .build();
        
        ActionResult findResult = action.perform(findAllOptions, priceCollection);
        
        if (findResult.isSuccess()) {
            log.info("Found {} price labels", findResult.getMatchList().size());
            
            // Custom validation of found matches
            findResult.getMatchList().stream()
                .filter(match -> isValidPriceFormat(match))
                .findFirst()
                .ifPresent(validMatch -> {
                    log.info("Valid price found at score: {}", validMatch.getScore());
                    processPriceMatch(validMatch);
                });
        } else {
            log.warn("No price labels found on page");
        }
    }
    
    private boolean isValidPriceFormat(Match match) {
        // In real implementation, might OCR the text and validate format
        // For demo, using score validation
        return match.getScore() > 0.9;
    }
    
    private void processPriceMatch(Match match) {
        log.info("Processing price with score: {}", match.getScore());
        // Price processing logic
    }
    
    /**
     * Complex conditional chain with multiple branches
     */
    public void complexBranchingExample() {
        log.info("=== Complex Branching Example ===");
        
        // Build a complex chain with multiple decision points
        ConditionalActionChain complexChain = ConditionalActionChain
            // First, try to find search field
            .find(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Search field found and clicked")
            // If not found, try alternative UI
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifFound(new ClickOptions.Builder().build())
            .ifFoundLog("Alternative search method found")
            // Final fallback
            .ifNotFoundLog("No search interface found!");
        
        ObjectCollection searchTargets = new ObjectCollection.Builder()
            .withImages(searchField, searchButton)
            .build();
        
        complexChain.perform(action, searchTargets);
    }
    
    /**
     * Demonstrates all custom logic examples
     */
    public void runAllExamples() {
        searchWithRetryAndTiming();
        log.info("");
        
        dynamicActionSelection();
        log.info("");
        
        patternMatchingExample();
        log.info("");
        
        complexBranchingExample();
    }
}