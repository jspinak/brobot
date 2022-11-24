package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Has the mask for one part of each image, i.e. all top-left corners of the patterns in a Brobot image.
 * The single histogram is calculated from the histograms of the patterns.
 */
@Getter
@Setter
public class HistogramRegion {

    private List<Mat> masks = new ArrayList<>();
    private List<Mat> histograms = new ArrayList<>();
    private Mat histogram;

}
