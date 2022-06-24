package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pixel scores are calculated based on the target color distribution.
 * When the pixel falls outside the min-max boundaries the score receives a large penalty.
 *   Suggested: 180 for Hue, 256 for Saturation and Value.
 * Scores within the min-max boundaries receive small bonuses for being closer to the mean.
 */
@Component
public class GetScoresHSV {

    int outsideRangePenaltyH = 180;
    int outsideRangePenaltySV = 255;

    public ScoresMat getScores(Mat onScreen, Region region, StateImageObject stateImageObject,
                               ColorProfile targetColorProfile) {
        ScoresMat scoresMat = new ScoresMat();
        scoresMat.setOnScreen(onScreen);
        scoresMat.setTopLeft(new Location(region.getTopLeft()));
        scoresMat.setStateImageObject(stateImageObject);
        scoresMat.setScores(getScoresMat(onScreen, targetColorProfile));
        scoresMat.setColorProfile(targetColorProfile);
        return scoresMat;
    }

    public Mat getScoresMat(Mat onScreen, ColorProfile targetColorProfile) {
        List<Mat> channels = new ArrayList<>();
        Core.split(onScreen, channels);
        ColorProfile t = targetColorProfile;
        Mat hScore = getChannelScore(channels.get(0), t.getMinH(), t.getMaxH(), t.getMeanH(),
                t.getStdDevH(), outsideRangePenaltyH);
        Mat sScore = getChannelScore(channels.get(1), t.getMinS(), t.getMaxS(), t.getMeanS(),
                t.getStdDevS(), outsideRangePenaltySV);
        Mat vScore = getChannelScore(channels.get(2), t.getMinV(), t.getMaxV(), t.getMeanV(),
                t.getStdDevV(), outsideRangePenaltySV);
        // Hue should be .6 of max score, Sat and Val .2 each
        double maxH = Core.minMaxLoc(hScore).maxVal; //double sumH = Core.sumElems(hScore).val[0];
        double maxS = Core.minMaxLoc(sScore).maxVal;
        double maxV = Core.minMaxLoc(vScore).maxVal;
        double sum = maxH + maxS + maxV;
        double adjustH = .6 / (maxH / sum);
        double adjustS = .2 / (maxS / sum);
        double adjustV = .2 / (maxV / sum);
        //System.out.format("sums HSV: %.1f %.1f %.1f , adjusts HSV: %.1f %.1f %.1f \n",
        //        maxH, maxS, maxV, adjustH, adjustS, adjustV);
        Core.multiply(hScore, new Scalar(adjustH), hScore);
        Core.multiply(sScore, new Scalar(adjustS), sScore);
        Core.multiply(vScore, new Scalar(adjustV), vScore);
        // H, S, and V Mats have now been normalized
        Mat scores = new Mat(hScore.size(), 2);
        // add H, S, and V scores together for each cell
        Core.add(scores, hScore, scores);
        Core.add(scores, sScore, scores);
        Core.add(scores, vScore, scores);
        //System.out.println(hScore.dump());
        //System.out.println(sScore.dump());
        //System.out.println(vScore.dump());
        //System.out.println(scores);
        //System.out.println(scores.dump());
        return scores;
    }
    
    private Mat getChannelScore(Mat channel, double min, double max, double mean, double stddev, int penalty) {
        Mat minMat = new Mat(channel.size(), 0, new Scalar(min));
        Mat belowMin = new Mat(channel.size(), 0);
        Core.subtract(minMat, channel, belowMin, new Mat(), 0); // belowMin should now be 0 everywhere inside the min-max bounderies
        //System.out.println("belowMin = \n"+belowMin.dump());
        Mat belowPenalty = getPenaltyMat(belowMin, penalty); // belowPenalty is <penalty> or 0 in each cell
        //System.out.println("penaltyMatBelow = \n"+belowPenalty.dump());
        Mat aboveMax = new Mat(channel.size(), 0);
        Core.subtract(channel, new Scalar(max), aboveMax);
        Mat abovePenalty = getPenaltyMat(aboveMax, penalty);
        //System.out.println("penaltyMatAbove = \n"+abovePenalty.dump());
        Mat meanMat = new Mat(channel.size(), channel.type(), new Scalar(mean));
        Mat distFromMean = new Mat(channel.size(), 2);
        Core.absdiff(channel, meanMat, distFromMean); // absolute distance from mean
        //System.out.println("distFromMean = \n"+distFromMean.dump());
        //Core.divide(distFromMean, new Scalar(stddev), distFromMean); // divide by the stddev
        //Core.multiply(distFromMean, distFromMean, distFromMean); // squared
        //System.out.println("distFromMean adjusted for stddev= \n"+distFromMean.dump()); //ok
        //System.out.println(belowPenalty.dump());
        Mat scores = new Mat();
        Core.add(distFromMean, belowPenalty, scores, new Mat(), 2); // add penalty below min
        //System.out.println("scores with penalties:\n"+scores.dump());
        Core.add(scores, abovePenalty, scores, new Mat(), 2); // add penalty above max
        //System.out.println("scores with penalties:\n"+scores.dump());
        Scalar size = new Scalar(scores.rows()*scores.cols());
        //System.out.println("channel scores = \n"+scores.dump());
        //Core.divide(scores, size, scores); // we will be adding all cells later, it's better to have smaller numbers
        return scores;
    }

    /*
    Return a Mat with the penalty in all non-zero cells.
     */
    private Mat getPenaltyMat(Mat mat, int outsideRangePenalty) {
        Mat penaltyMat = new Mat(mat.size(), 0);
        mat.copyTo(penaltyMat);
        Core.min(penaltyMat, new Scalar(1), penaltyMat);
        Core.multiply(penaltyMat, new Scalar(outsideRangePenalty), penaltyMat);
        return penaltyMat;
    }

}
