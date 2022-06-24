package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The overall strategy should be:
 * 1. read the image(s) from file as hsv (all filenames)
 * 2. get the main color(s) to search for based on k-means
 * 3. find the range of this color in the image
 * 4. construct a min-max-mean-stdev object for this color (similarity will be a function of stdev)
 * 5. get a Mat of the screen region as hsv
 * 6. create a mask of the color range
 * 7. use Canny to get contours from the mask, use findContours to get a List of MatOfPoint
 * 8. convert the contours from the MatOfPoint objects to MatchObjects
 *
 */
@Component
public class FindColorClusters {

    private SelectColors selectColors;
    private SelectRegions selectRegions;
    private Time time;
    private ColorComposition colorComposition;

    public FindColorClusters(SelectColors selectColors, SelectRegions selectRegions,
                             Time time, ColorComposition colorComposition) {
        this.selectColors = selectColors;
        this.selectRegions = selectRegions;
        this.time = time;
        this.colorComposition = colorComposition;
    }

    // actionOptions has a minSimilarity, which can be converted into a maxDistance for selecting regions of color
    public Matches getMatches(ActionOptions actionOptions, List<StateImageObject> images) {
        ColorClusters colorClusters = new ColorClusters();
        if (actionOptions.getDiameter() < 0) return new Matches();
        images.forEach(img -> colorClusters.addAllClusters(forOneImage(actionOptions, img)));
        colorClusters.sort(actionOptions.getColor());
        int maxMatchObjects = actionOptions.getMaxMatchesToActOn();
        Matches matches = new Matches();
        colorClusters.getClusters().forEach(cc -> {
            if (maxMatchObjects <= 0 || matches.size() < maxMatchObjects) {
                try {
                    matches.add(
                            new MatchObject(
                                    new Match(cc.getRegion(), cc.getScore()),
                                    cc.getStateImageObject(),
                                    time.getDuration(ActionOptions.Action.FIND).getSeconds()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }});
        Report.println("cluster size = "+colorClusters.getClusters().size());
        for (int i=0; i<Math.min(colorClusters.getClusters().size(), 10); i++) {
            ColorCluster cc = colorClusters.getClusters().get(i);
            Report.print(cc.getImage().dump());
            Report.formatln(" score=%.1f x.y=%d.%d", cc.getScore(), cc.getRegion().x, cc.getRegion().y);
        }
        return matches;
    }

    private ColorClusters forOneImage(ActionOptions actionOptions, StateImageObject image) {
        List<Region> searchRegions = selectRegions.getRegions(actionOptions, image);
        ColorClusters colorClusters = new ColorClusters();
        if (actionOptions.getDiameter() == 0) // diameter=0 looks for single pixels
            onePixelClusters(searchRegions, colorClusters, actionOptions, image);
        else multiPixelClusters(searchRegions, colorClusters, actionOptions, image);
        return colorClusters;
    }

    /**
     * Used to find color when only looking for 1 pixel at a time.
     * Set ActionOptions.diameter = 0 to use this class.
     */
    private void onePixelClusters(List<Region> searchRegions, ColorClusters colorClusters,
                                           ActionOptions actionOptions, StateImageObject image) {
        /*
        Each search region will create a different Mat (if HSV) or set of Mats (if BGR).
        To find minimum distances, all Mats for the same search region should be compared for
        minimum values.
         */
        searchRegions.forEach(sr -> {
            if (actionOptions.getColor() == ActionOptions.Color.KMEANS) {
                DistanceMatrices dms = colorComposition.getDistanceMatrices(image, sr, actionOptions.getKmeans());
                colorClusters.addAllClusters(dms.getPixels(
                        DistSimConversion.convertToDistance(actionOptions.getSimilarity())));
            }
            if (actionOptions.getColor() == ActionOptions.Color.MU) {
                ScoresMat scoresMat = selectColors.getScoresMatHSV(image, sr);
                colorClusters.addAllClusters(
                        scoresMat.getPixels(DistSimConversion.convertToScoreHSV(actionOptions.getMinScore())));
            }
        });
    }

    /**
     * Used to find color when looking for more than 1 pixel in a color cluster.
     */
    private void multiPixelClusters(List<Region> searchRegions, ColorClusters colorClusters,
                                             ActionOptions actionOptions, StateImageObject image) {
        searchRegions.forEach(reg -> colorClusters.addAllClusters(
                selectColors.findRegions(actionOptions, image, reg)));
    }
}
