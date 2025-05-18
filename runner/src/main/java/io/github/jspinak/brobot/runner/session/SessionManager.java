package io.github.jspinak.brobot.runner.session;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Manages automation sessions with persistence capabilities
 */
@Component
public class SessionManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;
    private final ResourceManager resourceManager;
    private final JsonParser jsonParser;
    private final StateTransitionsRepository stateTransitionsRepository;

    private final ScheduledExecutorService scheduler;
    private final AtomicReference<Session> currentSession = new AtomicReference<>();
    private Path sessionStoragePath;

    @Getter
    private LocalDateTime lastAutosaveTime;

    @Autowired
    public SessionManager(EventBus eventBus,
                          BrobotRunnerProperties properties,
                          ResourceManager resourceManager,
                          JsonParser jsonParser,
                          StateTransitionsRepository stateTransitionsRepository) {
        this.eventBus = eventBus;
        this.properties = properties;
        this.resourceManager = resourceManager;
        this.jsonParser = jsonParser;
        this.stateTransitionsRepository = stateTransitionsRepository;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Session-Persistence");
            t.setDaemon(true);
            return t;
        });

        resourceManager.registerResource(this, "SessionManager");
    }

    @PostConstruct
    public void initialize() {
        sessionStoragePath = Paths.get(properties.getLogPath(), "sessions");

        try {
            Files.createDirectories(sessionStoragePath);
            eventBus.publish(LogEvent.info(this,
                    "Session directory created: " + sessionStoragePath, "Session"));
        } catch (IOException e) {
            eventBus.publish(LogEvent.error(this,
                    "Failed to create session directory: " + e.getMessage(), "Session", e));
        }

        // Schedule periodic session autosave
        scheduler.scheduleAtFixedRate(
                this::autosaveCurrentSession,
                5, 5, TimeUnit.MINUTES);
    }

    /**
     * Starts a new session with the given configuration
     */
    public Session startNewSession(String projectName, String configPath, String imagePath) {
        if (isSessionActive()) {
            // Save and close the current session
            endCurrentSession();
        }

        String sessionId = UUID.randomUUID().toString();
        Session session = new Session();
        session.setId(sessionId);
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        session.setProjectName(projectName);
        session.setConfigPath(configPath);
        session.setImagePath(imagePath);

        currentSession.set(session);

        eventBus.publish(LogEvent.info(this,
                "Session started: " + sessionId, "Session"));

        // Save the initial session state
        saveSession(session);

        return session;
    }

    /**
     * Ends the current session
     */
    public void endCurrentSession() {
        Session session = currentSession.get();
        if (session != null && session.isActive()) {
            session.setEndTime(LocalDateTime.now());
            session.setActive(false);

            // Save the final session state
            saveSession(session);

            eventBus.publish(LogEvent.info(this,
                    "Session ended: " + session.getId(), "Session"));

            currentSession.set(null);
        }
    }

    /**
     * Checks if there's an active session
     */
    public boolean isSessionActive() {
        Session session = currentSession.get();
        return session != null && session.isActive();
    }

    /**
     * Gets the current session or null if none exists
     */
    public Session getCurrentSession() {
        return currentSession.get();
    }

    /**
     * Autosaves the current session if one exists
     */
    public void autosaveCurrentSession() {
        Session session = currentSession.get();
        if (session != null && session.isActive()) {
            try {
                // Capture current application state before saving
                captureApplicationState(session);
                saveSession(session);
                lastAutosaveTime = LocalDateTime.now();

                eventBus.publish(LogEvent.info(this,
                        "Session autosaved: " + session.getId(), "Session"));
            } catch (Exception e) {
                eventBus.publish(LogEvent.error(this,
                        "Failed to autosave session: " + e.getMessage(), "Session", e));
            }
        }
    }

    /**
     * Captures the current application state into the session
     */
    private void captureApplicationState(Session session) {
        try {
            // Capture state transitions
            List<StateTransitions> stateTransitions =
                    stateTransitionsRepository.getAllStateTransitionsAsCopy();
            session.setStateTransitions(stateTransitions);

            // Get active states, if any
            Set<State> activeStates = getActiveStates();
            if (activeStates != null && !activeStates.isEmpty()) {
                session.setActiveStateIds(
                        activeStates.stream()
                                .map(State::getId)
                                .collect(Collectors.toSet())
                );
            }

            // Add a session event for this capture
            session.addEvent(new SessionEvent("STATE_CAPTURE",
                    "Application state captured",
                    "Active states: " + (activeStates != null ? activeStates.size() : 0)));

        } catch (Exception e) {
            logger.error("Error capturing application state", e);
            session.addEvent(new SessionEvent("ERROR",
                    "Failed to capture application state: " + e.getMessage()));
        }
    }

    /**
     * Gets the currently active states (implementation depends on Brobot state management)
     */
    private Set<State> getActiveStates() {
        // This would need to be implemented to access the Brobot state system
        // As a placeholder, we'll return an empty set
        return new HashSet<>();
    }

    /**
     * Saves a session to disk
     */
    public void saveSession(Session session) {
        if (session == null) return;

        // Create a filename with session ID and timestamp
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(LocalDateTime.now());
        String filename = "session_" + session.getId() + "_" + timestamp + ".json";

        Path filePath = sessionStoragePath.resolve(filename);

        try {
            // Use the safe serialization approach for complex objects
            String json = jsonParser.toPrettyJsonSafe(session);
            Files.writeString(filePath, json);

            eventBus.publish(LogEvent.debug(this,
                    "Session saved: " + session.getId(), "Session"));
        } catch (Exception e) {
            eventBus.publish(LogEvent.error(this,
                    "Failed to save session: " + e.getMessage(), "Session", e));
        }
    }

    /**
     * Loads a session by ID
     */
    public Optional<Session> loadSession(String sessionId) {
        File[] files = sessionStoragePath.toFile().listFiles(
                (dir, name) -> name.contains("session_" + sessionId));

        if (files == null || files.length == 0) {
            eventBus.publish(LogEvent.warning(this,
                    "No session found with ID: " + sessionId, "Session"));
            return Optional.empty();
        }

        // Find the latest session file
        File latestFile = files[0];
        for (File file : files) {
            if (file.lastModified() > latestFile.lastModified()) {
                latestFile = file;
            }
        }

        try {
            String json = Files.readString(latestFile.toPath());
            Session loadedSession = jsonParser.convertJson(json, Session.class);

            eventBus.publish(LogEvent.info(this,
                    "Session loaded: " + sessionId, "Session"));

            return Optional.of(loadedSession);
        } catch (Exception e) {
            eventBus.publish(LogEvent.error(this,
                    "Failed to load session: " + e.getMessage(), "Session", e));
            return Optional.empty();
        }
    }

    /**
     * Gets a list of all available session IDs
     */
    public List<SessionSummary> getAllSessionSummaries() {
        List<SessionSummary> summaries = new ArrayList<>();
        File[] files = sessionStoragePath.toFile().listFiles(
                (dir, name) -> name.startsWith("session_") && name.endsWith(".json"));

        if (files == null || files.length == 0) {
            return summaries;
        }

        // Group files by session ID
        Map<String, List<File>> sessionFiles = new HashMap<>();
        for (File file : files) {
            String name = file.getName();
            int endIndex = name.indexOf('_', "session_".length());
            if (endIndex < 0) continue;

            String sessionId = name.substring("session_".length(), endIndex);
            sessionFiles.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(file);
        }

        // Create summaries from the latest file for each session
        for (Map.Entry<String, List<File>> entry : sessionFiles.entrySet()) {
            String sessionId = entry.getKey();
            List<File> sessionFileList = entry.getValue();

            // Find the latest file
            File latestFile = sessionFileList.stream()
                    .max(Comparator.comparing(File::lastModified))
                    .orElse(null);

            if (latestFile == null) continue;

            try {
                String json = Files.readString(latestFile.toPath());
                JsonNode rootNode = jsonParser.parseJson(json);

                SessionSummary summary = new SessionSummary();
                summary.setId(sessionId);
                summary.setProjectName(getStringValue(rootNode, "projectName"));
                summary.setStartTime(getDateTimeValue(rootNode, "startTime"));
                summary.setEndTime(getDateTimeValue(rootNode, "endTime"));
                summary.setLastSaved(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(latestFile.lastModified()),
                        java.time.ZoneId.systemDefault()));
                summary.setActive(getBooleanValue(rootNode, "active"));

                summaries.add(summary);
            } catch (Exception e) {
                logger.error("Error reading session file: " + latestFile.getName(), e);
            }
        }

        // Sort by start time (most recent first)
        summaries.sort(Comparator.comparing(SessionSummary::getStartTime).reversed());

        return summaries;
    }

    private String getStringValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    private LocalDateTime getDateTimeValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            try {
                return LocalDateTime.parse(node.get(fieldName).asText());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Boolean getBooleanValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asBoolean();
        }
        return null;
    }

    /**
     * Restores a session
     */
    public boolean restoreSession(String sessionId) {
        Optional<Session> loadedSession = loadSession(sessionId);
        if (loadedSession.isEmpty()) {
            return false;
        }

        try {
            Session session = loadedSession.get();

            // End current session if active
            if (isSessionActive()) {
                endCurrentSession();
            }

            // Mark session as active if it wasn't explicitly ended
            if (session.getEndTime() == null) {
                session.setActive(true);
            }

            // Set as current session
            currentSession.set(session);

            // Restore application state
            restoreApplicationState(session);

            // Add a restoration event
            session.addEvent(new SessionEvent("RESTORED",
                    "Session restored",
                    "Application state restored from saved session"));

            // Save the restored session
            saveSession(session);

            eventBus.publish(LogEvent.info(this,
                    "Session restored: " + sessionId, "Session"));

            return true;
        } catch (Exception e) {
            eventBus.publish(LogEvent.error(this,
                    "Failed to restore session: " + e.getMessage(), "Session", e));
            return false;
        }
    }

    /**
     * Restores the application state from a session
     */
    private void restoreApplicationState(Session session) {
        try {
            // Clear current state
            // This would need to access the Brobot state management system

            // Restore state transitions if available
            List<StateTransitions> stateTransitions = session.getStateTransitions();
            if (stateTransitions != null && !stateTransitions.isEmpty()) {
                // Clear existing transitions
                stateTransitionsRepository.emptyRepos();

                // Add restored transitions
                for (StateTransitions transition : stateTransitions) {
                    stateTransitionsRepository.add(transition);
                }

                eventBus.publish(LogEvent.info(this,
                        "Restored " + stateTransitions.size() + " state transitions", "Session"));
            }

            // Restore active states if available
            Set<Long> activeStateIds = session.getActiveStateIds();
            if (activeStateIds != null && !activeStateIds.isEmpty()) {
                // This would activate the states in the Brobot system
                eventBus.publish(LogEvent.info(this,
                        "Restored " + activeStateIds.size() + " active states", "Session"));
            }

        } catch (Exception e) {
            eventBus.publish(LogEvent.error(this,
                    "Error restoring application state: " + e.getMessage(), "Session", e));
            throw new RuntimeException("Failed to restore application state", e);
        }
    }

    /**
     * Deletes a session
     */
    public boolean deleteSession(String sessionId) {
        File[] files = sessionStoragePath.toFile().listFiles(
                (dir, name) -> name.contains("session_" + sessionId));

        if (files == null || files.length == 0) {
            eventBus.publish(LogEvent.warning(this,
                    "No session found to delete with ID: " + sessionId, "Session"));
            return false;
        }

        boolean allDeleted = true;
        for (File file : files) {
            try {
                boolean deleted = file.delete();
                if (!deleted) {
                    allDeleted = false;
                    eventBus.publish(LogEvent.warning(this,
                            "Failed to delete session file: " + file.getName(), "Session"));
                }
            } catch (Exception e) {
                allDeleted = false;
                eventBus.publish(LogEvent.error(this,
                        "Error deleting session file: " + e.getMessage(), "Session", e));
            }
        }

        if (allDeleted) {
            eventBus.publish(LogEvent.info(this,
                    "Session deleted: " + sessionId, "Session"));
        }

        return allDeleted;
    }

    @Override
    public void close() {
        // Ensure any active session is saved
        if (isSessionActive()) {
            endCurrentSession();
        }

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        eventBus.publish(LogEvent.info(this, "Session manager closed", "Resources"));
    }
}