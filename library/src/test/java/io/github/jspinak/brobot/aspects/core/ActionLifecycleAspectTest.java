package io.github.jspinak.brobot.aspects.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.logging.modular.ActionLoggingService;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
public class ActionLifecycleAspectTest extends BrobotTestBase {

    private ActionLifecycleAspect aspect;

    @Mock private ActionLoggingService actionLoggingService;

    @Mock private ProceedingJoinPoint joinPoint;

    @Mock private ActionInterface mockAction;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        aspect =
                new ActionLifecycleAspect(
                        actionLoggingService,
                        0, // preActionPause
                        0, // postActionPause
                        true, // logEvents
                        false, // captureBeforeScreenshot
                        false // captureAfterScreenshot
                        );
    }

    @Test
    public void testManageActionLifecycle_SuccessfulAction() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        StateImage stateImage = new StateImage.Builder().setName("testImage").build();
        ObjectCollection objCollection =
                new ObjectCollection.Builder().withImages(stateImage).build();
        PatternFindOptions config = new PatternFindOptions.Builder().build();
        actionResult.setActionConfig(config);

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            actionResult.setSuccess(true);
                            actionResult.setDuration(Duration.ofMillis(100));
                            return actionResult;
                        });

        // Act
        Object result = aspect.manageActionLifecycle(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(actionResult, result);
        assertTrue(actionResult.isSuccess());
        verify(actionLoggingService, times(1)).logAction(actionResult);

        // Verify execution context was populated
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        assertNotNull(execContext);
        assertEquals("FIND", execContext.getActionType());
        assertNotNull(execContext.getActionId());
        assertNotNull(execContext.getStartTime());
        assertNotNull(execContext.getEndTime());
        assertTrue(execContext.isSuccess());
    }

    @Test
    public void testManageActionLifecycle_FailedAction() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        ObjectCollection objCollection = new ObjectCollection.Builder().build();
        ClickOptions config = new ClickOptions.Builder().build();
        actionResult.setActionConfig(config);
        RuntimeException exception = new RuntimeException("Test failure");

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed()).thenThrow(exception);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> aspect.manageActionLifecycle(joinPoint));

        // Verify logging was called even for failure
        verify(actionLoggingService, times(1)).logAction(actionResult);

        // Verify execution context was populated with error
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        assertNotNull(execContext);
        assertEquals("CLICK", execContext.getActionType());
        assertFalse(execContext.isSuccess());
        assertEquals(exception, execContext.getExecutionError());
        assertNotNull(execContext.getEndTime());
    }

    @Test
    public void testManageActionLifecycle_WithMultipleObjectCollections() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        StateImage stateImage1 = new StateImage.Builder().setName("image1").build();
        StateImage stateImage2 = new StateImage.Builder().setName("image2").build();
        ObjectCollection objCollection1 =
                new ObjectCollection.Builder().withImages(stateImage1).build();
        ObjectCollection objCollection2 =
                new ObjectCollection.Builder().withImages(stateImage2).build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs())
                .thenReturn(new Object[] {actionResult, objCollection1, objCollection2});
        when(joinPoint.proceed()).thenReturn(actionResult);

        // Act
        Object result = aspect.manageActionLifecycle(joinPoint);

        // Assert
        assertNotNull(result);
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        assertNotNull(execContext);
        // Should use first collection
        assertEquals(1, execContext.getTargetImages().size());
        assertEquals("image1", execContext.getTargetImages().get(0).getName());
    }

    @Test
    public void testManageActionLifecycle_WithPauses() throws Throwable {
        // Arrange
        ReflectionTestUtils.setField(aspect, "preActionPause", 10);
        ReflectionTestUtils.setField(aspect, "postActionPause", 10);

        ActionResult actionResult = new ActionResult();
        ObjectCollection objCollection = new ObjectCollection.Builder().build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed()).thenReturn(actionResult);

        long startTime = System.currentTimeMillis();

        // Act
        Object result = aspect.manageActionLifecycle(joinPoint);

        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result);
        // Should have paused at least 20ms (pre + post)
        assertTrue(endTime - startTime >= 20);
    }

    @Test
    public void testManageActionLifecycle_ExtractsStateImageMetadata() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        StateImage stateImage =
                new StateImage.Builder()
                        .setName("buttonOK")
                        .setOwnerStateName("LoginDialog")
                        .build();
        StateString stateString = new StateString.Builder().setString("Submit").build();
        StateRegion stateRegion =
                new StateRegion.Builder().setSearchRegion(new Region(0, 0, 100, 100)).build();

        ObjectCollection objCollection =
                new ObjectCollection.Builder()
                        .withImages(stateImage)
                        .withStrings(stateString)
                        .withRegions(stateRegion)
                        .build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed()).thenReturn(actionResult);

        // Act
        aspect.manageActionLifecycle(joinPoint);

        // Assert
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        assertNotNull(execContext);
        assertEquals("LoginDialog.buttonOK", execContext.getPrimaryTargetName());
        assertEquals(1, execContext.getTargetImages().size());
        assertEquals(1, execContext.getTargetStrings().size());
        assertEquals("Submit", execContext.getTargetStrings().get(0));
        assertEquals(1, execContext.getTargetRegions().size());
    }

    @Test
    public void testGetCurrentActionContext_DuringExecution() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        ObjectCollection objCollection = new ObjectCollection.Builder().build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            // During execution, check that context is available
                            Optional<ActionLifecycleAspect.ActionContext> context =
                                    aspect.getCurrentActionContext();
                            assertTrue(context.isPresent());
                            assertNotNull(context.get().getActionId());
                            assertNotNull(context.get().getStartTime());
                            return actionResult;
                        });

        // Act
        aspect.manageActionLifecycle(joinPoint);

        // Assert - After execution, context should be cleared
        Optional<ActionLifecycleAspect.ActionContext> context = aspect.getCurrentActionContext();
        assertFalse(context.isPresent());
    }

    @Test
    public void testManageActionLifecycle_NoObjectCollection() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult});
        when(joinPoint.proceed()).thenReturn(actionResult);

        // Act
        Object result = aspect.manageActionLifecycle(joinPoint);

        // Assert
        assertNotNull(result);
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        assertNotNull(execContext);
        assertTrue(execContext.getTargetImages().isEmpty());
        assertTrue(execContext.getTargetStrings().isEmpty());
        assertTrue(execContext.getTargetRegions().isEmpty());
    }

    @Test
    public void testManageActionLifecycle_ObjectCollectionAsArray() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        ObjectCollection objCollection1 = new ObjectCollection.Builder().build();
        ObjectCollection objCollection2 = new ObjectCollection.Builder().build();
        ObjectCollection[] collectionsArray = {objCollection1, objCollection2};

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, collectionsArray});
        when(joinPoint.proceed()).thenReturn(actionResult);

        // Act
        Object result = aspect.manageActionLifecycle(joinPoint);

        // Assert
        assertNotNull(result);
        verify(actionLoggingService, times(1)).logAction(actionResult);
    }

    @Test
    public void testExtractActionType_FromPatternFindOptions() {
        // Arrange
        PatternFindOptions config = new PatternFindOptions.Builder().build();

        // Act
        String actionType =
                (String)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "extractActionType", config, mockAction);

        // Assert
        assertEquals("FIND", actionType);
    }

    @Test
    public void testExtractActionType_FromClickOptions() {
        // Arrange
        ClickOptions config = new ClickOptions.Builder().build();

        // Act
        String actionType =
                (String)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "extractActionType", config, mockAction);

        // Assert
        assertEquals("CLICK", actionType);
    }

    @Test
    public void testExtractActionType_FallbackToActionClass() {
        // Test the fallback logic with a simple mock
        // Since the method uses getClass().getSimpleName(), we can test with any object
        class TestAction {
            public String getSimpleName() {
                return "TestAction";
            }
        }

        // Create a mock action for testing the fallback
        ActionInterface simpleAction = mock(ActionInterface.class);

        // Act
        String actionType =
                (String)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "extractActionType", null, simpleAction);

        // Assert - Should use the mock class name and convert to uppercase
        assertNotNull(actionType);
        assertTrue(actionType.length() > 0);
        // The result should be based on the mock's class name
        assertTrue(actionType.contains("MOCK") || actionType.length() > 3);
    }

    @Test
    public void testManageActionLifecycle_UpdatesDurationIfNotSet() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        ObjectCollection objCollection = new ObjectCollection.Builder().build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            Thread.sleep(50); // Simulate action taking some time
                            return actionResult;
                        });

        // Act
        aspect.manageActionLifecycle(joinPoint);

        // Assert
        assertNotNull(actionResult.getDuration());
        assertTrue(actionResult.getDuration().toMillis() >= 50);
    }

    @Test
    public void testManageActionLifecycle_SetsDurationWhenNotProvided() throws Throwable {
        // Arrange
        ActionResult actionResult = new ActionResult();
        // Check the initial duration state (might be null or zero duration)
        Duration initialDuration = actionResult.getDuration();
        ObjectCollection objCollection = new ObjectCollection.Builder().build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            Thread.sleep(50); // Simulate some execution time
                            return actionResult;
                        });

        // Act
        aspect.manageActionLifecycle(joinPoint);

        // Assert - The aspect should set a meaningful duration
        assertNotNull(actionResult.getDuration());
        assertTrue(actionResult.getDuration().toMillis() >= 50);
        assertTrue(actionResult.getDuration().toMillis() < 200); // Should be reasonable
    }

    @Test
    public void testManageActionLifecycle_WithScreenshotCapture() throws Throwable {
        // Arrange
        ReflectionTestUtils.setField(aspect, "captureBeforeScreenshot", true);
        ReflectionTestUtils.setField(aspect, "captureAfterScreenshot", true);

        ActionResult actionResult = new ActionResult();
        ObjectCollection objCollection = new ObjectCollection.Builder().build();

        when(joinPoint.getTarget()).thenReturn(mockAction);
        when(joinPoint.getArgs()).thenReturn(new Object[] {actionResult, objCollection});
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            actionResult.setSuccess(true);
                            return actionResult;
                        });

        // Act
        Object result = aspect.manageActionLifecycle(joinPoint);

        // Assert
        assertNotNull(result);
        assertTrue(actionResult.isSuccess());
        // Screenshots are not actually captured (just logged) but the flow should work
    }

    @Test
    public void testIsSuccessfulResult_WithActionResult() {
        // Arrange
        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);

        ActionResult failureResult = new ActionResult();
        failureResult.setSuccess(false);

        // Act & Assert
        assertTrue(
                (boolean)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "isSuccessfulResult", successResult));
        assertFalse(
                (boolean)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "isSuccessfulResult", failureResult));
    }

    @Test
    public void testIsSuccessfulResult_WithOtherTypes() {
        // Act & Assert
        assertTrue(
                (boolean)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "isSuccessfulResult", "non-null result"));
        assertFalse(
                (boolean)
                        ReflectionTestUtils.invokeMethod(
                                aspect, "isSuccessfulResult", new Object[] {null}[0]));
    }
}
