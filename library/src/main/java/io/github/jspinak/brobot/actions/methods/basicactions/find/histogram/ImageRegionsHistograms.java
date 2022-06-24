package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImage;
import lombok.Getter;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.copyTo;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

@Component
@Getter
public class ImageRegionsHistograms {

    private GetImage getImage;

    private int hueBins = 12;
    private int satBins = 2;
    private int valBins = 1;
    private int totalBins = hueBins * satBins * valBins;

    public ImageRegionsHistograms(GetImage getImage) {
        this.getImage = getImage;
    }

    public void setBins(int hueBins, int satBins, int valBins) {
        this.hueBins = hueBins;
        this.satBins = satBins;
        this.valBins = valBins;
        totalBins = hueBins * satBins * valBins;
    }

    /**
     * Gets histograms for each region in the given image.
     * @param image Image objects may have Patterns with very different color profiles
     *              (i.e. red dot and yellow dot).
     *              For these cases, the Pattern can be selected with this method.
     * @param patternIndex The index is bounded by the first and the last filenames in the Image.
     * @return a list of the histograms for each region.
     */
    public List<Mat> getHistogramsFromPattern(Image image, int patternIndex) {
        String path = ImagePath.getBundlePath()+"/"+image.getFilename(patternIndex);
        Mat img = getImage.getMatFromFilename(path, true);
        //Imgproc.cvtColor(img, img, Imgproc.COLOR_HSV2BGR);
        //imwrite("image for histogram.png", img);
        return getHistograms(img);
    }

    public List<List<Mat>> getHistogramsFromAllPatterns(Image image) {
        List<List<Mat>> allHists = new ArrayList<>();
        for (int i=0; i<image.getFilenames().size(); i++) allHists.add(getHistogramsFromPattern(image, i));
        return allHists;
    }

    public List<Mat> getHistogramsFromRegion(Region region) {
        Mat img = getImage.getMatFromScreen(region, true);
        return getHistograms(img);
    }

    private List<Mat> getHistograms(Mat image) {
        List<Mat> histograms = new ArrayList<>();
        List<Mat> images = new ArrayList<>();
        images.add(image);
        /*
        Hue has values from 0 to 180, Saturation and Value from 0 to 255.
        We use 8 bins for Hue, 12 for Saturation, and 3 for Value.
        */
        float[] ranges = { 0, 180, 0, 256, 0, 256 };
        MatOfFloat rangeMat = new MatOfFloat(ranges);
        int[] channels = { 0, 1, 2 };
        MatOfInt channelMat = new MatOfInt(channels);
        int[] bins = { hueBins, satBins, valBins };
        MatOfInt binsMat = new MatOfInt(bins);
        ImageRegions imageRegions = new ImageRegions(image);
        imageRegions.getCornerMasks().forEach(mask -> histograms.add(
                getHist(images, channelMat, mask, binsMat, rangeMat, totalBins)));
        histograms.add(getHist(images, channelMat, imageRegions.getEllipseMask(), binsMat, rangeMat, totalBins));
        return histograms;
    }

    private Mat getHist(List<Mat> images, MatOfInt channelMat, Mat mask,
                        MatOfInt binsMat, MatOfFloat rangeMat, int totalBins) {
        Mat hist = new Mat();
        Imgproc.calcHist(images, channelMat, mask, hist, binsMat, rangeMat);
        Core.normalize(hist, hist);
        hist = hist.reshape(1, totalBins);
        return hist;
    }

    public void showHistogram(Mat hist, String name) {
        int hist_w = totalBins;
        int hist_h = 50;
        int bin_w = 1;
        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));
        System.out.println(hist.cols()+"."+hist.rows()+" "+hist.size()+" "+hist.channels());
        System.out.println(hist);
        System.out.println(name+" dump:\n"+hist.dump());
        for (int i = 1; i < hist_w; i++){
            Imgproc.line(histImage,
                    new Point(bin_w * (i - 1),
                            hist_h -
                            //Math.round(hist.get(i - 1, 0)[0])),
                            hist_h * hist.get(i - 1, 0)[0]),
                    new Point(bin_w * (i),
                            hist_h -
                                    //Math.round(hist.get(i, 0)[0])),
                                    hist_h * hist.get(i, 0)[0]),
                    new Scalar(255, 0, 0), 2, 8, 0);
        }
        imwrite(name+"hist.png", histImage);
    }

    public void showMaskedImage(Region region) {
        Mat img = getImage.getMatFromScreen(region, true);
        showMaskedImageSub(img);
    }

    public void showMaskedImage(Image image) {
        Mat img = getImage.getMatFromFilename(
                ImagePath.getBundlePath() + "/" + image.getFirstFilename(), true);
        showMaskedImageSub(img);
    }

    private void showMaskedImageSub(Mat img) {
        System.out.println(img);
        imwrite("original-hsv.png", img);
        Mat bgr = new Mat();
        Imgproc.cvtColor(img, bgr, Imgproc.COLOR_HSV2BGR);
        imwrite("original-bgr.png", bgr);
        ImageRegions imageRegions = new ImageRegions(img);
        showMaskedImage(img, imageRegions.getEllipseMask(), "ellipse");
        for (int i=0; i<4; i++) showMaskedImage(img, imageRegions.getCornerMasks().get(i), i+"");
    }

    private void showMaskedImage(Mat img, Mat mask, String name) {
        System.out.println(mask);
        //Mat src1_mask = new Mat();
        //cvtColor(imageRegions.getEllipseMask(), src1_mask, COLOR_GRAY2BGR); //change mask to a 3 channel image
        Mat mask_out = new Mat();
        //subtract(src1_mask, img, mask_out);
        //subtract(src1_mask, mask_out, mask_out);
        copyTo(img, mask_out, mask);
        System.out.println(mask_out.dump());
        Imgproc.cvtColor(mask_out, mask_out, Imgproc.COLOR_HSV2BGR);
        System.out.println(mask_out.dump());
        imwrite(name+".png", mask_out);
    }

}
