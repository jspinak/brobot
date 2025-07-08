package io.github.jspinak.brobot.runner.session.services;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for capturing and restoring application state for sessions.
 * Manages the interaction between sessions and the Brobot state system.
 */
@Slf4j
@Service
public class SessionStateService {
    
    private final EventBus eventBus;
    private final StateTransitionStore stateTransitionsRepository;
    
    @Autowired
    public SessionStateService(
            EventBus eventBus,
            StateTransitionStore stateTransitionsRepository) {
        
        this.eventBus = eventBus;
        this.stateTransitionsRepository = stateTransitionsRepository;
    }
    
    /**
     * Captures the current application state into the session.
     */
    public void captureApplicationState(Session session) {
        if (session == null) {
            log.warn("Cannot capture state for null session");
            return;
        }
        
        try {
            // Capture state transitions
            captureStateTransitions(session);
            
            // Capture active states
            captureActiveStates(session);
            
            // Add a session event for this capture
            session.addEvent(new SessionEvent(
                "STATE_CAPTURE",
                "Application state captured",
                createCaptureDetails(session)
            ));
            
            log.info("Application state captured for session: {}", session.getId());
            
        } catch (Exception e) {
            log.error("Error capturing application state", e);
            session.addEvent(new SessionEvent(
                "ERROR",
                "Failed to capture application state: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Restores the application state from a session.
     */
    public void restoreApplicationState(Session session) {
        if (session == null) {
            log.warn("Cannot restore state from null session");
            return;
        }
        
        try {
            // Clear current state
            clearCurrentState();
            
            // Restore state transitions
            restoreStateTransitions(session);
            
            // Restore active states
            restoreActiveStates(session);
            
            // Add restoration event
            session.addEvent(new SessionEvent(
                "RESTORED",
                "Session restored",
                "Application state restored from saved session"
            ));
            
            log.info("Application state restored for session: {}", session.getId());
            
        } catch (Exception e) {
            log.error("Error restoring application state", e);
            eventBus.publish(LogEvent.error(this,
                    "Error restoring application state: " + e.getMessage(), "Session", e));
            throw new RuntimeException("Failed to restore application state", e);
        }
    }
    
    /**
     * Captures state transitions from the repository.
     */
    private void captureStateTransitions(Session session) {
        List<StateTransitions> stateTransitions = 
            stateTransitionsRepository.getAllStateTransitionsAsCopy();
        
        session.setStateTransitions(stateTransitions);
        
        log.debug("Captured {} state transitions", stateTransitions.size());
        eventBus.publish(LogEvent.debug(this,
                "Captured " + stateTransitions.size() + " state transitions", "Session"));
    }
    
    /**
     * Captures currently active states.
     */
    private void captureActiveStates(Session session) {
        Set<State> activeStates = getActiveStates();
        
        if (activeStates != null && !activeStates.isEmpty()) {
            Set<Long> activeStateIds = activeStates.stream()
                .map(State::getId)
                .collect(Collectors.toSet());
            
            session.setActiveStateIds(activeStateIds);
            
            log.debug("Captured {} active states", activeStates.size());
        }
    }
    
    /**
     * Gets the currently active states.
     * This is a placeholder implementation - would need to integrate with actual Brobot state system.
     */
    private Set<State> getActiveStates() {
        // TODO: Integrate with Brobot state management system
        // This would need to access the actual state manager to get active states
        return new HashSet<>();
    }
    
    /**
     * Clears the current application state.
     */
    private void clearCurrentState() {
        // Clear state transitions repository
        stateTransitionsRepository.emptyRepos();
        
        // TODO: Clear active states in Brobot state system
        
        log.debug("Current application state cleared");
    }
    
    /**
     * Restores state transitions to the repository.
     */
    private void restoreStateTransitions(Session session) {
        List<StateTransitions> stateTransitions = session.getStateTransitions();
        
        if (stateTransitions != null && !stateTransitions.isEmpty()) {
            // Clear existing transitions
            stateTransitionsRepository.emptyRepos();
            
            // Add restored transitions
            for (StateTransitions transition : stateTransitions) {
                stateTransitionsRepository.add(transition);
            }
            
            log.info("Restored {} state transitions", stateTransitions.size());
            eventBus.publish(LogEvent.info(this,
                    "Restored " + stateTransitions.size() + " state transitions", "Session"));
        }
    }
    
    /**
     * Restores active states.
     */
    private void restoreActiveStates(Session session) {
        Optional.ofNullable(session.getActiveStateIds())
            .filter(ids -> !ids.isEmpty())
            .ifPresent(activeStateIds -> {
                // TODO: Activate the states in the Brobot system
                // This would need to interact with the state manager
                
                log.info("Restored {} active states", activeStateIds.size());
                eventBus.publish(LogEvent.info(this,
                        "Restored " + activeStateIds.size() + " active states", "Session"));
            });
    }
    
    /**
     * Creates details string for state capture event.
     */
    private String createCaptureDetails(Session session) {
        int transitionCount = session.getStateTransitions() != null ? 
            session.getStateTransitions().size() : 0;
        int activeStateCount = session.getActiveStateIds() != null ? 
            session.getActiveStateIds().size() : 0;
        
        return String.format("Transitions: %d, Active states: %d", 
            transitionCount, activeStateCount);
    }
    
    /**
     * Validates that a session has valid state data.
     */
    public boolean hasValidStateData(Session session) {
        if (session == null) {
            return false;
        }
        
        boolean hasTransitions = session.getStateTransitions() != null && 
                                !session.getStateTransitions().isEmpty();
        boolean hasActiveStates = session.getActiveStateIds() != null && 
                                 !session.getActiveStateIds().isEmpty();
        
        return hasTransitions || hasActiveStates;
    }
}