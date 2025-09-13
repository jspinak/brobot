package io.github.jspinak.brobot.test.config.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.action.internal.region.SearchRegionDependencyRegistry;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.SearchRegionDependencyInitializer;
import io.github.jspinak.brobot.statemanagement.StateMemory;

/**
 * Minimal configuration for integration tests. Only provides the essential beans needed for tests
 * to run.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Profile("integration-minimal")
public class IntegrationTestMinimalConfig {

    static {
        // Enable mock mode before Spring context loads
        MockModeManager.setMockMode(true);
        System.setProperty("java.awt.headless", "true");
        System.setProperty("brobot.mock", "true");
        System.setProperty("brobot.mock", "true");
        System.setProperty("brobot.mock", "true");
    }

    @Bean
    @Primary
    public ScreenCaptureService screenCaptureService() {
        ScreenCaptureService service = mock(ScreenCaptureService.class);
        BufferedImage mockImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        when(service.captureScreen()).thenReturn(mockImage);
        when(service.captureRegion(any())).thenReturn(mockImage);
        return service;
    }

    @Bean
    @Primary
    public Action action() {
        Action action = mock(Action.class);

        // Configure all method signatures to return appropriate results
        when(action.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
                .thenAnswer(invocation -> createSuccessResult());

        when(action.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                .thenAnswer(
                        invocation -> {
                            ActionConfig config = invocation.getArgument(0);
                            ObjectCollection collection = invocation.getArgument(1);

                            // Create result with match
                            ActionResult result = new ActionResult();
                            result.setSuccess(true);

                            // Add a mock match for all operations
                            Match mockMatch =
                                    new Match.Builder()
                                            .setRegion(new Region(100, 100, 50, 50))
                                            .setSimScore(0.95)
                                            .setName("MockMatch")
                                            .build();
                            result.add(mockMatch);

                            // Special handling for type operations - capture the text
                            if (collection != null && !collection.getStateStrings().isEmpty()) {
                                Text text = new Text();
                                text.add(collection.getStateStrings().get(0).getString());
                                result.setText(text);
                            }

                            return result;
                        });

        // Configure find, click, type convenience methods
        when(action.find(any(StateImage[].class))).thenReturn(createSuccessResult());
        when(action.find(any(ObjectCollection[].class))).thenReturn(createSuccessResult());
        when(action.click(any(StateImage[].class))).thenReturn(createSuccessResult());
        when(action.type(any(ObjectCollection[].class))).thenReturn(createSuccessResult());

        return action;
    }

    private ActionResult createSuccessResult() {
        ActionResult result = new ActionResult();
        result.setSuccess(true);

        // Add a default match
        Match mockMatch =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 50, 50))
                        .setSimScore(0.95)
                        .setName("MockMatch")
                        .build();
        result.add(mockMatch);

        return result;
    }

    @Bean
    public Find find() {
        return mock(Find.class);
    }

    @Bean
    public Click click() {
        return mock(Click.class);
    }

    @Bean
    public TypeText typeText() {
        return mock(TypeText.class);
    }

    @Bean
    public StateService stateService() {
        return mock(StateService.class);
    }

    @Bean
    public StateMemory stateMemory() {
        return mock(StateMemory.class);
    }

    @Bean
    public SearchRegionDependencyRegistry searchRegionDependencyRegistry() {
        return mock(SearchRegionDependencyRegistry.class);
    }

    @Bean
    public DynamicRegionResolver dynamicRegionResolver() {
        return mock(DynamicRegionResolver.class);
    }

    @Bean
    public SearchRegionDependencyInitializer searchRegionDependencyInitializer() {
        return mock(SearchRegionDependencyInitializer.class);
    }

    @Bean
    public ActionChainExecutor actionChainExecutor() {
        return mock(ActionChainExecutor.class);
    }
}
