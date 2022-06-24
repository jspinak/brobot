package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;

@Getter
@Setter
public class ScoresMat {

    private Mat onScreen; // the image on the screen
    private Mat color; // the target color when RGB
    private ColorProfile colorProfile; // the color stats when HSV
    private Mat scores; // when RGB, the distance to the target color; when HSV, scores
    private Location topLeft; // the screen location of the search region's top left corner
    private StateImageObject stateImageObject;

    public ColorCluster getCluster(int row, int col, int diameter, int matchingPixels) {
        Region reg = getRegion(row, col, diameter);
        Mat img = onScreen.submat(row, row + diameter, col, col + diameter);
        Mat dist = scores.submat(row, row + diameter, col, col + diameter);
        return new ColorCluster(reg, img, dist, matchingPixels, stateImageObject);
    }

    public Region getRegion(int row, int col, int diameter) {
        return new Region(topLeft.getX() + col, topLeft.getY() + row, diameter, diameter);
    }

    /*
    Returns all matching pixels (clusters of 1).
     */
    public ColorClusters getPixels(double maxScore) {
        ColorClusters cc = new ColorClusters();
        for (int i=0; i<scores.rows(); i++) {
            for (int j=0; j<scores.cols(); j++)
                if (scores.get(j,i)[0] <= maxScore) cc.addCluster(getCluster(i, j, 1, 1));
        }
        return cc;
    }
}
