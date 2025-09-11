package io.github.jspinak.brobot.runner.session.state;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages application state capture and restoration for sessions.
 *
 * <p>This service is responsible for capturing the current application state, storing it in
 * sessions, and restoring state from saved sessions.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Service("legacySessionStateService")
public class SessionStateService implements DiagnosticCapable {

    private final StateTransitionStore stateTransitionStore;

    // State snapshots
    private final Map<String, StateSnapshot> snapshots = new ConcurrentHashMap<>();

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    @Autowired
    public SessionStateService(StateTransitionStore stateTransitionStore) {
        this.stateTransitionStore = stateTransitionStore;
    }

    /**
     * Captures the current application state.
     *
     * @return captured application state
     */
    public ApplicationState captureCurrentState() {
        log.debug("Capturing current application state");

        ApplicationState appState = new ApplicationState();
        appState.setCaptureTime(LocalDateTime.now());

        // Capture state transitions
        List<StateTransitions> transitions = new ArrayList<>();
        Set<State> activeStates = getActiveStates();

        for (State state : activeStates) {
            Optional<StateTransitions> stateTransitions = stateTransitionStore.get(state.getId());
            stateTransitions.ifPresent(transitions::add);
        }

        appState.setStateTransitions(transitions);
        appState.setActiveStateIds(
                activeStates.stream().map(State::getId).collect(Collectors.toSet()));

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] State captured - Active states: {}, Transitions: {}",
                    activeStates.size(),
                    transitions.size());
        }

        return appState;
    }

    /**
     * Restores application state from a saved state.
     *
     * @param appState the application state to restore
     */
    public void restoreState(ApplicationState appState) {
        if (appState == null) {
            log.warn("Cannot restore null application state");
            return;
        }

        log.info("Restoring application state from {}", appState.getCaptureTime());

        try {
            // Clear current state - recreate the store
            // Note: StateTransitionStore doesn't have a clear method, so we work with what's
            // available

            // Restore state transitions
            if (appState.getStateTransitions() != null) {
                for (StateTransitions transitions : appState.getStateTransitions()) {
                    stateTransitionStore.add(transitions);
                }
            }

            // Restore active states
            if (appState.getActiveStateIds() != null && !appState.getActiveStateIds().isEmpty()) {
                // Note: Actual state activation would depend on the application's state management
                log.info(
                        "Restored {} state transitions and {} active states",
                        appState.getStateTransitions() != null
                                ? appState.getStateTransitions().size()
                                : 0,
                        appState.getActiveStateIds().size());
            }

            if (diagnosticMode.get()) {
                log.info(
                        "[DIAGNOSTIC] State restored - Transitions: {}, Active states: {}",
                        appState.getStateTransitions() != null
                                ? appState.getStateTransitions().size()
                                : 0,
                        appState.getActiveStateIds() != null
                                ? appState.getActiveStateIds().size()
                                : 0);
            }

        } catch (Exception e) {
            log.error("Failed to restore application state", e);
            throw new RuntimeException("Failed to restore application state: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a snapshot of the current state.
     *
     * @param description description of the snapshot
     * @return created snapshot
     */
    public StateSnapshot createSnapshot(String description) {
        String snapshotId = UUID.randomUUID().toString();

        ApplicationState currentState = captureCurrentState();

        StateSnapshot snapshot = new StateSnapshot();
        snapshot.setId(snapshotId);
        snapshot.setDescription(description);
        snapshot.setTimestamp(LocalDateTime.now());
        snapshot.setApplicationState(currentState);

        snapshots.put(snapshotId, snapshot);

        log.info("Created state snapshot: {} - {}", snapshotId, description);

        return snapshot;
    }

    /**
     * Restores a specific snapshot.
     *
     * @param snapshotId the snapshot ID to restore
     * @return true if restored successfully
     */
    public boolean restoreSnapshot(String snapshotId) {
        StateSnapshot snapshot = snapshots.get(snapshotId);
        if (snapshot == null) {
            log.warn("Snapshot not found: {}", snapshotId);
            return false;
        }

        try {
            restoreState(snapshot.getApplicationState());
            log.info("Restored snapshot: {} - {}", snapshotId, snapshot.getDescription());
            return true;
        } catch (Exception e) {
            log.error("Failed to restore snapshot: {}", snapshotId, e);
            return false;
        }
    }

    /**
     * Captures current application state and updates the session.
     *
     * @param session the session to update with current state
     */
    public void captureState(Session session) {
        if (session == null) {
            return;
        }

        ApplicationState currentState = captureCurrentState();

        // Update session with current state
        session.setStateTransitions(currentState.getStateTransitions());
        session.setActiveStateIds(currentState.getActiveStateIds());

        // Add event
        session.addEvent(
                new SessionEvent(
                        "STATE_CAPTURE",
                        "Application state captured",
                        String.format(
                                "Active states: %d, Transitions: %d",
                                currentState.getActiveStateIds() != null
                                        ? currentState.getActiveStateIds().size()
                                        : 0,
                                currentState.getStateTransitions() != null
                                        ? currentState.getStateTransitions().size()
                                        : 0)));

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] State captured for session - Session: {}, Active states: {}",
                    session.getId(),
                    currentState.getActiveStateIds() != null
                            ? currentState.getActiveStateIds().size()
                            : 0);
        }
    }

    /**
     * Restores application state from a session.
     *
     * @param session the session to restore state from
     */
    public void restoreState(Session session) {
        if (session == null) {
            return;
        }

        log.info("Restoring state from session: {}", session.getId());

        ApplicationState appState =
                ApplicationState.builder()
                        .stateTransitions(session.getStateTransitions())
                        .activeStateIds(session.getActiveStateIds())
                        .lastModified(LocalDateTime.now())
                        .build();

        restoreState(appState);

        // Add event
        session.addEvent(
                new SessionEvent(
                        "STATE_RESTORED",
                        "Application state restored",
                        String.format(
                                "Restored %d transitions and %d active states",
                                appState.getStateTransitions() != null
                                        ? appState.getStateTransitions().size()
                                        : 0,
                                appState.getActiveStateIds() != null
                                        ? appState.getActiveStateIds().size()
                                        : 0)));
    }

    /**
     * Updates session with current state information.
     *
     * @param session the session to update
     */
    public void updateSessionState(Session session) {
        if (session == null) {
            return;
        }

        ApplicationState currentState = captureCurrentState();

        session.setStateTransitions(currentState.getStateTransitions());
        session.setActiveStateIds(currentState.getActiveStateIds());

        // Add state metadata
        session.addStateData("lastStateCapture", LocalDateTime.now());
        session.addStateData(
                "stateCount",
                currentState.getActiveStateIds() != null
                        ? currentState.getActiveStateIds().size()
                        : 0);
    }

    /**
     * Gets all available snapshots.
     *
     * @return map of snapshots
     */
    public Map<String, StateSnapshot> getAllSnapshots() {
        return new ConcurrentHashMap<>(snapshots);
    }

    /**
     * Deletes a snapshot.
     *
     * @param snapshotId the snapshot ID to delete
     * @return true if deleted
     */
    public boolean deleteSnapshot(String snapshotId) {
        return snapshots.remove(snapshotId) != null;
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("snapshotCount", snapshots.size());
        states.put("currentActiveStates", getActiveStates().size());
        states.put(
                "stateTransitionCount", stateTransitionStore.getAllStateTransitionsAsCopy().size());

        // Memory usage estimate
        long memoryUsage =
                snapshots.values().stream()
                        .mapToLong(snapshot -> estimateSnapshotSize(snapshot))
                        .sum();
        states.put("estimatedMemoryUsageKB", memoryUsage / 1024);

        return DiagnosticInfo.builder().component("SessionStateService").states(states).build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }

    /** Gets currently active states from the state transition store. */
    private Set<State> getActiveStates() {
        // This would typically interact with the actual state management system
        // For now, returning an empty set as the actual implementation would
        // depend on the state management system
        return new HashSet<>();
    }

    /** Estimates the memory size of a snapshot. */
    private long estimateSnapshotSize(StateSnapshot snapshot) {
        // Rough estimation based on content
        long size = 0;
        if (snapshot.getApplicationState() != null) {
            ApplicationState appState = snapshot.getApplicationState();
            if (appState.getStateTransitions() != null) {
                size += appState.getStateTransitions().size() * 1024; // 1KB per transition estimate
            }
            if (appState.getActiveStateIds() != null) {
                size += appState.getActiveStateIds().size() * 8; // 8 bytes per ID
            }
        }
        return size;
    }
}
