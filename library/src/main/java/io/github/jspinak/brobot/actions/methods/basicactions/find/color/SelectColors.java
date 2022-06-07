package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

@Component
public class SelectColors {

    private ColorComposition colorComposition;

    public SelectColors(ColorComposition colorComposition) {
        this.colorComposition = colorComposition;
    }

    public ColorClusters findRegions(StateImageObject stateImageObject, Region region,
                               int minDiameter, double maxColorDifference, int means) {
        DistanceMatrices distanceMatrices =
                colorComposition.getDistanceMatrices(stateImageObject, region, means);
        ColorClusters colorClusters = new ColorClusters();
        // found regions may overlap
        distanceMatrices.getAll().forEach(distMatx ->
                colorClusters.addAllClusters(distMatx.getClusters(minDiameter, maxColorDifference,
                        stateImageObject)));
        return colorClusters;
    }

}
