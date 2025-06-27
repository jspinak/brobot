package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;
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

/**
 * Calculates and manages histograms for image regions in HSV color space.
 * This class provides functionality to compute histograms for different regions
 * of an image (corners and center) and combine them for analysis.
 * 
 * <p>The class supports configurable histogram bins for Hue, Saturation, and Value
 * channels, allowing for flexible histogram resolution based on analysis needs.</p>
 * 
 * <p>Key features:
 * <ul>
 * <li>Computes HSV histograms for image regions</li>
 * <li>Supports multiple image processing with combined histograms</li>
 * <li>Configurable bin sizes for each HSV channel</li>
 * <li>Normalizes histograms for consistent comparison</li>
 * </ul></p>
 * 
 * @see HistogramRegions
 * @see HistogramRegion
 * @see StateImage
 */
@Component
@Getter
public class HistogramExtractor {

    private final ImageLoader getImage;

    /**
     * HSV channel ranges: Hue [0-180], Saturation [0-256], Value [0-256].
     * These ranges define the boundaries for histogram calculation in HSV color space.
     */
    private final float[] ranges = { 0, 180, 0, 256, 0, 256 };
    
    /**
     * Channel indices for HSV: 0=Hue, 1=Saturation, 2=Value
     */
    private final int[] channels = { 0, 1, 2 };
    
    private int hueBins = 90;
    private int satBins = 2;
    private int valBins = 1;
    private int totalBins = hueBins * satBins * valBins;

    /**
     * Constructs a HistogramExtractor instance with the specified image retrieval service.
     * 
     * @param getImage service for retrieving and converting images to OpenCV Mat format
     */
    public HistogramExtractor(ImageLoader getImage) {
        this.getImage = getImage;
    }

    /**
     * Sets the number of bins for each HSV channel in the histogram.
     * This method modifies the histogram resolution and affects all subsequent
     * histogram calculations.
     * 
     * @param hueBins number of bins for the Hue channel (typically 30-180)
     * @param satBins number of bins for the Saturation channel (typically 1-256)
     * @param valBins number of bins for the Value channel (typically 1-256)
     */
    public void setBins(int hueBins, int satBins, int valBins) {
        this.hueBins = hueBins;
        this.satBins = satBins;
        this.valBins = valBins;
        totalBins = hueBins * satBins * valBins;
    }

    /**
     * Computes histograms for a single masked region of an image.
     * The mask defines the area of interest for histogram calculation.
     * 
     * @param maskOnScene binary mask indicating the region to analyze
     * @return {@link HistogramRegions} containing histograms for the masked region
     */
    public HistogramRegions getHistogramFromRegion(Mat maskOnScene) {
        IntPointer channelsPtr = new IntPointer(channels);
        IntPointer binsPtr = new IntPointer(hueBins, satBins, valBins);
        PointerPointer<FloatPointer> rangesPtrPtr = new PointerPointer<>(ranges);
        HistogramRegions histRegs = new HistogramRegions(maskOnScene);
        getHistogramRegions(histRegs, channelsPtr, binsPtr, rangesPtrPtr);
        return histRegs;
    }

    /**
     * Computes HSV histograms for all regions of a StateImage.
     * This method processes the image in HSV color space and calculates
     * histograms for five regions: four corners and a central ellipse.
     * 
     * @param image the {@link StateImage} to analyze
     * @return {@link HistogramRegions} containing histograms for all regions
     * @see ColorCluster.ColorSchemaName#HSV
     */
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

    /**
     * Calculates histograms for all regions across multiple images.
     * This method processes each image in the collection and accumulates
     * histograms for each region. After processing all images, it combines
     * the individual histograms.
     * 
     * @param histRegs the {@link HistogramRegions} object to populate with histograms
     * @param channelsUsed pointer to array of channel indices to use
     * @param bins pointer to array of bin counts for each channel
     * @param ranges pointer to array of value ranges for each channel
     */
    public void getHistogramRegions(HistogramRegions histRegs, IntPointer channelsUsed,
                                                IntPointer bins, PointerPointer<FloatPointer> ranges) {
        for (int i=0; i<histRegs.getImages().size(); i++) {
            setHistogramRegions(histRegs, i, 1, channelsUsed, histRegs.getImages().get(i), bins, ranges);
        }
        histRegs.setCombinedHistograms();
    }

    /**
     * Sets histograms for all five regions (four corners and center) of an image.
     * 
     * @param histRegs the histogram regions container
     * @param index the index of the current image being processed
     * @param numberOfImages number of images to process (typically 1)
     * @param channelsUsed pointer to channel indices
     * @param img the image Mat to analyze
     * @param bins pointer to bin counts
     * @param ranges pointer to value ranges
     */
    private void setHistogramRegions(HistogramRegions histRegs, int index, int numberOfImages, IntPointer channelsUsed,
                                     Mat img, IntPointer bins, PointerPointer<FloatPointer> ranges) {
        setHistogramRegion(histRegs.getTopLeft(), numberOfImages, channelsUsed, histRegs.getTopLeft().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getTopRight(), numberOfImages, channelsUsed, histRegs.getTopRight().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getBottomLeft(), numberOfImages, channelsUsed, histRegs.getBottomLeft().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getBottomRight(), numberOfImages, channelsUsed, histRegs.getBottomRight().getMasks().get(index), img, bins, ranges);
        setHistogramRegion(histRegs.getEllipse(), numberOfImages, channelsUsed, histRegs.getEllipse().getMasks().get(index), img, bins, ranges);
    }

    /**
     * Calculates and sets the histogram for a specific region.
     * If the region already has a histogram, the new histogram is added to it,
     * allowing for accumulation across multiple images.
     * 
     * @param histReg the histogram region to update
     * @param numberOfImages number of images being processed
     * @param channelsUsed pointer to channel indices
     * @param mask binary mask defining the region
     * @param img the image to analyze
     * @param bins pointer to bin counts
     * @param ranges pointer to value ranges
     */
    private void setHistogramRegion(HistogramRegion histReg, int numberOfImages, IntPointer channelsUsed, Mat mask,
                                    Mat img, IntPointer bins, PointerPointer<FloatPointer> ranges) {
        Mat hist = getHist(img, numberOfImages, channelsUsed, mask, bins, ranges);
        histReg.getHistograms().add(hist);
        if (histReg.getHistogram().empty()) histReg.setHistogram(hist);
        else add(histReg.getHistogram(), hist, histReg.getHistogram());
    }

    /**
     * Calculates a normalized histogram for the specified image region.
     * The histogram is computed using OpenCV's calcHist function and then
     * normalized to ensure consistent comparison between histograms.
     * 
     * @param image the image Mat to process
     * @param numberOfImages number of images (typically 1)
     * @param channelsUsed pointer to channel indices to include
     * @param mask binary mask defining the region of interest
     * @param bins pointer to bin counts for each channel
     * @param ranges pointer to value ranges for each channel
     * @return normalized histogram as a Mat
     */
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

    /**
     * Calculates a histogram from multiple images simultaneously.
     * This method is currently not used since patterns in Brobot images
     * typically have different sizes, making multi-image histogram
     * calculation less meaningful.
     * 
     * @param images list of image Mats to process together
     * @param channelsUsed pointer to channel indices to include
     * @param mask binary mask for region of interest
     * @param bins pointer to bin counts for each channel
     * @param ranges pointer to value ranges for each channel
     * @return normalized combined histogram as a Mat
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
