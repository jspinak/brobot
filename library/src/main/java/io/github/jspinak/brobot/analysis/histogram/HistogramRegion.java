package io.github.jspinak.brobot.analysis.histogram;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a specific region within images for histogram analysis. This class manages masks and
 * histograms for a particular region (e.g., top-left corner, bottom-right corner, or center
 * ellipse) across multiple pattern images.
 *
 * <p>Each HistogramRegion maintains:
 *
 * <ul>
 *   <li>A list of masks defining the region boundaries for each pattern
 *   <li>Individual histograms computed for each pattern's region
 *   <li>A combined histogram aggregating all pattern histograms
 * </ul>
 *
 * <p>The combined histogram is typically computed by summing the individual pattern histograms,
 * providing an overall color distribution for this specific region across all patterns in a Brobot
 * image.
 *
 * @see HistogramRegions
 * @see HistogramExtractor
 */
@Getter
@Setter
public class HistogramRegion {

    /**
     * Binary masks defining this region's boundaries for each pattern image. Each mask has white
     * pixels (255) in the region of interest and black pixels (0) elsewhere.
     */
    private List<Mat> masks = new ArrayList<>();

    /**
     * Individual histograms computed for this region from each pattern image. Each histogram
     * represents the color distribution within the masked region.
     */
    private List<Mat> histograms = new ArrayList<>();

    /**
     * Combined histogram aggregating all individual pattern histograms. This represents the overall
     * color distribution for this region across all patterns.
     */
    private Mat histogram;
}
