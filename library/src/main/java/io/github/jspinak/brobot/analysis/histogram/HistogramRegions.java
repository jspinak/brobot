package io.github.jspinak.brobot.analysis.histogram;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.ellipse;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import io.github.jspinak.brobot.model.element.Grid;
import io.github.jspinak.brobot.model.element.Region;

import lombok.Getter;

/**
 * Manages histogram regions for spatial color analysis of images. This class divides images into
 * five distinct regions to enable spatially-aware histogram comparisons and color-based matching.
 *
 * <p>The five regions consist of:
 *
 * <ul>
 *   <li>An elliptical region at the center (covering ~75% of image dimensions)
 *   <li>Four corner regions (top-left, top-right, bottom-left, bottom-right)
 * </ul>
 *
 * <p>This spatial division allows histogram comparisons to capture not just color distribution but
 * also the spatial arrangement of colors, improving matching accuracy for images with similar
 * colors but different layouts.
 *
 * <p>For Brobot images containing multiple patterns, each pattern is independently divided into
 * these five regions, with histograms computed and combined across all patterns.
 *
 * @see HistogramRegion
 * @see HistogramExtractor
 * @see Grid
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

    /**
     * Constructs HistogramRegions for a single image. Initializes the five regions and prepares
     * masks for histogram calculation.
     *
     * @param mat the image Mat to process
     */
    public HistogramRegions(Mat mat) {
        images.add(mat);
        imageSizes.add(new Region(0, 0, mat.cols(), mat.rows()));
        setAll();
    }

    /**
     * Constructs HistogramRegions for multiple images. Each image is divided into five regions with
     * corresponding masks.
     *
     * @param mats list of image Mats to process
     */
    public HistogramRegions(List<Mat> mats) {
        images.addAll(mats);
        mats.forEach(mat -> imageSizes.add(new Region(0, 0, mat.cols(), mat.rows())));
        setAll();
    }

    /**
     * Initializes all regions by creating grids, masks, and histogram containers. This method
     * orchestrates the complete setup process for spatial regions.
     */
    private void setAll() {
        divideIntoFourRectangles();
        setEllipseMasksAllImages();
        setCornerMasksAllImages();
        initAllHistograms();
    }

    /**
     * Initializes empty histogram containers for all five regions. These will be populated during
     * histogram calculation.
     */
    private void initAllHistograms() {
        topLeft.setHistogram(new Mat());
        topRight.setHistogram(new Mat());
        bottomLeft.setHistogram(new Mat());
        bottomRight.setHistogram(new Mat());
        ellipse.setHistogram(new Mat());
    }

    /**
     * Creates a 2x2 grid for each image to define the four corner regions. Each grid divides the
     * image into quadrants for spatial analysis.
     */
    private void divideIntoFourRectangles() {
        imageSizes.forEach(
                imageSize ->
                        grids.add(
                                new Grid.Builder()
                                        .setRegion(imageSize)
                                        .setRows(2)
                                        .setColumns(2)
                                        .build()));
    }

    /**
     * Creates elliptical masks for the center region of all images. Each mask defines the central
     * area for histogram calculation.
     */
    private void setEllipseMasksAllImages() {
        for (int i = 0; i < images.size(); i++) {
            setEllipticalMask(imageSizes.get(i));
        }
    }

    /**
     * Creates an elliptical mask for the center region of an image. The ellipse covers 75% of the
     * image dimensions, centered at the image center.
     *
     * @param imageSize the dimensions of the image
     */
    private void setEllipticalMask(Region imageSize) {
        // Calculate center directly without using sikuli() to avoid null pointer in tests
        int centerX = imageSize.x() + imageSize.w() / 2;
        int centerY = imageSize.y() + imageSize.h() / 2;
        Point ellipseCenter = new Point(centerX, centerY);
        int width = (int) ((imageSize.w() * .75) / 2);
        int height = (int) ((imageSize.h() * .75) / 2);
        Size axes = new Size(width, height);
        Mat ellipseMask = new Mat(imageSize.h(), imageSize.w(), CV_8UC1, Scalar.BLACK);
        Scalar color = new Scalar(255, 255, 255, 0);
        ellipse(ellipseMask, ellipseCenter, axes, 0, 0, 360, color, -1, 0, 0);
        ellipse.getMasks().add(ellipseMask);
    }

    /**
     * Creates corner masks for all images by subtracting the ellipse from each quadrant, ensuring
     * no overlap between regions.
     */
    private void setCornerMasksAllImages() {
        for (int i = 0; i < images.size(); i++) {
            setCornerMasksForOneImage(grids.get(i), imageSizes.get(i), ellipse.getMasks().get(i));
        }
    }

    /**
     * Creates masks for the four corner regions of a single image. Each corner mask is created by
     * drawing a rectangle for the quadrant and subtracting the ellipse mask to avoid overlap.
     *
     * @param grid the 2x2 grid defining quadrants
     * @param imageSize the dimensions of the image
     * @param ellipseMask the central ellipse mask to subtract
     */
    private void setCornerMasksForOneImage(Grid grid, Region imageSize, Mat ellipseMask) {
        List<Mat> cornerMasks = new ArrayList<>();
        grid.getGridRegions()
                .forEach(
                        reg -> {
                            Mat mask = new Mat(imageSize.h(), imageSize.w(), CV_8UC1, Scalar.BLACK);
                            rectangle(
                                    mask,
                                    new Point(reg.x(), reg.y()),
                                    new Point(reg.x2(), reg.y2()),
                                    new Scalar(255));
                            subtract(mask, ellipseMask, mask);
                            cornerMasks.add(mask);
                        });
        topLeft.getMasks().add(cornerMasks.get(0));
        topRight.getMasks().add(cornerMasks.get(1));
        bottomLeft.getMasks().add(cornerMasks.get(2));
        bottomRight.getMasks().add(cornerMasks.get(3));
    }

    /**
     * Combines histograms from all five regions for each image and across all images. This method
     * creates:
     *
     * <ul>
     *   <li>A combined histogram for each image (sum of its five regions)
     *   <li>A total combined histogram (sum across all images)
     * </ul>
     *
     * The combined histograms provide overall color distributions while maintaining the spatial
     * information through the region-based approach.
     */
    public void setCombinedHistograms() {
        if (images.size() == 0) return;
        Size size = topLeft.getHistograms().get(0).size();
        int type = topLeft.getHistograms().get(0).type();
        Mat allImagesCombined = new Mat(size, type, Scalar.BLACK);
        for (int i = 0; i < images.size(); i++) {
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
