package io.github.jspinak.brobot.tools.history;

import static io.github.jspinak.brobot.action.ActionType.*;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Controls when and how action results are illustrated to prevent redundancy.
 *
 * <p>This component acts as a gatekeeper for the illustration system, implementing intelligent
 * filtering to avoid creating duplicate or unnecessary illustrations. It tracks action history to
 * detect repetitions and applies configurable permission rules to determine which actions should be
 * visualized.
 *
 * <p>Key filtering criteria:
 *
 * <ul>
 *   <li>Action permissions: Per-action type enablement via {@link BrobotProperties}
 *   <li>Illustration directives: Explicit YES/NO overrides in {@link ActionConfig}
 *   <li>Repetition detection: Suppresses duplicate illustrations for same action/objects
 *   <li>Global history setting: Master switch for all illustrations
 * </ul>
 *
 * <p>Repetition detection logic:
 *
 * <ul>
 *   <li>Actions are considered repeated if they have the same type, find method, and object
 *       collections
 *   <li>Repeated FIND operations are particularly common and filtered by default
 *   <li>The {@code drawRepeatedActions} setting can override this filtering
 * </ul>
 *
 * <p>Supported action types (configurable via BrobotProperties):
 *
 * <ul>
 *   <li>FIND - Object detection operations
 *   <li>CLICK - Mouse click actions
 *   <li>DRAG - Drag operations
 *   <li>MOVE - Cursor movements
 *   <li>HIGHLIGHT - Visual highlighting
 *   <li>CLASSIFY - Image classification
 *   <li>DEFINE - Region definition
 * </ul>
 *
 * <p>State management: Maintains last action details to enable repetition detection across
 * consecutive operations. This state is instance-level and not thread-safe.
 *
 * @see VisualizationOrchestrator
 * @see BrobotProperties
 * @see ActionConfig.Illustrate
 */
@Slf4j
@Component
@Getter
public class IllustrationController {

    private final BrobotProperties brobotProperties;
    private final ImageFileUtilities imageUtils;
    private final ActionVisualizer draw;
    private final VisualizationOrchestrator illustrationManager;
    private final LoggingVerbosityConfig loggingConfig;

    private List<ObjectCollection> lastCollections = new ArrayList<>();
    private ActionType lastAction = ActionType.TYPE;
    private PatternFindOptions.Strategy lastFind = PatternFindOptions.Strategy.ALL;

    private Map<ActionType, Boolean> actionPermissions = new HashMap<>();

    @Autowired
    public IllustrationController(
            BrobotProperties brobotProperties,
            ImageFileUtilities imageUtils,
            ActionVisualizer draw,
            VisualizationOrchestrator illustrationManager,
            @Autowired(required = false) LoggingVerbosityConfig loggingConfig) {
        this.brobotProperties = brobotProperties;
        this.imageUtils = imageUtils;
        this.draw = draw;
        this.illustrationManager = illustrationManager;
        this.loggingConfig = loggingConfig;
    }

    /**
     * Initializes action-specific illustration permissions from global settings.
     *
     * <p>Maps each supported action type to its corresponding BrobotProperties flag. This allows
     * fine-grained control over which actions generate illustrations, useful for reducing visual
     * noise or focusing on specific operations.
     *
     * <p>Called lazily on first illustration request to ensure settings are loaded.
     */
    private void setActionPermissions() {
        actionPermissions.put(FIND, brobotProperties.getIllustration().isDrawFind());
        actionPermissions.put(CLICK, brobotProperties.getIllustration().isDrawClick());
        actionPermissions.put(DRAG, brobotProperties.getIllustration().isDrawDrag());
        actionPermissions.put(MOVE, brobotProperties.getIllustration().isDrawMove());
        actionPermissions.put(HIGHLIGHT, brobotProperties.getIllustration().isDrawHighlight());
        actionPermissions.put(CLASSIFY, brobotProperties.getIllustration().isDrawClassify());
        actionPermissions.put(DEFINE, brobotProperties.getIllustration().isDrawDefine());
    }

    /**
     * Determines whether an action should be illustrated based on multiple criteria.
     *
     * <p>Evaluates illustration eligibility through a hierarchy of checks:
     *
     * <ol>
     *   <li>Global history saving must be enabled or explicit YES directive
     *   <li>Explicit NO directive always prevents illustration
     *   <li>Action type must be supported and enabled in settings
     *   <li>Repetition filtering applied unless overridden
     * </ol>
     *
     * <p>Repetition detection is particularly important for FIND operations which often repeat
     * during element detection loops. The same action with the same objects is considered a
     * repetition and filtered by default.
     *
     * <p>Side effects: Updates action permissions on first call.
     *
     * @param actionConfig configuration including action type and illustration directive
     * @param objectCollections target objects for repetition comparison
     * @return true if illustration should proceed, false if filtered out
     */
    public boolean okToIllustrate(
            ActionConfig actionConfig, ObjectCollection... objectCollections) {
        setActionPermissions();

        // Verbose logging
        if (isVerbose()) {
            log.debug("[ILLUSTRATION] Checking if ok to illustrate:");
            log.debug(
                    "  brobotProperties.getScreenshot().isSaveHistory(): {}",
                    brobotProperties.getScreenshot().isSaveHistory());
            log.debug(
                    "  brobotProperties.getScreenshot().getHistoryPath(): {}",
                    brobotProperties.getScreenshot().getHistoryPath());
            log.debug("  ActionConfig.illustrate: {}", actionConfig.getIllustrate());
            log.debug("  Action type: determined at runtime");
        }

        if (!brobotProperties.getScreenshot().isSaveHistory()
                && actionConfig.getIllustrate() != ActionConfig.Illustrate.YES) {
            if (isVerbose()) {
                log.debug("  Result: NO - saveHistory is false and illustrate is not YES");
            }
            return false;
        }
        if (actionConfig.getIllustrate() == ActionConfig.Illustrate.NO) {
            if (isVerbose()) {
                log.debug("  Result: NO - illustrate is explicitly NO");
            }
            return false;
        }
        // Get action type from config
        ActionType action = getActionType(actionConfig);
        if (!actionPermissions.containsKey(action)) {
            if (isVerbose()) {
                log.debug("  Result: NO - action {} not available to illustrate", action);
            }
            return false;
        }
        if (!actionPermissions.get(action)) {
            if (isVerbose()) {
                log.debug("  Result: NO - action {} not permitted in settings", action);
                log.debug(
                        "  brobotProperties.getIllustration().isDrawFind(): {}",
                        brobotProperties.getIllustration().isDrawFind());
                log.debug(
                        "  brobotProperties.getIllustration().isDrawClick(): {}",
                        brobotProperties.getIllustration().isDrawClick());
            }
            return false;
        }
        if (brobotProperties.getIllustration().isDrawRepeatedActions()) {
            if (isVerbose()) {
                log.debug("  Result: YES - drawRepeatedActions is true");
            }
            return true;
        }
        // otherwise, if the action is a repeat (same Action, same ObjectCollections),
        // false
        boolean isRepeat =
                lastFind == getFindStrategy(actionConfig)
                        && lastAction == action
                        && sameCollections(Arrays.asList(objectCollections));

        if (isVerbose()) {
            log.debug("  Is repeat action: {}", isRepeat);
            log.debug("  Result: {} - illustration", !isRepeat ? "YES" : "NO");
        }

        return !isRepeat;
    }

    /**
     * Compares object collections to detect repeated actions.
     *
     * <p>Performs deep equality check on collections to determine if the current action targets the
     * same objects as the previous action. This comparison is critical for filtering redundant
     * illustrations.
     *
     * <p>The comparison ensures element-wise equality between collections, comparing corresponding
     * elements at each position.
     *
     * @param objectCollections current collections to compare against stored last collections
     * @return true if collections match (indicating a repeated action)
     */
    private boolean sameCollections(List<ObjectCollection> objectCollections) {
        if (objectCollections.size() != lastCollections.size()) return false;
        for (int i = 0; i < objectCollections.size(); i++) {
            if (!objectCollections.get(i).equals(lastCollections.get(i))) return false;
        }
        return true;
    }

    /**
     * Conditionally creates illustrations based on permission and repetition checks.
     *
     * <p>This method serves as the main entry point for illustration requests. It applies all
     * filtering logic before delegating to the IllustrationManager for actual drawing. Successful
     * illustrations update the repetition tracking state for future filtering decisions.
     *
     * <p>State updates on success:
     *
     * <ul>
     *   <li>Last action type is recorded
     *   <li>Find type is recorded for FIND actions
     *   <li>Object collections are stored for comparison
     * </ul>
     *
     * <p>Side effects: Modifies instance state for repetition tracking.
     *
     * @param matches action results to illustrate
     * @param searchRegions regions where searches occurred
     * @param actionConfig action configuration and illustration settings
     * @param objectCollections objects involved in the action
     * @return true if illustration was created, false if filtered out
     */
    public boolean illustrateWhenAllowed(
            ActionResult matches,
            List<Region> searchRegions,
            ActionConfig actionConfig,
            ObjectCollection... objectCollections) {
        ActionType actionType = getActionType(actionConfig);
        log.debug("[ILLUSTRATION] illustrateWhenAllowed called for action: {}", actionType);
        if (isVerbose()) {
            log.debug("[ILLUSTRATION] Verbose mode - additional details will be shown");
        }

        if (!okToIllustrate(actionConfig, objectCollections)) {
            log.debug("[ILLUSTRATION] Illustration not allowed, skipping");
            return false;
        }

        lastAction = getActionType(actionConfig);
        if (lastAction == FIND) lastFind = getFindStrategy(actionConfig);
        lastCollections = Arrays.asList(objectCollections);

        if (isVerbose()) {
            log.debug("[ILLUSTRATION] Creating illustration for action: {}", actionType);
            log.debug(
                    "[ILLUSTRATION] History path: {}",
                    brobotProperties.getScreenshot().getHistoryPath());
            log.debug("[ILLUSTRATION] Calling illustrationManager.draw()");
        }

        try {
            illustrationManager.draw(matches, searchRegions, actionConfig);
            if (isVerbose()) {
                log.debug("[ILLUSTRATION] Illustration completed successfully");
            }
        } catch (Exception e) {
            log.error("[ILLUSTRATION] Error creating illustration: {}", e.getMessage(), e);
            if (isVerbose()) {
                log.debug("[ILLUSTRATION] Full error:", e);
            }
        }

        return true;
    }

    /*
     * REMOVED DUPLICATE METHOD - Already defined above
     * public boolean illustrateWhenAllowed(ActionResult matches, List<Region>
     * searchRegions, ActionConfig actionConfig,
     * ObjectCollection... objectCollections) {
     * log.debug("[ILLUSTRATION] illustrateWhenAllowed called for ActionConfig: {}",
     * actionConfig.getClass().getSimpleName());
     *
     * if (!okToIllustrate(actionConfig, objectCollections)) {
     * log.debug("[ILLUSTRATION] Illustration not allowed, skipping");
     * return false;
     * }
     *
     * log.
     * debug("[ILLUSTRATION] Illustration allowed, proceeding to create illustration"
     * );
     *
     * // Update last action tracking
     * ActionType mappedAction = getActionType(actionConfig);
     * if (mappedAction != null) {
     * lastAction = mappedAction;
     * if (mappedAction == FIND && actionConfig instanceof PatternFindOptions) {
     * lastFind = mapFindStrategy((PatternFindOptions) actionConfig);
     * }
     * }
     * lastCollections = Arrays.asList(objectCollections);
     *
     * // For now, convert to ActionConfig for the visualization
     * ActionConfig actionConfig = convertToActionConfig(actionConfig);
     *
     * log.debug("[ILLUSTRATION] Calling illustrationManager.draw()");
     * try {
     * illustrationManager.draw(matches, searchRegions, actionConfig);
     * log.debug("[ILLUSTRATION] Illustration completed successfully");
     * } catch (Exception e) {
     * log.error("[ILLUSTRATION] Error creating illustration: {}", e.getMessage(),
     * e);
     * }
     *
     * return true;
     * }
     */

    // Duplicate method removed - using the one at line 136

    /**
     * Maps ActionConfig types to ActionType enum values.
     *
     * @param config the ActionConfig to map
     * @return corresponding ActionType or null if not mappable
     */
    private ActionType getActionType(ActionConfig config) {
        if (config instanceof PatternFindOptions) return ActionType.FIND;
        if (config instanceof ClickOptions) return ActionType.CLICK;
        if (config instanceof TypeOptions) return ActionType.TYPE;
        if (config instanceof DragOptions) return ActionType.DRAG;
        if (config instanceof MouseMoveOptions) return ActionType.MOVE;
        if (config instanceof HighlightOptions) return ActionType.HIGHLIGHT;
        if (config instanceof DefineRegionOptions) return ActionType.DEFINE;
        // CLASSIFY would need its own ActionConfig implementation
        return ActionType.FIND; // Default
    }

    /**
     * Maps PatternFindOptions strategy to ActionConfig.Find enum.
     *
     * @param findOptions the find options to map
     * @return corresponding ActionConfig.Find value
     */
    private PatternFindOptions.Strategy mapFindStrategy(PatternFindOptions findOptions) {
        // Just return the strategy as-is
        return findOptions.getStrategy();
    }

    /**
     * Converts an ActionConfig to ActionConfig for backward compatibility. This is a temporary
     * method until the illustration system is fully updated to work with ActionConfig directly.
     *
     * @param config the ActionConfig to convert
     * @return equivalent ActionConfig
     */

    /** Helper method to check if verbose logging is enabled. */
    private boolean isVerbose() {
        return loggingConfig != null
                && loggingConfig.getVerbosity() == LoggingVerbosityConfig.VerbosityLevel.VERBOSE;
    }

    /** Helper method to extract find strategy from PatternFindOptions. */
    private PatternFindOptions.Strategy getFindStrategy(ActionConfig actionConfig) {
        if (actionConfig instanceof PatternFindOptions) {
            return ((PatternFindOptions) actionConfig).getStrategy();
        }
        return PatternFindOptions.Strategy.FIRST; // Default
    }
}
