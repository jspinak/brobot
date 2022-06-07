package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

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
        colorClusters.sort();
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
        Each search region may create a different size Mat.
        To find minimum distances, all Mats for the same search region should be compared for
        minimum values.
         */
        searchRegions.forEach(sr -> {
            DistanceMatrices dms = colorComposition.getDistanceMatrices(image, sr, actionOptions.getKmeans());
            colorClusters.addAllClusters(dms.getPixels(
                    DistSimConversion.convertToDistance(actionOptions.getSimilarity())));
        });
    }

    /**
     * Used to find color when looking for more than 1 pixel in a color cluster.
     */
    private void multiPixelClusters(List<Region> searchRegions, ColorClusters colorClusters,
                                             ActionOptions actionOptions, StateImageObject image) {
        searchRegions.forEach(reg -> colorClusters.addAllClusters(
                selectColors.findRegions(image, reg, actionOptions.getDiameter(),
                        DistSimConversion.convertToDistance(actionOptions.getSimilarity()),
                        actionOptions.getKmeans())));
    }
}
