package io.github.jspinak.brobot.tools.history;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.util.file.FilenameAllocator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.action.ActionType.FIND;

/**
 * Generates unique filenames for Brobot's visual history illustrations.
 * <p>
 * This component manages the naming convention for all illustration files
 * generated
 * during action execution. It ensures unique filenames while encoding important
 * metadata like action types, target images, and analysis results. The naming
 * system prevents filename collisions and makes illustrations easily
 * searchable.
 * <p>
 * Filename structure patterns:
 * <ul>
 * <li>Basic: {prefix}_{action}_{imageNames}.png</li>
 * <li>Scene analysis:
 * {prefix}_{action}_in-{sceneName}_{targetImages}_{analysisType}.png</li>
 * <li>With Find type: {prefix}_FIND_{findType}_{imageNames}.png</li>
 * </ul>
 * <p>
 * Key features:
 * <ul>
 * <li>Automatic uniqueness through {@link FilenameAllocator} integration</li>
 * <li>Metadata encoding in filename for easy identification</li>
 * <li>Support for multiple naming strategies based on action type</li>
 * <li>Configurable base path and prefix via {@link FrameworkSettings}</li>
 * </ul>
 * <p>
 * Common naming components:
 * <ul>
 * <li>Action type: FIND, CLICK, CLASSIFY, etc.</li>
 * <li>Scene name: Name of the analyzed scene or "screenshot"</li>
 * <li>Target images: Underscore-separated list of searched images</li>
 * <li>Analysis descriptors: "classes", "scene", etc.</li>
 * </ul>
 * <p>
 * Thread safety: This component is thread-safe through its dependency on
 * thread-safe {@link FilenameAllocator} for filename reservation.
 *
 * @see FilenameAllocator
 * @see FrameworkSettings
 * @see ActionResult
 * @see SceneAnalysis
 */
@Component
public class HistoryFileNamer {

    private final FilenameAllocator filenameRepo;

    public HistoryFileNamer(FilenameAllocator filenameRepo) {
        this.filenameRepo = filenameRepo;
    }

    /**
     * Generates a single filename for the primary illustration of an action result.
     * <p>
     * This method handles cases where matches may contain multiple or no
     * SceneAnalyses.
     * When SceneAnalyses are present, it uses the first one; otherwise, it falls
     * back
     * to generating a filename from match objects. This ensures every action gets
     * an illustration filename regardless of analysis availability.
     * <p>
     * Filename structure with SceneAnalysis:
     * <ul>
     * <li>Action name (FIND, CLICK, CLASSIFY, etc.)</li>
     * <li>"in-{SceneName}": scene identifier or "screenshot"</li>
     * <li>Target image names separated by underscores</li>
     * <li>Analysis type descriptor ("classes" for pixel classification, etc.)</li>
     * </ul>
     * <p>
     * Usage note: For multiple SceneAnalyses, call
     * {@link #getFilenameFromSceneAnalysis}
     * for each analysis to get individual filenames.
     *
     * @param matches      action results containing search targets and analysis
     *                     data
     * @param actionConfig configuration including the action type
     * @return unique filename path for the illustration
     */
    public String getSingleFilename(ActionResult matches, ActionConfig actionConfig) {
        if (matches.getSceneAnalysisCollection().isEmpty())
            return getFilenameFromMatchObjects(matches, actionConfig);
        SceneAnalysis sceneAnalysis = matches.getSceneAnalysisCollection().getSceneAnalyses().get(0);
        return getFilenameFromSceneAnalysis(sceneAnalysis, actionConfig);
    }

    /**
     * Generates a filename based on match objects when no scene analysis is
     * available.
     * <p>
     * Creates filenames by extracting state object names from all matches and
     * combining them with the action type. This fallback method ensures
     * illustrations
     * can be saved even when detailed scene analysis isn't performed.
     * <p>
     * The method aggregates unique state object names to avoid duplication in
     * filenames, making them more readable and preventing excessively long names.
     *
     * @param matches      action results containing matched objects
     * @param actionConfig configuration with action type information
     * @return unique filename path constructed from match data
     */
    public String getFilenameFromMatchObjects(ActionResult matches, ActionConfig actionConfig) {
        String prefix = FrameworkSettings.historyPath + FrameworkSettings.historyFilename;
        ActionType actionType = getActionTypeFromConfig(actionConfig);
        Set<String> imageNames = matches.getMatchList().stream().map(
                m -> m.getStateObjectData().getStateObjectName()).collect(Collectors.toSet());
        String names = String.join("", imageNames);
        String suffix = actionType.toString() + "_" + names;
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    /**
     * Generates a detailed filename from scene analysis data with optional
     * descriptors.
     * <p>
     * Creates rich filenames that encode the complete context of an analysis:
     * action type, scene name, target images, and any additional descriptors.
     * This detailed naming helps identify illustrations at a glance and supports
     * efficient searching through history files.
     * <p>
     * The "in-{sceneName}" convention clearly indicates which scene was analyzed,
     * while additional descriptors can specify analysis types like "classes" for
     * classification results or "colorProfile" for color analysis.
     *
     * @param sceneAnalysis         analysis results including scene and target
     *                              information
     * @param actionConfig          configuration with action type
     * @param additionalDescription optional descriptors appended to filename (e.g.,
     *                              "classes", "contours")
     * @return unique filename path with encoded analysis metadata
     */
    public String getFilenameFromSceneAnalysis(SceneAnalysis sceneAnalysis, ActionConfig actionConfig,
            String... additionalDescription) {
        String prefix = FrameworkSettings.historyPath;
        ActionType actionType = getActionTypeFromConfig(actionConfig);
        String sceneName = sceneAnalysis.getScene().getPattern().getName();
        String imageNames = sceneAnalysis.getImageNames();
        String names = String.join("_", imageNames);
        String suffix = actionType.toString() + "_in-" + sceneName + "_" + names
                + String.join("_", additionalDescription);
        ConsoleReporter.println("Filename: " + prefix + suffix);
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    /**
     * Composes a complete filename with action and image names.
     * <p>
     * Helper method that assembles the final filename by appending action type
     * and image names to a base path, ensuring the standard .png extension.
     *
     * @param freepath   base path without extension
     * @param action     action type to include in filename
     * @param imageNames concatenated image names
     * @return complete filename with .png extension
     */
    private String composeName(String freepath, ActionType actionType, String imageNames) {
        return freepath + "_" + actionType.toString() + "_" + imageNames + ".png";
    }

    /**
     * Composes a numbered filename for sequential illustrations.
     * <p>
     * Creates filenames with numeric identifiers, useful for generating
     * sequences of related illustrations (e.g., animation frames or
     * multi-step processes).
     *
     * @param path       directory path
     * @param baseName   base filename without number
     * @param number     sequence number to include
     * @param action     action type to include in filename
     * @param imageNames concatenated image names
     * @return complete numbered filename with .png extension
     */
    private String composeName(String path, String baseName, int number, ActionType actionType, String imageNames) {
        return path + baseName + number + "_" + actionType.toString() + "_" + imageNames + ".png";
    }

    /**
     * Generates a filename from multiple object collections.
     * <p>
     * Processes multiple ObjectCollections to create comprehensive filenames
     * that include all involved state images. Special handling for FIND actions
     * includes the find type (e.g., FIND_ALL, FIND_FIRST) in the filename for
     * better identification of search strategies.
     * <p>
     * This method aggregates image names from all collections, making it ideal
     * for complex operations involving multiple state objects or when comparing
     * results across different object sets.
     *
     * @param actionConfig      configuration including action type and find options
     * @param objectCollections variable number of collections containing state
     *                          images
     * @return unique filename incorporating all object collection data
     */
    public String getFilename(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        String prefix = FrameworkSettings.historyPath + FrameworkSettings.historyFilename;
        ActionType actionType = getActionTypeFromConfig(actionConfig);
        List<String> names = new ArrayList<>();
        for (ObjectCollection objectCollection : objectCollections) {
            names.addAll(objectCollection.getStateImages().stream().map(StateImage::getName).toList());
        }
        String allNames = String.join("", names);
        String suffix = actionType.toString();
        // Add find strategy if applicable
        if (actionType == FIND
                && actionConfig instanceof io.github.jspinak.brobot.action.basic.find.PatternFindOptions) {
            io.github.jspinak.brobot.action.basic.find.PatternFindOptions findOptions = (io.github.jspinak.brobot.action.basic.find.PatternFindOptions) actionConfig;
            suffix += "_" + findOptions.getStrategy();
        }
        if (!allNames.isEmpty())
            suffix += "_" + allNames;
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    /**
     * Generates a simple filename with action type and custom name.
     * <p>
     * Provides maximum flexibility by accepting a custom name string,
     * useful for special cases where standard naming conventions don't
     * apply. Common uses include debugging illustrations, custom analysis
     * results, or user-specified illustration names.
     *
     * @param action action type to include in filename
     * @param name   custom name component for the filename
     * @return unique filename with action and custom name
     */
    public String getFilename(ActionType actionType, String name) {
        String prefix = FrameworkSettings.historyPath + FrameworkSettings.historyFilename;
        String suffix = actionType.toString() + "_" + name;
        return filenameRepo.reserveFreePath(prefix, suffix);
    }

    /**
     * Helper method to extract ActionType from ActionConfig.
     */
    private ActionType getActionTypeFromConfig(ActionConfig actionConfig) {
        String className = actionConfig.getClass().getSimpleName();
        if (className.contains("Click"))
            return ActionType.CLICK;
        if (className.contains("Find") || className.contains("Pattern"))
            return ActionType.FIND;
        if (className.contains("Type"))
            return ActionType.TYPE;
        if (className.contains("Drag"))
            return ActionType.DRAG;
        if (className.contains("Highlight"))
            return ActionType.HIGHLIGHT;
        if (className.contains("Scroll"))
            return ActionType.SCROLL_MOUSE_WHEEL;
        if (className.contains("Define"))
            return ActionType.DEFINE;
        if (className.contains("Move"))
            return ActionType.MOVE;
        return ActionType.FIND; // default
    }
}
