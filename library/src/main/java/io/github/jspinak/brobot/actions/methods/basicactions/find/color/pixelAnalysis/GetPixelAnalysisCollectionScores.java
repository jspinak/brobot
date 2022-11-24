package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.SCORE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;
import static org.bytedeco.opencv.global.opencv_core.min;

@Component
public class GetPixelAnalysisCollectionScores {

    private GetPixelScores getPixelScores;

    public GetPixelAnalysisCollectionScores(GetPixelScores getPixelScores) {
        this.getPixelScores = getPixelScores;
    }

    /**
     * The overall scores for the entire PixelAnalysisCollection are the minimum of the scores for each PixelAnalysis.
     * @param pixelAnalysisCollection contains a list of PixelAnalysis objects, each with scores for BGR and HSV
     * @param colorSchemaName the color schema to use
     * @return the overall scores for the entire PixelAnalysisCollection
     */
    public Mat setScores(PixelAnalysisCollection pixelAnalysisCollection, ColorCluster.ColorSchemaName colorSchemaName) {
        if (pixelAnalysisCollection.getPixelAnalyses().isEmpty()) {
            Report.println("No pixel analyses to set scores for.");
            return new Mat();
        }
        List<PixelAnalysis> pixelAnalyses = pixelAnalysisCollection.getPixelAnalyses();
        PixelAnalysis firstAnalysis = pixelAnalyses.get(0);
        Size size = firstAnalysis.getAnalyses(PixelAnalysis.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName).size();
        int type = firstAnalysis.getAnalyses(PixelAnalysis.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName).type();
        Mat scores = new Mat(size, type, new Scalar(255, 255, 255, 255)); // initialize to worst score
        for (PixelAnalysis pixelAnalysis : pixelAnalysisCollection.getPixelAnalyses()) {
            Mat pixelScores = getPixelScores.setScores(pixelAnalysis, colorSchemaName);
            min(scores, pixelScores, scores);
        }
        return scores;
    }

    public void setScores(PixelAnalysisCollection pixelAnalysisCollection, ActionOptions actionOptions) {
        Mat scoresBGR = setScores(pixelAnalysisCollection, BGR);
        Mat scoresHSV = setScores(pixelAnalysisCollection, HSV);
        pixelAnalysisCollection.setAnalyses(SCORE, BGR, scoresBGR);
        pixelAnalysisCollection.setAnalyses(SCORE, HSV, scoresHSV);
        Mat scoreDistBelowThresholdBGR = getPixelScores.getDistBelowThreshhold(scoresBGR, actionOptions);
        Mat scoreDistBelowThresholdHSV = getPixelScores.getDistBelowThreshhold(scoresHSV, actionOptions);
        pixelAnalysisCollection.setAnalyses(SCORE_DIST_BELOW_THRESHHOLD, BGR, scoreDistBelowThresholdBGR);
        pixelAnalysisCollection.setAnalyses(SCORE_DIST_BELOW_THRESHHOLD, HSV, scoreDistBelowThresholdHSV);
    }
}
