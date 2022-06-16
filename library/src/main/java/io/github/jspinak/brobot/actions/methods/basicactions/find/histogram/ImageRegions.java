package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

/**
 * Divides a region into five sub-regions to facilitate histogram and color Find operations.
 * The five regions are: an ellipse at the center of the image, and the remaining pixels in the 4 corners.
 * Dividing an image into smaller parts in this way allows an operation comparing histograms to
 * have some spatial relevance.
 */
@Getter
public class ImageRegions {

    private final Region region;
    private Grid fourRegions;
    private Mat ellipseMask;
    private List<Mat> cornerMasks;

    public ImageRegions(Region region) {
        this.region = new Region(0, 0, region.w, region.h);
        setAll();
    }

    public ImageRegions(Mat mat) {
        this.region = new Region(0, 0, mat.cols(), mat.rows());
        setAll();
    }

    private void setAll() {
        divideIntoFourRectangles();
        setEllipticalMask();
        setCornerMasks();
    }

    private void divideIntoFourRectangles() {
        fourRegions = new Grid.Builder()
                .setRegion(region)
                .setRows(2)
                .setColumns(2)
                .build();
        //System.out.println(fourRegions.getGridRegions());
    }

    private void setEllipticalMask() {
        Point ellipseCenter = new Point(region.getCenter().x, region.getCenter().y);
        int width = (int) ((region.w * .75) / 2);
        int height = (int) ((region.h * .75) / 2);
        Size axes = new Size(width, height);
        ellipseMask = Mat.zeros(region.h, region.w, CvType.CV_8UC1);
        Scalar color = new Scalar(255, 255, 255);
        Imgproc.ellipse(ellipseMask, ellipseCenter, axes, 0, 0, 360, color, -1, 0, 0);
    }

    private void setCornerMasks() {
        cornerMasks = new ArrayList<>();
        fourRegions.getGridRegions().forEach(reg -> {
            Mat mask = Mat.zeros(region.h, region.w, CvType.CV_8UC1);
            //Report.println("x and y "+reg.x+" "+reg.y);
            Imgproc.rectangle(mask, new Point(reg.x, reg.y), new Point(reg.getX2(), reg.getY2()),
                    new Scalar(255), -1);
            Core.subtract(mask, ellipseMask, mask);
            cornerMasks.add(mask);
        });
    }

    // for testing
    public void saveMasks() {
        imwrite("ellipse.png", ellipseMask);
        imwrite("topleft.png", cornerMasks.get(0));
        imwrite("topright.png", cornerMasks.get(1));
        imwrite("bottomleft.png", cornerMasks.get(2));
        imwrite("bottomright.png", cornerMasks.get(3));
    }

}
