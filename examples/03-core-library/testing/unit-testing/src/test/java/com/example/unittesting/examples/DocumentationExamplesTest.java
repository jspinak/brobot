package com.example.unittesting.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/** Unit test examples directly from the documentation. From: /docs/04-testing/unit-testing.md */
@SpringBootTest
@TestPropertySource(
        properties = {
            "brobot.core.mock=true",
            "brobot.screenshot.path=src/test/resources/screenshots/"
        })
class DocumentationExamplesTest {

    @Autowired private Action action;

    /** Basic Unit Test Example from documentation */
    @Test
    @DisplayName("Successful Login - Documentation Example")
    void testSuccessfulLogin() {
        // Arrange - Create state objects
        StateImage usernameField = new StateImage.Builder().addPatterns("username_field").build();
        StateImage passwordField = new StateImage.Builder().addPatterns("password_field").build();
        StateImage loginButton = new StateImage.Builder().addPatterns("login_button").build();

        // Act - Perform actions
        // Find and click username field
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .build();
        ActionResult usernameResult = action.perform(findOptions, usernameField);

        // Type username
        TypeOptions typeOptions = new TypeOptions.Builder().setTypeDelay(0.05).build();
        action.perform(typeOptions, new ObjectCollection.Builder().withStrings("testuser").build());

        // Find and click password field
        ActionResult passwordResult = action.perform(findOptions, passwordField);
        action.perform(typeOptions, new ObjectCollection.Builder().withStrings("testpass").build());

        // Click login button
        ClickOptions clickOptions =
                new ClickOptions.Builder().setClickType(ClickOptions.Type.LEFT).build();
        ActionResult loginResult = action.perform(clickOptions, loginButton);

        // Assert
        assertTrue(usernameResult.isSuccess());
        assertTrue(passwordResult.isSuccess());
        assertTrue(loginResult.isSuccess());
        assertEquals(1, loginResult.size());
        assertThat(loginResult.getBestMatch()).isPresent();
    }

    /** Testing with Multiple Screenshots from documentation */
    @Test
    @DisplayName("Navigation Flow - Documentation Example")
    void testNavigationFlow() {
        // Screenshots configured via properties file
        // brobot.screenshot.path=src/test/resources/screenshots/
        // Place files: step1_login.png, step2_dashboard.png, step3_settings.png

        // Create find options for navigation
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.8)
                        .build();

        // Test navigation sequence
        // Step 1: Login
        StateImage loginButton = new StateImage.Builder().addPatterns("login_button.png").build();
        ActionResult loginResult = action.perform(findOptions, loginButton);
        action.perform(new ClickOptions.Builder().build(), loginButton);

        // Step 2: Navigate to dashboard
        StateImage dashboardLink = new StateImage.Builder().addPatterns("dashboard_link").build();
        ActionResult dashboardResult = action.perform(findOptions, dashboardLink);
        action.perform(new ClickOptions.Builder().build(), dashboardLink);

        // Step 3: Open settings
        StateImage settingsIcon = new StateImage.Builder().addPatterns("settings_icon.png").build();
        ActionResult settingsResult = action.perform(findOptions, settingsIcon);
        action.perform(new ClickOptions.Builder().build(), settingsIcon);

        // Verify each step
        assertTrue(loginResult.isSuccess());
        assertTrue(dashboardResult.isSuccess());
        assertTrue(settingsResult.isSuccess());
    }

    /** Working with ActionResult from documentation */
    @Test
    @DisplayName("ActionResult Operations - Documentation Example")
    void testFindOperations() {
        // Create find configuration
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.7)
                        .build();

        // Perform find action
        StateImage submitButton = new StateImage.Builder().addPatterns("submit_button.png").build();
        ActionResult result = action.perform(findOptions, submitButton);

        // Test result properties
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        // Access best match
        Optional<Match> bestMatch = result.getBestMatch();
        assertTrue(bestMatch.isPresent());
        assertTrue(bestMatch.get().getScore() > 0.8);

        // Test specific regions
        List<Region> regions = result.getMatchRegions();
        assertThat(regions).hasSize(2);

        // Test filtering
        ActionResult highScoreMatches = new ActionResult();
        result.getMatchList().stream()
                .filter(match -> match.getScore() > 0.9)
                .forEach(highScoreMatches::add);
        assertTrue(highScoreMatches.size() > 0);
    }

    /** Mock Behavior Verification from documentation */
    @Test
    // Note: @TestPropertySource would normally be at class level
    // Properties: brobot.mock.time-click=0.1, brobot.mock.time-find-first=0.2
    @DisplayName("Mock Timings - Documentation Example")
    void testMockTimings() {
        long startTime = System.currentTimeMillis();

        // Perform mocked actions
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build();

        StateImage button = new StateImage.Builder().addPatterns("button.png").build();
        ActionResult findResult = action.perform(findOptions, button);

        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ActionResult clickResult = action.perform(clickOptions, button);

        long duration = System.currentTimeMillis() - startTime;

        // Verify mock timing (should be approximately 300ms)
        assertTrue(duration >= 250 && duration <= 350);
    }

    /** Pattern-Based Testing from documentation */
    @Test
    @DisplayName("Pattern Matching - Documentation Example")
    void testPatternMatching() {
        // Test with different similarity thresholds
        PatternFindOptions strictFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.95)
                        .build();

        PatternFindOptions relaxedFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.70)
                        .build();

        StateImage targetPattern =
                new StateImage.Builder().addPatterns("target_button.png").build();

        // Test strict matching
        ActionResult strictResult = action.perform(strictFind, targetPattern);
        assertTrue(strictResult.size() <= 1, "Strict matching should find at most one match");

        // Test relaxed matching
        ActionResult relaxedResult = action.perform(relaxedFind, targetPattern);
        assertTrue(
                relaxedResult.size() >= strictResult.size(),
                "Relaxed matching should find at least as many matches");
    }
}
