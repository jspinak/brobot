package io.github.jspinak.brobot.analysis.compare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.EmptyMatch;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;

/**
 * Comprehensive test suite for ImageComparer. Tests image comparison and pattern matching
 * functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageComparer Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.CV)
@Tag(TestCategories.IMAGE)
@Tag(TestCategories.FAST)
@Tag(TestCategories.CI_SAFE)
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class ImageComparerTest extends BrobotTestBase {

    @Mock private ExecutionModeController mockOrLive;

    @Mock private SizeComparator compareSize;

    @InjectMocks private ImageComparer imageComparer;

    @Mock private StateImage stateImage1;

    @Mock private StateImage stateImage2;

    @Mock private Pattern pattern1;

    @Mock private Pattern pattern2;

    @Mock private BufferedImage bufferedImage1;

    @Mock private BufferedImage bufferedImage2;

    @Mock private Image image1;

    @Mock private Image image2;

    @Mock private Match match1;

    @Mock private Match match2;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Setup default mock behaviors with lenient() to avoid unnecessary stubbing errors
        lenient().when(pattern1.getBImage()).thenReturn(bufferedImage1);
        lenient().when(pattern2.getBImage()).thenReturn(bufferedImage2);
        lenient().when(pattern1.getNameWithoutExtension()).thenReturn("Pattern1");
        lenient().when(pattern2.getNameWithoutExtension()).thenReturn("Pattern2");
        lenient().when(pattern1.getImage()).thenReturn(image1);
        lenient().when(pattern2.getImage()).thenReturn(image2);

        lenient().when(stateImage1.getPatterns()).thenReturn(Collections.singletonList(pattern1));
        lenient().when(stateImage2.getPatterns()).thenReturn(Collections.singletonList(pattern2));

        lenient().when(match1.getScore()).thenReturn(0.8);
        lenient().when(match2.getScore()).thenReturn(0.9);
    }

    @Nested
    @DisplayName("Pattern Comparison")
    class PatternComparison {

        @Test
        @DisplayName("Should compare two valid patterns successfully")
        void shouldCompareTwoValidPatterns() {
            List<Pattern> sortedPatterns = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(pattern1, pattern2))
                    .thenReturn(sortedPatterns);
            when(mockOrLive.findAll(eq(pattern1), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1));

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertEquals(match1, result);
            verify(compareSize).getEnvelopedFirstOrNone(pattern1, pattern2);
            verify(mockOrLive).findAll(eq(pattern1), any(Scene.class));
        }

        @Test
        @DisplayName("Should return EmptyMatch when first pattern is null")
        void shouldReturnEmptyMatchForNullFirstPattern() {
            Match result = imageComparer.compare(null, pattern2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
            verify(compareSize, never()).getEnvelopedFirstOrNone(any(), any());
        }

        @Test
        @DisplayName("Should return EmptyMatch when second pattern is null")
        void shouldReturnEmptyMatchForNullSecondPattern() {
            Match result = imageComparer.compare(pattern1, null);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
            verify(compareSize, never()).getEnvelopedFirstOrNone(any(), any());
        }

        @Test
        @DisplayName("Should return EmptyMatch when first pattern has null BufferedImage")
        void shouldReturnEmptyMatchForNullFirstBufferedImage() {
            when(pattern1.getBImage()).thenReturn(null);

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
        }

        @Test
        @DisplayName("Should return EmptyMatch when second pattern has null BufferedImage")
        void shouldReturnEmptyMatchForNullSecondBufferedImage() {
            when(pattern2.getBImage()).thenReturn(null);

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
        }

        @Test
        @DisplayName("Should return EmptyMatch when size comparison returns empty list")
        void shouldReturnEmptyMatchWhenSizeComparisonFails() {
            when(compareSize.getEnvelopedFirstOrNone(pattern1, pattern2))
                    .thenReturn(Collections.emptyList());

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
            verify(mockOrLive, never()).findAll(any(), any());
        }

        @Test
        @DisplayName("Should return best scoring match from multiple matches")
        void shouldReturnBestScoringMatch() {
            List<Pattern> sortedPatterns = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(pattern1, pattern2))
                    .thenReturn(sortedPatterns);
            when(mockOrLive.findAll(eq(pattern1), any(Scene.class)))
                    .thenReturn(Arrays.asList(match1, match2));

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertEquals(match2, result); // match2 has higher score (0.9)
        }

        @Test
        @DisplayName("Should create EmptyMatch with metadata when no matches found")
        void shouldCreateEmptyMatchWithMetadata() {
            List<Pattern> sortedPatterns = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(pattern1, pattern2))
                    .thenReturn(sortedPatterns);
            when(mockOrLive.findAll(eq(pattern1), any(Scene.class)))
                    .thenReturn(Collections.emptyList());

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
            verify(mockOrLive).findAll(eq(pattern1), any(Scene.class));
        }
    }

    @Nested
    @DisplayName("StateImage Comparison")
    class StateImageComparison {

        @Test
        @DisplayName("Should compare two StateImages with single patterns")
        void shouldCompareTwoStateImagesWithSinglePatterns() {
            List<Pattern> sortedPatterns = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(pattern1, pattern2))
                    .thenReturn(sortedPatterns);
            when(mockOrLive.findAll(eq(pattern1), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1));

            Match result = imageComparer.compare(stateImage1, stateImage2);

            assertNotNull(result);
            assertEquals(match1, result);
        }

        @Test
        @DisplayName("Should compare StateImages with multiple patterns")
        void shouldCompareStateImagesWithMultiplePatterns() {
            Pattern pattern3 = mock(Pattern.class);
            Pattern pattern4 = mock(Pattern.class);
            when(pattern3.getBImage()).thenReturn(bufferedImage1);
            when(pattern4.getBImage()).thenReturn(bufferedImage2);

            when(stateImage1.getPatterns()).thenReturn(Arrays.asList(pattern1, pattern3));
            when(stateImage2.getPatterns()).thenReturn(Arrays.asList(pattern2, pattern4));

            List<Pattern> sortedPatterns = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(any(Pattern.class), any(Pattern.class)))
                    .thenReturn(sortedPatterns);

            Match match3 = mock(Match.class);
            when(match3.getScore()).thenReturn(0.95);

            when(mockOrLive.findAll(any(Pattern.class), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match2))
                    .thenReturn(Collections.singletonList(match3))
                    .thenReturn(Collections.singletonList(match1));

            Match result = imageComparer.compare(stateImage1, stateImage2);

            assertNotNull(result);
            assertEquals(match3, result); // Highest score
            verify(mockOrLive, times(4)).findAll(any(Pattern.class), any(Scene.class));
        }

        @Test
        @DisplayName("Should handle StateImages with empty pattern lists")
        void shouldHandleEmptyPatternLists() {
            when(stateImage1.getPatterns()).thenReturn(Collections.emptyList());
            when(stateImage2.getPatterns()).thenReturn(Collections.emptyList());

            Match result = imageComparer.compare(stateImage1, stateImage2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
            verify(mockOrLive, never()).findAll(any(), any());
        }

        @Test
        @DisplayName("Should handle StateImage with single empty pattern list")
        void shouldHandleSingleEmptyPatternList() {
            when(stateImage1.getPatterns()).thenReturn(Collections.emptyList());

            Match result = imageComparer.compare(stateImage1, stateImage2);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
        }
    }

    @Nested
    @DisplayName("StateImage List Comparison")
    class StateImageListComparison {

        @Test
        @DisplayName("Should compare list of StateImages against single StateImage")
        void shouldCompareListOfStateImages() {
            List<StateImage> stateImages = Arrays.asList(stateImage1, stateImage2);
            StateImage targetStateImage = mock(StateImage.class);
            Pattern targetPattern = mock(Pattern.class);

            when(targetPattern.getBImage()).thenReturn(bufferedImage1);
            when(targetPattern.getNameWithoutExtension()).thenReturn("TargetPattern");
            Image targetPatternImage = mock(Image.class);
            when(targetPattern.getImage()).thenReturn(targetPatternImage);
            when(targetStateImage.getPatterns())
                    .thenReturn(Collections.singletonList(targetPattern));

            List<Pattern> sortedPatterns = Arrays.asList(pattern1, targetPattern);
            when(compareSize.getEnvelopedFirstOrNone(any(Pattern.class), eq(targetPattern)))
                    .thenReturn(sortedPatterns);

            when(mockOrLive.findAll(any(Pattern.class), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match2));

            Match result = imageComparer.compare(stateImages, targetStateImage);

            assertNotNull(result);
            assertEquals(match2, result); // Higher score
            verify(mockOrLive, times(2)).findAll(any(Pattern.class), any(Scene.class));
        }

        @Test
        @DisplayName("Should handle empty StateImage list")
        void shouldHandleEmptyStateImageList() {
            List<StateImage> emptyList = Collections.emptyList();

            Match result = imageComparer.compare(emptyList, stateImage1);

            assertNotNull(result);
            assertTrue(result instanceof EmptyMatch);
        }

        @Test
        @DisplayName("Should return best match from large list")
        void shouldReturnBestMatchFromLargeList() {
            List<StateImage> stateImages = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                StateImage img = mock(StateImage.class);
                Pattern p = mock(Pattern.class);
                when(p.getBImage()).thenReturn(bufferedImage1);
                when(img.getPatterns()).thenReturn(Collections.singletonList(p));
                stateImages.add(img);
            }

            List<Pattern> sortedPatterns = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(any(Pattern.class), any(Pattern.class)))
                    .thenReturn(sortedPatterns);

            Match bestMatch = mock(Match.class);
            when(bestMatch.getScore()).thenReturn(0.99);

            // Return different scores for each comparison
            when(mockOrLive.findAll(any(Pattern.class), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match2))
                    .thenReturn(Collections.singletonList(bestMatch))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match1))
                    .thenReturn(Collections.singletonList(match1));

            Match result = imageComparer.compare(stateImages, stateImage2);

            assertNotNull(result);
            assertEquals(bestMatch, result);
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should handle complex nested comparison scenario")
        void shouldHandleComplexNestedComparison() {
            // Use lenient mode for complex test with many mocks
            lenient()
                    .when(
                            compareSize.getEnvelopedFirstOrNone(
                                    any(Pattern.class), any(Pattern.class)))
                    .thenReturn(Arrays.asList(pattern1, pattern2));
            lenient()
                    .when(mockOrLive.findAll(any(Pattern.class), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1));
            // Create multiple patterns for each StateImage
            List<Pattern> patterns1 = new ArrayList<>();
            List<Pattern> patterns2 = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                Pattern p1 = mock(Pattern.class);
                Pattern p2 = mock(Pattern.class);
                BufferedImage img1 = mock(BufferedImage.class);
                BufferedImage img2 = mock(BufferedImage.class);

                lenient().when(p1.getBImage()).thenReturn(img1);
                lenient().when(p1.getNameWithoutExtension()).thenReturn("P1-" + i);
                Image pImage1 = mock(Image.class);
                Image pImage2 = mock(Image.class);
                lenient().when(p1.getImage()).thenReturn(pImage1);

                lenient().when(p2.getBImage()).thenReturn(img2);
                lenient().when(p2.getNameWithoutExtension()).thenReturn("P2-" + i);
                lenient().when(p2.getImage()).thenReturn(pImage2);

                patterns1.add(p1);
                patterns2.add(p2);
            }

            lenient().when(stateImage1.getPatterns()).thenReturn(patterns1);
            lenient().when(stateImage2.getPatterns()).thenReturn(patterns2);

            Match result = imageComparer.compare(stateImage1, stateImage2);

            assertNotNull(result);
            verify(mockOrLive, times(9))
                    .findAll(any(Pattern.class), any(Scene.class)); // 3x3 combinations
        }

        @Test
        @DisplayName("Should handle size comparison edge cases")
        void shouldHandleSizeComparisonEdgeCases() {
            // Test when patterns are exactly the same size
            List<Pattern> sameSize = Arrays.asList(pattern1, pattern2);
            when(compareSize.getEnvelopedFirstOrNone(pattern1, pattern2)).thenReturn(sameSize);
            when(mockOrLive.findAll(eq(pattern1), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1));

            Match result = imageComparer.compare(pattern1, pattern2);

            assertNotNull(result);
            assertEquals(match1, result);
        }
    }

    @Nested
    @DisplayName("Performance and Memory")
    class PerformanceAndMemory {

        @Test
        @DisplayName("Should handle large number of patterns efficiently")
        void shouldHandleLargeNumberOfPatterns() {
            // Create StateImages with many patterns
            List<Pattern> manyPatterns1 = new ArrayList<>();
            List<Pattern> manyPatterns2 = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                Pattern p1 = mock(Pattern.class);
                Pattern p2 = mock(Pattern.class);
                when(p1.getBImage()).thenReturn(bufferedImage1);
                when(p2.getBImage()).thenReturn(bufferedImage2);
                manyPatterns1.add(p1);
                manyPatterns2.add(p2);
            }

            when(stateImage1.getPatterns()).thenReturn(manyPatterns1);
            when(stateImage2.getPatterns()).thenReturn(manyPatterns2);

            when(compareSize.getEnvelopedFirstOrNone(any(Pattern.class), any(Pattern.class)))
                    .thenReturn(Arrays.asList(manyPatterns1.get(0), manyPatterns2.get(0)));

            when(mockOrLive.findAll(any(Pattern.class), any(Scene.class)))
                    .thenReturn(Collections.singletonList(match1));

            long startTime = System.currentTimeMillis();
            Match result = imageComparer.compare(stateImage1, stateImage2);
            long endTime = System.currentTimeMillis();

            assertNotNull(result);
            assertTrue(endTime - startTime < 1000, "Comparison should complete within 1 second");
        }
    }
}
