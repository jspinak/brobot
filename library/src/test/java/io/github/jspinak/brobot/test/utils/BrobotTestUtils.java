package io.github.jspinak.brobot.test.utils;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Common test utilities for Brobot tests.
 * Provides factory methods and helpers for creating test data.
 */
public class BrobotTestUtils {
    
    private static final Random random = new Random();
    
    /**
     * Create a test State with default values.
     */
    public static State createTestState(String name) {
        State.Builder builder = new State.Builder(name);
        return builder.build();
    }
    
    /**
     * Create a test StateImage with default values.
     */
    public static StateImage createTestStateImage(String name) {
        StateImage.Builder builder = new StateImage.Builder();
        builder.setName(name);
        return builder.build();
    }
    
    /**
     * Create a test Match at a specific location.
     */
    public static Match createTestMatch(int x, int y, int width, int height, double score) {
        Match match = new Match();
        match.setRegion(new Region(x, y, width, height));
        match.setScore(score);
        return match;
    }
    
    /**
     * Create a test Match with random values.
     */
    public static Match createRandomMatch() {
        return createTestMatch(
            random.nextInt(1920),
            random.nextInt(1080),
            50 + random.nextInt(200),
            50 + random.nextInt(200),
            0.7 + random.nextDouble() * 0.3
        );
    }
    
    /**
     * Create a list of test matches.
     */
    public static List<Match> createTestMatches(int count) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            matches.add(createRandomMatch());
        }
        return matches;
    }
    
    /**
     * Create a successful ActionResult with matches.
     */
    public static ActionResult createSuccessfulResult(int matchCount) {
        ActionResult result = new ActionResult();
        result.setSuccess(true);
        for (Match match : createTestMatches(matchCount)) {
            result.add(match);
        }
        result.getTimingData().setTotalDuration(Duration.ofMillis(100 + random.nextInt(900)));
        return result;
    }
    
    /**
     * Create a failed ActionResult.
     */
    public static ActionResult createFailedResult() {
        ActionResult result = new ActionResult();
        result.setSuccess(false);
        result.getTimingData().setTotalDuration(Duration.ofMillis(100 + random.nextInt(900)));
        return result;
    }
    
    /**
     * Create a test Location.
     */
    public static Location createTestLocation(int x, int y) {
        return new Location(x, y);
    }
    
    /**
     * Create a random test Location within screen bounds.
     */
    public static Location createRandomLocation() {
        return createTestLocation(random.nextInt(1920), random.nextInt(1080));
    }
    
    /**
     * Create a test Region.
     */
    public static Region createTestRegion(int x, int y, int width, int height) {
        return new Region(x, y, width, height);
    }
    
    /**
     * Create a random test Region.
     */
    public static Region createRandomRegion() {
        int x = random.nextInt(1920);
        int y = random.nextInt(1080);
        int width = Math.min(50 + random.nextInt(200), 1920 - x);
        int height = Math.min(50 + random.nextInt(200), 1080 - y);
        return createTestRegion(x, y, width, height);
    }
    
    /**
     * Assert that two Locations are approximately equal (within tolerance).
     */
    public static boolean areLocationsApproximatelyEqual(Location loc1, Location loc2, int tolerance) {
        return Math.abs(loc1.getX() - loc2.getX()) <= tolerance &&
               Math.abs(loc1.getY() - loc2.getY()) <= tolerance;
    }
    
    /**
     * Assert that two Regions are approximately equal (within tolerance).
     */
    public static boolean areRegionsApproximatelyEqual(Region r1, Region r2, int tolerance) {
        return Math.abs(r1.getX() - r2.getX()) <= tolerance &&
               Math.abs(r1.getY() - r2.getY()) <= tolerance &&
               Math.abs(r1.getW() - r2.getW()) <= tolerance &&
               Math.abs(r1.getH() - r2.getH()) <= tolerance;
    }
    
    /**
     * Create ActionConfig for testing.
     * Note: Use specific options classes like PatternFindOptions or ClickOptions
     * for actual testing instead of generic ActionConfig.
     */
    public static ActionConfig createTestActionConfig() {
        // Return null as ActionConfig is abstract
        // Tests should use specific options classes
        return null;
    }
    
    /**
     * Sleep for a short duration (useful for timing tests).
     */
    public static void shortSleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Sleep for a specific duration in milliseconds.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Generate a unique test name with a prefix.
     */
    public static String generateTestName(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }
    
    /**
     * Check if we're running in a CI/CD environment.
     */
    public static boolean isRunningInCI() {
        return System.getenv("CI") != null || 
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("JENKINS_HOME") != null ||
               System.getenv("GITLAB_CI") != null;
    }
    
    /**
     * Check if we're running in a headless environment.
     */
    public static boolean isHeadless() {
        return java.awt.GraphicsEnvironment.isHeadless();
    }
}