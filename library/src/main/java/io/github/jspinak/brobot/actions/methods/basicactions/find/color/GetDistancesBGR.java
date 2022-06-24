package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.*;

/**
 * The Distance Matrix stores every pixel's distance to the target color.
 * Clusters are found by the following process:
 *   Get a matrix of 1s and 0s. 1s are for pixels that are less than the max distance.
 *   Given a minDiameter parameter, add the minDiameter number of rows using matrix multiplication.
 *   Do the same for the columns.
 *   - for example, with a minDiameter of 3 and a max distance of 20:
 *   50 19 14 11 45
 *   19 18 14 15 16
 *   49 10 11 17 90
 *   - we get the binary matrix:
 *   0 1 1 1 0
 *   1 1 1 1 1
 *   0 1 1 1 0
 *   - after adding rows by groups of 3 pixels:
 *   2 3 2
 *   3 3 3
 *   2 3 2
 *   - after adding columns by groups of 3 pixels:
 *   7 9 7
 *   - the 9 (3^2) tells us we have a matches at x.y.w.h = 1.1.3.3
 *
 *   Overlapping regions are possible.
 */
@Component
public class GetDistancesBGR {

    public ScoresMat getScores(Mat onScreen, Mat color, Region region, StateImageObject stateImageObject) {
        ScoresMat scoresMat = new ScoresMat();
        scoresMat.setOnScreen(onScreen);
        scoresMat.setTopLeft(new Location(region.getTopLeft()));
        scoresMat.setColor(color);
        Mat rgbDistance = getColorDifference(onScreen, color);
        Mat distance = convertRGBdistanceToOneDistance(rgbDistance);
        scoresMat.setScores(distance);
        scoresMat.setStateImageObject(stateImageObject);
        return scoresMat;
    }

    /**
     * Returns a Mat that is a 3d representation of the difference between the colors of
     * pixels in a region and a specific k-means color in rgb. The returned Mat will have the difference
     * for r & g & b for each pixel.
     * @param onScreen The Mat from the Region
     * @param color The rgb values of one of the k-means selected from the Image
     * @return the difference in rgb for each pixel
     */
    public Mat getColorDifference(Mat onScreen, Mat color) {
        Mat repeatedColor = new Mat();
        List<Mat> colors = new ArrayList<>();
        colors.add(color);
        merge(colors, repeatedColor);
        Mat repCol = new Mat();
        Core.repeat(repeatedColor, onScreen.rows(), onScreen.cols(), repCol);
        repCol.convertTo(repCol, CvType.CV_8UC3);
        Mat dist = new Mat();
        absdiff(onScreen, repCol, dist);
        return dist;
    }

    /**
     * Takes a 3-dimensional color tensor (a 2d image with 3 color channels) and for each pixel,
     * calculates the euclidean distance to the target color. The resulting Mat is 2d.
     * @param colorDiff
     * @return
     */
    public Mat convertRGBdistanceToOneDistance(Mat colorDiff) {
        // sum up the channels of the image:
        // 1 .store initial nr of rows/columns
        int initialRows = colorDiff.rows();
        int initialCols = colorDiff.cols();
        // 2. check if matrix is continous
        if (!colorDiff.isContinuous())
        {
            colorDiff = colorDiff.clone();
        }
        // 3. reshape matrix to 3 color vectors
        colorDiff = colorDiff.reshape(3, initialRows*initialCols);
        // 4. convert matrix to store bigger values than 255
        colorDiff.convertTo(colorDiff, CvType.CV_32F);
        // 4b square
        multiply(colorDiff, colorDiff, colorDiff);
        // 5. sum up the three color vectors
        Mat dotWith = Mat.ones(1,3, CvType.CV_64F);
        transform(colorDiff, colorDiff, dotWith);
        sqrt(colorDiff, colorDiff);
        // 6. reshape to initial size
        colorDiff = colorDiff.reshape(1, initialRows);
        // 7. convert back to CV_8UC1
        colorDiff.convertTo(colorDiff, CvType.CV_8U);
        return colorDiff;
    }

}
