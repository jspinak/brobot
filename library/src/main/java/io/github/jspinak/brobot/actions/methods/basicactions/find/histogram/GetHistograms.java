package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import lombok.Getter;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.add;
import static org.bytedeco.opencv.global.opencv_core.normalize;
import static org.bytedeco.opencv.global.opencv_imgproc.calcHist;

@Component
@Getter
public class GetHistograms {

    private final GetImageJavaCV getImage;

    /*
     Hue has values from 0 to 180, Saturation and Value from 0 to 255.
    */
    private final float[] ranges = { 0, 180, 0, 256, 0, 256 };
    private final int[] channels = { 0, 1, 2 };
    private int hueBins = 90;
    private int satBins = 2;
    private int valBins = 1;
    private int totalBins = hueBins * satBins * valBins;

    public GetHistograms(GetImageJavaCV getImage) {
        this.getImage = getImage;
    }

    public void setBins(int hueBins, int satBins, int valBins) {
        this.hueBins = hueBins;
        this.satBins = satBins;
        this.valBins = valBins;
        totalBins = hueBins * satBins * valBins;
    }

    public HistogramRegions getHistogramFromRegion(Mat maskOnScene) {
        IntPointer channelsPtr = new IntPointer(channels);
        IntPointer binsPtr = new IntPointer(hueBins, satBins, valBins);
        PointerPointer<FloatPointer> rangesPtrPtr = new PointerPointer<>(ranges);
        HistogramRegions histRegs = new HistogramRegions(maskOnScene);
        getHistogramRegions(histRegs, channelsPtr, binsPtr, rangesPtrPtr);
        return histRegs;
    }

    public HistogramRegions getHistogramsHSV(StateImage image) {
        List<Mat> patternsAsMats = getImage.getMats(image, ColorCluster.ColorSchemaName.HSV);
        IntPointer channelsPtr = new IntPointer(channels);
        IntPointer binsPtr = new IntPointer(hueBins, satBins, valBins);
        PointerPointer<FloatPointer> rangesPtrPtr = new PointerPointer<>(ranges);
        HistogramRegions histRegs = new HistogramRegions(patternsAsMats);
        histRegs.getTopLeft().setHistogram(new Mat());
        histRegs.getTopRight().setHistogram(new Mat());
        histRegs.getBottomLeft().setHistogram(new Mat());
        histRegs.getBottomRight().setHistogram(new Mat());
        histRegs.getEllipse().setHistogram(new Mat());
        getHistogramRegions(histRegs, channelsPtr, binsPtr, rangesPtrPtr);
        return histRegs;
    }

    public void getHistogramRegions(HistogramRegions histRegs, IntPointer channelsUsed,
                                                IntPointer bins, PointerPointer<FloatPointer> ranges) {
        for (int i=0; i<histRegs.getImages().size(); i++) {
            setHistogramRegions(histRegs, i, 1, channelsUsed, histRegs.getImages().get(i), bins, ranges);
        }
        histRegs.setCombinedHistograms();
    }

    private void setHistogramRegions(HistogramRegions histRegs, int index, int numberOfImages, IntPointer channelsUsed,
                                     Mat img, IntPointer bins, PointerPointer<FloatPointer> ranges) {
        setHistogramRegion(histRegs.getTopLeft(), numberOfImages, channelsUsed, histRegs.getTopLeft().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getTopRight(), numberOfImages, channelsUsed, histRegs.getTopRight().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getBottomLeft(), numberOfImages, channelsUsed, histRegs.getBottomLeft().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getBottomRight(), numberOfImages, channelsUsed, histRegs.getBottomRight().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getEllipse(), numberOfImages, channelsUsed, histRegs.getEllipse().getMasks().get(index), img, bins, ranges);
    }

    private void setHistogramRegion(HistogramRegion histReg, int numberOfImages, IntPointer channelsUsed, Mat mask,
                                    Mat img, IntPointer bins, PointerPointer<FloatPointer> ranges) {
        Mat hist = getHist(img, numberOfImages, channelsUsed, mask, bins, ranges);
        histReg.getHistograms().add(hist);
        if (histReg.getHistogram().empty()) histReg.setHistogram(hist);
        else add(histReg.getHistogram(), hist, histReg.getHistogram());
    }

    private Mat getHist(Mat image, int numberOfImages, IntPointer channelsUsed, Mat mask, IntPointer bins, PointerPointer<FloatPointer> ranges) {
        Mat hist = new Mat();
        calcHist(
                image,              // the image to be processed
                numberOfImages,     // number of images
                channelsUsed,       // channels used
                mask,               // the mask to be used
                hist,               // the histogram to be calculated
                1,                  // number of dimensions
                bins,               // the number of bins
                ranges,             // the ranges of the histogram
                true,               // uniformity
                false);             // accumulate
        normalize(hist, hist);
        //hist = hist.reshape(1, totalBins);
        return hist;
    }

    /*
    Multi-image histogram. This is currently not used since patterns in a Brobot image have different sizes.
     */
    private Mat getHist(List<Mat> images, IntPointer channelsUsed, Mat mask, IntPointer bins, FloatPointer ranges) {
        Mat hist = new Mat();
        MatVector imagesVector = new MatVector(images.toArray(new Mat[0]));
        calcHist(
                imagesVector,       // the image to be processed
                channelsUsed,       // channels used
                mask,               // the mask to be used
                hist,               // the histogram to be calculated
                bins,               // the number of bins
                ranges);            // the ranges of the histogram
        normalize(hist, hist);
        //hist = hist.reshape(1, totalBins);
        return hist;
    }
}
