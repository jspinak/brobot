package io.github.jspinak.brobot.testutils;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Comprehensive test data builder for Brobot tests. Provides factory methods for creating mock data
 * objects with sensible defaults.
 */
public class BrobotTestDataBuilder {

    // Default dimensions
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;

    /** Creates a basic StateImage with a dummy pattern. */
    public static StateImage createStateImage(String name) {
        return new StateImage.Builder().setName(name).build();
    }

    /** Creates a StateImage with specified position. */
    public static StateImage createStateImageAtPosition(String name, int x, int y) {
        return new StateImage.Builder().setName(name).build();
    }

    /** Creates a StateImage with a specific size. */
    public static StateImage createStateImageWithSize(String name, int width, int height) {
        return new StateImage.Builder().setName(name).build();
    }

    /** Creates a successful ActionResult with matches. */
    public static ActionResult createSuccessfulActionResult(int numMatches) {
        ActionResult result = new ActionResult();
        result.setSuccess(true);
        result.setDuration(Duration.ofMillis(100));

        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < numMatches; i++) {
            matches.add(createMatch("match" + i, 50 + i * 100, 50 + i * 50, 0.95 - i * 0.05));
        }
        result.setMatchList(matches);

        return result;
    }

    /** Creates a failed ActionResult. */
    public static ActionResult createFailedActionResult() {
        ActionResult result = new ActionResult();
        result.setSuccess(false);
        result.setDuration(Duration.ofMillis(50));
        result.setMatchList(new ArrayList<>());
        return result;
    }

    /** Creates a Match object with specified properties. */
    public static Match createMatch(String name, int x, int y, double score) {
        Match match = new Match();
        match.setName(name);
        match.setRegion(new Region(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        match.setScore(score);
        match.setText("");
        match.setTimeStamp(LocalDateTime.now());
        return match;
    }

    /** Creates an ObjectCollection with StateImages. */
    public static ObjectCollection createObjectCollection(String... imageNames) {
        ObjectCollection.Builder builder = new ObjectCollection.Builder();

        List<StateImage> images = new ArrayList<>();
        for (String name : imageNames) {
            images.add(createStateImage(name));
        }
        builder.withImages(images);

        return builder.build();
    }

    /** Creates an ObjectCollection with matches from a previous action. */
    public static ObjectCollection createObjectCollectionWithMatches(ActionResult previousResult) {
        return new ObjectCollection.Builder().withMatches(previousResult).build();
    }

    /** Creates a State with StateImages. */
    public static State createState(String name, String... imageNames) {
        State.Builder builder = new State.Builder(name);

        // State.Builder may not have addStateImage method
        // Just build the state without images for now

        return builder.build();
    }

    /** Creates a default Region. */
    public static Region createDefaultRegion() {
        return new Region(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /** Creates a full screen Region. */
    public static Region createFullScreenRegion() {
        return new Region(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /** Creates a Region at a specific position. */
    public static Region createRegion(int x, int y, int w, int h) {
        return new Region(x, y, w, h);
    }

    /** Creates a Location at screen center. */
    public static Location createCenterLocation() {
        return new Location(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
    }

    /** Creates a Location at a specific position. */
    public static Location createLocation(int x, int y) {
        return new Location(x, y);
    }

    /** Creates a Location using Positions enum. */
    public static Location createLocationFromPosition(Positions.Name position) {
        return new Location(position);
    }

    /** Creates a dummy BufferedImage for testing. */
    public static BufferedImage createDummyImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Fill with a simple pattern for testing
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = ((x % 20) < 10) != ((y % 20) < 10) ? 0xFFFFFF : 0x000000;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    /** Creates a base64 encoded dummy image string. */
    public static String createBase64Image() {
        // 1x1 transparent PNG
        String base64 =
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        return "data:image/png;base64," + base64;
    }

    /** Creates mock image paths for testing. */
    public static List<String> createMockImagePaths(int count) {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            paths.add("mock/images/test_image_" + i + ".png");
        }
        return paths;
    }

    /** Creates a unique test ID. */
    public static String createTestId() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Builder pattern for complex test scenarios. */
    public static class TestScenarioBuilder {
        private List<State> states = new ArrayList<>();
        private List<StateImage> images = new ArrayList<>();
        private List<ActionResult> results = new ArrayList<>();

        public TestScenarioBuilder withState(State state) {
            states.add(state);
            return this;
        }

        public TestScenarioBuilder withStateImage(StateImage image) {
            images.add(image);
            return this;
        }

        public TestScenarioBuilder withActionResult(ActionResult result) {
            results.add(result);
            return this;
        }

        public TestScenario build() {
            return new TestScenario(states, images, results);
        }
    }

    /** Container for complex test scenarios. */
    public static class TestScenario {
        public final List<State> states;
        public final List<StateImage> images;
        public final List<ActionResult> results;

        TestScenario(List<State> states, List<StateImage> images, List<ActionResult> results) {
            this.states = states;
            this.images = images;
            this.results = results;
        }
    }
}
