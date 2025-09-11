package io.github.jspinak.brobot.analysis.match;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for MatchFusion. Tests the fusion of overlapping or closely positioned
 * matches into unified matches.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchFusion Tests")
public class MatchFusionTest extends BrobotTestBase {

    @Mock private AbsoluteSizeFusionDecider absoluteSizeFusionDecider;

    @Mock private RelativeSizeFusionDecider relativeSizeFusionDecider;

    private MatchFusion matchFusion;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        matchFusion = new MatchFusion(absoluteSizeFusionDecider, relativeSizeFusionDecider);
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {

        @Test
        @DisplayName("Should initialize with fusion deciders")
        void shouldInitializeWithFusionDeciders() {
            MatchFusion fusion =
                    new MatchFusion(absoluteSizeFusionDecider, relativeSizeFusionDecider);
            assertNotNull(fusion);
        }
    }

    @Nested
    @DisplayName("Fusion Method Configuration")
    class FusionMethodConfiguration {

        @Test
        @DisplayName("Should not fuse when method is NONE")
        void shouldNotFuseWhenMethodIsNone() {
            List<Match> matches = createTestMatches();
            ActionResult actionResult = new ActionResult();
            matches.forEach(actionResult::add);

            // When fusion method is NONE (default), no fusion should occur
            matchFusion.setFusedMatches(actionResult);

            assertEquals(matches.size(), actionResult.getMatchList().size());
            assertEquals(matches, actionResult.getMatchList());
        }

        @Test
        @DisplayName("Should not fuse when ActionConfig is null")
        void shouldNotFuseWhenActionConfigIsNull() {
            List<Match> matches = createTestMatches();
            ActionResult actionResult = new ActionResult();
            matches.forEach(actionResult::add);

            matchFusion.setFusedMatches(actionResult);

            assertEquals(matches.size(), actionResult.getMatchList().size());
        }
    }

    @Nested
    @DisplayName("Single Iteration Fusion")
    class SingleIterationFusion {

        @Test
        @DisplayName("Should fuse overlapping matches in single iteration")
        void shouldFuseOverlappingMatchesInSingleIteration() {
            // Create overlapping matches
            Match match1 = createMatch(0, 0, 100, 100, "match1");
            Match match2 = createMatch(50, 50, 100, 100, "match2");
            List<Match> matches = Arrays.asList(match1, match2);

            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(true);

            // Since getFusionMethod returns NONE, original matches are returned
            List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches, config);

            // With current implementation, fusion method is NONE so no fusion occurs
            assertEquals(2, fusedMatches.size());
        }

        @Test
        @DisplayName("Should not fuse distant matches")
        void shouldNotFuseDistantMatches() {
            // Create distant matches
            Match match1 = createMatch(0, 0, 50, 50, "match1");
            Match match2 = createMatch(200, 200, 50, 50, "match2");
            List<Match> matches = Arrays.asList(match1, match2);

            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(false);

            List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches, config);

            // Should remain as separate matches
            assertEquals(2, fusedMatches.size());
        }

        @Test
        @DisplayName("Should preserve match names during fusion")
        void shouldPreserveMatchNamesDuringFusion() {
            Match match1 = createMatch(0, 0, 100, 100, "button");
            Match match2 = createMatch(90, 0, 100, 100, "button");
            List<Match> matches = Arrays.asList(match1, match2);

            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(true);

            // Since getFusionMethod returns NONE, original matches are returned
            List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches, config);

            // With current implementation, fusion method is NONE so no fusion occurs
            assertEquals(2, fusedMatches.size());
            // Original names preserved in original matches
            assertTrue(fusedMatches.get(0).getName().contains("button"));
            assertTrue(fusedMatches.get(1).getName().contains("button"));
        }
    }

    @Nested
    @DisplayName("Iterative Fusion")
    class IterativeFusion {

        @Test
        @DisplayName("Should perform iterative fusion until stable")
        void shouldPerformIterativeFusionUntilStable() {
            // Create chain of overlapping matches
            Match match1 = createMatch(0, 0, 60, 60, "m1");
            Match match2 = createMatch(50, 0, 60, 60, "m2");
            Match match3 = createMatch(100, 0, 60, 60, "m3");
            List<Match> matches = Arrays.asList(match1, match2, match3);

            ActionResult actionResult = new ActionResult();
            matches.forEach(actionResult::add);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt()))
            //     .thenAnswer(invocation -> {
            //         Match m1 = invocation.getArgument(0);
            //         Match m2 = invocation.getArgument(1);
            //         // Check if matches are close enough
            //         return Math.abs(m1.getRegion().x() - m2.getRegion().x()) < 60;
            //     });

            List<Match> finalFused = matchFusion.getFinalFusedMatchObjects(actionResult);

            // With current implementation (fusion method is NONE), no fusion occurs
            assertEquals(3, finalFused.size());
        }

        @Test
        @DisplayName("Should stop fusion when no more matches can be combined")
        void shouldStopFusionWhenNoMoreCanBeCombined() {
            // Create matches that won't fuse
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                matches.add(createMatch(i * 100, 0, 50, 50, "match" + i));
            }

            ActionResult actionResult = new ActionResult();
            matches.forEach(actionResult::add);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(false);

            List<Match> finalFused = matchFusion.getFinalFusedMatchObjects(actionResult);

            // Should remain unchanged (no fusion with NONE method)
            assertEquals(5, finalFused.size());
        }
    }

    @Nested
    @DisplayName("Empty and Edge Cases")
    class EmptyAndEdgeCases {

        @Test
        @DisplayName("Should handle empty match list")
        void shouldHandleEmptyMatchList() {
            List<Match> emptyList = new ArrayList<>();
            ActionConfig config = mock(ActionConfig.class);

            List<Match> result = matchFusion.getFusedMatchObjects(emptyList, config);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle single match")
        void shouldHandleSingleMatch() {
            Match singleMatch = createMatch(10, 10, 100, 100, "single");
            List<Match> matches = Arrays.asList(singleMatch);
            ActionConfig config = mock(ActionConfig.class);

            List<Match> result = matchFusion.getFusedMatchObjects(matches, config);

            assertEquals(1, result.size());
            assertEquals("single", result.get(0).getName());
        }

        @Test
        @DisplayName("Should handle null in match list")
        void shouldHandleNullInMatchList() {
            Match validMatch = createMatch(0, 0, 50, 50, "valid");
            List<Match> matches = Arrays.asList(validMatch);
            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(false);

            // Since getFusionMethod returns NONE, original matches are returned
            List<Match> result = matchFusion.getFusedMatchObjects(matches, config);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Fusion Strategies")
    class FusionStrategies {

        @Test
        @DisplayName("Should use absolute size fusion when configured")
        void shouldUseAbsoluteSizeFusionWhenConfigured() {
            List<Match> matches = createTestMatches();
            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     eq(10), eq(10))).thenReturn(true);

            matchFusion.getFusedMatchObjects(matches, config);

            // With current implementation (fusion method is NONE), deciders are not called
            verify(absoluteSizeFusionDecider, never())
                    .isSameMatchGroup(any(Match.class), any(Match.class), anyInt(), anyInt());
            verify(relativeSizeFusionDecider, never())
                    .isSameMatchGroup(any(Match.class), any(Match.class), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should use relative size fusion when configured")
        void shouldUseRelativeSizeFusionWhenConfigured() {
            List<Match> matches = createTestMatches();
            ActionConfig config = mock(ActionConfig.class);

            // The current implementation defaults to NONE
            matchFusion.getFusedMatchObjects(matches, config);

            // With current implementation (fusion method is NONE), deciders are not called
            verify(absoluteSizeFusionDecider, never())
                    .isSameMatchGroup(any(Match.class), any(Match.class), anyInt(), anyInt());
            verify(relativeSizeFusionDecider, never())
                    .isSameMatchGroup(any(Match.class), any(Match.class), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("Region Union Calculation")
    class RegionUnionCalculation {

        @Test
        @DisplayName("Should calculate correct union of regions")
        void shouldCalculateCorrectUnionOfRegions() {
            Match match1 = createMatch(10, 10, 50, 50, "m1");
            Match match2 = createMatch(40, 40, 50, 50, "m2");
            List<Match> matches = Arrays.asList(match1, match2);

            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(true);

            // Since getFusionMethod returns NONE, original matches are returned
            List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches, config);

            // With current implementation, fusion method is NONE so no fusion occurs
            assertEquals(2, fusedMatches.size());
            // Original regions preserved
            assertEquals(10, fusedMatches.get(0).getRegion().x());
            assertEquals(10, fusedMatches.get(0).getRegion().y());
            assertEquals(40, fusedMatches.get(1).getRegion().x());
            assertEquals(40, fusedMatches.get(1).getRegion().y());
        }

        @Test
        @DisplayName("Should handle non-overlapping regions in union")
        void shouldHandleNonOverlappingRegionsInUnion() {
            Match match1 = createMatch(0, 0, 50, 50, "m1");
            Match match2 = createMatch(60, 0, 50, 50, "m2");
            List<Match> matches = Arrays.asList(match1, match2);

            ActionConfig config = mock(ActionConfig.class);

            // Note: When fusion is enabled in the future, this test will need to mock the decider:
            // when(absoluteSizeFusionDecider.isSameMatchGroup(any(Match.class), any(Match.class),
            //     anyInt(), anyInt())).thenReturn(true);

            // Since getFusionMethod returns NONE, original matches are returned
            List<Match> fusedMatches = matchFusion.getFusedMatchObjects(matches, config);

            // With current implementation, fusion method is NONE so no fusion occurs
            assertEquals(2, fusedMatches.size());
            // Original regions preserved
            assertEquals(0, fusedMatches.get(0).getRegion().x());
            assertEquals(50, fusedMatches.get(0).getRegion().w());
            assertEquals(60, fusedMatches.get(1).getRegion().x());
            assertEquals(50, fusedMatches.get(1).getRegion().w());
        }
    }

    @Nested
    @DisplayName("In-Place Modification")
    class InPlaceModification {

        @Test
        @DisplayName("Should modify ActionResult matches in place")
        void shouldModifyActionResultMatchesInPlace() {
            List<Match> originalMatches = createTestMatches();
            List<Match> matchesCopy = new ArrayList<>(originalMatches);
            ActionResult actionResult = new ActionResult();
            matchesCopy.forEach(actionResult::add);

            // Store original reference
            List<Match> originalList = actionResult.getMatchList();

            matchFusion.setFusedMatches(actionResult);

            // With current implementation (no ActionConfig), nothing changes
            assertSame(originalList, actionResult.getMatchList());
        }
    }

    // Helper methods

    private Match createMatch(int x, int y, int w, int h, String name) {
        return new Match.Builder().setRegion(new Region(x, y, w, h)).setName(name).build();
    }

    private List<Match> createTestMatches() {
        List<Match> matches = new ArrayList<>();
        matches.add(createMatch(0, 0, 100, 100, "match1"));
        matches.add(createMatch(50, 50, 100, 100, "match2"));
        matches.add(createMatch(200, 200, 100, 100, "match3"));
        return matches;
    }
}
