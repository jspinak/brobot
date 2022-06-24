package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

@Getter
public class ColorCluster {

    private Region region;
    private Mat image;
    private Mat colorDeltas;
    private int matchingPixels;
    private double colorDistance;
    /*
    To get the score, average the differences to the target color for each pixel in the cluster.
    The maximum difference possible would be sqrt(255^2 + 255^2 + 255^2) = sqrt(195075) =
    441.67.
    Convert to a scale of 0-100 to fit with the similarity score of image matching.
    A color distance of 0 would be a similarity score of 100, and a distance of ___ would give
    a score of 0. A linear mapping gives us similarity scores for all other color distances.
     */
    private double score;
    private StateImageObject stateImageObject;

    public ColorCluster(Region region, Mat image, Mat colorDeltas, int matchingPixels,
                        StateImageObject stateImageObject) {
        this.region = region;
        this.image = image;
        this.colorDeltas = colorDeltas;
        this.matchingPixels = matchingPixels;
        setAverageDistance();
        score = colorDistance; //DistSimConversion.convertToSimilarity(colorDistance);
        this.stateImageObject = stateImageObject; // for creating MatchObjects
    }

    private void setAverageDistance() {
        MatOfDouble mu = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();
        Core.meanStdDev(colorDeltas, mu, sigma);
        colorDistance = mu.get(0,0)[0]; // get the avg of all cells
    }
}
