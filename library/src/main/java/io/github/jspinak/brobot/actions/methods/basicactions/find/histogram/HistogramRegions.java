package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.ellipse;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

/**
 * Divides a region into five subregions to facilitate histogram and color Find operations.
 * The five regions are: an ellipse at the center of the image, and the remaining pixels in the 4 corners.
 * Dividing an image into smaller parts in this way allows an operation comparing histograms to
 * have some spatial relevance.
 *
 * Brobot Images may have multiple Patterns. Each Pattern is divided into five regions and stored in the
 * variables in this class.
 * There is one region and one grid for each Pattern.
 */
@Getter
public class HistogramRegions {

    private final List<Mat> images = new ArrayList<>();
    private final List<Region> imageSizes = new ArrayList<>();
    private final List<Grid> grids = new ArrayList<>();
    private final HistogramRegion topLeft = new HistogramRegion();
    private final HistogramRegion topRight = new HistogramRegion();
    private final HistogramRegion bottomLeft = new HistogramRegion();
    private final HistogramRegion bottomRight = new HistogramRegion();
    private final HistogramRegion ellipse = new HistogramRegion();
    private final HistogramRegion combined = new HistogramRegion();

    public HistogramRegions(Mat mat) {
        images.add(mat);
        imageSizes.add(new Region(0, 0, mat.cols(), mat.rows()));
        setAll();
    }

    public HistogramRegions(List<Mat> mats) {
        images.addAll(mats);
        mats.forEach(mat -> imageSizes.add(new Region(0, 0, mat.cols(), mat.rows())));
        setAll();
    }

    private void setAll() {
        divideIntoFourRectangles();
        setEllipseMasksAllImages();
        setCornerMasksAllImages();
        initAllHistograms();
    }

    private void initAllHistograms() {
        topLeft.setHistogram(new Mat());
        topRight.setHistogram(new Mat());
        bottomLeft.setHistogram(new Mat());
        bottomRight.setHistogram(new Mat());
        ellipse.setHistogram(new Mat());
    }

    private void divideIntoFourRectangles() {
        imageSizes.forEach(imageSize -> grids.add(
                new Grid.Builder()
                        .setRegion(imageSize)
                        .setRows(2)
                        .setColumns(2)
                        .build()));
    }

    private void setEllipseMasksAllImages() {
        for (int i=0; i<images.size(); i++) {
            setEllipticalMask(imageSizes.get(i));
        }
    }

    private void setEllipticalMask(Region imageSize) {
        Point ellipseCenter = new Point(imageSize.sikuli().getCenter().x, imageSize.sikuli().getCenter().y);
        int width = (int) ((imageSize.w() * .75) / 2);
        int height = (int) ((imageSize.h() * .75) / 2);
        Size axes = new Size(width, height);
        Mat ellipseMask = new Mat(imageSize.h(), imageSize.w(), CV_8UC1, Scalar.BLACK);
        Scalar color = new Scalar(255, 255, 255, 0);
        ellipse(ellipseMask, ellipseCenter, axes, 0, 0, 360, color, -1, 0, 0);
        ellipse.getMasks().add(ellipseMask);
    }

    private void setCornerMasksAllImages() {
        for (int i=0; i<images.size(); i++) {
            setCornerMasksForOneImage(grids.get(i), imageSizes.get(i), ellipse.getMasks().get(i));
        }
    }

    private void setCornerMasksForOneImage(Grid grid, Region imageSize, Mat ellipseMask) {
        List<Mat> cornerMasks = new ArrayList<>();
        grid.getGridRegions().forEach(reg -> {
            Mat mask = new Mat(imageSize.h(), imageSize.w(), CV_8UC1, Scalar.BLACK);
            rectangle(mask, new Point(reg.x(), reg.y()), new Point(reg.x2(), reg.y2()), new Scalar(255));
            subtract(mask, ellipseMask, mask);
            cornerMasks.add(mask);
        });
        topLeft.getMasks().add(cornerMasks.get(0));
        topRight.getMasks().add(cornerMasks.get(1));
        bottomLeft.getMasks().add(cornerMasks.get(2));
        bottomRight.getMasks().add(cornerMasks.get(3));
    }

    public void setCombinedHistograms() {
        if (images.size() == 0) return;
        Size size = topLeft.getHistograms().get(0).size();
        int type = topLeft.getHistograms().get(0).type();
        Mat allImagesCombined = new Mat(size, type, Scalar.BLACK);
        for (int i=0; i<images.size(); i++) {
            Mat combinedHistogram = new Mat(size, type, Scalar.ZERO);
            add(topLeft.getHistograms().get(i), topRight.getHistograms().get(i), combinedHistogram);
            add(combinedHistogram, bottomLeft.getHistograms().get(i), combinedHistogram);
            add(combinedHistogram, bottomRight.getHistograms().get(i), combinedHistogram);
            add(combinedHistogram, ellipse.getHistograms().get(i), combinedHistogram);
            combined.getHistograms().add(combinedHistogram);
            add(allImagesCombined, combinedHistogram, allImagesCombined);
        }
        combined.setHistogram(allImagesCombined);
    }

}
