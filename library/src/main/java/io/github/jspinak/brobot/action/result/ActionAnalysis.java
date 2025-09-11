package io.github.jspinak.brobot.action.result;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bytedeco.opencv.opencv_core.Mat;

import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;

import lombok.Data;

/**
 * Manages analysis data from action execution. Includes scene analysis, masks, and custom analysis
 * results.
 *
 * <p>This class encapsulates analysis functionality that was previously embedded in ActionResult.
 *
 * @since 2.0
 */
@Data
public class ActionAnalysis {
    private SceneAnalyses sceneAnalyses = new SceneAnalyses();
    @com.fasterxml.jackson.annotation.JsonIgnore private Mat mask;
    private Map<String, Object> customAnalysis = new HashMap<>();

    /** Creates an empty ActionAnalysis. */
    public ActionAnalysis() {}

    /**
     * Adds a scene analysis result.
     *
     * @param analysis The scene analysis to add
     */
    public void addSceneAnalysis(SceneAnalysis analysis) {
        if (analysis != null) {
            sceneAnalyses.add(analysis);
        }
    }

    /**
     * Sets the binary mask for the analysis. Used by FIXED_PIXELS, DYNAMIC_PIXELS, and motion
     * detection.
     *
     * @param mask The OpenCV mask matrix
     */
    public void setMask(Mat mask) {
        this.mask = mask;
    }

    /**
     * Adds custom analysis data.
     *
     * @param key The analysis identifier
     * @param data The analysis data
     */
    public void addCustomAnalysis(String key, Object data) {
        if (key != null && !key.isEmpty() && data != null) {
            customAnalysis.put(key, data);
        }
    }

    /**
     * Gets custom analysis data by key.
     *
     * @param key The analysis identifier
     * @return Optional containing the data, or empty if not found
     */
    public Optional<Object> getCustomAnalysis(String key) {
        return Optional.ofNullable(customAnalysis.get(key));
    }

    /**
     * Gets custom analysis data with type casting.
     *
     * @param key The analysis identifier
     * @param type The expected type class
     * @param <T> The type parameter
     * @return Optional containing the typed data, or empty if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getCustomAnalysis(String key, Class<T> type) {
        Object data = customAnalysis.get(key);
        if (data != null && type.isInstance(data)) {
            return Optional.of((T) data);
        }
        return Optional.empty();
    }

    /**
     * Checks if a mask is present.
     *
     * @return true if a mask exists
     */
    public boolean hasMask() {
        return mask != null;
    }

    /**
     * Checks if scene analyses are present.
     *
     * @return true if scene analyses exist
     */
    public boolean hasSceneAnalyses() {
        return sceneAnalyses != null && !sceneAnalyses.isEmpty();
    }

    /**
     * Checks if custom analyses are present.
     *
     * @return true if custom analyses exist
     */
    public boolean hasCustomAnalyses() {
        return !customAnalysis.isEmpty();
    }

    /**
     * Checks if any analysis data exists.
     *
     * @return true if any analysis data is present
     */
    public boolean hasAnalysis() {
        return hasMask() || hasSceneAnalyses() || hasCustomAnalyses();
    }

    /**
     * Gets the number of scene analyses.
     *
     * @return Count of scene analyses
     */
    public int getSceneAnalysisCount() {
        return sceneAnalyses != null && sceneAnalyses.getSceneAnalyses() != null
                ? sceneAnalyses.getSceneAnalyses().size()
                : 0;
    }

    /**
     * Gets the number of custom analyses.
     *
     * @return Count of custom analyses
     */
    public int getCustomAnalysisCount() {
        return customAnalysis.size();
    }

    /**
     * Merges analysis data from another instance.
     *
     * @param other The ActionAnalysis to merge
     */
    public void merge(ActionAnalysis other) {
        if (other != null) {
            if (other.sceneAnalyses != null) {
                sceneAnalyses.merge(other.sceneAnalyses);
            }

            // Take the other's mask if we don't have one
            if (mask == null && other.mask != null) {
                mask = other.mask;
            }

            customAnalysis.putAll(other.customAnalysis);
        }
    }

    /**
     * Clears all analysis data. Note: Does not release OpenCV Mat resources - caller must handle
     * that.
     */
    public void clear() {
        sceneAnalyses = new SceneAnalyses();
        mask = null;
        customAnalysis.clear();
    }

    /**
     * Gets analysis type summary.
     *
     * @return String describing types of analysis present
     */
    public String getAnalysisTypes() {
        StringBuilder types = new StringBuilder();

        if (hasSceneAnalyses()) {
            types.append("Scene(").append(getSceneAnalysisCount()).append(")");
        }

        if (hasMask()) {
            if (types.length() > 0) types.append(", ");
            types.append("Mask");
        }

        if (hasCustomAnalyses()) {
            if (types.length() > 0) types.append(", ");
            types.append("Custom(").append(getCustomAnalysisCount()).append(")");
        }

        return types.length() > 0 ? types.toString() : "None";
    }

    /**
     * Formats the analysis data as a string summary.
     *
     * @return Formatted analysis summary
     */
    public String format() {
        if (!hasAnalysis()) {
            return "No analysis data";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Analysis: ");
        sb.append(getAnalysisTypes());

        if (!customAnalysis.isEmpty() && customAnalysis.size() <= 5) {
            sb.append(" [");
            sb.append(String.join(", ", customAnalysis.keySet()));
            sb.append("]");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return format();
    }
}
