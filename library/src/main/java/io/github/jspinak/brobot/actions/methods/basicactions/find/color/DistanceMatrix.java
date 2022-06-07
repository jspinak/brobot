package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.warpAffine;

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
@Getter
public class DistanceMatrix {

    private Mat onScreen; // the image on the screen
    private Mat color; // the target color
    private Mat distance; // the distance to the target color
    private Location topLeft; // the screen location of the search region's top left corner
    private StateImageObject stateImageObject;

    public DistanceMatrix(Mat onScreen, Mat color, Region region, StateImageObject stateImageObject) {
        this.onScreen = onScreen;
        this.topLeft = new Location(region.getTopLeft());
        this.color = color;
        Mat rgbDistance = getColorDifference(onScreen, color);
        distance = convertRGBdistanceToOneDistance(rgbDistance);
        this.stateImageObject = stateImageObject;
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

    /**
     * Returns clusters of pixels with colors similar to the target color.
     * ColorDiff is a 2d Mat.
     *
     * @param minDiameter
     * @param maxColorDifference
     * @return
     */
    public ColorClusters getClusters(int minDiameter, double maxColorDifference, StateImageObject image) {
        Mat colorDiff = new Mat();
        // populate colorDiff with t/f
        Scalar maxDist = new Scalar(maxColorDifference);
        compare(distance, maxDist, colorDiff, CMP_LE);
        divide(colorDiff, new Scalar(255), colorDiff);
        // sum up cells horizontally and vertically by minDiameter in both directions
        // do this by creating a new Mat by moving all cells 1 to the left
        colorDiff = addAllOffsets(colorDiff, minDiameter, minDiameter);
        // cells that have minDiameter * 2 are matching boxes of color of h & w = minDiameter
        ColorClusters colorClusters = new ColorClusters();
        for (int i=0; i<colorDiff.rows()-minDiameter; i++) {
            for (int j=0; j<colorDiff.cols()-minDiameter; j++) {
                int numberOfMatchingPixels = (int)colorDiff.get(i, j)[0];
                if (numberOfMatchingPixels >= minDiameter) {
                    Region matchReg = getRegion(i, j, minDiameter);
                    Mat sub = distance.submat(i, i + minDiameter, j, j + minDiameter);
                    Mat clusterPixels = onScreen.submat(i,i+minDiameter, j,j+minDiameter);
                    colorClusters.addCluster(
                            new ColorCluster(matchReg, clusterPixels, sub, numberOfMatchingPixels, image));
                }
            }
        }
        return colorClusters;
    }

    private Mat translateImg(Mat img, int offsetx, int offsety) {
        Mat trans_mat = new Mat( 2, 3, CvType.CV_64FC1);
        int row = 0, col = 0;
        trans_mat.put(row ,col, 1, 0, offsetx, 0, 1, offsety);
        warpAffine(img, img, trans_mat, img.size());
        return img;
    }

    /**
     * Move Mat by 1 in the specified direction.
     * Add original and offset Mats.
     * Do until full offset reached as specified in the parameters.
     * offsetx and offsety are performed separately, and added together.
     * @param img
     * @param offsetx
     * @param offsety
     * @return
     */
    private Mat addAllOffsets(Mat img, int offsetx, int offsety) {
        Mat result = Mat.zeros(img.size(), img.type());
        add(result, img, result);
        int signx = Math.abs(offsetx) / offsetx;
        Mat shifted;
        for (int x=1; x<=Math.abs(offsetx); x++) {
            shifted = translateImg(img, signx * x, 0);
            add(result, shifted, result);
        }
        int signy = Math.abs(offsety) / offsety;
        for (int y=1; y<=Math.abs(offsety); y++) {
            shifted = translateImg(img, signy * y, 0);
            add(result, shifted, result);
        }
        return result;
    }

    public Region getRegion(int row, int col, int diameter) {
        return new Region(topLeft.getX() + col, topLeft.getY() + row, diameter, diameter);
    }

    public ColorCluster getCluster(int row, int col, int diameter, int matchingPixels) {
        Region reg = getRegion(row, col, diameter);
        Mat img = onScreen.submat(row, row + diameter, col, col + diameter);
        Mat dist = distance.submat(row, row + diameter, col, col + diameter);
        return new ColorCluster(reg, img, dist, matchingPixels, stateImageObject);
    }
}
