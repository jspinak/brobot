package io.github.jspinak.brobot.action.composite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("PlaybackOptions Tests")
public class PlaybackOptionsTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("Builder Construction")
    class BuilderConstruction {

        @Test
        @DisplayName("Should create default playback options")
        public void testDefaultPlaybackOptions() {
            PlaybackOptions options = new PlaybackOptions.Builder().build();

            assertNotNull(options);
            assertEquals(-1, options.getStartPlayback());
            assertEquals(5.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should set start playback time")
        public void testSetStartPlayback() {
            PlaybackOptions options = new PlaybackOptions.Builder().setStartPlayback(10.5).build();

            assertEquals(10.5, options.getStartPlayback());
        }

        @Test
        @DisplayName("Should set playback duration")
        public void testSetPlaybackDuration() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setPlaybackDuration(30.0).build();

            assertEquals(30.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should support fluent builder pattern")
        public void testFluentBuilder() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(15.0)
                            .setPlaybackDuration(20.0)
                            .build();

            assertNotNull(options);
            assertEquals(15.0, options.getStartPlayback());
            assertEquals(20.0, options.getPlaybackDuration());
        }
    }

    @Nested
    @DisplayName("Copy Constructor")
    class CopyConstructor {

        @Test
        @DisplayName("Should copy all values from original")
        public void testCopyConstructor() {
            PlaybackOptions original =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(5.0)
                            .setPlaybackDuration(10.0)
                            .build();

            PlaybackOptions copy = new PlaybackOptions.Builder(original).build();

            assertEquals(original.getStartPlayback(), copy.getStartPlayback());
            assertEquals(original.getPlaybackDuration(), copy.getPlaybackDuration());
            assertNotSame(original, copy);
        }

        @Test
        @DisplayName("Should allow modification after copy")
        public void testModificationAfterCopy() {
            PlaybackOptions original =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(5.0)
                            .setPlaybackDuration(10.0)
                            .build();

            PlaybackOptions modified =
                    new PlaybackOptions.Builder(original).setStartPlayback(8.0).build();

            assertEquals(5.0, original.getStartPlayback());
            assertEquals(8.0, modified.getStartPlayback());
            assertEquals(10.0, modified.getPlaybackDuration());
        }
    }

    @Nested
    @DisplayName("Start Playback Values")
    class StartPlaybackValues {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 1.0, 5.5, 10.0, 100.0, 1000.0})
        @DisplayName("Should accept various positive start times")
        public void testPositiveStartTimes(double startTime) {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setStartPlayback(startTime).build();

            assertEquals(startTime, options.getStartPlayback());
        }

        @Test
        @DisplayName("Should accept -1 for dynamic start")
        public void testDynamicStart() {
            PlaybackOptions options = new PlaybackOptions.Builder().setStartPlayback(-1).build();

            assertEquals(-1, options.getStartPlayback());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-2.0, -10.0, -100.0})
        @DisplayName("Should accept negative values other than -1")
        public void testNegativeStartTimes(double startTime) {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setStartPlayback(startTime).build();

            assertEquals(startTime, options.getStartPlayback());
        }

        @Test
        @DisplayName("Should handle zero start time")
        public void testZeroStartTime() {
            PlaybackOptions options = new PlaybackOptions.Builder().setStartPlayback(0.0).build();

            assertEquals(0.0, options.getStartPlayback());
        }
    }

    @Nested
    @DisplayName("Playback Duration Values")
    class PlaybackDurationValues {

        @ParameterizedTest
        @ValueSource(doubles = {0.1, 1.0, 5.0, 10.0, 60.0, 3600.0})
        @DisplayName("Should accept various duration values")
        public void testVariousDurations(double duration) {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setPlaybackDuration(duration).build();

            assertEquals(duration, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should accept zero duration")
        public void testZeroDuration() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setPlaybackDuration(0.0).build();

            assertEquals(0.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should accept negative duration")
        public void testNegativeDuration() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setPlaybackDuration(-5.0).build();

            assertEquals(-5.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should handle very small durations")
        public void testVerySmallDuration() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setPlaybackDuration(0.001).build();

            assertEquals(0.001, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should handle very large durations")
        public void testVeryLargeDuration() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder().setPlaybackDuration(Double.MAX_VALUE).build();

            assertEquals(Double.MAX_VALUE, options.getPlaybackDuration());
        }
    }

    @Nested
    @DisplayName("Combined Configurations")
    class CombinedConfigurations {

        @ParameterizedTest
        @CsvSource({"0.0, 5.0", "10.0, 20.0", "-1, 15.0", "5.5, 10.5", "100.0, 0.1"})
        @DisplayName("Should handle various start and duration combinations")
        public void testStartAndDurationCombinations(double start, double duration) {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(start)
                            .setPlaybackDuration(duration)
                            .build();

            assertEquals(start, options.getStartPlayback());
            assertEquals(duration, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should represent recording segment")
        public void testRecordingSegment() {
            double start = 10.0;
            double duration = 15.0;

            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(start)
                            .setPlaybackDuration(duration)
                            .build();

            assertEquals(start, options.getStartPlayback());
            assertEquals(duration, options.getPlaybackDuration());

            // Calculate implied end time
            double impliedEndTime = start + duration;
            assertEquals(25.0, impliedEndTime);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("Should create immutable options")
        public void testImmutability() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(5.0)
                            .setPlaybackDuration(10.0)
                            .build();

            // Options should be final and immutable
            double originalStart = options.getStartPlayback();
            double originalDuration = options.getPlaybackDuration();

            // Create a new instance with different values
            PlaybackOptions options2 =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(8.0)
                            .setPlaybackDuration(15.0)
                            .build();

            // Original should remain unchanged
            assertEquals(originalStart, options.getStartPlayback());
            assertEquals(originalDuration, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should not share state between builders")
        public void testBuilderIndependence() {
            PlaybackOptions.Builder builder1 = new PlaybackOptions.Builder().setStartPlayback(5.0);

            PlaybackOptions.Builder builder2 = new PlaybackOptions.Builder().setStartPlayback(10.0);

            PlaybackOptions options1 = builder1.build();
            PlaybackOptions options2 = builder2.build();

            assertNotEquals(options1.getStartPlayback(), options2.getStartPlayback());
        }
    }

    @Nested
    @DisplayName("Builder Reuse")
    class BuilderReuse {

        @Test
        @DisplayName("Should allow multiple builds from same builder")
        public void testMultipleBuilds() {
            PlaybackOptions.Builder builder =
                    new PlaybackOptions.Builder().setStartPlayback(5.0).setPlaybackDuration(10.0);

            PlaybackOptions options1 = builder.build();
            PlaybackOptions options2 = builder.build();

            assertNotNull(options1);
            assertNotNull(options2);
            assertNotSame(options1, options2);
            assertEquals(options1.getStartPlayback(), options2.getStartPlayback());
            assertEquals(options1.getPlaybackDuration(), options2.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should allow builder modification between builds")
        public void testBuilderModificationBetweenBuilds() {
            PlaybackOptions.Builder builder = new PlaybackOptions.Builder();

            PlaybackOptions options1 = builder.setStartPlayback(5.0).build();

            PlaybackOptions options2 = builder.setStartPlayback(10.0).build();

            assertEquals(5.0, options1.getStartPlayback());
            assertEquals(10.0, options2.getStartPlayback());
        }
    }

    @Nested
    @DisplayName("Practical Use Cases")
    class PracticalUseCases {

        @Test
        @DisplayName("Should configure full recording playback")
        public void testFullRecordingPlayback() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(0.0)
                            .setPlaybackDuration(60.0)
                            .build();

            assertEquals(0.0, options.getStartPlayback());
            assertEquals(60.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should configure partial recording playback")
        public void testPartialRecordingPlayback() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(10.0)
                            .setPlaybackDuration(20.0)
                            .build();

            assertEquals(10.0, options.getStartPlayback());
            assertEquals(20.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should configure dynamic start playback")
        public void testDynamicStartPlayback() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(-1)
                            .setPlaybackDuration(30.0)
                            .build();

            assertEquals(-1, options.getStartPlayback());
            assertEquals(30.0, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should configure loop segment")
        public void testLoopSegment() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(5.0)
                            .setPlaybackDuration(2.0)
                            .build();

            assertEquals(5.0, options.getStartPlayback());
            assertEquals(2.0, options.getPlaybackDuration());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle Double.NaN values")
        public void testNaNValues() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(Double.NaN)
                            .setPlaybackDuration(Double.NaN)
                            .build();

            assertTrue(Double.isNaN(options.getStartPlayback()));
            assertTrue(Double.isNaN(options.getPlaybackDuration()));
        }

        @Test
        @DisplayName("Should handle infinity values")
        public void testInfinityValues() {
            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(Double.POSITIVE_INFINITY)
                            .setPlaybackDuration(Double.NEGATIVE_INFINITY)
                            .build();

            assertEquals(Double.POSITIVE_INFINITY, options.getStartPlayback());
            assertEquals(Double.NEGATIVE_INFINITY, options.getPlaybackDuration());
        }

        @Test
        @DisplayName("Should handle extreme precision")
        public void testExtremePrecision() {
            double preciseDuration = 10.123456789012345;
            double preciseStart = 5.987654321098765;

            PlaybackOptions options =
                    new PlaybackOptions.Builder()
                            .setStartPlayback(preciseStart)
                            .setPlaybackDuration(preciseDuration)
                            .build();

            assertEquals(preciseStart, options.getStartPlayback());
            assertEquals(preciseDuration, options.getPlaybackDuration());
        }
    }
}
