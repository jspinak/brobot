package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static org.bytedeco.opencv.global.opencv_core.hconcat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Component
public class DrawClasses {

    private IllustrationFilename illustrationFilename;
    private ClassificationLegend classificationLegend;

    public DrawClasses(IllustrationFilename illustrationFilename, ClassificationLegend classificationLegend) {
        this.illustrationFilename = illustrationFilename;
        this.classificationLegend = classificationLegend;
    }

    public void paintClasses(Matches matches, ActionOptions actionOptions) {
        // CLASSIFY produces only one SceneAnalysisCollection, so we can use the first one
        matches.getSceneAnalysisCollection().getSceneAnalyses().forEach(sceneAnalysis -> {
            String outputPath = illustrationFilename.getFilenameFromMatchObjects(matches, actionOptions);
            writeImage(outputPath, sceneAnalysis);
        });
    }

    private void writeImage(String file, SceneAnalysis sceneAnalysis) {
        Mat legend = classificationLegend.draw(sceneAnalysis.getAnalysis(ColorCluster.ColorSchemaName.BGR, BGR_FROM_INDICES_2D), sceneAnalysis);
        Mat fused = fuseScreenAndLegend(sceneAnalysis.getAnalysis(ColorCluster.ColorSchemaName.BGR, BGR_FROM_INDICES_2D), legend);
        imwrite(file, fused);
    }

    private Mat fuseScreenAndLegend(Mat screen, Mat legend) {
        MatVector concatMats = new MatVector(screen, legend);
        Mat result = new Mat();
        hconcat(concatMats, result);
        return result;
    }

}
