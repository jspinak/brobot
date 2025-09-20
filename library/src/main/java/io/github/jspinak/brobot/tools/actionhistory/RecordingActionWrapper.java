package io.github.jspinak.brobot.tools.actionhistory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for Action that automatically records execution history.
 *
 * <p>This wrapper intercepts action executions and automatically records them to ActionHistory for
 * later analysis and mock testing.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Automatic recording of all action executions
 *   <li>Session management with auto-save
 *   <li>Toggle recording on/off
 *   <li>Performance metrics collection
 *   <li>Multi-pattern history tracking
 * </ul>
 *
 * @since 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecordingActionWrapper {

    private final Action action;
    private final ActionHistoryPersistence persistence;

    @Setter private boolean recordingEnabled = true;

    @Setter private boolean autoSaveEnabled = true;

    @Setter private int autoSaveInterval = 50;

    private final Map<String, Integer> recordCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionStartTimes = new ConcurrentHashMap<>();

    /**
     * Execute action with automatic history recording.
     *
     * @param config the action configuration
     * @param objects the object collection
     * @return the action result
     */
    public ActionResult performWithRecording(ActionConfig config, ObjectCollection objects) {
        long startTime = System.currentTimeMillis();
        ActionResult result = action.perform(config, objects);
        long duration = System.currentTimeMillis() - startTime;

        if (recordingEnabled) {
            recordExecution(result, objects, config, duration);
        }

        return result;
    }

    /**
     * Execute action and record to specific pattern.
     *
     * @param config the action configuration
     * @param pattern the pattern to record to
     * @param objects the object collection
     * @return the action result
     */
    public ActionResult performWithRecording(
            ActionConfig config, Pattern pattern, ObjectCollection objects) {
        ActionResult result = action.perform(config, objects);

        if (recordingEnabled && pattern != null) {
            persistence.captureCurrentExecution(result, pattern, config);

            // Track record count for auto-save
            String patternKey = pattern.getName();
            int count = recordCounts.merge(patternKey, 1, Integer::sum);

            if (autoSaveEnabled && count % autoSaveInterval == 0) {
                try {
                    persistence.saveSessionHistory(pattern, "auto-" + patternKey);
                    log.debug("Auto-saved {} after {} records", patternKey, count);
                } catch (IOException e) {
                    log.error("Failed to auto-save pattern history", e);
                }
            }
        }

        return result;
    }

    /** Record execution to all patterns in the object collection. */
    private void recordExecution(
            ActionResult result, ObjectCollection objects, ActionConfig config, long duration) {
        // Record to StateImages' patterns
        for (StateImage stateImage : objects.getStateImages()) {
            if (!stateImage.getPatterns().isEmpty()) {
                Pattern pattern = stateImage.getPatterns().get(0);
                persistence.captureCurrentExecution(result, pattern, config);
                updateRecordCount(pattern.getName());
            }
        }
    }

    private void updateRecordCount(String patternName) {
        int count = recordCounts.merge(patternName, 1, Integer::sum);

        if (autoSaveEnabled && count % autoSaveInterval == 0) {
            log.debug("Pattern {} reached {} records - consider saving", patternName, count);
        }
    }

    /**
     * Start a new recording session.
     *
     * @param sessionName the name of the session
     */
    public void startSession(String sessionName) {
        sessionStartTimes.put(sessionName, System.currentTimeMillis());
        recordCounts.clear();
        recordingEnabled = true;
        log.info("Started recording session: {}", sessionName);
    }

    /**
     * End a recording session and save all histories.
     *
     * @param sessionName the name of the session
     * @param patterns patterns to save histories from
     */
    public void endSession(String sessionName, Collection<Pattern> patterns) {
        Long startTime = sessionStartTimes.remove(sessionName);
        if (startTime == null) {
            log.warn("No active session found: {}", sessionName);
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Ending session {} after {} ms", sessionName, duration);

        // Save all pattern histories
        for (Pattern pattern : patterns) {
            if (pattern.getMatchHistory() != null
                    && !pattern.getMatchHistory().getSnapshots().isEmpty()) {
                try {
                    String filename = String.format("%s_%s", sessionName, pattern.getName());
                    persistence.saveSessionHistory(pattern, filename);
                } catch (IOException e) {
                    log.error("Failed to save history for pattern: {}", pattern.getName(), e);
                }
            }
        }

        recordCounts.clear();
    }

    /**
     * Get statistics for the current recording session.
     *
     * @return map of pattern names to record counts
     */
    public Map<String, Integer> getRecordingStatistics() {
        return new HashMap<>(recordCounts);
    }

    /** Clear all recording data without saving. */
    public void clearRecordingData() {
        recordCounts.clear();
        sessionStartTimes.clear();
        log.info("Cleared all recording data");
    }

    /** Convenience methods that delegate to the wrapped Action. */
    public ActionResult find(StateImage stateImage) {
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        return performWithRecording(
                findOptions, new ObjectCollection.Builder().withImages(stateImage).build());
    }

    public ActionResult click(StateImage stateImage) {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        return performWithRecording(
                clickOptions, new ObjectCollection.Builder().withImages(stateImage).build());
    }

    public ActionResult type(String text) {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        return performWithRecording(
                typeOptions, new ObjectCollection.Builder().withStrings(text).build());
    }

    /**
     * Find with timeout and recording.
     *
     * @param timeout timeout in seconds
     * @param stateImage the state image to find
     * @return the action result
     */
    public ActionResult findWithTimeout(double timeout, StateImage stateImage) {
        ActionResult result = action.findWithTimeout(timeout, stateImage);

        if (recordingEnabled && !stateImage.getPatterns().isEmpty()) {
            Pattern pattern = stateImage.getPatterns().get(0);
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setPauseBeforeBegin(timeout).build();
            persistence.captureCurrentExecution(result, pattern, findOptions);
            updateRecordCount(pattern.getName());
        }

        return result;
    }

    /**
     * Get the underlying Action instance.
     *
     * @return the wrapped Action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Check if recording is currently enabled.
     *
     * @return true if recording is enabled
     */
    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    /**
     * Get the current auto-save interval.
     *
     * @return number of records between auto-saves
     */
    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }
}
