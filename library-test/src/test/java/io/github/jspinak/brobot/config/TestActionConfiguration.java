package io.github.jspinak.brobot.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.highlight.Highlight;
import io.github.jspinak.brobot.action.basic.mouse.MoveMouse;
import io.github.jspinak.brobot.action.basic.wait.WaitVanish;
import io.github.jspinak.brobot.action.internal.capture.RegionDefiner;

/**
 * Test configuration that provides mocked versions of actions to break circular dependencies.
 *
 * <p>This configuration is activated when running tests to prevent Spring context initialization
 * failures caused by circular dependencies in the Find action chain.
 */
@TestConfiguration
@Profile("test")
public class TestActionConfiguration {

    /**
     * Provides a mocked Find action that immediately returns success. This breaks the circular
     * dependency chain: Find → FindPipeline → Click → Find
     */
    @Bean("testFind")
    @Primary
    public Find testFind() {
        Find mockFind = Mockito.mock(Find.class);

        // Configure mock to immediately return success
        doAnswer(
                        invocation -> {
                            ActionResult matches = invocation.getArgument(0);
                            matches.setSuccess(true);
                            return null;
                        })
                .when(mockFind)
                .perform(any(ActionResult.class), any(ObjectCollection[].class));

        return mockFind;
    }

    /**
     * Provides a simplified Click action that doesn't depend on Find. Uses the mocked Find for any
     * find operations.
     */
    @Bean("testClick")
    @Primary
    public Click testClick() {
        Click mockClick = Mockito.mock(Click.class);

        // Configure mock to immediately return success
        doAnswer(
                        invocation -> {
                            ActionResult matches = invocation.getArgument(0);
                            matches.setSuccess(true);
                            return null;
                        })
                .when(mockClick)
                .perform(any(ActionResult.class), any(ObjectCollection[].class));

        return mockClick;
    }

    /** Provides a mocked Highlight action to avoid circular dependencies. */
    @Bean("testHighlight")
    @Primary
    public Highlight testHighlight() {
        Highlight mockHighlight = Mockito.mock(Highlight.class);

        doAnswer(
                        invocation -> {
                            ActionResult matches = invocation.getArgument(0);
                            matches.setSuccess(true);
                            return null;
                        })
                .when(mockHighlight)
                .perform(any(ActionResult.class), any(ObjectCollection[].class));

        return mockHighlight;
    }

    /** Provides a mocked WaitVanish action to avoid circular dependencies. */
    @Bean("testWaitVanish")
    @Primary
    public WaitVanish testWaitVanish() {
        WaitVanish mockVanish = Mockito.mock(WaitVanish.class);

        doAnswer(
                        invocation -> {
                            ActionResult matches = invocation.getArgument(0);
                            matches.setSuccess(true);
                            return null;
                        })
                .when(mockVanish)
                .perform(any(ActionResult.class), any(ObjectCollection[].class));

        return mockVanish;
    }

    /** Provides a mocked MoveMouse action to avoid circular dependencies. */
    @Bean("testMoveMouse")
    @Primary
    public MoveMouse testMoveMouse() {
        MoveMouse mockMove = Mockito.mock(MoveMouse.class);

        doAnswer(
                        invocation -> {
                            ActionResult matches = invocation.getArgument(0);
                            matches.setSuccess(true);
                            return null;
                        })
                .when(mockMove)
                .perform(any(ActionResult.class), any(ObjectCollection[].class));

        return mockMove;
    }

    // Commented out as ConfirmedFinds class doesn't exist
    // /**
    //  * Provides a mocked ConfirmedFinds action to avoid circular dependencies.
    //  */
    // @Bean("testConfirmedFinds")
    // @Primary
    // public ConfirmedFinds testConfirmedFinds() {
    //     ConfirmedFinds mockFinds = Mockito.mock(ConfirmedFinds.class);
    //
    //     doAnswer(invocation -> {
    //         ActionResult matches = invocation.getArgument(0);
    //         matches.setSuccess(true);
    //         return null;
    //     }).when(mockFinds).perform(any(ActionResult.class), any(ObjectCollection[].class));
    //
    //     return mockFinds;
    // }

    /** Provides a mocked RegionDefiner to avoid circular dependencies. */
    @Bean("testRegionDefiner")
    @Primary
    public RegionDefiner testRegionDefiner() {
        return Mockito.mock(RegionDefiner.class);
    }
}
