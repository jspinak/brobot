package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.javacpp.indexer.IntRawIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * This builder puts a bunch of Mats together.
 * The underlying Mat will be stretched to include the other Mats if too small.
 * If the submat max width or height is set, submats will be shrunk to fit.
 */
public class MatBuilder {

    private String name = "";
    private Mat mat;
    private Map<Location, Mat> subMats = new HashMap<>();
    private List<Mat> horizontalSubmats = new ArrayList<>();
    private List<Mat> verticalSubmats = new ArrayList<>();
    private int submatMaxWidth = 0;
    private int submatMaxHeight = 0;
    private int spaceBetween = 0;
    private int x2 = 0;
    private int y2 = 0;

    public MatBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public MatBuilder setWH(int w, int h) {
        mat = new Mat(h, w, CV_8UC3, new Scalar(0, 0, 0, 0));
        x2 = w;
        y2 = h;
        return this;
    }

    public MatBuilder init(Region region) {
        int w = region.w();
        int h = region.h();
        return setWH(w,h);
    }

    public MatBuilder init() {
        return init(new Region());
    }

    public MatBuilder setMat(Mat mat) {
        this.mat = mat.clone();
        x2 = mat.cols();
        y2 = mat.rows();
        return this;
    }

    public MatBuilder newOneChannelRowMat(int... values) {
        x2 = values.length;
        y2 = 1;
        mat = new Mat(y2, x2, 4);
        IntRawIndexer indexer32s = mat.createIndexer();
        for (int i = 0; i < values.length; i++) {
            indexer32s.put(0, i, values[i]);
        }
        return this;
    }

    public MatBuilder addHorizontalSubmats(Mat... mats) {
        addHorizontalSubmats(Arrays.asList(mats));
        return this;
    }

    public MatBuilder addVerticalSubmats(Mat... mats) {
        addVerticalSubmats(Arrays.asList(mats));
        return this;
    }

    private List<Mat> getNonNullMats(List<Mat> mats) {
        List<Mat> nonNullMats = new ArrayList<>();
        for (Mat mat : mats) {
            if (mat != null) {
                nonNullMats.add(mat);
            }
        }
        return nonNullMats;
    }

    public MatBuilder addHorizontalSubmats(List<Mat> mats) {
        List<Mat> nonNullMats = getNonNullMats(mats);
        horizontalSubmats.addAll(nonNullMats);
        return this;
    }

    public MatBuilder addVerticalSubmats(List<Mat> mats) {
        List<Mat> nonNullMats = getNonNullMats(mats);
        verticalSubmats.addAll(nonNullMats);
        return this;
    }

    private void concatSubmats() {
        concatSubmatsHorizontally(horizontalSubmats);
        concatSubmatsVertically(verticalSubmats);
    }

    public MatBuilder concatSubmatsHorizontally(List<Mat> submats) {
        int i = 0;
        for (Mat submat : submats) {
            addSubMat(new Location(x2,0), submat);
            x2 = Math.max(x2, getEndSpot(i, submatMaxWidth, submat.cols(), x2,submats.size()-1));
            if (submatMaxHeight <= 0) y2 = Math.max(y2, submat.rows());
            i++;
        }
        if (submatMaxHeight > 0) y2 = Math.max(y2, submatMaxHeight);
        return this;
    }

    public MatBuilder concatSubmatsVertically(List<Mat> submats) {
        int i = 0;
        for (Mat submat : submats) {
            addSubMat(new Location(0, y2), submat);
            y2 = Math.max(y2, getEndSpot(i, submatMaxHeight, submat.rows(), y2,submats.size()-1));
            if (submatMaxWidth <= 0) x2 = Math.max(x2, submat.cols());
            i++;
        }
        if (submatMaxWidth > 0) x2 = Math.max(x2, submatMaxWidth);
        return this;
    }

    private int getStartSpot(int i, int max, int submatSize, int lastEndSpot, int lastI) {
        if (i==0) return 0;
        return getEndSpot(i, max, submatSize, lastEndSpot, lastI);
    }

    private int getEndSpot(int i, int maxLength, int submatLength, int lastEndSpot, int lastI) {
        int z;
        if (maxLength > 0) z = maxLength;
        else z = submatLength;
        if (i < lastI) z += spaceBetween;
        return lastEndSpot + z;
    }

    public MatBuilder addSubMat(Location location, Mat subMat) {
        if (subMat == null) return this;
        subMats.put(location, subMat);
        return this;
    }

    public MatBuilder setSubmatMaxWidth(int submatMaxWidth) {
        this.submatMaxWidth = submatMaxWidth;
        return this;
    }

    public MatBuilder setSubmatMaxHeight(int submatMaxHeight) {
        this.submatMaxHeight = submatMaxHeight;
        return this;
    }

    public MatBuilder setSpaceBetween(int spaceBetween) {
        this.spaceBetween = spaceBetween;
        return this;
    }

    public MatBuilder setSubMats(List<Mat> subMats) {
        horizontalSubmats = new ArrayList<>(subMats);
        return this;
    }

    private void resizeUnderlyingMat(int w, int h) {
        if (mat == null || mat.empty()) {
            setWH(w,h);
            return;
        }
        Rect oldLoc = new Rect(0, 0, mat.cols(), mat.rows());
        Mat oldMat = mat.clone();
        mat = new Mat(h, w, mat.type()); //ok
        Mat insertOld = mat.apply(oldLoc);
        oldMat.copyTo(insertOld);
    }

    private void resizeBaseIfNecessary() {
        if (mat.cols() < x2 || mat.rows() < y2) {
            resizeUnderlyingMat(x2, y2);
        }
    }

    private double getAdjustFactor(Mat submat) {
        double factor = 1;
        if (submatMaxWidth > 0 && submat.cols() > submatMaxWidth) {
            factor = Math.min(factor, (double) submatMaxWidth / submat.cols());
        }
        if (submatMaxHeight > 0 && submat.rows() > submatMaxHeight) {
            factor = Math.min(factor, (double) submatMaxHeight / submat.rows());
        }
        return factor;
    }

    private void resizeSubmatsIfNecessary() {
        if (submatMaxWidth <= 0 && submatMaxHeight <= 0) return;
        resizeSubmats(horizontalSubmats);
        resizeSubmats(verticalSubmats);
        resizeSubmats(new ArrayList<>(subMats.values()));
    }

    private void resizeSubmats(List<Mat> subMats) {
        subMats.forEach(subMat -> {
            double factor = getAdjustFactor(subMat);
            int newWidth = (int) (subMat.cols() * factor);
            int newHeight = (int) (subMat.rows() * factor);
            Size newSize = new Size(newWidth, newHeight);
            resize(subMat, subMat, newSize);
        });
    }

    private void insertMat(Location location, Mat subMat) {
        Rect rect = new Rect(location.getX(), location.getY(), subMat.cols(), subMat.rows());
        subMat.convertTo(subMat, mat.type());
        Mat target = mat.apply(rect);
        subMat.copyTo(target);
    }

    public Mat build() {
        if (mat == null) mat = new Mat();
        resizeSubmatsIfNecessary();
        concatSubmats();
        resizeBaseIfNecessary();
        subMats.forEach(this::insertMat);
        return mat;
    }

}
