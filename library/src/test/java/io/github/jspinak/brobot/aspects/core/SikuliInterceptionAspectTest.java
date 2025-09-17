package io.github.jspinak.brobot.aspects.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.FindFailed;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.exception.ActionFailedException;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class SikuliInterceptionAspectTest extends BrobotTestBase {

    private SikuliInterceptionAspect aspect;

    @Mock private BrobotProperties brobotProperties;
    @Mock private BrobotProperties.Core mockCore;
    @Mock private BrobotLogger brobotLogger;

    @Mock private LogBuilder logBuilder;

    @Mock private ProceedingJoinPoint joinPoint;

    @Mock private Signature signature;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Setup BrobotProperties mock with lenient to avoid issues in tests that don't check mock mode
        lenient().when(brobotProperties.getCore()).thenReturn(mockCore);
        lenient().when(mockCore.isMock()).thenReturn(false); // Default to normal mode

        aspect = new SikuliInterceptionAspect(brobotProperties, brobotLogger);
    }

    private void setupLoggingMocks() {
        // Setup log builder chain using lenient to avoid unnecessary stubbing issues
        lenient().when(brobotLogger.log()).thenReturn(logBuilder);
        lenient().when(logBuilder.type(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.level(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.action(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.metadata(any(), any())).thenReturn(logBuilder);
        lenient().when(logBuilder.success(anyBoolean())).thenReturn(logBuilder);
        lenient().when(logBuilder.duration(anyLong())).thenReturn(logBuilder);
        lenient().when(logBuilder.error(any())).thenReturn(logBuilder);
        lenient().doNothing().when(logBuilder).log();
    }

    private void setMockModeEnabled() {
        when(mockCore.isMock()).thenReturn(true);
    }

    @Test
    public void testInterceptSikuliCall_SuccessInNormalMode() throws Throwable {
        // Arrange
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"image.png"});
        Object expectedResult = new Object();
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertEquals(expectedResult, result);
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testInterceptSikuliCall_MockMode() throws Throwable {
        // Arrange
        setupLoggingMocks();
        setMockModeEnabled();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"image.png"});

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
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"missing.png"});
        FindFailed exception = new FindFailed("Image not found");
        when(joinPoint.proceed()).thenThrow(exception);

        // Act & Assert
        ActionFailedException thrown =
                assertThrows(
                        ActionFailedException.class, () -> aspect.interceptSikuliCall(joinPoint));
        assertTrue(thrown.getMessage().contains("Sikuli find operation failed"));
        assertEquals(exception, thrown.getCause());

        // Verify error logging
        verify(logBuilder, atLeastOnce()).error(exception);
        verify(logBuilder, atLeastOnce()).success(false);
        verify(logBuilder, atLeastOnce()).log();

        // Verify metrics were updated
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metrics =
                aspect.getMetrics();
        assertTrue(metrics.containsKey("find()"));
        SikuliInterceptionAspect.OperationMetrics metric = metrics.get("find()");
        assertEquals(1, metric.getTotalCalls());
        assertEquals(0, metric.getSuccessfulCalls());
        assertEquals(1, metric.getFailedCalls());
    }

    @Test
    public void testInterceptSikuliCall_GeneralException() throws Throwable {
        // Arrange
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(signature.getName()).thenReturn("click");
        when(joinPoint.getArgs()).thenReturn(new Object[] {100, 200});
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(joinPoint.proceed()).thenThrow(exception);

        // Act & Assert
        ActionFailedException thrown =
                assertThrows(
                        ActionFailedException.class, () -> aspect.interceptSikuliCall(joinPoint));
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
        setupLoggingMocks();
        setMockModeEnabled();

        // Test find
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(signature.getName()).thenReturn("find");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"image.png"});

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("MockMatch"));
    }

    @Test
    public void testMockProvider_ClickOperations() throws Throwable {
        // Arrange
        setupLoggingMocks();
        setMockModeEnabled();

        // Test click
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(signature.getName()).thenReturn("click");
        when(joinPoint.getArgs()).thenReturn(new Object[] {100, 200});

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(1, result); // Success code
    }

    @Test
    public void testMockProvider_TypeOperations() throws Throwable {
        // Arrange
        setupLoggingMocks();
        setMockModeEnabled();

        // Test type
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("type()");
        when(signature.getName()).thenReturn("type");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"Hello World"});

        // Act
        Object result = aspect.interceptSikuliCall(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(1, result); // Success code
    }

    @Test
    public void testMockProvider_GetScreen() throws Throwable {
        // Arrange
        setupLoggingMocks();
        setMockModeEnabled();

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
    public void testLogOperationStart() {
        // Test that logging works through the public interface
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testOp");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"arg1"});

        // This tests the logging indirectly through the aspect
        assertDoesNotThrow(() -> {
            // The logOperationStart method is private and called internally
            // We test it by verifying it doesn't throw when called internally
            ReflectionTestUtils.invokeMethod(
                    aspect, "logOperationStart", joinPoint);
        });
    }

    @Test
    public void testLogOperationSuccess() {
        // Test that logging works through the public interface
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testOp");

        // This tests the logging indirectly through the aspect
        assertDoesNotThrow(() -> {
            // The logOperationSuccess method is private and called internally
            // We test it by verifying it doesn't throw when called internally
            ReflectionTestUtils.invokeMethod(
                    aspect, "logOperationSuccess", joinPoint, "result", 100L);
        });
    }

    @Test
    public void testResetMetrics() {
        // First add a metric by performing an operation
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("test()");
        when(signature.getName()).thenReturn("test");
        when(joinPoint.getArgs()).thenReturn(new Object[] {});

        try {
            when(joinPoint.proceed()).thenReturn("success");
            aspect.interceptSikuliCall(joinPoint);
        } catch (Throwable e) {
            // Ignore
        }

        // Verify metrics has data
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metricsBefore = aspect.getMetrics();
        assertFalse(metricsBefore.isEmpty());

        // Reset metrics
        aspect.resetMetrics();

        // Verify metrics is now empty
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metricsAfter = aspect.getMetrics();
        assertTrue(metricsAfter.isEmpty());
    }

    @Test
    public void testOperationMetrics() throws Throwable {
        // Arrange
        setupLoggingMocks();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("hover()");
        when(signature.getName()).thenReturn("hover");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"image.png"});
        when(joinPoint.proceed()).thenReturn("success");

        // Act - Multiple calls to test metrics
        aspect.interceptSikuliCall(joinPoint);
        aspect.interceptSikuliCall(joinPoint);

        // Simulate a failure
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test error"));
        try {
            aspect.interceptSikuliCall(joinPoint);
        } catch (ActionFailedException e) {
            // Expected
        }

        // Assert
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metrics =
                aspect.getMetrics();
        SikuliInterceptionAspect.OperationMetrics metric = metrics.get("hover()");
        assertNotNull(metric);
        assertEquals(3, metric.getTotalCalls());
        assertEquals(2, metric.getSuccessfulCalls());
        assertEquals(1, metric.getFailedCalls());
        // Total time tracking would require accessing internal state
    }

    @Test
    public void testOperationMetrics_EmptyInitialState() {
        // Test that metrics start empty
        ConcurrentHashMap<String, SikuliInterceptionAspect.OperationMetrics> metrics =
                aspect.getMetrics();
        assertTrue(metrics.isEmpty());
    }

    @Test
    public void testSanitizeArgs_WithImagePath() {
        // Test internal method for sanitizing arguments
        Object[] args = {"path/to/image.png", 100, "text"};
        String sanitized = (String) ReflectionTestUtils.invokeMethod(aspect, "sanitizeArgs", (Object) args);
        assertNotNull(sanitized);
        assertTrue(sanitized.contains("image.png"));
    }

    @Test
    public void testExtractImagePath() {
        // Test internal method for extracting image path
        // The method returns the full path, not just the filename
        Object[] args = {"/full/path/to/pattern.png", 123};
        String path = (String) ReflectionTestUtils.invokeMethod(aspect, "extractImagePath", (Object) args);
        assertEquals("/full/path/to/pattern.png", path);

        // Test with no path
        args = new Object[] {456, "text"};
        path = (String) ReflectionTestUtils.invokeMethod(aspect, "extractImagePath", (Object) args);
        assertNull(path);
    }
}