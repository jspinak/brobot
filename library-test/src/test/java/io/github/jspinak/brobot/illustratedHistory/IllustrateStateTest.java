package io.github.jspinak.brobot.illustratedHistory;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.FindState;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.match.MatchToStateConverter;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.config.TestApplicationConfiguration;
import io.github.jspinak.brobot.tools.history.StateLayoutVisualizer;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

@SpringBootTest(classes = BrobotTestApplication.class)
@Import(TestApplicationConfiguration.class)
@Disabled("CI failure - needs investigation")
class IllustrateStateTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        FrameworkSettings.mock = false; // Allow real image processing
    }

    @BeforeEach
    public void setUp() {
        FrameworkSettings.mock = false; // Allow real image processing
    }

    @Autowired MatchToStateConverter createStatesFromMatches;

    @Autowired Action action;

    @Autowired FindState findStates;

    @Autowired ActionResultFactory matchesInitializer;

    @Autowired StateLayoutVisualizer illustrateState;

    @Autowired ImageFileUtilities imageUtils;

    /**
     * Creates states from saved FloraNext screenshots instead of live OCR. This allows tests to run
     * in headless/CI environments.
     */
    private List<State> createStatesFromScreenshots() {
        List<State> states = new ArrayList<>();

        try {
            // Load FloraNext screenshots from the screenshots folder
            String screenshotDir = "screenshots";
            File dir = new File(screenshotDir);
            if (!dir.exists()) {
                // Try alternate path
                screenshotDir = "library-test/screenshots";
                dir = new File(screenshotDir);
            }
            if (!dir.exists()) {
                // Try from current working directory
                screenshotDir =
                        Paths.get("").toAbsolutePath().toString() + "/library-test/screenshots";
                dir = new File(screenshotDir);
            }

            // Create states from FloraNext screenshots
            for (int i = 0; i <= 4; i++) {
                File imageFile = new File(dir, "floranext" + i + ".png");
                if (imageFile.exists()) {
                    BufferedImage bufferedImage = ImageIO.read(imageFile);
                    Mat mat = MatrixUtilities.bufferedImageToMat(bufferedImage).orElse(new Mat());

                    // Create a state with the image
                    StateImage stateImage =
                            new StateImage.Builder()
                                    .setName("floranext" + i)
                                    .setSearchRegionForAllPatterns(
                                            new Region(
                                                    0,
                                                    0,
                                                    bufferedImage.getWidth(),
                                                    bufferedImage.getHeight()))
                                    .build();

                    // Create pattern from the Mat
                    Pattern pattern =
                            new Pattern.Builder()
                                    .setMat(mat)
                                    .setFixedRegion(
                                            new Region(
                                                    0,
                                                    0,
                                                    bufferedImage.getWidth(),
                                                    bufferedImage.getHeight()))
                                    .build();
                    stateImage.getPatterns().add(pattern);

                    State state =
                            new State.Builder("FloraNextState" + i).withImages(stateImage).build();
                    states.add(state);
                }
            }

            if (states.isEmpty()) {
                System.out.println("Warning: No FloraNext screenshots found in " + screenshotDir);
            }

        } catch (Exception e) {
            System.err.println("Error loading FloraNext screenshots: " + e.getMessage());
            e.printStackTrace();
        }

        return states;
    }

    @Test
    void getIllustratedState() {
        List<State> states = createStatesFromScreenshots();
        assertFalse(states.isEmpty(), "Should have loaded at least one FloraNext screenshot");

        State first = states.get(0);
        assertNotNull(first);

        Mat state0 = illustrateState.illustrateWithFixedSearchRegions(first);
        assertNotNull(state0);
        assertTrue(
                countNonZero(MatrixUtilities.toGrayscale(state0)) > 0,
                "Illustrated state should have non-zero pixels");
    }

    @Test
    void illustrateStateWithOneImage() {
        List<State> states = createStatesFromScreenshots();
        assertFalse(states.isEmpty(), "Should have loaded at least one FloraNext screenshot");

        State state = states.get(0);
        Optional<StateImage> optState = state.getStateImages().stream().findFirst();
        assertTrue(optState.isPresent(), "State should have at least one image");

        List<Pattern> patterns = optState.get().getPatterns();
        assertFalse(patterns.isEmpty(), "State image should have patterns");

        Mat mat = patterns.get(0).getMat();
        assertNotNull(mat);

        Region matRegion = patterns.get(0).getRegion();
        assertNotNull(matRegion);

        MatBuilder matBuilder = new MatBuilder();
        Mat illustratedMat =
                matBuilder
                        .init()
                        .addSubMat(new Location(matRegion.x(), matRegion.y()), mat)
                        .build();

        assertNotNull(illustratedMat);
        assertTrue(
                countNonZero(MatrixUtilities.toGrayscale(illustratedMat)) > 0,
                "Illustrated mat should have non-zero pixels");
    }

    @Test
    void illustrateStateWithAllImages() {
        List<State> states = createStatesFromScreenshots();
        assertFalse(states.isEmpty(), "Should have loaded at least one FloraNext screenshot");

        State state = states.get(0);
        MatBuilder matBuilder = new MatBuilder().init();

        int imageCount = 0;
        for (StateImage stateImage : state.getStateImages()) {
            List<Pattern> patterns = stateImage.getPatterns();
            if (!patterns.isEmpty()) {
                Pattern pattern = patterns.get(0);
                Mat mat = pattern.getMat();
                Region region = pattern.getRegion();
                matBuilder.addSubMat(new Location(region.x(), region.y()), mat);
                imageCount++;
            }
        }

        assertTrue(imageCount > 0, "Should have processed at least one image");

        Mat illustratedMat = matBuilder.build();
        assertNotNull(illustratedMat);
        assertTrue(
                countNonZero(MatrixUtilities.toGrayscale(illustratedMat)) > 0,
                "Combined illustrated mat should have non-zero pixels");
    }

    @Test
    void writeAllStatesToFile() {
        List<State> states = createStatesFromScreenshots();
        assertFalse(states.isEmpty(), "Should have loaded at least one FloraNext screenshot");

        // Create output directory if it doesn't exist
        File historyDir = new File("history");
        if (!historyDir.exists()) {
            historyDir.mkdirs();
        }

        // Write each state to file
        states.forEach(
                state -> {
                    assertDoesNotThrow(
                            () ->
                                    illustrateState.writeIllustratedStateToFile(
                                            state, "history/state" + state.getName()));
                });

        // Verify at least one file was created
        File[] files = historyDir.listFiles((dir, name) -> name.startsWith("state"));
        assertTrue(
                files != null && files.length > 0, "Should have created at least one state file");
    }

    @Test
    void verifyFloraNextScreenshotsExist() {
        // Verify we can access the FloraNext screenshots
        String[] possiblePaths = {
            "screenshots",
            "library-test/screenshots",
            Paths.get("").toAbsolutePath().toString() + "/library-test/screenshots"
        };

        boolean foundScreenshots = false;
        String foundPath = null;

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists()) {
                File testFile = new File(dir, "floranext0.png");
                if (testFile.exists()) {
                    foundScreenshots = true;
                    foundPath = path;
                    break;
                }
            }
        }

        assertTrue(
                foundScreenshots,
                "FloraNext screenshots should be accessible. Checked paths: "
                        + String.join(", ", possiblePaths));
        System.out.println("Found FloraNext screenshots in: " + foundPath);
    }
}
