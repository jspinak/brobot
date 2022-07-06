package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.imageUtils.GetImage;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Find the color thresholds of an image to use with a Find.COLOR operation.
 * We usually want to search for 1 or 2 colors that dominate an image.
 * The below methods allow us to specify how to select color from an image.
 */
@Component
public class ColorComposition {

    private GetImage getImage;
    private MockColor mockColor;
    private GetDistancesBGR getDistancesBGR;

    public ColorComposition(GetImage getImage, MockColor mockColor, GetDistancesBGR getDistancesBGR) {
        this.getImage = getImage;
        this.mockColor = mockColor;
        this.getDistancesBGR = getDistancesBGR;
    }

    public void showKmeans(Region region, int centers) {
        kmeans(getImage.getMatFromScreen(region), centers);
    }

    public void showKmeans(Image image, int centers) {
        kmeans(getImage.getMatFromBufferedImage(image.getBufferedImage(0)), centers);
    }

    public DistanceMatrices getDistanceMatrices(StateImageObject stateImageObject, Region region, int means) {
        DistanceMatrices distanceMatrices = new DistanceMatrices();
        stateImageObject.getImage().getFilenames().forEach(
                name -> distanceMatrices.addMatrices(
                        name, getColorDifference(stateImageObject, region,
                                kmeans(getImage.getMatFromFilename(
                                                ImagePath.getBundlePath()+"/"+name, false),
                                        means)
                )));
        return distanceMatrices;
    }

    public Mat kmeans(Mat mat, int numberOfCenters) {
        mat.convertTo(mat, CvType.CV_32F);
        Mat data = mat.reshape(1, (int)mat.total());
        Mat bestLabels = new Mat();
        TermCriteria criteria = new TermCriteria();
        int attempts = 10;
        int flags = Core.KMEANS_PP_CENTERS;
        Mat centers = new Mat();
        Core.kmeans(data, numberOfCenters, bestLabels, criteria, attempts, flags, centers);
        return centers.reshape(3, numberOfCenters);
    }

    /**
     * @param stateImageObject The StateImageObject that produced the k-means colors.
     * @param region The Region on the screen to use
     * @param centers The k-means colors from the Image
     * @return a list of DistanceMatrix objects, each with the color difference to one of
     * the k-means colors.
     */
    public List<ScoresMat> getColorDifference(StateImageObject stateImageObject, Region region, Mat centers) {
        Mat onScreen;
        if (BrobotSettings.mock) onScreen = mockColor.getMockMat(stateImageObject.getImage(), region);
        else onScreen = getImage.getMatFromScreen(region);
        List<ScoresMat> dists = new ArrayList<>();
        // for each k-means color from the Image, get the difference to the colors in the region
        for (int i=0; i<centers.rows(); i++)
            dists.add(getDistancesBGR.getScores(onScreen, centers.row(i), region, stateImageObject));
        return dists;
    }

}
