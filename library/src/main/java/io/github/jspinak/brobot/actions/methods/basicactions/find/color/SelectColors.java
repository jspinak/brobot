package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.imageUtils.GetImage;
import io.github.jspinak.brobot.reports.Report;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

@Component
public class SelectColors {

    private ColorComposition colorComposition;
    private GetClusters getClusters;
    private SetColorProfile setColorProfile;
    private GetImage getImage;
    private GetScoresHSV getScoresHSV;

    public SelectColors(ColorComposition colorComposition, GetClusters getClusters,
                        SetColorProfile setColorProfile, GetImage getImage, GetScoresHSV getScoresHSV) {
        this.colorComposition = colorComposition;
        this.getClusters = getClusters;
        this.setColorProfile = setColorProfile;
        this.getImage = getImage;
        this.getScoresHSV = getScoresHSV;
    }

    public ColorClusters findRegions(ActionOptions actionOptions, StateImageObject stateImageObject,
                                     Region region) {
        if (actionOptions.getColor() == ActionOptions.Color.KMEANS)
            return findRegionsBGR(stateImageObject, region, actionOptions.getDiameter(),
                    DistSimConversion.convertToDistance(actionOptions.getSimilarity()),
                    actionOptions.getKmeans());
        if (actionOptions.getColor() == ActionOptions.Color.MU)
            return findRegionsHSV(stateImageObject, region, actionOptions.getDiameter(),
                    DistSimConversion.convertToScoreHSV(actionOptions.getMinScore()));
        // otherwise, the selected method has no implementation (this shouldn't happen)
        Report.println("The selected Color method is not implemented. ");
        return new ColorClusters();
    }

    public ScoresMat getScoresMatHSV(StateImageObject stateImageObject, Region region) {
        ColorProfile colorProfile = setColorProfile.setProfile(stateImageObject.getImage());
        Mat onScreen = getImage.getMatFromScreen(region, true);
        return getScoresHSV.getScores(onScreen, region, stateImageObject, colorProfile);
    }

    public ColorClusters findRegionsHSV(StateImageObject stateImageObject,
                                        Region region, int minDiameter, double maxColorDifference) {
        ScoresMat scoresMat = getScoresMatHSV(stateImageObject, region);
        return getClusters.getClusters(scoresMat, minDiameter, maxColorDifference, stateImageObject);
    }

    public ColorClusters findRegionsBGR(StateImageObject stateImageObject,
                                        Region region, int minDiameter, double maxColorDifference, int means) {
        DistanceMatrices distanceMatrices =
                colorComposition.getDistanceMatrices(stateImageObject, region, means);
        ColorClusters colorClusters = new ColorClusters();
        // found regions may overlap
        distanceMatrices.getAll().forEach(distMatx -> {
                ColorClusters clusters = getClusters.getClusters(distMatx, minDiameter,
                        maxColorDifference, stateImageObject);
                colorClusters.addAllClusters(clusters);
        });
        return colorClusters;
    }

}
