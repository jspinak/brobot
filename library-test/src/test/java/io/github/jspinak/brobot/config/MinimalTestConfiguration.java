package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.composite.drag.Drag;
import io.github.jspinak.brobot.action.basic.highlight.Highlight;
import io.github.jspinak.brobot.action.basic.wait.WaitForChange;
import io.github.jspinak.brobot.action.basic.wait.WaitVanish;
import io.github.jspinak.brobot.action.basic.mouse.MoveMouse;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Minimal test configuration that provides mocked beans to avoid circular dependencies.
 * This configuration is designed for fast test execution without Spring context issues.
 */
@TestConfiguration
@Profile("minimal-test")
public class MinimalTestConfiguration {

    @Bean
    @Primary
    public Find mockFind() {
        Find mockFind = Mockito.mock(Find.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            // Add a dummy match for mock mode
            Match match = new Match(new Region(100, 100, 50, 50));
            match.setScore(0.95);
            result.add(match);
            result.setSuccess(true);
            return null;
        }).when(mockFind).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockFind;
    }

    @Bean
    @Primary
    public Click mockClick() {
        Click mockClick = Mockito.mock(Click.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            // Click success is tracked via result.setSuccess
            return null;
        }).when(mockClick).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockClick;
    }

    @Bean
    @Primary
    public TypeText mockType() {
        TypeText mockType = Mockito.mock(TypeText.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockType).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockType;
    }

    @Bean
    @Primary
    public Drag mockDrag() {
        Drag mockDrag = Mockito.mock(Drag.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockDrag).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockDrag;
    }

    @Bean
    @Primary
    public Highlight mockHighlight() {
        Highlight mockHighlight = Mockito.mock(Highlight.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockHighlight).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockHighlight;
    }

    @Bean
    @Primary
    public WaitForChange mockWait() {
        WaitForChange mockWait = Mockito.mock(WaitForChange.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockWait).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockWait;
    }

    @Bean
    @Primary
    public WaitVanish mockWaitVanish() {
        WaitVanish mockWaitVanish = Mockito.mock(WaitVanish.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockWaitVanish).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockWaitVanish;
    }

    @Bean
    @Primary
    public MoveMouse mockMoveMouse() {
        MoveMouse mockMoveMouse = Mockito.mock(MoveMouse.class);
        doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            result.setSuccess(true);
            return null;
        }).when(mockMoveMouse).perform(any(ActionResult.class), any(ObjectCollection[].class));
        return mockMoveMouse;
    }

    @Bean
    @Primary
    public ActionService mockActionService() {
        ActionService mockService = Mockito.mock(ActionService.class);
        
        // Return the appropriate mock action based on the config type
        when(mockService.getAction(any())).thenAnswer(invocation -> {
            Object config = invocation.getArgument(0);
            String className = config.getClass().getSimpleName();
            
            ActionInterface action;
            switch (className) {
                case "PatternFindOptions":
                case "TextFindOptions":
                    action = mockFind();
                    break;
                case "ClickOptions":
                    action = mockClick();
                    break;
                case "TypeOptions":
                    action = mockType();
                    break;
                case "DragOptions":
                    action = mockDrag();
                    break;
                case "HighlightOptions":
                    action = mockHighlight();
                    break;
                case "WaitOptions":
                    action = mockWait();
                    break;
                case "WaitVanishOptions":
                    action = mockWaitVanish();
                    break;
                case "MoveMouseOptions":
                    action = mockMoveMouse();
                    break;
                default:
                    action = mockFind(); // Default to Find
            }
            return Optional.of(action);
        });
        
        return mockService;
    }
}