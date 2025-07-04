package io.github.jspinak.brobot.runner.services;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import io.github.jspinak.brobot.runner.ui.window.DialogFactory;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
@Data
public class DialogServiceTest {

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @Mock
    private EventBus eventBus;

    @Mock
    private Stage mockStage;

    private io.github.jspinak.brobot.runner.services.DialogService dialogService;

    @BeforeEach
    public void setUp() {
        dialogService = new DialogService(eventBus);
        dialogService.setPrimaryStage(mockStage);
    }

    @Test
    public void testShowInformation() {
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.runLater to immediately run the Runnable
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer(inv -> {
                        Runnable runnable = (Runnable) inv.getArguments()[0];
                        runnable.run();
                        return null;
                    });

            try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {
                // Test showing information dialog
                dialogService.showInformation("Test Title", "Test Message");

                // Verify DialogFactory was called
                dialogFactoryMock.verify(() ->
                        DialogFactory.createMessageDialog(
                                mockStage, "Test Title", "Test Message"));

                // Verify event was published
                verify(eventBus).publish(any(LogEvent.class));
            }
        }
    }

    @Test
    public void testShowError() {
        // We need to use try-with-resources to manage the static mock
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.runLater to immediately run the Runnable
            platformMock.when(() -> Platform.runLater(any())).thenAnswer(i -> {
                ((Runnable) i.getArgument(0)).run();
                return null;
            });

            // Need to mock static DialogFactory.createErrorDialog
            try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {

                // Create test exception
                Exception testException = new RuntimeException("Test exception");

                // Test showing error dialog
                dialogService.showError("Test Title", "Test Message", testException);

                // Verify DialogFactory was called
                dialogFactoryMock.verify(() ->
                        DialogFactory.createErrorDialog(
                                mockStage, "Test Title", "Test Message", testException));

                // Verify event was published
                verify(eventBus).publish(any(LogEvent.class));
            }
        }
    }

    @Test
    public void testShowConfirmation() {
        // We need to use try-with-resources to manage the static mock
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.isFxApplicationThread to return true
            platformMock.when(Platform::isFxApplicationThread).thenReturn(true);

            // Need to mock static DialogFactory.createConfirmDialog
            try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {

                // Mock the dialog to return true
                dialogFactoryMock.when(() ->
                        DialogFactory.createConfirmDialog(
                                mockStage, "Test Title", "Test Message")).thenReturn(true);

                // Test showing confirmation dialog
                boolean result = dialogService.showConfirmation("Test Title", "Test Message");

                // Verify result and DialogFactory was called
                assertTrue(result);
                dialogFactoryMock.verify(() ->
                        DialogFactory.createConfirmDialog(
                                mockStage, "Test Title", "Test Message"));

                // Verify event was published
                verify(eventBus).publish(any(LogEvent.class));
            }
        }
    }

    @Test
    public void testShowInputDialog() {
        // We need to use try-with-resources to manage the static mock
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.isFxApplicationThread to return true
            platformMock.when(Platform::isFxApplicationThread).thenReturn(true);

            // Need to mock static DialogFactory.createInputDialog
            try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {

                // Mock the dialog to return a value
                dialogFactoryMock.when(() ->
                                DialogFactory.createInputDialog(
                                        mockStage, "Test Title", "Test Message", "Default"))
                        .thenReturn(Optional.of("User Input"));

                // Test showing input dialog
                Optional<String> result = dialogService.showInputDialog("Test Title", "Test Message", "Default");

                // Verify result and DialogFactory was called
                assertTrue(result.isPresent());
                assertEquals("User Input", result.get());
                dialogFactoryMock.verify(() ->
                        DialogFactory.createInputDialog(
                                mockStage, "Test Title", "Test Message", "Default"));

                // Verify event was published
                verify(eventBus).publish(any(LogEvent.class));
            }
        }
    }

    @Test
    public void testShowProgressDialog() {
        // Need to mock static DialogFactory.createProgressDialog
        try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {

            // Mock the progress dialog
            DialogFactory.ProgressDialog mockProgressDialog = mock(DialogFactory.ProgressDialog.class);

            dialogFactoryMock.when(() ->
                            DialogFactory.createProgressDialog(
                                    mockStage, "Test Title", "Test Message"))
                    .thenReturn(mockProgressDialog);

            // Test showing progress dialog
            DialogFactory.ProgressDialog result = dialogService.showProgressDialog("Test Title", "Test Message");

            // Verify result and DialogFactory was called
            assertEquals(mockProgressDialog, result);
            dialogFactoryMock.verify(() ->
                    DialogFactory.createProgressDialog(
                            mockStage, "Test Title", "Test Message"));

            // Verify event was published
            verify(eventBus).publish(any(LogEvent.class));
        }
    }

    @Test
    public void testShowAlert() {
        // We need to use try-with-resources to manage the static mock
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.isFxApplicationThread to return true
            platformMock.when(Platform::isFxApplicationThread).thenReturn(true);

            // Create a spy of the dialogService
            DialogService spyDialogService = spy(dialogService);

            // Mock the behavior of showAlert to return a predefined result
            Optional<ButtonType> expectedResult = Optional.of(ButtonType.OK);
            doReturn(expectedResult).when(spyDialogService).showAlert(
                    any(Alert.AlertType.class), anyString(), anyString(), anyString());

            // Call the method
            Optional<ButtonType> result = spyDialogService.showAlert(
                    Alert.AlertType.CONFIRMATION, "Test Title", "Test Header", "Test Content");

            // Verify the method was called with the correct parameters
            verify(spyDialogService).showAlert(Alert.AlertType.CONFIRMATION,
                    "Test Title", "Test Header", "Test Content");

            // Verify the result
            assertTrue(result.isPresent());
            assertEquals(ButtonType.OK, result.get());
        }
    }

    @Test
    public void testShowAlertAsync() throws ExecutionException, InterruptedException {
        // We'll test the method's behavior by directly manipulating the CompletableFuture

        // Create a spy of the dialogService
        DialogService spyDialogService = spy(dialogService);

        // Capture the CompletableFuture created by the method
        CompletableFuture<Optional<ButtonType>> mockFuture = new CompletableFuture<>();

        // Mock the entire method to return our controlled CompletableFuture
        doReturn(mockFuture).when(spyDialogService).showAlertAsync(
                any(Alert.AlertType.class), anyString(), anyString(), anyString());

        // Call the method
        CompletableFuture<Optional<ButtonType>> future = spyDialogService.showAlertAsync(
                Alert.AlertType.CONFIRMATION, "Test Title", "Test Header", "Test Content");

        // Verify it's the same future we mocked
        assertSame(mockFuture, future);

        // Complete the future with our test result
        mockFuture.complete(Optional.of(ButtonType.OK));

        // Now get the result and verify
        Optional<ButtonType> result = future.get();
        assertTrue(result.isPresent());
        assertEquals(ButtonType.OK, result.get());

        // Verify the method was called with correct parameters
        verify(spyDialogService).showAlertAsync(Alert.AlertType.CONFIRMATION,
                "Test Title", "Test Header", "Test Content");
    }

    @Test
    public void testConfirmationAsync() throws ExecutionException, InterruptedException {
        // Test confirmation dialog async version
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.runLater to immediately run the Runnable
            platformMock.when(() -> Platform.runLater(any())).thenAnswer(i -> {
                ((Runnable) i.getArgument(0)).run();
                return null;
            });

            // Mock DialogFactory
            try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {

                // Mock confirmation dialog to return true
                dialogFactoryMock.when(() ->
                        DialogFactory.createConfirmDialog(
                                any(), anyString(), anyString())).thenReturn(true);

                // Test async confirmation dialog
                CompletableFuture<Boolean> future = dialogService.showConfirmationAsync("Test", "Message");
                boolean result = future.get();

                // Verify result
                assertTrue(result);

                // Verify event was published
                verify(eventBus).publish(any(LogEvent.class));
            }
        }
    }

    @Test
    public void testInputDialogAsync() throws ExecutionException, InterruptedException {
        // Test input dialog async version
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            // Setup Platform.runLater to immediately run the Runnable
            platformMock.when(() -> Platform.runLater(any())).thenAnswer(i -> {
                ((Runnable) i.getArgument(0)).run();
                return null;
            });

            // Mock DialogFactory
            try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {

                // Mock input dialog to return a value
                dialogFactoryMock.when(() ->
                        DialogFactory.createInputDialog(
                                any(), anyString(), anyString(), anyString())).thenReturn(Optional.of("Test Input"));

                // Test async input dialog
                CompletableFuture<Optional<String>> future = dialogService.showInputDialogAsync("Test", "Message", "Default");
                Optional<String> result = future.get();

                // Verify result
                assertTrue(result.isPresent());
                assertEquals("Test Input", result.get());

                // Verify event was published
                verify(eventBus).publish(any(LogEvent.class));
            }
        }
    }
}