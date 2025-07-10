package io.github.jspinak.brobot.runner.session.state;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionStateServiceTest {

    private SessionStateService stateService;
    
    @Mock
    private StateTransitionStore stateTransitionStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stateService = new SessionStateService(stateTransitionStore);
    }

    @Test
    void testCaptureCurrentState_ReturnsApplicationState() {
        // Given
        List<StateTransitions> mockTransitions = Arrays.asList(
                mock(StateTransitions.class),
                mock(StateTransitions.class)
        );
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(mockTransitions);

        // When
        ApplicationState state = stateService.captureCurrentState();

        // Then
        assertThat(state).isNotNull();
        assertThat(state.getCaptureTime()).isNotNull();
        assertThat(state.getStateTransitions()).hasSize(2);
        assertThat(state.getActiveStateIds()).isNotNull();
        verify(stateTransitionStore).getAllStateTransitionsAsCopy();
    }

    @Test
    void testCaptureState_UpdatesSessionWithCurrentState() {
        // Given
        Session session = createTestSession();
        List<StateTransitions> mockTransitions = Arrays.asList(mock(StateTransitions.class));
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(mockTransitions);

        // When
        stateService.captureState(session);

        // Then
        assertThat(session.getStateTransitions()).hasSize(1);
        assertThat(session.getActiveStateIds()).isNotNull();
        assertThat(session.getEvents()).hasSize(1);
        assertThat(session.getEvents().get(0).getType()).isEqualTo("STATE_CAPTURE");
    }

    @Test
    void testCaptureState_WithNullSession_ReturnsEarly() {
        // When/Then - should not throw
        assertThatCode(() -> stateService.captureState(null))
                .doesNotThrowAnyException();
    }

    @Test
    void testRestoreState_FromApplicationState() {
        // Given
        List<StateTransitions> transitions = Arrays.asList(
                mock(StateTransitions.class),
                mock(StateTransitions.class)
        );
        Set<Long> activeStateIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        
        ApplicationState appState = ApplicationState.builder()
                .stateTransitions(transitions)
                .activeStateIds(activeStateIds)
                .lastModified(LocalDateTime.now())
                .build();

        // When
        stateService.restoreState(appState);

        // Then
        verify(stateTransitionStore).emptyRepos();
        verify(stateTransitionStore, times(2)).add(any(StateTransitions.class));
    }

    @Test
    void testRestoreState_FromSession() {
        // Given
        Session session = createTestSession();
        List<StateTransitions> transitions = Arrays.asList(mock(StateTransitions.class));
        Set<Long> activeStateIds = new HashSet<>(Arrays.asList(1L, 2L));
        
        session.setStateTransitions(transitions);
        session.setActiveStateIds(activeStateIds);

        // When
        stateService.restoreState(session);

        // Then
        verify(stateTransitionStore).emptyRepos();
        verify(stateTransitionStore).add(any(StateTransitions.class));
        assertThat(session.getEvents()).hasSize(1);
        assertThat(session.getEvents().get(0).getType()).isEqualTo("STATE_RESTORED");
    }

    @Test
    void testRestoreState_WithNullSession_ReturnsEarly() {
        // When/Then - should not throw
        assertThatCode(() -> stateService.restoreState((Session) null))
                .doesNotThrowAnyException();
        
        verify(stateTransitionStore, never()).emptyRepos();
    }

    @Test
    void testCreateSnapshot() {
        // Given
        List<StateTransitions> mockTransitions = Arrays.asList(mock(StateTransitions.class));
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(mockTransitions);

        // When
        StateSnapshot snapshot = stateService.createSnapshot("Test snapshot");

        // Then
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getId()).isNotNull();
        assertThat(snapshot.getDescription()).isEqualTo("Test snapshot");
        assertThat(snapshot.getTimestamp()).isNotNull();
        assertThat(snapshot.getApplicationState()).isNotNull();
        assertThat(snapshot.getApplicationState().getStateTransitions()).hasSize(1);
    }

    @Test
    void testRestoreSnapshot_ValidSnapshot() {
        // Given
        StateSnapshot snapshot = stateService.createSnapshot("Test snapshot");

        // When
        boolean restored = stateService.restoreSnapshot(snapshot.getId());

        // Then
        assertThat(restored).isTrue();
        verify(stateTransitionStore, atLeastOnce()).emptyRepos();
    }

    @Test
    void testRestoreSnapshot_InvalidSnapshotId() {
        // When
        boolean restored = stateService.restoreSnapshot("non-existent-id");

        // Then
        assertThat(restored).isFalse();
        verify(stateTransitionStore, never()).emptyRepos();
    }

    @Test
    void testUpdateSessionState() {
        // Given
        Session session = createTestSession();
        List<StateTransitions> mockTransitions = Arrays.asList(mock(StateTransitions.class));
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(mockTransitions);

        // When
        stateService.updateSessionState(session);

        // Then
        assertThat(session.getStateTransitions()).hasSize(1);
        assertThat(session.getStateData()).containsKey("lastStateCapture");
        assertThat(session.getStateData()).containsKey("stateCount");
    }

    @Test
    void testGetAllSnapshots() {
        // Given
        stateService.createSnapshot("Snapshot 1");
        stateService.createSnapshot("Snapshot 2");

        // When
        Map<String, StateSnapshot> snapshots = stateService.getAllSnapshots();

        // Then
        assertThat(snapshots).hasSize(2);
    }

    @Test
    void testDeleteSnapshot() {
        // Given
        StateSnapshot snapshot = stateService.createSnapshot("To be deleted");
        String snapshotId = snapshot.getId();

        // When
        boolean deleted = stateService.deleteSnapshot(snapshotId);

        // Then
        assertThat(deleted).isTrue();
        assertThat(stateService.getAllSnapshots()).doesNotContainKey(snapshotId);
    }

    @Test
    void testDeleteSnapshot_NonExistent() {
        // When
        boolean deleted = stateService.deleteSnapshot("non-existent-id");

        // Then
        assertThat(deleted).isFalse();
    }

    @Test
    void testConcurrentSnapshotOperations() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        Set<String> snapshotIds = Collections.synchronizedSet(new HashSet<>());

        // When - create snapshots concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                StateSnapshot snapshot = stateService.createSnapshot("Concurrent snapshot " + index);
                snapshotIds.add(snapshot.getId());
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertThat(stateService.getAllSnapshots()).hasSize(threadCount);
        assertThat(snapshotIds).hasSize(threadCount);
    }

    @Test
    void testDiagnosticInfo() {
        // Given
        stateService.createSnapshot("Test snapshot");
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(Arrays.asList(mock(StateTransitions.class)));

        // When
        DiagnosticInfo diagnosticInfo = stateService.getDiagnosticInfo();

        // Then
        assertThat(diagnosticInfo.getComponent()).isEqualTo("SessionStateService");
        assertThat(diagnosticInfo.getStates()).containsKeys(
                "snapshotCount", "currentActiveStates", "stateTransitionCount", "estimatedMemoryUsageKB"
        );
        assertThat(diagnosticInfo.getStates().get("snapshotCount")).isEqualTo(1);
    }

    @Test
    void testDiagnosticMode() {
        // Given
        assertThat(stateService.isDiagnosticModeEnabled()).isFalse();

        // When
        stateService.enableDiagnosticMode(true);

        // Then
        assertThat(stateService.isDiagnosticModeEnabled()).isTrue();

        // When
        stateService.enableDiagnosticMode(false);

        // Then
        assertThat(stateService.isDiagnosticModeEnabled()).isFalse();
    }

    @Test
    void testStateRestoration_WithNullTransitions() {
        // Given
        ApplicationState appState = ApplicationState.builder()
                .stateTransitions(null)
                .activeStateIds(null)
                .lastModified(LocalDateTime.now())
                .build();

        // When/Then - should handle gracefully
        assertThatCode(() -> stateService.restoreState(appState))
                .doesNotThrowAnyException();
        
        verify(stateTransitionStore).emptyRepos();
        verify(stateTransitionStore, never()).add(any());
    }

    private Session createTestSession() {
        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setProjectName("Test Project");
        session.setConfigPath("/test/config.json");
        session.setImagePath("/test/images");
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        return session;
    }
}