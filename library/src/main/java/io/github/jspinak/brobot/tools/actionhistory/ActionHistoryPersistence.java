package io.github.jspinak.brobot.tools.actionhistory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for persisting and loading ActionHistory data.
 *
 * <p>This class provides methods for saving and loading ActionHistory to/from JSON files, capturing
 * execution data, and managing history sessions.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Save/load ActionHistory to/from JSON files
 *   <li>Capture and record live execution data
 *   <li>Auto-save functionality
 *   <li>Batch loading and merging of histories
 *   <li>Session management with timestamps
 * </ul>
 *
 * @since 1.2.0
 */
@Component
@Slf4j
public class ActionHistoryPersistence {

    private final ObjectMapper objectMapper;

    public ActionHistoryPersistence() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static final String DEFAULT_HISTORY_PATH = "src/test/resources/histories";
    private static final int AUTO_SAVE_INTERVAL = 100;

    /**
     * Save ActionHistory to a JSON file.
     *
     * @param history the ActionHistory to save
     * @param filename the name of the file (without path)
     * @throws IOException if unable to save the file
     */
    public void saveToFile(ActionHistory history, String filename) throws IOException {
        saveToFile(history, filename, DEFAULT_HISTORY_PATH);
    }

    /**
     * Save ActionHistory to a JSON file in a specific directory.
     *
     * @param history the ActionHistory to save
     * @param filename the name of the file
     * @param directory the directory path
     * @throws IOException if unable to save the file
     */
    public void saveToFile(ActionHistory history, String filename, String directory)
            throws IOException {
        Path path = Path.of(directory, filename);
        Files.createDirectories(path.getParent());

        try {
            String json = objectMapper.writeValueAsString(history);
            Files.writeString(path, json);
            log.info("Saved ActionHistory to {}", path);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ActionHistory", e);
            throw new IOException("Failed to serialize ActionHistory", e);
        }
    }

    /**
     * Load ActionHistory from a JSON file.
     *
     * @param filename the name of the file to load
     * @return the loaded ActionHistory
     * @throws IOException if unable to load the file
     */
    public ActionHistory loadFromFile(String filename) throws IOException {
        return loadFromFile(filename, DEFAULT_HISTORY_PATH);
    }

    /**
     * Load ActionHistory from a JSON file in a specific directory.
     *
     * @param filename the name of the file to load
     * @param directory the directory path
     * @return the loaded ActionHistory
     * @throws IOException if unable to load the file
     */
    public ActionHistory loadFromFile(String filename, String directory) throws IOException {
        Path path = Path.of(directory, filename);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + path);
        }

        String json = Files.readString(path);

        try {
            // Automatically migrates legacy ActionOptions format if needed
            ActionHistory history = objectMapper.readValue(json, ActionHistory.class);
            log.info("Loaded ActionHistory from {}", path);
            return history;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize ActionHistory from {}", path, e);
            throw new IOException("Failed to deserialize ActionHistory", e);
        }
    }

    /**
     * Save ActionHistory from a StateImage's first Pattern with session metadata.
     *
     * @param stateImage the StateImage containing patterns with ActionHistory
     * @param sessionName the name of the session
     * @throws IOException if unable to save
     */
    public void saveSessionHistory(StateImage stateImage, String sessionName) throws IOException {
        if (stateImage == null || stateImage.getPatterns().isEmpty()) {
            log.warn("Cannot save session history: StateImage is null or has no patterns");
            return;
        }

        // Save history from first pattern
        Pattern firstPattern = stateImage.getPatterns().get(0);
        saveSessionHistory(firstPattern, sessionName);
    }

    /**
     * Save ActionHistory from a Pattern with session metadata.
     *
     * @param pattern the Pattern containing the ActionHistory
     * @param sessionName the name of the session
     * @throws IOException if unable to save
     */
    public void saveSessionHistory(Pattern pattern, String sessionName) throws IOException {
        if (pattern == null) {
            log.warn("Cannot save session history: Pattern is null");
            return;
        }

        ActionHistory history = pattern.getMatchHistory();
        if (history == null || history.getSnapshots().isEmpty()) {
            log.warn("No history to save for session: {}", sessionName);
            return;
        }

        // Add session metadata
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s_%s.json", sessionName, timestamp);

        saveToFile(history, filename);
    }

    /**
     * Capture current execution and add to Pattern's ActionHistory.
     *
     * @param result the ActionResult from the execution
     * @param pattern the Pattern to add the record to
     * @param config the ActionConfig used
     */
    public void captureCurrentExecution(ActionResult result, Pattern pattern, ActionConfig config) {
        if (pattern == null) {
            log.debug("Cannot capture execution: Pattern is null");
            return;
        }

        String textResult = "";
        if (result.getText() != null && !result.getText().isEmpty()) {
            textResult = String.join(", ", result.getText().getAll());
        }

        ActionRecord record =
                new ActionRecord.Builder()
                        .setActionConfig(config)
                        .setMatchList(result.getMatchList())
                        .setText(textResult)
                        .setActionSuccess(result.isSuccess())
                        .setDuration(result.getDuration().toMillis())
                        .build();

        // Add to history
        ActionHistory history = pattern.getMatchHistory();
        if (history == null) {
            history = new ActionHistory();
            pattern.setMatchHistory(history);
        }
        history.addSnapshot(record);

        // Auto-save if threshold reached
        if (history.getSnapshots().size() % AUTO_SAVE_INTERVAL == 0) {
            try {
                saveSessionHistory(pattern, "auto-save");
                log.debug("Auto-saved history after {} records", AUTO_SAVE_INTERVAL);
            } catch (IOException e) {
                log.error("Failed to auto-save history", e);
            }
        }
    }

    /**
     * Batch load all history files from a directory.
     *
     * @param directory the directory to load from
     * @return map of filename to ActionHistory
     * @throws IOException if unable to read directory
     */
    public Map<String, ActionHistory> loadAllHistories(String directory) throws IOException {
        Map<String, ActionHistory> histories = new HashMap<>();
        Path dir = Path.of(directory);

        if (!Files.exists(dir)) {
            log.warn("Directory {} does not exist", dir);
            return histories;
        }

        List<Path> jsonFiles =
                Files.list(dir)
                        .filter(p -> p.toString().endsWith(".json"))
                        .collect(Collectors.toList());

        for (Path path : jsonFiles) {
            try {
                String name = path.getFileName().toString().replace(".json", "");
                String json = Files.readString(path);
                histories.put(name, objectMapper.readValue(json, ActionHistory.class));
                log.debug("Loaded history: {}", name);
            } catch (IOException e) {
                log.error("Failed to load {}: {}", path, e.getMessage());
            }
        }

        log.info("Loaded {} history files from {}", histories.size(), directory);
        return histories;
    }

    /**
     * Load all histories from the default directory.
     *
     * @return map of filename to ActionHistory
     * @throws IOException if unable to read directory
     */
    public Map<String, ActionHistory> loadAllHistories() throws IOException {
        return loadAllHistories(DEFAULT_HISTORY_PATH);
    }

    /**
     * Merge multiple history files into one.
     *
     * @param filenames list of filenames to merge
     * @return merged ActionHistory
     * @throws IOException if unable to load files
     */
    public ActionHistory mergeHistories(List<String> filenames) throws IOException {
        return mergeHistories(filenames, DEFAULT_HISTORY_PATH);
    }

    /**
     * Merge multiple history files from a specific directory.
     *
     * @param filenames list of filenames to merge
     * @param directory the directory containing the files
     * @return merged ActionHistory
     * @throws IOException if unable to load files
     */
    public ActionHistory mergeHistories(List<String> filenames, String directory)
            throws IOException {
        ActionHistory merged = new ActionHistory();

        for (String filename : filenames) {
            ActionHistory history = loadFromFile(filename, directory);
            merged.getSnapshots().addAll(history.getSnapshots());
        }

        log.info(
                "Merged {} histories with {} total records",
                filenames.size(),
                merged.getSnapshots().size());
        return merged;
    }

    /**
     * Delete old history files older than specified days.
     *
     * @param directory the directory to clean
     * @param daysToKeep number of days to keep
     * @return number of files deleted
     */
    public int cleanOldHistories(String directory, int daysToKeep) {
        Path dir = Path.of(directory);
        if (!Files.exists(dir)) {
            return 0;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        int deleted = 0;

        try {
            List<Path> oldFiles =
                    Files.list(dir)
                            .filter(p -> p.toString().endsWith(".json"))
                            .filter(
                                    p -> {
                                        try {
                                            LocalDateTime modified =
                                                    LocalDateTime.ofInstant(
                                                            Files.getLastModifiedTime(p)
                                                                    .toInstant(),
                                                            java.time.ZoneId.systemDefault());
                                            return modified.isBefore(cutoff);
                                        } catch (IOException e) {
                                            return false;
                                        }
                                    })
                            .collect(Collectors.toList());

            for (Path file : oldFiles) {
                try {
                    Files.delete(file);
                    deleted++;
                    log.debug("Deleted old history file: {}", file.getFileName());
                } catch (IOException e) {
                    log.error("Failed to delete {}", file, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to clean old histories", e);
        }

        if (deleted > 0) {
            log.info("Deleted {} old history files from {}", deleted, directory);
        }

        return deleted;
    }
}
