package io.github.jspinak.brobot.runner.ui.automation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;

@ExtendWith(MockitoExtension.class)
class HotkeyIntegrationServiceTest {

    @Mock private HotkeyManager hotkeyManager;

    private HotkeyIntegrationService service;

    @BeforeEach
    void setUp() {
        service = new HotkeyIntegrationService(hotkeyManager);
    }

    @Test
    void testRegisterAutomationActions() {
        // Given
        AtomicBoolean pauseCalled = new AtomicBoolean(false);
        AtomicBoolean resumeCalled = new AtomicBoolean(false);
        AtomicBoolean stopCalled = new AtomicBoolean(false);
        AtomicBoolean toggleCalled = new AtomicBoolean(false);

        Runnable pauseAction = () -> pauseCalled.set(true);
        Runnable resumeAction = () -> resumeCalled.set(true);
        Runnable stopAction = () -> stopCalled.set(true);
        Runnable toggleAction = () -> toggleCalled.set(true);

        // When
        service.registerAutomationActions(pauseAction, resumeAction, stopAction, toggleAction);

        // Then - Verify all actions were registered with HotkeyManager
        verify(hotkeyManager).registerAction(eq(HotkeyAction.PAUSE), any(Runnable.class));
        verify(hotkeyManager).registerAction(eq(HotkeyAction.RESUME), any(Runnable.class));
        verify(hotkeyManager).registerAction(eq(HotkeyAction.STOP), any(Runnable.class));
        verify(hotkeyManager).registerAction(eq(HotkeyAction.TOGGLE_PAUSE), any(Runnable.class));

        // Verify actions are stored
        assertTrue(service.isActionRegistered(HotkeyAction.PAUSE));
        assertTrue(service.isActionRegistered(HotkeyAction.RESUME));
        assertTrue(service.isActionRegistered(HotkeyAction.STOP));
        assertTrue(service.isActionRegistered(HotkeyAction.TOGGLE_PAUSE));
    }

    @Test
    void testTriggerAction() {
        // Given
        AtomicInteger callCount = new AtomicInteger(0);
        Runnable testAction = callCount::incrementAndGet;

        service.registerAutomationActions(testAction, () -> {}, () -> {}, () -> {});

        // When
        service.triggerAction(HotkeyAction.PAUSE);

        // Then
        assertEquals(1, callCount.get());
    }

    @Test
    void testTriggerUnregisteredAction() {
        // When - Trigger action that wasn't registered
        service.triggerAction(HotkeyAction.PAUSE);

        // Then - Should not throw exception
        // Just logs a warning
    }

    @Test
    void testUpdateActionHandler() {
        // Given
        AtomicInteger oldCallCount = new AtomicInteger(0);
        AtomicInteger newCallCount = new AtomicInteger(0);

        Runnable oldAction = oldCallCount::incrementAndGet;
        Runnable newAction = newCallCount::incrementAndGet;

        service.registerAutomationActions(oldAction, () -> {}, () -> {}, () -> {});

        // When - Update the pause action
        service.updateActionHandler(HotkeyAction.PAUSE, newAction);
        service.triggerAction(HotkeyAction.PAUSE);

        // Then
        assertEquals(0, oldCallCount.get());
        assertEquals(1, newCallCount.get());
        verify(hotkeyManager, times(2)).registerAction(eq(HotkeyAction.PAUSE), any(Runnable.class));
    }

    @Test
    void testErrorHandlingInAction() {
        // Given
        Runnable errorAction =
                () -> {
                    throw new RuntimeException("Test error");
                };

        // Configure to not show error dialog
        HotkeyIntegrationService.HotkeyConfiguration config =
                HotkeyIntegrationService.HotkeyConfiguration.builder()
                        .showDialogOnError(false)
                        .build();
        service.setConfiguration(config);

        service.registerAutomationActions(errorAction, () -> {}, () -> {}, () -> {});

        // When - Capture the wrapped action
        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(hotkeyManager).registerAction(eq(HotkeyAction.PAUSE), actionCaptor.capture());

        // Execute the wrapped action
        Runnable wrappedAction = actionCaptor.getValue();

        // Then - Should not throw exception
        assertDoesNotThrow(() -> wrappedAction.run());
    }

    @Test
    void testConfiguration() {
        // Given
        HotkeyIntegrationService.HotkeyConfiguration config =
                HotkeyIntegrationService.HotkeyConfiguration.builder()
                        .autoStart(false)
                        .showDialogOnError(true)
                        .customDescription(HotkeyAction.PAUSE, "Custom Pause Description")
                        .build();

        // When
        service.setConfiguration(config);

        // Then - Configuration is applied
        Map<HotkeyAction, String> registeredActions = service.getRegisteredActions();
        // Actions not registered yet, but configuration is stored
        assertTrue(registeredActions.isEmpty());
    }

    @Test
    void testGetHotkeyInfo() {
        // When
        String info = service.getHotkeyInfo();

        // Then
        assertNotNull(info);
        assertTrue(info.contains("Default hotkeys"));
        assertTrue(info.contains("Ctrl+P"));
        assertTrue(info.contains("Ctrl+R"));
        assertTrue(info.contains("Ctrl+S"));
        assertTrue(info.contains("Ctrl+Space"));
        assertTrue(info.contains("Pause automation"));
        assertTrue(info.contains("Resume automation"));
        assertTrue(info.contains("Stop all automation"));
        assertTrue(info.contains("Toggle pause/resume"));
    }

    @Test
    void testGetHotkeyHint() {
        // When
        String hint = service.getHotkeyHint();

        // Then
        assertEquals("(Ctrl+P: Pause, Ctrl+R: Resume, Ctrl+S: Stop)", hint);
    }

    @Test
    void testGetRegisteredActions() {
        // Given
        service.registerAutomationActions(() -> {}, () -> {}, () -> {}, () -> {});

        // When
        Map<HotkeyAction, String> actions = service.getRegisteredActions();

        // Then
        assertEquals(4, actions.size());
        assertEquals("Pause automation", actions.get(HotkeyAction.PAUSE));
        assertEquals("Resume automation", actions.get(HotkeyAction.RESUME));
        assertEquals("Stop all automation", actions.get(HotkeyAction.STOP));
        assertEquals("Toggle pause/resume", actions.get(HotkeyAction.TOGGLE_PAUSE));
    }

    @Test
    void testClearActions() {
        // Given
        service.registerAutomationActions(() -> {}, () -> {}, () -> {}, () -> {});
        assertTrue(service.isActionRegistered(HotkeyAction.PAUSE));

        // When
        service.clearActions();

        // Then
        assertFalse(service.isActionRegistered(HotkeyAction.PAUSE));
        assertFalse(service.isActionRegistered(HotkeyAction.RESUME));
        assertFalse(service.isActionRegistered(HotkeyAction.STOP));
        assertFalse(service.isActionRegistered(HotkeyAction.TOGGLE_PAUSE));
    }

    @Test
    void testStartStopListening() {
        // Initially not listening
        assertFalse(service.isListening());

        // Start listening
        service.startListening();
        assertTrue(service.isListening());

        // Start again (should not change state)
        service.startListening();
        assertTrue(service.isListening());

        // Stop listening
        service.stopListening();
        assertFalse(service.isListening());

        // Stop again (should not change state)
        service.stopListening();
        assertFalse(service.isListening());
    }

    @Test
    void testAutoStartConfiguration() {
        // Given - autoStart enabled by default
        AtomicBoolean actionCalled = new AtomicBoolean(false);

        // When
        service.registerAutomationActions(
                () -> actionCalled.set(true), () -> {}, () -> {}, () -> {});

        // Then - listening should have started automatically
        assertTrue(service.isListening());
    }

    @Test
    void testNoAutoStartConfiguration() {
        // Given
        HotkeyIntegrationService.HotkeyConfiguration config =
                HotkeyIntegrationService.HotkeyConfiguration.builder().autoStart(false).build();
        service.setConfiguration(config);

        // When
        service.registerAutomationActions(() -> {}, () -> {}, () -> {}, () -> {});

        // Then - listening should not start automatically
        assertFalse(service.isListening());
    }

    @Test
    void testCustomDescriptions() {
        // Given
        HotkeyIntegrationService.HotkeyConfiguration config =
                HotkeyIntegrationService.HotkeyConfiguration.builder()
                        .customDescription(HotkeyAction.PAUSE, "Pause the running task")
                        .customDescription(HotkeyAction.STOP, "Emergency stop")
                        .build();
        service.setConfiguration(config);
        service.registerAutomationActions(() -> {}, () -> {}, () -> {}, () -> {});

        // When
        Map<HotkeyAction, String> actions = service.getRegisteredActions();

        // Then
        assertEquals("Pause the running task", actions.get(HotkeyAction.PAUSE));
        assertEquals("Emergency stop", actions.get(HotkeyAction.STOP));
        assertEquals("Resume automation", actions.get(HotkeyAction.RESUME)); // Default
    }
}
