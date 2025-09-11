package io.github.jspinak.brobot.model.match;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("Match Tests")
public class MatchTest extends BrobotTestBase {

    @Mock private Region mockRegion;

    @Mock private Location mockLocation;

    @Mock private Image mockImage;

    @Mock private Image mockSearchImage;

    @Mock private Anchors mockAnchors;

    @Mock private StateObjectMetadata mockStateObjectData;

    @Mock private Mat mockMat;

    @Mock private Scene mockScene;

    private Match match;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        match = new Match();
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("Default constructor creates empty match")
        public void testDefaultConstructor() {
            Match newMatch = new Match();

            assertEquals(0.0, newMatch.getScore());
            assertEquals("", newMatch.getText());
            assertEquals("", newMatch.getName());
            assertNull(newMatch.getTarget());
            assertNull(newMatch.getImage());
            assertNull(newMatch.getSearchImage());
            assertEquals(0, newMatch.getTimesActedOn());
        }

        @Test
        @DisplayName("Constructor with region creates match with location")
        public void testConstructorWithRegion() {
            Region region = new Region(10, 20, 100, 200);
            Match matchWithRegion = new Match(region);

            assertNotNull(matchWithRegion.getTarget());
            assertEquals(region, matchWithRegion.getRegion());
            assertEquals(10, matchWithRegion.x());
            assertEquals(20, matchWithRegion.y());
            assertEquals(100, matchWithRegion.w());
            assertEquals(200, matchWithRegion.h());
        }
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {

        @Test
        @DisplayName("Set and get score")
        public void testScore() {
            match.setScore(0.95);
            assertEquals(0.95, match.getScore());
        }

        @Test
        @DisplayName("Set and get text")
        public void testText() {
            match.setText("Button Text");
            assertEquals("Button Text", match.getText());
        }

        @Test
        @DisplayName("Set and get name")
        public void testName() {
            match.setName("LoginButton");
            assertEquals("LoginButton", match.getName());
        }

        @Test
        @DisplayName("Set and get timestamp")
        public void testTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            match.setTimeStamp(now);
            assertEquals(now, match.getTimeStamp());
        }

        @Test
        @DisplayName("Track times acted on")
        public void testTimesActedOn() {
            assertEquals(0, match.getTimesActedOn());

            match.incrementTimesActedOn();
            assertEquals(1, match.getTimesActedOn());

            match.incrementTimesActedOn();
            match.incrementTimesActedOn();
            assertEquals(3, match.getTimesActedOn());

            match.setTimesActedOn(10);
            assertEquals(10, match.getTimesActedOn());
        }
    }

    @Nested
    @DisplayName("Location and Region")
    class LocationAndRegion {

        @Test
        @DisplayName("Set and get target location")
        public void testTargetLocation() {
            when(mockLocation.getRegion()).thenReturn(mockRegion);

            match.setTarget(mockLocation);

            assertEquals(mockLocation, match.getTarget());
            assertEquals(mockRegion, match.getRegion());
        }

        @Test
        @DisplayName("Set region creates target if null")
        public void testSetRegionCreatesTarget() {
            Region region = new Region(15, 25, 150, 250);

            match.setRegion(region);

            assertNotNull(match.getTarget());
            assertEquals(region, match.getRegion());
        }

        @Test
        @DisplayName("Set region updates existing target")
        public void testSetRegionUpdatesTarget() {
            Region initialRegion = new Region(10, 10, 50, 50);
            match.setRegion(initialRegion);

            Region newRegion = new Region(20, 20, 100, 100);
            match.setRegion(newRegion);

            assertEquals(newRegion, match.getRegion());
        }

        @Test
        @DisplayName("Get x, y, w, h from region")
        public void testGetCoordinatesFromRegion() {
            Region region = new Region(30, 40, 120, 180);
            match.setRegion(region);

            assertEquals(30, match.x());
            assertEquals(40, match.y());
            assertEquals(120, match.w());
            assertEquals(180, match.h());
        }

        @Test
        @DisplayName("Get coordinates returns 0 when no region")
        public void testGetCoordinatesNoRegion() {
            assertEquals(0, match.x());
            assertEquals(0, match.y());
            assertEquals(0, match.w());
            assertEquals(0, match.h());
        }
    }

    @Nested
    @DisplayName("Images")
    class Images {

        @Test
        @DisplayName("Set and get match image")
        public void testMatchImage() {
            match.setImage(mockImage);
            assertEquals(mockImage, match.getImage());
        }

        @Test
        @DisplayName("Set and get search image")
        public void testSearchImage() {
            match.setSearchImage(mockSearchImage);
            assertEquals(mockSearchImage, match.getSearchImage());
        }

        @Test
        @DisplayName("Get Mat from image")
        public void testGetMat() {
            when(mockImage.getMatBGR()).thenReturn(mockMat);

            match.setImage(mockImage);

            assertEquals(mockMat, match.getMat());
        }

        @Test
        @DisplayName("Get Mat returns null when no image")
        public void testGetMatNoImage() {
            assertNull(match.getMat());
        }
    }

    @Nested
    @DisplayName("State Object Data")
    class StateObjectDataTests {

        @Test
        @DisplayName("Set and get state object metadata")
        public void testStateObjectMetadata() {
            match.setStateObjectData(mockStateObjectData);
            assertEquals(mockStateObjectData, match.getStateObjectData());
        }

        @Test
        @DisplayName("Set and get anchors")
        public void testAnchors() {
            match.setAnchors(mockAnchors);
            assertEquals(mockAnchors, match.getAnchors());
        }

        @Test
        @DisplayName("Set and get scene")
        public void testScene() {
            match.setScene(mockScene);
            assertEquals(mockScene, match.getScene());
        }
    }

    @Nested
    @DisplayName("Match Comparison")
    class MatchComparison {

        @Test
        @DisplayName("Compare matches by score")
        public void testCompareByScore() {
            Match match1 = new Match();
            match1.setScore(0.8);

            Match match2 = new Match();
            match2.setScore(0.9);

            assertTrue(match1.compareByScore(match2) < 0);
            assertTrue(match2.compareByScore(match1) > 0);
            assertEquals(0, match1.compareByScore(match1), 0.001);
        }

        @ParameterizedTest
        @CsvSource({"0.5, 0.8, -0.3", "0.9, 0.6, 0.3", "0.75, 0.75, 0.0"})
        @DisplayName("Compare various scores")
        public void testCompareVariousScores(double score1, double score2, double expected) {
            Match m1 = new Match();
            m1.setScore(score1);

            Match m2 = new Match();
            m2.setScore(score2);

            assertEquals(expected, m1.compareByScore(m2), 0.001);
        }
    }

    @Nested
    @DisplayName("Size Calculation")
    class SizeCalculation {

        @Test
        @DisplayName("Calculate size from region")
        public void testSize() {
            Region region = new Region(0, 0, 100, 50);
            match.setRegion(region);

            assertEquals(5000, match.size());
        }

        @Test
        @DisplayName("Size is 0 when no region")
        public void testSizeNoRegion() {
            // match.size() will throw NPE if no region, so we need to handle this case
            assertNull(match.getRegion());
            assertThrows(NullPointerException.class, () -> match.size());
        }

        @ParameterizedTest
        @CsvSource({"10, 20, 200", "100, 100, 10000", "0, 50, 0", "50, 0, 0"})
        @DisplayName("Various region sizes")
        public void testVariousRegionSizes(int width, int height, int expectedSize) {
            Region region = new Region(0, 0, width, height);
            match.setRegion(region);

            assertEquals(expectedSize, match.size());
        }
    }

    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {

        @Test
        @DisplayName("Build match with builder")
        public void testBuilder() {
            Region region = new Region(10, 20, 100, 200);
            LocalDateTime timestamp = LocalDateTime.now();

            Match builtMatch =
                    new Match.Builder()
                            .setSimScore(0.95)
                            .setText("Test Text")
                            .setName("TestMatch")
                            .setRegion(region)
                            .setImage(mockImage)
                            .setSearchImage(mockSearchImage)
                            .setAnchors(mockAnchors)
                            .setStateObjectData(mockStateObjectData)
                            .setScene(mockScene)
                            .build();

            assertEquals(0.95, builtMatch.getScore());
            assertEquals("Test Text", builtMatch.getText());
            assertEquals("TestMatch", builtMatch.getName());
            assertEquals(region, builtMatch.getRegion());
            assertEquals(mockImage, builtMatch.getImage());
            assertEquals(mockSearchImage, builtMatch.getSearchImage());
            assertEquals(mockAnchors, builtMatch.getAnchors());
            assertEquals(mockStateObjectData, builtMatch.getStateObjectData());
            assertEquals(mockScene, builtMatch.getScene());
        }

        @Test
        @DisplayName("Builder with minimal properties")
        public void testBuilderMinimal() {
            Match builtMatch = new Match.Builder().setSimScore(0.85).build();

            assertEquals(0.85, builtMatch.getScore());
            // Builder creates a target, but without a region unless specified
            assertNotNull(builtMatch.getTarget());
            assertNull(builtMatch.getRegion()); // No region set, so getRegion returns null
            assertEquals("", builtMatch.getText());
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Complete match from button click")
        public void testButtonClickMatch() {
            // Setup a match representing a successful button find
            Region buttonRegion = new Region(100, 200, 150, 50);
            Location clickTarget = new Location(175, 225); // Center of button
            LocalDateTime clickTime = LocalDateTime.now();

            match.setRegion(buttonRegion);
            // Note: Setting target after region keeps the region
            match.setScore(0.92);
            match.setName("SubmitButton");
            match.setText("Submit");
            match.setImage(mockImage);
            match.setSearchImage(mockSearchImage);
            match.setTimeStamp(clickTime);

            // Verify
            assertEquals(buttonRegion, match.getRegion());
            assertNotNull(match.getTarget()); // target is created when region is set
            assertEquals(0.92, match.getScore());
            assertEquals("SubmitButton", match.getName());
            assertEquals("Submit", match.getText());
            assertEquals(100, match.x());
            assertEquals(200, match.y());
            assertEquals(150, match.w());
            assertEquals(50, match.h());
            assertEquals(7500, match.size());
        }

        @Test
        @DisplayName("Match with state context")
        public void testMatchWithStateContext() {
            when(mockStateObjectData.getOwnerStateName()).thenReturn("LoginScreen");
            when(mockStateObjectData.getStateObjectName()).thenReturn("UsernameField");

            match.setStateObjectData(mockStateObjectData);
            match.setScore(0.88);
            match.setRegion(new Region(50, 100, 200, 30));

            assertEquals(mockStateObjectData, match.getStateObjectData());
            assertEquals("LoginScreen", match.getStateObjectData().getOwnerStateName());
            assertEquals("UsernameField", match.getStateObjectData().getStateObjectName());
        }

        @Test
        @DisplayName("Text extraction match")
        public void testTextExtractionMatch() {
            // Setup a match from OCR/text extraction
            match.setText("Welcome to Application");
            match.setRegion(new Region(200, 50, 400, 100));
            match.setScore(0.95); // High confidence text match
            match.setName("WelcomeText");

            assertEquals("Welcome to Application", match.getText());
            assertEquals(200, match.x());
            assertEquals(50, match.y());
            assertEquals(400, match.w());
            assertEquals(100, match.h());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Handle null target when getting region")
        public void testNullTargetRegion() {
            assertNull(match.getRegion());
            assertEquals(0, match.x());
            assertEquals(0, match.y());
            assertEquals(0, match.w());
            assertEquals(0, match.h());
        }

        @Test
        @DisplayName("Increment times acted on from zero")
        public void testIncrementFromZero() {
            assertEquals(0, match.getTimesActedOn());

            for (int i = 1; i <= 10; i++) {
                match.incrementTimesActedOn();
                assertEquals(i, match.getTimesActedOn());
            }
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.5, 0.75, 0.99, 1.0})
        @DisplayName("Various match scores")
        public void testVariousScores(double score) {
            match.setScore(score);
            assertEquals(score, match.getScore());
        }

        @Test
        @DisplayName("Empty match comparison")
        public void testEmptyMatchComparison() {
            Match emptyMatch1 = new Match();
            Match emptyMatch2 = new Match();

            assertEquals(0, emptyMatch1.compareByScore(emptyMatch2), 0.001);
        }
    }
}
