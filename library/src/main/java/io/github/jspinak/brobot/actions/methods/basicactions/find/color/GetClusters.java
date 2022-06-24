package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.springframework.stereotype.Component;

import static org.opencv.core.Core.*;
import static org.opencv.core.Core.add;
import static org.opencv.imgproc.Imgproc.warpAffine;

@Component
public class GetClusters {

    /**
     * Returns clusters of pixels with colors similar to the target color.
     * ColorDiff is a 2d Mat.
     *
     * @param minDiameter
     * @param maxColorDifference
     * @return
     */
    public ColorClusters getClusters(ScoresMat scor, int minDiameter,
                                     double maxColorDifference, StateImageObject image) {
        Mat colorDiff = new Mat();
        // populate colorDiff with t/f (1/0)
        Scalar maxDist = new Scalar(maxColorDifference);
        compare(scor.getScores(), maxDist, colorDiff, CMP_LE);
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
                    Region matchReg = scor.getRegion(i, j, minDiameter);
                    Mat sub = scor.getScores().submat(i, i + minDiameter, j, j + minDiameter);
                    Mat clusterPixels = scor.getOnScreen().submat(i,i+minDiameter, j,j+minDiameter);
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

}
