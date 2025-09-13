package io.github.jspinak.brobot.test.config.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Configuration specifically for integration tests. Uses the "integration" profile to avoid
 * conflicts with other test configurations.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(
        basePackages = "io.github.jspinak.brobot",
        excludeFilters = {
            // Exclude all test configurations
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Test.*"),
            // Exclude mock configurations that might conflict
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Mock.*Config.*"),
            // Exclude screen capture services that conflict
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {
                        io.github.jspinak.brobot.annotations.AnnotationProcessor.class,
                        io.github.jspinak.brobot.startup.orchestration.StartupRunner.class
                    }),
        })
@Profile("integration")
public class IntegrationTestProfile {

    static {
        // Enable mock mode before Spring context loads
        MockModeManager.setMockMode(true);
        System.setProperty("java.awt.headless", "true");
        System.setProperty("brobot.mock", "true");
        System.setProperty("brobot.mock", "true");
        System.setProperty("brobot.mock", "true");
    }

    /** Mock ScreenCaptureService for integration tests */
    @Bean
    @Primary
    @Profile("integration")
    public ScreenCaptureService integrationScreenCaptureService() {
        ScreenCaptureService service = mock(ScreenCaptureService.class);
        BufferedImage mockImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        when(service.captureScreen()).thenReturn(mockImage);
        when(service.captureRegion(any())).thenReturn(mockImage);
        return service;
    }

    /** Mock Find implementation for integration tests */
    @Bean
    @Primary
    @Profile("integration")
    @ConditionalOnMissingBean(name = "mockFind")
    public Find integrationMockFind() {
        Find find = mock(Find.class);
        // Mock behavior will be handled by the Action mock
        return find;
    }

    /** Mock Click implementation for integration tests */
    @Bean
    @Primary
    @Profile("integration")
    @ConditionalOnMissingBean(name = "mockClick")
    public Click integrationMockClick() {
        Click click = mock(Click.class);
        // Mock behavior will be handled by the Action mock
        return click;
    }

    /** Mock TypeText implementation for integration tests */
    @Bean
    @Primary
    @Profile("integration")
    @ConditionalOnMissingBean(name = "mockTypeText")
    public TypeText integrationMockTypeText() {
        TypeText typeText = mock(TypeText.class);
        // Mock behavior will be handled by the Action mock
        return typeText;
    }

    /** Mock Action bean for integration tests - only if not already provided */
    @Bean
    @Primary
    @Profile("integration")
    @ConditionalOnMissingBean(Action.class)
    public Action integrationMockAction() {
        Action action = mock(Action.class);

        // Configure default successful behavior
        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);

        // Add a mock match
        Match mockMatch =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 50, 50))
                        .setSimScore(0.95)
                        .setName("MockMatch")
                        .build();
        successResult.add(mockMatch);

        // Configure all method signatures with specific types
        // Using doReturn to avoid ambiguity issues
        doReturn(successResult)
                .when(action)
                .perform(any(ActionConfig.class), any(ObjectCollection[].class));
        doReturn(successResult)
                .when(action)
                .perform(any(ActionConfig.class), any(ObjectCollection.class));
        doReturn(successResult).when(action).find(any(StateImage[].class));
        doReturn(successResult).when(action).find(any(ObjectCollection[].class));
        doReturn(successResult).when(action).click(any(StateImage[].class));
        doReturn(successResult).when(action).type(any(ObjectCollection[].class));

        return action;
    }
}
