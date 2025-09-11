package io.github.jspinak.brobot.tools.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.draw.DrawClassesLegend;
import io.github.jspinak.brobot.tools.history.draw.DrawContours;
import io.github.jspinak.brobot.tools.history.draw.DrawMatch;
import io.github.jspinak.brobot.tools.history.draw.DrawRect;
import io.github.jspinak.brobot.tools.history.visual.AnalysisSidebar;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

/**
 * Tests for VisualizationOrchestrator class. Verifies illustration orchestration and drawing
 * coordination.
 */
public class VisualizationOrchestratorTest extends BrobotTestBase {

    @Mock private AnalysisSidebar mockSidebar;

    @Mock private HistoryFileNamer mockFileNamer;

    @Mock private DrawMatch mockDrawMatch;

    @Mock private DrawRect mockDrawRect;

    @Mock private DrawClassesLegend mockDrawClassesLegend;

    @Mock private ImageFileUtilities mockImageUtils;

    @Mock private ActionVisualizer mockActionVisualizer;

    @Mock private DrawContours mockDrawContours;

    @Mock private Visualization mockVisualization;

    @Mock private SceneAnalysis mockSceneAnalysis;

    @Mock private SceneAnalyses mockSceneAnalysisCollection;

    private VisualizationOrchestrator orchestrator;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        orchestrator =
                new VisualizationOrchestrator(
                        mockSidebar,
                        mockFileNamer,
                        mockDrawMatch,
                        mockDrawRect,
                        mockDrawClassesLegend,
                        mockImageUtils,
                        mockActionVisualizer,
                        mockDrawContours);
    }

    @Nested
    @DisplayName("Basic Drawing Tests")
    class BasicDrawingTests {

        @Test
        @DisplayName("Should orchestrate drawing for single scene")
        public void testDrawSingleScene() {
            // Setup
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions = Arrays.asList(new Region(0, 0, 100, 100));
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            when(mockFileNamer.getFilenameFromSceneAnalysis(any(), any())).thenReturn("test.png");
            when(mockSceneAnalysis.getIllustrations()).thenReturn(mockVisualization);
            when(mockSceneAnalysisCollection.getSceneAnalyses())
                    .thenReturn(Collections.singletonList(mockSceneAnalysis));
            when(mockSceneAnalysisCollection.getAllIllustratedScenes())
                    .thenReturn(Collections.singletonList(mockVisualization));
            when(mockVisualization.getFinishedMats())
                    .thenReturn(Collections.singletonList(new Mat()));
            when(mockVisualization.getFilenames())
                    .thenReturn(Collections.singletonList("test.png"));

            // Execute
            orchestrator.draw(result, searchRegions, config);

            // Verify drawing sequence
            verify(mockDrawRect)
                    .drawRectAroundMatch(
                            eq(mockVisualization), eq(searchRegions), any(Scalar.class));
            verify(mockDrawContours).draw(mockSceneAnalysis);
            verify(mockDrawMatch).drawMatches(mockVisualization, result);
            verify(mockSidebar).drawSidebars(eq(mockVisualization), eq(result), eq(config), any());
            verify(mockSidebar).mergeSceneAndSidebar(mockVisualization);
            verify(mockImageUtils).writeAllWithUniqueFilename(any(), any());
        }

        @Test
        @DisplayName("Should orchestrate drawing for multiple scenes")
        public void testDrawMultipleScenes() {
            // Setup
            ActionResult result = createActionResultWithScenes(3);
            List<Region> searchRegions = Collections.emptyList();
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            SceneAnalysis scene1 = mock(SceneAnalysis.class);
            SceneAnalysis scene2 = mock(SceneAnalysis.class);
            SceneAnalysis scene3 = mock(SceneAnalysis.class);

            Visualization viz1 = mock(Visualization.class);
            Visualization viz2 = mock(Visualization.class);
            Visualization viz3 = mock(Visualization.class);

            when(scene1.getIllustrations()).thenReturn(viz1);
            when(scene2.getIllustrations()).thenReturn(viz2);
            when(scene3.getIllustrations()).thenReturn(viz3);

            when(mockSceneAnalysisCollection.getSceneAnalyses())
                    .thenReturn(Arrays.asList(scene1, scene2, scene3));
            when(mockSceneAnalysisCollection.getAllIllustratedScenes())
                    .thenReturn(Arrays.asList(viz1, viz2, viz3));

            when(mockFileNamer.getFilenameFromSceneAnalysis(any(), any()))
                    .thenReturn("test1.png", "test2.png", "test3.png");

            // Mock Mat and filenames for each visualization
            for (Visualization viz : Arrays.asList(viz1, viz2, viz3)) {
                when(viz.getFinishedMats()).thenReturn(Collections.singletonList(new Mat()));
                when(viz.getFilenames()).thenReturn(Collections.singletonList("test.png"));
            }

            // Execute
            orchestrator.draw(result, searchRegions, config);

            // Verify each scene was processed
            verify(mockDrawMatch, times(3)).drawMatches(any(Visualization.class), eq(result));
            verify(mockSidebar, times(3)).drawSidebars(any(), eq(result), eq(config), any());
            verify(mockImageUtils, times(3)).writeAllWithUniqueFilename(any(), any());
        }
    }

    @Nested
    @DisplayName("Action-Specific Drawing Tests")
    class ActionSpecificDrawingTests {

        @Test
        @DisplayName("Should draw click action visualization")
        public void testDrawClickAction() {
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions = Collections.emptyList();
            ClickOptions config = new ClickOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            verify(mockActionVisualizer).drawClick(mockVisualization, result);
        }

        @Test
        @DisplayName("Should draw drag action visualization")
        public void testDrawDragAction() {
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions = Collections.emptyList();
            DragOptions config = new DragOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            verify(mockActionVisualizer).drawDrag(mockVisualization, result);
        }

        @Test
        @DisplayName("Should draw move action visualization")
        public void testDrawMoveAction() {
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions = Collections.emptyList();
            MouseMoveOptions config = new MouseMoveOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            // Verify drawMove was called without varargs
            verify(mockActionVisualizer).drawMove(eq(mockVisualization), eq(result));
        }

        @Test
        @DisplayName("Should draw define region visualization")
        public void testDrawDefineRegion() {
            ActionResult result = createActionResultWithScene();
            result.setDefinedRegions(Collections.singletonList(new Region(50, 50, 200, 200)));
            List<Region> searchRegions = Collections.emptyList();
            DefineRegionOptions config = new DefineRegionOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            verify(mockActionVisualizer).drawDefinedRegion(mockVisualization, result);
        }
    }

    @Nested
    @DisplayName("Classification Legend Tests")
    class ClassificationLegendTests {

        @Test
        @DisplayName("Should not draw legend for non-classification action")
        public void testNoLegendForNonClassify() {
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions = Collections.emptyList();
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            // PatternFindOptions doesn't contain "Classify", "Histogram", or "Color"
            verify(mockDrawClassesLegend, never()).drawLegend(any(), any(), any());
            verify(mockDrawClassesLegend, never()).mergeClassesAndLegend(any());
        }
    }

    @Nested
    @DisplayName("Search Region Drawing Tests")
    class SearchRegionDrawingTests {

        @Test
        @DisplayName("Should draw search regions in blue")
        public void testDrawSearchRegionsInBlue() {
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions =
                    Arrays.asList(new Region(0, 0, 100, 100), new Region(200, 200, 150, 150));
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            // Verify blue color is used for search regions
            ArgumentCaptor<Scalar> colorCaptor = ArgumentCaptor.forClass(Scalar.class);
            verify(mockDrawRect)
                    .drawRectAroundMatch(
                            eq(mockVisualization), eq(searchRegions), colorCaptor.capture());

            Scalar capturedColor = colorCaptor.getValue();
            assertNotNull(capturedColor);
            // Blue regions use Scalar(235, 206, 135, 0)
            assertEquals(235, capturedColor.get(0), 0.01);
            assertEquals(206, capturedColor.get(1), 0.01);
            assertEquals(135, capturedColor.get(2), 0.01);
        }

        @Test
        @DisplayName("Should handle empty search regions")
        public void testEmptySearchRegions() {
            ActionResult result = createActionResultWithScene();
            List<Region> searchRegions = Collections.emptyList();
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            setupMocksForDraw();

            orchestrator.drawIllustrations(result, searchRegions, config);

            // Should still call drawRect even with empty list
            verify(mockDrawRect)
                    .drawRectAroundMatch(
                            eq(mockVisualization), eq(searchRegions), any(Scalar.class));
        }
    }

    @Nested
    @DisplayName("Filename Management Tests")
    class FilenameManagementTests {

        @Test
        @DisplayName("Should set unique filenames for each scene")
        public void testUniqueFilenames() {
            ActionResult result = createActionResultWithScenes(2);
            List<Region> searchRegions = Collections.emptyList();
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            SceneAnalysis scene1 = mock(SceneAnalysis.class);
            SceneAnalysis scene2 = mock(SceneAnalysis.class);
            Visualization viz1 = mock(Visualization.class);
            Visualization viz2 = mock(Visualization.class);

            when(scene1.getIllustrations()).thenReturn(viz1);
            when(scene2.getIllustrations()).thenReturn(viz2);

            when(mockSceneAnalysisCollection.getSceneAnalyses())
                    .thenReturn(Arrays.asList(scene1, scene2));

            when(mockFileNamer.getFilenameFromSceneAnalysis(scene1, config))
                    .thenReturn("scene1.png");
            when(mockFileNamer.getFilenameFromSceneAnalysis(scene2, config))
                    .thenReturn("scene2.png");

            orchestrator.drawIllustrations(result, searchRegions, config);

            verify(viz1).setFilenames("scene1.png");
            verify(viz2).setFilenames("scene2.png");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null scene analysis gracefully")
        public void testNullSceneAnalysis() {
            ActionResult result = new ActionResult();
            result.setSceneAnalysisCollection(mockSceneAnalysisCollection);
            when(mockSceneAnalysisCollection.getSceneAnalyses())
                    .thenReturn(Collections.emptyList());
            when(mockSceneAnalysisCollection.getAllIllustratedScenes())
                    .thenReturn(Collections.emptyList());

            List<Region> searchRegions = Collections.emptyList();
            PatternFindOptions config = new PatternFindOptions.Builder().build();

            // Should not throw exception
            assertDoesNotThrow(() -> orchestrator.draw(result, searchRegions, config));

            // Should not attempt to write files
            verify(mockImageUtils, never()).writeAllWithUniqueFilename(any(), any());
        }
    }

    // Helper methods

    private ActionResult createActionResultWithScene() {
        ActionResult result = new ActionResult();
        result.setSceneAnalysisCollection(mockSceneAnalysisCollection);
        result.setMatchList(Arrays.asList(new Match.Builder().setRegion(10, 10, 20, 20).build()));
        return result;
    }

    private ActionResult createActionResultWithScenes(int count) {
        ActionResult result = new ActionResult();
        result.setSceneAnalysisCollection(mockSceneAnalysisCollection);
        return result;
    }

    private void setupMocksForDraw() {
        when(mockSceneAnalysis.getIllustrations()).thenReturn(mockVisualization);
        when(mockSceneAnalysisCollection.getSceneAnalyses())
                .thenReturn(Collections.singletonList(mockSceneAnalysis));
        when(mockFileNamer.getFilenameFromSceneAnalysis(any(), any())).thenReturn("test.png");
        when(mockSceneAnalysis.getMatchList()).thenReturn(Collections.emptyList());
    }
}
