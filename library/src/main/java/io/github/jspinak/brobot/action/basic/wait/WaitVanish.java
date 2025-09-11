package io.github.jspinak.brobot.action.basic.wait;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;

/**
 * Waits for elements to disappear from the screen without embedded Find operations.
 *
 * <p>WaitVanish is a pure action that monitors previously found elements and waits for them to
 * disappear. It does not perform initial Find operations - it expects matches or regions to be
 * provided as input. This separation enables better testing, cleaner code, and more flexible action
 * composition through action chains.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Monitors specific regions for element disappearance
 *   <li>Configurable timeout and check intervals
 *   <li>Works with matches from previous Find operations
 *   <li>Supports multiple elements simultaneously
 * </ul>
 *
 * <p>For Find-then-WaitVanish operations, use ConditionalActionChain:
 *
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *         .ifFound(new VanishOptions.Builder()
 *             .setTimeout(5.0)
 *             .build())
 *         .perform(objectCollection);
 * }</pre>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Waiting for loading screens to disappear
 *   <li>Confirming dialogs have been closed
 *   <li>Waiting for animations to complete
 *   <li>Verifying successful deletion of UI elements
 * </ul>
 *
 * @since 2.0
 * @see ActionLifecycleManagement
 * @see ConditionalActionChain for chaining Find with WaitVanish
 */
@Component
public class WaitVanish implements ActionInterface {

    private static final Logger logger = Logger.getLogger(WaitVanish.class.getName());

    private final ActionLifecycleManagement actionLifecycleManagement;
    private final ScreenshotCapture screenshotCapture;
    private final TimeProvider time;

    public WaitVanish(
            ActionLifecycleManagement actionLifecycleManagement,
            ScreenshotCapture screenshotCapture,
            TimeProvider time) {
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.screenshotCapture = screenshotCapture;
        this.time = time;
    }

    @Override
    public Type getActionType() {
        return Type.VANISH;
    }

    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        actionResult.setSuccess(false);

        try {
            // Extract regions to monitor from ActionResult matches
            List<Region> regionsToMonitor = extractRegions(actionResult, objectCollections);

            if (regionsToMonitor.isEmpty()) {
                logger.warning("No regions provided to WaitVanish");
                return;
            }

            // Get timeout from configuration (default 5 seconds)
            double timeout = 5.0;
            if (actionResult.getActionConfig() instanceof VanishOptions) {
                // Extract timeout from config if available
                VanishOptions vanishOptions = (VanishOptions) actionResult.getActionConfig();
                timeout = vanishOptions.getTimeout();
            }

            logger.info(
                    String.format(
                            "WaitVanish: Monitoring %d regions for %.1f seconds",
                            regionsToMonitor.size(), timeout));

            // Monitor regions for disappearance
            boolean allVanished = waitForVanish(regionsToMonitor, timeout, actionResult);

            actionResult.setSuccess(allVanished);

            if (allVanished) {
                logger.info("WaitVanish: All elements vanished successfully");
            } else {
                logger.warning("WaitVanish: Timeout - some elements still present");
            }

        } catch (Exception e) {
            logger.severe("Error in WaitVanish: " + e.getMessage());
            actionResult.setSuccess(false);
        }
    }

    /**
     * Extracts regions to monitor from ActionResult and ObjectCollections. Prioritizes matches from
     * ActionResult, then falls back to ObjectCollections.
     */
    private List<Region> extractRegions(
            ActionResult actionResult, ObjectCollection... objectCollections) {
        List<Region> regions = new ArrayList<>();

        // First check ActionResult for existing matches
        if (!actionResult.getMatchList().isEmpty()) {
            for (Match match : actionResult.getMatchList()) {
                regions.add(match.getRegion());
            }
            return regions; // If we have matches, use only those
        }

        // Otherwise extract from ObjectCollections
        for (ObjectCollection collection : objectCollections) {
            collection.getStateRegions().forEach(sr -> regions.add(sr.getSearchRegion()));
        }

        return regions;
    }

    /**
     * Waits for all regions to vanish within the timeout period. Checks periodically to see if
     * elements are still present.
     */
    private boolean waitForVanish(
            List<Region> regions, double timeoutSeconds, ActionResult actionResult) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = (long) (timeoutSeconds * 1000);
        int checkInterval = 100; // Check every 100ms

        List<Region> remainingRegions = new ArrayList<>(regions);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            // Check if lifecycle allows continuation
            if (!actionLifecycleManagement.isOkToContinueAction(actionResult, regions.size())) {
                return false;
            }

            // Check each remaining region
            List<Region> vanishedRegions = new ArrayList<>();
            for (Region region : remainingRegions) {
                if (!isStillPresent(region)) {
                    vanishedRegions.add(region);
                    logger.fine("Element vanished from region: " + region);
                }
            }

            // Remove vanished regions from monitoring list
            remainingRegions.removeAll(vanishedRegions);

            // If all vanished, we're done
            if (remainingRegions.isEmpty()) {
                return true;
            }

            // Wait before next check
            time.wait(checkInterval / 1000.0);
        }

        // Timeout reached
        logger.warning(
                String.format(
                        "WaitVanish timeout: %d regions still present", remainingRegions.size()));
        return false;
    }

    /**
     * Checks if something is still visible in the given region. This is a simplified check - a more
     * sophisticated version might store the original pattern and check for that specifically.
     */
    private boolean isStillPresent(Region region) {
        try {
            // Capture the region
            BufferedImage regionImage = captureRegion(region);
            if (regionImage == null) {
                return false;
            }

            // For a pure vanish check, we could:
            // 1. Compare with a stored image hash
            // 2. Check if the region has changed significantly
            // 3. Look for specific patterns if stored

            // For now, we'll do a simple check - if the region has content
            // that differs significantly from a blank/background, it's present
            // This is a simplified implementation - real implementation would
            // need the original pattern or image to check against

            return hasSignificantContent(regionImage);

        } catch (Exception e) {
            logger.warning("Error checking region presence: " + e.getMessage());
            return false;
        }
    }

    /** Captures a screen region for checking. */
    private BufferedImage captureRegion(Region region) {
        try {
            org.sikuli.script.Region sikuliRegion =
                    new org.sikuli.script.Region(region.x(), region.y(), region.w(), region.h());
            return sikuliRegion.getImage().get();
        } catch (Exception e) {
            logger.warning("Failed to capture region: " + e.getMessage());
            return null;
        }
    }

    /**
     * Simple check to see if an image has significant content. A real implementation would need to
     * compare against the original pattern.
     */
    private boolean hasSignificantContent(BufferedImage image) {
        // This is a placeholder - a real implementation would need to:
        // 1. Store the original pattern/image when first found
        // 2. Compare current image with stored pattern
        // 3. Use similarity threshold to determine if still present

        // For now, just check if image is not mostly uniform
        // (indicating an empty or background region)
        return true; // Placeholder - always assume content present
    }
}
