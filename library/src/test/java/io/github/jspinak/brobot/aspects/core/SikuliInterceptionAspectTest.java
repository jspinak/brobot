package io.github.jspinak.brobot.aspects.core;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.exception.ActionFailedException;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.FindFailed;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
public class SikuliInterceptionAspectTest extends BrobotTestBase {

    private SikuliInterceptionAspect aspect;

    @Mock
    private BrobotLogger brobotLogger;

    @Mock
    private LogBuilder logBuilder;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        aspect = new SikuliInterceptionAspect();
        ReflectionTestUtils.setField(aspect, "brobotLogger", brobotLogger);

        // Setup log builder chain - use lenient() to avoid UnnecessaryStubbingException
        lenient().when(brobotLogger.log()).thenReturn(logBuilder);
        lenient().when(logBuilder.type(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.level(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.action(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.metadata(any(), any())).thenReturn(logBuilder);
        lenient().when(logBuilder.success(anyBoolean())).thenReturn(logBuilder);
        lenient().when(logBuilder.duration(anyLong())).thenReturn(logBuilder);
        lenient().when(logBuilder.error(any())).thenReturn(logBuilder);

        // Mock the void log() method
        lenient().doNothing().when(logBuilder).log();
    }

    @Test
    public void testInterceptSikuliCall_SuccessInNormalMode() throws Throwable {
        // Arrange
        FrameworkSettings.mock = false;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "image.png" });
        Object expectedResult = new Object();
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertEquals(expectedResult, result);
        verify(logBuilder, atLeastOnce()).log();

        // Verify metrics were updated
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metrics = aspect.getMetrics();
        assertTrue(metrics.containsKey("find()"));
        SikuliInterceptionAspect.OperationMetrics metric = metrics.get("find()");
        assertEquals(1, metric.getTotalCalls());
        assertEquals(1, metric.getSuccessfulCalls());
        assertEquals(0, metric.getFailedCalls());
    }

    @Test
    public void testInterceptSikuliCall_MockMode() throws Throwable {
        // Arrange
        FrameworkSettings.mock = true;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "image.png" });

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, never()).proceed(); // Should not proceed in mock mode
        verify(logBuilder, atLeastOnce()).metadata("mock", true);
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testInterceptSikuliCall_FindFailedException() throws Throwable {
        // Arrange
        FrameworkSettings.mock = false;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "missing.png" });
        FindFailed exception = new FindFailed("Image not found");
        when(joinPoint.proceed()).thenThrow(exception);

        // Act & Assert
        ActionFailedException thrown = assertThrows(ActionFailedException.class,
                () -> aspect.interceptSikuliCall(joinPoint));
        assertTrue(thrown.getMessage().contains("Sikuli find operation failed"));
        assertEquals(exception, thrown.getCause());

        // Verify error logging
        verify(logBuilder, atLeastOnce()).error(exception);
        verify(logBuilder, atLeastOnce()).success(false);
        verify(logBuilder, atLeastOnce()).log();

        // Verify metrics were updated
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metrics = aspect.getMetrics();
        assertTrue(metrics.containsKey("find()"));
        SikuliInterceptionAspect.OperationMetrics metric = metrics.get("find()");
        assertEquals(1, metric.getTotalCalls());
        assertEquals(0, metric.getSuccessfulCalls());
        assertEquals(1, metric.getFailedCalls());
    }

    @Test
    public void testInterceptSikuliCall_GeneralException() throws Throwable {
        // Arrange
        FrameworkSettings.mock = false;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(signature.getName()).thenReturn("click");
        when(joinPoint.getArgs()).thenReturn(new Object[] { 100, 200 });
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(joinPoint.proceed()).thenThrow(exception);

        // Act & Assert
        ActionFailedException thrown = assertThrows(ActionFailedException.class,
                () -> aspect.interceptSikuliCall(joinPoint));
        assertTrue(thrown.getMessage().contains("Sikuli operation failed"));
        assertEquals(exception, thrown.getCause());

        // Verify error logging
        verify(logBuilder, atLeastOnce()).error(exception);
        verify(logBuilder, atLeastOnce()).success(false);
        verify(logBuilder, atLeastOnce()).metadata("errorType", "RuntimeException");
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testMockProvider_FindOperations() throws Throwable {
        // Arrange
        FrameworkSettings.mock = true;

        // Test find
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "image.png" });

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("MockMatch"));
    }

    @Test
    public void testMockProvider_ClickOperations() throws Throwable {
        // Arrange
        FrameworkSettings.mock = true;

        // Test click
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(signature.getName()).thenReturn("click");
        when(joinPoint.getArgs()).thenReturn(new Object[] { 100, 200 });

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(1, result); // Success code
    }

    @Test
    public void testMockProvider_TypeOperations() throws Throwable {
        // Arrange
        FrameworkSettings.mock = true;

        // Test type
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("type()");
        when(signature.getName()).thenReturn("type");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "Hello World" });

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(1, result); // Success code
    }

    @Test
    public void testMockProvider_GetScreen() throws Throwable {
        // Arrange
        FrameworkSettings.mock = true;

        // Test getScreen
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("getScreen()");
        when(signature.getName()).thenReturn("getScreen");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("MockScreen"));
    }

    @Test
    public void testSanitizeArgs_WithImagePath() {
        // Arrange
        Object[] args = new Object[] { "/path/to/image.png", 100, null };

        // Act - Pass the array as a single parameter
        String sanitized = (String) ReflectionTestUtils.invokeMethod(
                aspect, "sanitizeArgs", (Object) args);

        // Assert
        assertNotNull(sanitized);
        assertTrue(sanitized.contains("image.png"));
        assertFalse(sanitized.contains("/path/to/"));
        assertTrue(sanitized.contains("100"));
        assertTrue(sanitized.contains("null"));
    }

    @Test
    public void testExtractImagePath() {
        // Arrange
        Object[] argsWithPng = new Object[] { "test.png", 100 };
        Object[] argsWithJpg = new Object[] { 100, "photo.jpg" };
        Object[] argsWithoutImage = new Object[] { 100, 200 };

        // Act & Assert
        assertEquals("test.png", ReflectionTestUtils.invokeMethod(
                aspect, "extractImagePath", (Object) argsWithPng));
        assertEquals("photo.jpg", ReflectionTestUtils.invokeMethod(
                aspect, "extractImagePath", (Object) argsWithJpg));
        assertNull(ReflectionTestUtils.invokeMethod(
                aspect, "extractImagePath", (Object) argsWithoutImage));
    }

    @Test
    public void testOperationMetrics() {
        // Arrange
        SikuliInterceptionAspect.OperationMetrics metrics = new SikuliInterceptionAspect.OperationMetrics("testOp");

        // Act
        metrics.recordOperation(true, 100);
        metrics.recordOperation(true, 200);
        metrics.recordOperation(false, 150);
        metrics.recordOperation(true, 50);

        // Assert
        assertEquals("testOp", metrics.getOperation());
        assertEquals(4, metrics.getTotalCalls());
        assertEquals(3, metrics.getSuccessfulCalls());
        assertEquals(1, metrics.getFailedCalls());
        assertEquals(500, metrics.getTotalDuration());
        assertEquals(50, metrics.getMinDuration());
        assertEquals(200, metrics.getMaxDuration());
        assertEquals(75.0, metrics.getSuccessRate(), 0.01);
        assertEquals(125.0, metrics.getAverageDuration(), 0.01);
    }

    @Test
    public void testResetMetrics() throws Throwable {
        // Arrange
        FrameworkSettings.mock = false;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "image.png" });
        when(joinPoint.proceed()).thenReturn(new Object());

        // Act - Record some operations
        aspect.interceptSikuliCall(joinPoint);
        aspect.interceptSikuliCall(joinPoint);

        // Verify metrics exist
        assertFalse(aspect.getMetrics().isEmpty());

        // Reset metrics
        aspect.resetMetrics();

        // Assert
        assertTrue(aspect.getMetrics().isEmpty());
    }

    @Test
    public void testLogOperationStart() throws Throwable {
        // Arrange
        FrameworkSettings.mock = false;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("exists()");
        when(signature.getName()).thenReturn("exists");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "button.png", 5.0 });
        when(joinPoint.proceed()).thenReturn(new Object());

        // Act
        aspect.interceptSikuliCall(joinPoint);

        // Assert - Verify logging calls
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(logBuilder, atLeastOnce()).action(actionCaptor.capture());

        boolean hasStartLog = actionCaptor.getAllValues().stream()
                .anyMatch(action -> action.equals("SIKULI_EXISTS"));
        assertTrue(hasStartLog);

        verify(logBuilder, atLeastOnce()).metadata("thread", Thread.currentThread().getName());
    }

    @Test
    public void testLogOperationSuccess() throws Throwable {
        // Arrange
        FrameworkSettings.mock = false;
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(signature.getName()).thenReturn("click");
        when(joinPoint.getArgs()).thenReturn(new Object[] { 100, 200 });
        String mockResult = "ClickResult";
        when(joinPoint.proceed()).thenReturn(mockResult);

        // Act
        aspect.interceptSikuliCall(joinPoint);

        // Assert
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(logBuilder, atLeastOnce()).action(actionCaptor.capture());

        boolean hasSuccessLog = actionCaptor.getAllValues().stream()
                .anyMatch(action -> action.equals("SIKULI_CLICK_SUCCESS"));
        assertTrue(hasSuccessLog);

        verify(logBuilder, atLeastOnce()).success(true);
        verify(logBuilder, atLeastOnce()).metadata("resultType", "String");
    }

    @Test
    public void testOperationMetrics_EmptyInitialState() {
        // Arrange
        SikuliInterceptionAspect.OperationMetrics metrics = new SikuliInterceptionAspect.OperationMetrics("emptyOp");

        // Assert - Test edge cases for empty metrics
        assertEquals(0, metrics.getTotalCalls());
        assertEquals(0, metrics.getSuccessfulCalls());
        assertEquals(0, metrics.getFailedCalls());
        assertEquals(0, metrics.getTotalDuration());
        assertEquals(0, metrics.getMinDuration());
        assertEquals(0, metrics.getMaxDuration());
        assertEquals(0.0, metrics.getSuccessRate(), 0.01);
        assertEquals(0.0, metrics.getAverageDuration(), 0.01);
    }
}