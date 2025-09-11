package io.github.jspinak.brobot.runner.ui.enhanced.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.ui.AutomationWindowController;

@ExtendWith(MockitoExtension.class)
class EnhancedWindowServiceTest {

    @Mock private AutomationWindowController windowController;

    private EnhancedWindowService service;

    @BeforeEach
    void setUp() {
        service = new EnhancedWindowService(windowController);
    }

    @Test
    void testInitialize() {
        // Given
        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(true)
                        .build());

        // When
        service.initialize();

        // Then
        verify(windowController).setAutoMinimizeEnabled(true);
    }

    @Test
    void testMinimizeForAutomation() {
        // Given
        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(true)
                        .minimizeDelayMs(0)
                        .build());

        // When
        service.minimizeForAutomation();

        // Then
        verify(windowController).minimizeForAutomation();
    }

    @Test
    void testMinimizeForAutomationDisabled() {
        // Given
        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(false)
                        .build());

        // When
        service.minimizeForAutomation();

        // Then
        verify(windowController, never()).minimizeForAutomation();
    }

    @Test
    void testMinimizeWithDelay() throws InterruptedException {
        // Given
        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(true)
                        .minimizeDelayMs(50)
                        .build());

        // When
        long start = System.currentTimeMillis();
        service.minimizeForAutomation();
        long elapsed = System.currentTimeMillis() - start;

        // Then
        assertTrue(elapsed >= 50);
        verify(windowController).minimizeForAutomation();
    }

    @Test
    void testRestoreAfterAutomation() {
        // Given
        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .restoreAfterAutomation(true)
                        .restoreDelayMs(0)
                        .build());

        // When
        service.restoreAfterAutomation();

        // Then
        verify(windowController).restoreAfterAutomation();
    }

    @Test
    void testRestoreAfterAutomationDisabled() {
        // Given
        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .restoreAfterAutomation(false)
                        .build());

        // When
        service.restoreAfterAutomation();

        // Then
        verify(windowController, never()).restoreAfterAutomation();
    }

    @Test
    void testIsAutoMinimizeEnabled() {
        // Given
        when(windowController.isAutoMinimizeEnabled()).thenReturn(true);

        // When
        boolean enabled = service.isAutoMinimizeEnabled();

        // Then
        assertTrue(enabled);
    }

    @Test
    void testSetAutoMinimizeEnabled() {
        // Given
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        // When
        service.setAutoMinimizeEnabled(true);

        // Then
        verify(windowController).setAutoMinimizeEnabled(true);
        assertTrue(service.getConfiguration().isAutoMinimizeEnabled());
        assertEquals("Auto-minimize enabled", logMessage.get());

        // When
        service.setAutoMinimizeEnabled(false);

        // Then
        verify(windowController).setAutoMinimizeEnabled(false);
        assertFalse(service.getConfiguration().isAutoMinimizeEnabled());
        assertEquals("Auto-minimize disabled", logMessage.get());
    }

    @Test
    void testLogWindowActions() {
        // Given
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(true)
                        .logWindowActions(true)
                        .minimizeDelayMs(0)
                        .build());

        // When
        service.minimizeForAutomation();

        // Then
        assertEquals("Minimizing window for automation", logMessage.get());
    }

    @Test
    void testLogWindowActionsDisabled() {
        // Given
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        service.setConfiguration(
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(true)
                        .logWindowActions(false)
                        .minimizeDelayMs(0)
                        .build());

        // When
        service.minimizeForAutomation();

        // Then
        assertNull(logMessage.get());
    }

    @Test
    void testConfiguration() {
        // Given
        EnhancedWindowService.WindowConfiguration config =
                EnhancedWindowService.WindowConfiguration.builder()
                        .autoMinimizeEnabled(false)
                        .restoreAfterAutomation(false)
                        .logWindowActions(false)
                        .minimizeDelayMs(100)
                        .restoreDelayMs(200)
                        .build();

        // When
        service.setConfiguration(config);

        // Then
        assertFalse(config.isAutoMinimizeEnabled());
        assertFalse(config.isRestoreAfterAutomation());
        assertFalse(config.isLogWindowActions());
        assertEquals(100, config.getMinimizeDelayMs());
        assertEquals(200, config.getRestoreDelayMs());
    }
}
