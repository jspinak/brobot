package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysis.Analysis.SCORES;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.python.modules.math.atanh;

@Component
public class GetPixelScores {

    private MatVisualize matVisualize;

    private int outsideRangePenalty = 3; //100

    // for score conversion
    private double bestScore = 0; // the value is the same as the target, corresponds to ActionOptions score of 100
    private double worstScore = 255; // anything >= this is the same as an ActionOptions score of 0
    private double scoreRange = worstScore - bestScore;
    private double maxMinScoreForTanh = 3;
    private double maxTanh = Math.tanh(maxMinScoreForTanh);
    private double invMaxTanh = 1 - maxTanh;

    public GetPixelScores(MatVisualize matVisualize) {
        this.matVisualize = matVisualize;
    }

    /**
     * Set the pixel scores using the other Mats from the PixelAnalysis object.
     * A penalty Mat is calculated from the distance outside the range, and added to the Mat holding the
     * distance from the target value. Lower scores mean higher similarity.
     * distBelowThreshhold is not necessary because the minimum of all scores will be used,
     * and this is only 1 of many potential scores matrices.
     *
     * @param pixelAnalysis the PixelAnalysis object to use
     * @param colorSchemaName the color schema to use
     * @return the pixel scores
     */
    public Mat setScores(PixelAnalysis pixelAnalysis, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat distOut = pixelAnalysis.getAnalyses(PixelAnalysis.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName);
        if (distOut == null) {
            Report.println("No scores Mat for "+colorSchemaName+" pixel analysis.");
            return new Mat(new Scalar(255));
        }
        Mat pixelScores = new Mat(distOut.size(), distOut.type());
        Mat distTarget = pixelAnalysis.getAnalyses(PixelAnalysis.Analysis.DIST_TO_TARGET, colorSchemaName);
        addAnalysisToScores(distTarget, pixelScores);
        pixelAnalysis.setAnalyses(SCORES, colorSchemaName, pixelScores);
        return pixelScores;
    }

    private Mat getOutsideRangePenalties(Mat distOut) {
        Mat outsideRangePenalties = new Mat(distOut.size(), distOut.type());
        Mat penaltyMat = new Mat(distOut.size(), distOut.type(), Scalar.all(outsideRangePenalty));
        multiply(distOut, penaltyMat, outsideRangePenalties);
        return outsideRangePenalties;
    }

    private void addAnalysisToScores(Mat analysis, Mat scores) {
        analysis.convertTo(analysis, scores.type());
        add(analysis, scores, scores);
    }

    /**
     * The minScore in ActionOptions is not specific to the evaluation method.
     * Each class that defines an evaluation method, such as this class, should also have a score conversion method.
     *
     * The minScore is adjusted so that it has an effect on the scores. Otherwise, all scores above 2 would be the same.
     * tanh(3) = 0.9950547536867305
     * tanh(2) = 0.9640275800758169
     * tanh(1) = 0.7615941559557649
     * tanh(.83) = 0.7011682020026853
     * tanh(0) = 0
     *
     * @param actionOptionsScore the ActionOptions score to convert
     * @return the maxScore for this class's evaluation method
     */
    public double convertActionOptionsScoreToPixelAnalysisScoreWithTanh(double actionOptionsScore) {
        double adjustedMinScore = actionOptionsScore * maxMinScoreForTanh; // minScore is now between 0 and maxMinScoreForTanh
        double tanh = Math.tanh(adjustedMinScore); // tanh(1) = 0.7615941559557649, tanh(0) = 0
        double invertedTanh = 1 - tanh; // (1 - 0.7615941559557649) = 0.2384058440442351, 1 - 0 = 1
        double normalizedTanh = (invertedTanh - invMaxTanh) / (1 - invMaxTanh); // values between 0 and 1
        return normalizedTanh * scoreRange + bestScore; // values between bestScore and worstScore (0 and 255)
    }

    public double convertActionOptionsScoreToPixelAnalysisScore(double actionOptionsScore) {
        Report.println("scoreRange: "+scoreRange);
        Report.println("score" + actionOptionsScore);
        Report.println("bestScore" + bestScore);
        return (1 - actionOptionsScore) * scoreRange + bestScore;
    }

    /**
     * This method converts the PixelAnalysis score to an ActionOptions score.
     * It is passed to the ContourOps Builder to convert PixelAnalysis scores to ActionOptions scores to
     * be used with matches.
     * Here, we use the tanh function to convert the score to a value between 0 and 1.
     * Tanh(3) ~ 0.995, which is close enough to 1 to be used as the maxScore, and we stretch the values from 0 to 1
     * to 0 to 3.
     * Tanh(0.87) ~ 0.7, which is the default similarity score used with SikuliX. To maintain the same
     * default similarity score, the minScore is adjusted so that 0.7 corresponds to 0.87 in the tanh function.
     *
     *
     * @param pixelAnalysisScore the PixelAnalysis score to convert
     * @return the ActionOptions equivalent of the PixelAnalysis score
     */
    public double convertPixelAnalysisScoreToActionOptionsScoreWithTanh(double pixelAnalysisScore) {
        double normalized = (pixelAnalysisScore - worstScore) / scoreRange; // values between 0 and 1
        double invertedTanh = normalized * (1 - invMaxTanh) + invMaxTanh; // values between invMaxTanh and 1
        double tanh = 1 - invertedTanh; // [0, 1-invMaxTanh] lower pixel scores are better, higher similarity is better
        double adjustedMinScore = atanh(tanh); // values between 0 and maxMinScoreForTanh
        return adjustedMinScore / maxMinScoreForTanh; // values between 0 and 1
    }

    public double convertPixelAnalysisScoreToActionOptionsScore(double pixelAnalysisScore) {
        double scoreRange = worstScore - bestScore;
        return - ((pixelAnalysisScore - bestScore) / (scoreRange)) + 1;
    }

    /**
     * Subtract the pixel scores from the threshold. If the pixel score is at or above the threshold, it is set to 0.
     * Low pixel scores, or high scores below the threshold, mean high similarity.
     * This Mat can serve as a mask for other operations that activate pixels below the threshold. For example,
     * pixels that are not similar to any image will be considered no match.
     *
     * @param scores the pixel scores
     * @param actionOptions has minScore, which is converted to the score threshold specific to this class's evaluation method
     * @return the distances from scores to the threshold (0 if >= threshold)
     */
    public Mat getDistBelowThreshhold(Mat scores, ActionOptions actionOptions) {
        double threshold = convertActionOptionsScoreToPixelAnalysisScoreWithTanh(actionOptions.getMinScore());
        Mat binaryScores = new Mat(scores.size(), CV_8UC3);
        Mat thresholdMat = new Mat(scores.size(), CV_8UC3, Scalar.all(threshold));
        subtract(thresholdMat, scores, binaryScores, null, scores.type());
        return binaryScores;
    }
}
