package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.basic.wait.WaitVanish;
import io.github.jspinak.brobot.action.composite.drag.Drag;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.config.MockModeManager;
import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test configuration that provides all required beans for Brobot testing.
 * This configuration ensures that Spring context can be properly initialized for tests
 * without requiring actual GUI resources or screen capture capabilities.
 */
@TestConfiguration
public class BrobotTestConfiguration {
    
    /**
     * Mock Action bean for testing.
     */
    @Bean
    @Primary
    public Action mockAction() {
        Action action = mock(Action.class);
        
        // Configure default behavior with mock matches
        io.github.jspinak.brobot.action.ActionResult successResult = new io.github.jspinak.brobot.action.ActionResult();
        successResult.setSuccess(true);
        
        // Add a mock match to the result for tests that expect matches
        io.github.jspinak.brobot.model.match.Match mockMatch = new io.github.jspinak.brobot.model.match.Match.Builder()
            .setRegion(new Region(100, 100, 50, 50))
            .setSimScore(0.95)
            .setName("MockMatch")
            .build();
        successResult.add(mockMatch);
        
        // Handle all different perform method signatures
        when(action.perform(any(ActionType.class), any(Object[].class))).thenReturn(successResult);
        when(action.perform(any(ActionType.class), anyString())).thenReturn(successResult);
        
        // Main perform method with ActionConfig - handle properly including nulls
        doReturn(successResult).when(action).perform(any(ActionConfig.class), any(ObjectCollection[].class));
        doReturn(successResult).when(action).perform(anyString(), any(ActionConfig.class), any(ObjectCollection[].class));
        
        // Also handle when called with explicit varargs
        when(action.perform(any(ActionConfig.class), any(ObjectCollection.class))).thenReturn(successResult);
        when(action.perform(any(ActionConfig.class), any(ObjectCollection.class), any(ObjectCollection.class))).thenReturn(successResult);
        
        // Create mock methods for specific action types  
        when(action.find(any(StateImage[].class))).thenReturn(successResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(successResult);
        when(action.click(any(StateImage[].class))).thenReturn(successResult);
        when(action.type(any(ObjectCollection[].class))).thenReturn(successResult);
        
        return action;
    }
    
    /**
     * Mock Click bean for testing.
     */
    @Bean
    @Primary
    public Click mockClick() {
        Click click = mock(Click.class);
        
        // Configure to handle null gracefully
        doAnswer(invocation -> {
            io.github.jspinak.brobot.action.ActionResult result = invocation.getArgument(0);
            if (result != null) {
                result.setSuccess(true);
            }
            return null;
        }).when(click).perform(any(), any());
        
        return click;
    }
    
    /**
     * Mock Find bean for testing.
     */
    @Bean
    @Primary
    public Find mockFind() {
        Find find = mock(Find.class);
        
        doAnswer(invocation -> {
            io.github.jspinak.brobot.action.ActionResult result = invocation.getArgument(0);
            if (result != null) {
                result.setSuccess(true);
            }
            return null;
        }).when(find).perform(any(), any());
        
        return find;
    }
    
    /**
     * Mock TypeText bean for testing.
     */
    @Bean
    @Primary
    public TypeText mockTypeText() {
        TypeText typeText = mock(TypeText.class);
        
        doAnswer(invocation -> {
            io.github.jspinak.brobot.action.ActionResult result = invocation.getArgument(0);
            if (result != null) {
                result.setSuccess(true);
            }
            return null;
        }).when(typeText).perform(any(), any());
        
        return typeText;
    }
    
    /**
     * Mock WaitVanish bean for testing.
     */
    @Bean
    @Primary
    public WaitVanish mockWaitVanish() {
        return mock(WaitVanish.class);
    }
    
    /**
     * Mock Drag bean for testing.
     */
    @Bean
    @Primary
    public Drag mockDrag() {
        return mock(Drag.class);
    }
    
    /**
     * Mock ActionChainExecutor bean for testing.
     */
    @Bean
    @Primary
    public ActionChainExecutor mockActionChainExecutor() {
        ActionChainExecutor executor = mock(ActionChainExecutor.class);
        
        // Configure default behavior
        when(executor.executeChain(any(), any(), any())).thenReturn(new io.github.jspinak.brobot.action.ActionResult());
        
        return executor;
    }
    
    /**
     * Mock ScreenCaptureService for testing.
     */
    @Bean
    @Primary
    public ScreenCaptureService mockScreenCaptureService() {
        ScreenCaptureService service = mock(ScreenCaptureService.class);
        
        // Return dummy image for all capture operations
        BufferedImage dummyImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        when(service.captureScreen()).thenReturn(dummyImage);
        when(service.captureRegion(any(Region.class))).thenReturn(dummyImage);
        when(service.captureRegion(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(dummyImage);
        
        return service;
    }
    
    /**
     * Mock MockModeManager bean for testing.
     * Note: MockModeManager uses static methods, so we'll configure the static mock mode.
     */
    @Bean
    @Primary
    public MockModeManager mockModeManager() {
        // MockModeManager uses static methods
        // The BrobotTestBase already sets mock mode via FrameworkSettings.mock = true
        return new MockModeManager();
    }
    
    /**
     * Provides a region for testing.
     */
    @Bean
    public Region testRegion() {
        return new Region(0, 0, 1920, 1080);
    }
}