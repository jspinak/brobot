package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.events.*;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.ui.dialogs.ErrorDialog;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UIEventHandlerTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private AutomationPanel automationPanel;

    @Mock
    private ConfigurationPanel configurationPanel;

    private UIEventHandler uiEventHandler;

    private MockedStatic<AutomationPanel> automationPanelMockedStatic;
    private MockedStatic<ConfigurationPanel> configurationPanelMockedStatic;
    private MockedStatic<Platform> platformMockedStatic;

    @BeforeEach
    void setUp() {
        // Setup mocked statics - these will be closed in tearDown
        automationPanelMockedStatic = mockStatic(AutomationPanel.class);
        configurationPanelMockedStatic = mockStatic(ConfigurationPanel.class);
        platformMockedStatic = mockStatic(Platform.class);

        // Configure the static mocks
        automationPanelMockedStatic.when(AutomationPanel::getInstance)
                .thenReturn(Optional.of(automationPanel));

        configurationPanelMockedStatic.when(ConfigurationPanel::getInstance)
                .thenReturn(Optional.of(configurationPanel));

        // Mock Platform.runLater to run code immediately
        platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable runnable = invocation.getArgument(0);
                    runnable.run();
                    return null;
                });

        // Create and initialize the handler
        uiEventHandler = new UIEventHandler(eventBus);
        uiEventHandler.initialize();
    }

    @AfterEach
    void tearDown() {
        // Close the static mocks to avoid leakage
        automationPanelMockedStatic.close();
        configurationPanelMockedStatic.close();
        platformMockedStatic.close();

        // Clean up the handler
        if (uiEventHandler != null) {
            uiEventHandler.cleanup();
        }
    }

    @Test
    void initialize_ShouldSubscribeToEvents() {
        // Verify that event subscriptions were made
        verify(eventBus, atLeastOnce()).subscribe(any(BrobotEvent.EventType.class), any());
    }

    @Test
    void handleExecutionStarted_ShouldUpdateUI() {
        // Arrange
        ExecutionStatus status = new ExecutionStatus();
        ExecutionStatusEvent event = ExecutionStatusEvent.started(this, status, "Test message");

        // Capture the event handler that was registered for EXECUTION_STARTED
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.EXECUTION_STARTED), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Act - invoke the handler with our test event
        handler.accept(event);

        // Assert
        verify(automationPanel).setStatusMessage(anyString());
        verify(automationPanel).setProgressValue(anyDouble());
        verify(automationPanel).log(anyString());
    }

    @Test
    void handleExecutionCompleted_ShouldUpdateUI() {
        // Arrange
        ExecutionStatus status = new ExecutionStatus();
        ExecutionStatusEvent event = ExecutionStatusEvent.completed(this, status, "Test message");

        // Capture the event handler that was registered for EXECUTION_COMPLETED
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.EXECUTION_COMPLETED), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Act - invoke the handler with our test event
        handler.accept(event);

        // Assert
        verify(automationPanel).setStatusMessage(anyString());
        verify(automationPanel).setProgressValue(1.0);
        verify(automationPanel).log(anyString());
        verify(automationPanel).updateButtonStates(false);
    }

    @Test
    void handleLogMessage_ShouldLogToAutomationPanel() {
        // Arrange
        LogEvent event = LogEvent.info(this, "Test log message", "Test");

        // Capture the event handler that was registered for LOG_MESSAGE
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.LOG_MESSAGE), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Act - invoke the handler with our test event
        handler.accept(event);

        // Assert
        verify(automationPanel).log(event.getMessage());
    }

    @Test
    void handleLogWarning_ShouldLogWarningToAutomationPanel() {
        // Arrange
        LogEvent event = LogEvent.warning(this, "Test warning message", "Test");

        // Capture the event handler that was registered for LOG_WARNING
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.LOG_WARNING), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Act - invoke the handler with our test event
        handler.accept(event);

        // Assert
        verify(automationPanel).log(contains("WARNING:"));
    }

    @Test
    void handleConfigLoaded_ShouldUpdateUI() {
        // Arrange
        ConfigurationEvent event = ConfigurationEvent.loaded(this, "test-config", "Test details");

        // Capture the event handler that was registered for CONFIG_LOADED
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.CONFIG_LOADED), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Act - invoke the handler with our test event
        handler.accept(event);

        // Assert
        verify(configurationPanel).updateStatus(anyString());
        verify(automationPanel).log(anyString());
        verify(automationPanel).refreshAutomationButtons();
    }

    @Test
    void handleConfigLoadingFailed_ShouldUpdateUIAndShowErrorDialog() {
        // Arrange
        ConfigurationEvent event = ConfigurationEvent.loadingFailed(this, "test-config",
                "Test error details", new RuntimeException("Test exception"));

        // Capture the event handler that was registered for CONFIG_LOADING_FAILED
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.CONFIG_LOADING_FAILED), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Mock the ErrorDialog.show method
        try (MockedStatic<ErrorDialog> errorDialogMockedStatic = mockStatic(ErrorDialog.class)) {
            // Act - invoke the handler with our test event
            handler.accept(event);

            // Assert
            verify(configurationPanel).updateStatus(anyString(), eq(true));
            errorDialogMockedStatic.verify(() ->
                    ErrorDialog.show(anyString(), anyString(), anyString()), times(1));
        }
    }

    @Test
    void handleErrorOccurred_WithHighSeverity_ShouldShowErrorDialog() {
        // Arrange
        ErrorEvent event = ErrorEvent.high(this, "Test error message",
                new RuntimeException("Test exception"), "TestComponent");

        // Capture the event handler that was registered for ERROR_OCCURRED
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.ERROR_OCCURRED), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Mock the ErrorDialog.show method
        try (MockedStatic<ErrorDialog> errorDialogMockedStatic = mockStatic(ErrorDialog.class)) {
            // Act - invoke the handler with our test event
            handler.accept(event);

            // Assert
            verify(automationPanel).log(contains("ERROR:"));
            errorDialogMockedStatic.verify(() ->
                    ErrorDialog.show(anyString(), anyString(), anyString()), times(1));
        }
    }

    @Test
    void handleErrorOccurred_WithLowSeverity_ShouldNotShowErrorDialog() {
        // Arrange
        ErrorEvent event = ErrorEvent.low(this, "Test error message",
                new RuntimeException("Test exception"), "TestComponent");

        // Capture the event handler that was registered for ERROR_OCCURRED
        ArgumentCaptor<Consumer<BrobotEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, atLeastOnce()).subscribe(eq(BrobotEvent.EventType.ERROR_OCCURRED), handlerCaptor.capture());
        Consumer<BrobotEvent> handler = handlerCaptor.getValue();

        // Mock the ErrorDialog.show method
        try (MockedStatic<ErrorDialog> errorDialogMockedStatic = mockStatic(ErrorDialog.class)) {
            // Act - invoke the handler with our test event
            handler.accept(event);

            // Assert
            verify(automationPanel, never()).log(anyString());
            errorDialogMockedStatic.verify(() ->
                    ErrorDialog.show(anyString(), anyString(), anyString()), never());
        }
    }

    @Test
    void cleanup_ShouldUnsubscribeFromEvents() {
        // Act
        uiEventHandler.cleanup();

        // Assert
        verify(eventBus, atLeastOnce()).unsubscribe(any(BrobotEvent.EventType.class), any());
    }
}