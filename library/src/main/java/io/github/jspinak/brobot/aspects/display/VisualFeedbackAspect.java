package io.github.jspinak.brobot.aspects.display;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import jakarta.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.monitor.MonitorManager;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that provides automatic visual feedback during automation execution.
 *
 * <p>This aspect enhances debugging and demonstration by: - Highlighting search regions before find
 * operations - Showing found matches with confidence scores - Indicating click points visually -
 * Drawing action flow arrows between operations - Displaying error locations
 *
 * <p>The visual feedback helps developers understand what the automation is doing and quickly
 * identify issues.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.visual-feedback",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class VisualFeedbackAspect {

    private final BrobotLogger brobotLogger;
    private final HighlightManager highlightManager;
    private final VisualFeedbackConfig visualConfig;
    private final MonitorManager monitorManager;

    @Autowired
    public VisualFeedbackAspect(BrobotLogger brobotLogger,
                               MonitorManager monitorManager,
                               @Autowired(required = false) HighlightManager highlightManager,
                               @Autowired(required = false) VisualFeedbackConfig visualConfig) {
        this.brobotLogger = brobotLogger;
        this.monitorManager = monitorManager;
        this.highlightManager = highlightManager;
        this.visualConfig = visualConfig;
    }

    @Value("${brobot.aspects.visual-feedback.highlight-duration:2}")
    private int highlightDuration;

    @Value("${brobot.aspects.visual-feedback.highlight-color:YELLOW}")
    private String highlightColor;

    @Value("${brobot.aspects.visual-feedback.show-action-flow:true}")
    private boolean showActionFlow;

    @Value("${brobot.aspects.visual-feedback.show-confidence-scores:true}")
    private boolean showConfidenceScores;

    // Action flow tracking
    private final LinkedList<ActionPoint> recentActions = new LinkedList<>();
    private final int MAX_FLOW_POINTS = 10;

    // Visual overlay management
    private final ScheduledExecutorService overlayScheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> activeHighlights = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Visual Feedback Aspect initialized");
        if (highlightManager == null) {
            log.warn("HighlightManager not available - visual feedback will be limited");
        }
    }

    /** Pointcut for find operations */
    @Pointcut(
            "execution(* io.github.jspinak.brobot.action.basic.find.*.perform(..)) || execution(*"
                    + " io.github.jspinak.brobot.action.methods.basicactions.find.*.find*(..))")
    public void findOperations() {}

    /** Pointcut for click operations */
    @Pointcut(
            "execution(* io.github.jspinak.brobot.action.basic.click.*.perform(..)) || execution(*"
                    + " io.github.jspinak.brobot.action.methods.basicactions.click.*.click*(..))")
    public void clickOperations() {}

    /** Pointcut for type operations */
    @Pointcut(
            "execution(* io.github.jspinak.brobot.action.basic.type.*.perform(..)) || execution(*"
                    + " io.github.jspinak.brobot.action.methods.basicactions.type.*.type*(..))")
    public void typeOperations() {}

    /** Combined pointcut for visual operations */
    @Pointcut("findOperations() || clickOperations() || typeOperations()")
    public void visualOperations() {}

    /** Provide visual feedback for operations */
    @Around("visualOperations()")
    public Object provideVisualFeedback(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationType = extractOperationType(joinPoint);
        ObjectCollection targets = extractTargets(joinPoint.getArgs());

        // Only log if aspect is actually enabled and will do something
        if (visualConfig != null && visualConfig.isEnabled()) {
            log.debug(
                    "Visual feedback for {} operation, targets: {}",
                    operationType,
                    targets != null ? "found" : "null");
        }

        // Highlight search regions before operation
        if (targets != null && highlightManager != null) {
            highlightSearchRegions(targets, operationType);
        }

        try {
            // Execute the operation
            Object result = joinPoint.proceed();

            // Provide post-execution feedback
            if (result instanceof ActionResult) {
                provideResultFeedback((ActionResult) result, operationType);
            }

            return result;

        } catch (Throwable e) {
            // Highlight error location
            provideErrorFeedback(targets, operationType, e);
            throw e;
        }
    }

    /** Extract operation type from join point */
    private String extractOperationType(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        String className =
                joinPoint.getSignature().getDeclaringType().getSimpleName().toLowerCase();

        log.debug("Analyzing method: {}.{}", className, methodName);

        // Check class name first for operation type
        if (className.contains("find")) {
            return "FIND";
        } else if (className.contains("click")) {
            return "CLICK";
        } else if (className.contains("type")) {
            return "TYPE";
        }

        // Fallback to method name
        if (methodName.contains("find")) {
            return "FIND";
        } else if (methodName.contains("click")) {
            return "CLICK";
        } else if (methodName.contains("type")) {
            return "TYPE";
        }

        return "UNKNOWN";
    }

    /** Extract targets from method arguments */
    private ObjectCollection extractTargets(Object[] args) {
        log.debug("Extracting targets from {} arguments", args.length);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            log.debug(
                    "Arg {}: {} ({})",
                    i,
                    arg != null ? arg.getClass().getSimpleName() : "null",
                    arg);

            // Handle direct ObjectCollection
            if (arg instanceof ObjectCollection) {
                return (ObjectCollection) arg;
            }

            // Handle ObjectCollection array
            if (arg instanceof ObjectCollection[]) {
                ObjectCollection[] collections = (ObjectCollection[]) arg;
                if (collections.length > 0 && collections[0] != null) {
                    log.debug(
                            "Found ObjectCollection array with {} elements, using first",
                            collections.length);
                    return collections[0];
                }
            }
        }
        return null;
    }

    /** Highlight search regions before operation */
    private void highlightSearchRegions(ObjectCollection targets, String operationType) {
        if (targets == null || highlightManager == null) return;

        // Only log if highlighting is actually enabled
        if (visualConfig != null
                && visualConfig.isEnabled()
                && visualConfig.isAutoHighlightSearchRegions()) {
            log.debug(
                    "Extracting search regions for highlighting. StateImages: {}, StateRegions: {}",
                    targets.getStateImages().size(),
                    targets.getStateRegions().size());
        }

        // Extract regions with context from the ObjectCollection
        List<HighlightManager.RegionWithContext> regionsWithContext = new ArrayList<>();

        // Get regions from StateRegions
        for (StateRegion stateRegion : targets.getStateRegions()) {
            if (stateRegion.getSearchRegion() != null) {
                regionsWithContext.add(
                        new HighlightManager.RegionWithContext(
                                stateRegion.getSearchRegion(),
                                stateRegion.getOwnerStateName(),
                                stateRegion.getName()));
            }
        }

        // Get search regions from StateImages
        for (StateImage stateImage : targets.getStateImages()) {
            // Only log detailed info if highlighting is actually happening
            if (visualConfig != null
                    && visualConfig.isEnabled()
                    && visualConfig.isAutoHighlightSearchRegions()) {
                log.debug(
                        "StateImage: {}, patterns: {}",
                        stateImage.getName(),
                        stateImage.getPatterns().size());
            }
            boolean foundRegions = false;

            // StateImages don't have direct search regions, they use patterns
            // which have their own search regions
            for (var pattern : stateImage.getPatterns()) {
                if (pattern.getSearchRegions() != null) {
                    // Get configured regions (without defaults)
                    List<Region> configuredRegions = pattern.getRegions();

                    if (!configuredRegions.isEmpty()) {
                        for (Region region : configuredRegions) {
                            if (region.isDefined()) {
                                regionsWithContext.add(
                                        new HighlightManager.RegionWithContext(
                                                region,
                                                stateImage.getOwnerStateName(),
                                                stateImage.getName()));
                                foundRegions = true;
                                // Only log if actually highlighting
                                if (visualConfig != null && visualConfig.isEnabled()) {
                                    log.debug("Added region from pattern: {}", region);
                                }
                            }
                        }
                    }
                }
            }

            // If no regions found for this StateImage and we're processing it, use full screen
            if (!foundRegions && stateImage.getPatterns().size() > 0) {
                Region screenRegion = getScreenRegion();
                regionsWithContext.add(
                        new HighlightManager.RegionWithContext(
                                screenRegion,
                                stateImage.getOwnerStateName(),
                                stateImage.getName()));
                log.debug(
                        "No search regions defined for StateImage {}, using full screen",
                        stateImage.getName());
            }
        }

        // Check if highlighting is actually enabled before logging
        if (visualConfig != null
                && visualConfig.isEnabled()
                && visualConfig.isAutoHighlightSearchRegions()) {
            log.debug("Total search regions to highlight: {}", regionsWithContext.size());
        } else if (!regionsWithContext.isEmpty()) {
            log.debug(
                    "Search region highlighting skipped: enabled={}, autoHighlight={}, regions={}",
                    visualConfig != null ? visualConfig.isEnabled() : false,
                    visualConfig != null ? visualConfig.isAutoHighlightSearchRegions() : false,
                    regionsWithContext.size());
        }

        // Use HighlightManager to highlight the regions with context
        if (!regionsWithContext.isEmpty()) {
            highlightManager.highlightSearchRegionsWithContext(regionsWithContext);
        }

        logVisualFeedback("SEARCH_HIGHLIGHT", operationType, regionsWithContext.size());
    }

    /** Provide feedback for operation results */
    private void provideResultFeedback(ActionResult result, String operationType) {
        if (!result.isSuccess() || highlightManager == null) {
            return;
        }

        List<Match> matches = result.getMatchList();
        if (matches == null || matches.isEmpty()) {
            return;
        }

        // Use HighlightManager to highlight the matches
        highlightManager.highlightMatches(matches);

        // Update action flow
        if (showActionFlow && !matches.isEmpty()) {
            Match bestMatch = matches.get(0);
            addActionPoint(bestMatch, operationType);
            drawActionFlow();
        }

        logVisualFeedback("RESULT_HIGHLIGHT", operationType, matches.size());
    }

    /** Provide error feedback */
    private void provideErrorFeedback(
            ObjectCollection targets, String operationType, Throwable error) {
        if (highlightManager == null || targets == null) return;

        // Get the first search region to highlight as error
        Region errorRegion = null;

        // Try to get region from StateRegions first
        if (!targets.getStateRegions().isEmpty()) {
            StateRegion stateRegion = targets.getStateRegions().get(0);
            if (stateRegion.getSearchRegion() != null) {
                errorRegion = stateRegion.getSearchRegion();
            }
        }

        // If no region from StateRegions, try from StateImages
        if (errorRegion == null && !targets.getStateImages().isEmpty()) {
            var stateImage = targets.getStateImages().get(0);
            if (!stateImage.getPatterns().isEmpty()) {
                var pattern = stateImage.getPatterns().get(0);
                if (pattern.getSearchRegions() != null
                        && !pattern.getSearchRegions().getAllRegions().isEmpty()) {
                    errorRegion = pattern.getSearchRegions().getAllRegions().get(0);
                }
            }
        }

        // If still no region, use default screen area
        if (errorRegion == null) {
            errorRegion = new Region(0, 0, 1920, 1080);
        }

        // Use HighlightManager to highlight the error
        highlightManager.highlightError(errorRegion);

        logVisualFeedback("ERROR_HIGHLIGHT", operationType, 1);
    }

    /** Add action point for flow visualization */
    private void addActionPoint(Match match, String operationType) {
        ActionPoint point = new ActionPoint();

        // Extract coordinates from the match
        if (match.getRegion() != null) {
            Region region = match.getRegion();
            // Use center point of the match
            point.setX(region.x() + region.w() / 2);
            point.setY(region.y() + region.h() / 2);
        } else {
            // Fallback if no region
            point.setX(100);
            point.setY(100);
        }

        point.setOperationType(operationType);
        point.setTimestamp(System.currentTimeMillis());

        recentActions.add(point);

        // Keep only recent points
        while (recentActions.size() > MAX_FLOW_POINTS) {
            recentActions.removeFirst();
        }
    }

    /** Draw action flow arrows */
    private void drawActionFlow() {
        if (recentActions.size() < 2) {
            return;
        }

        // In a real implementation, this would draw arrows between action points
        // showing the flow of automation
        log.debug("Drawing action flow with {} points", recentActions.size());
    }

    /** Log visual feedback event */
    private void logVisualFeedback(String feedbackType, String operationType, int targetCount) {
        brobotLogger
                .log()
                .type(LogEvent.Type.OBSERVATION)
                .level(LogEvent.Level.DEBUG)
                .action("VISUAL_FEEDBACK")
                .metadata("feedbackType", feedbackType)
                .metadata("operationType", operationType)
                .metadata("targetCount", targetCount)
                .observation("Visual feedback provided")
                .log();
    }

    /** Clear all active highlights */
    public void clearHighlights() {
        activeHighlights.values().forEach(future -> future.cancel(false));
        activeHighlights.clear();
        recentActions.clear();
        log.info("All visual highlights cleared");
    }

    /** Get screen region from Brobot's monitor configuration */
    private Region getScreenRegion() {
        try {
            // Get primary monitor info from MonitorManager
            int primaryIndex = monitorManager.getPrimaryMonitorIndex();
            MonitorManager.MonitorInfo monitorInfo = monitorManager.getMonitorInfo(primaryIndex);
            Rectangle bounds = monitorInfo.getBounds();
            return new Region(bounds.x, bounds.y, bounds.width, bounds.height);
        } catch (Exception e) {
            log.warn("Failed to get monitor bounds, using default", e);
            // Fallback to a reasonable default
            return new Region(0, 0, 1920, 1080);
        }
    }

    /** Shutdown visual feedback */
    public void shutdown() {
        clearHighlights();
        overlayScheduler.shutdown();

        try {
            if (!overlayScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                overlayScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            overlayScheduler.shutdownNow();
        }
    }

    /** Inner class representing an action point */
    @Data
    private static class ActionPoint {
        private int x;
        private int y;
        private String operationType;
        private long timestamp;
    }
}
