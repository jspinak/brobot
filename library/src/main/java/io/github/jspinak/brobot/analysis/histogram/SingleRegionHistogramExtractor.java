package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.model.element.Grid; 
import io.github.jspinak.brobot.model.element.OverlappingGrids;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs histogram-based pattern matching using a sliding window approach.
 * <p>
 * This class implements a specialized histogram matching strategy that divides
 * search regions into overlapping grids of cells. Each cell is approximately
 * the size of the target image, and histogram comparison is performed on each
 * cell to find the best matching locations.
 * <p>
 * The overlapping grid approach ensures that matches are not missed due to
 * arbitrary grid boundaries. This is particularly effective when:
 * <ul>
 * <li>The target object's exact location is unknown</li>
 * <li>Multiple instances of similar objects may exist</li>
 * <li>Precise template matching is too strict due to variations</li>
 * </ul>
 *
 * @see OverlappingGrids
 * @see HistogramComparator
 * @see Grid
 */
@Component
public class SingleRegionHistogramExtractor {

    private final HistogramComparator histComparison;

    public SingleRegionHistogramExtractor(HistogramComparator histComparison) {
        this.histComparison = histComparison;
    }

    /**
     * Searches for histogram matches within a specified region using overlapping grids.
     * <p>
     * This method implements a sliding window search strategy:
     * <ol>
     * <li>Adjusts the search region to fit within scene boundaries</li>
     * <li>Creates overlapping grids with cells sized to match the target image</li>
     * <li>Performs histogram comparison on each grid cell</li>
     * <li>Returns all matches found across the overlapping grids</li>
     * </ol>
     * <p>
     * The overlapping grids ensure that objects positioned at grid boundaries
     * are not missed. Cell dimensions are automatically determined based on
     * the average dimensions of the target StateImage.
     * <p>
     * Side effects: The input region's dimensions may be adjusted to prevent
     * out-of-bounds access to the scene.
     *
     * @param region The search region within the scene. Will be adjusted to fit scene bounds.
     * @param stateImage The target image whose histogram signature is being matched
     * @param sceneHSV The scene image in HSV color space for histogram extraction
     * @return List of Match objects representing similar regions, sorted by similarity score
     */
    public List<Match> find(Region region, StateImage stateImage, Mat sceneHSV) {
        region.setW(Math.min(region.w() + region.x(), sceneHSV.cols()) - region.x());
        region.setH(Math.min(region.h() + region.y(), sceneHSV.rows()) - region.y());
        OverlappingGrids overlappingGrids = new OverlappingGrids(new Grid.Builder()
                .setRegion(region)
                .setCellWidth((int)stateImage.getAverageWidth())
                .setCellHeight((int)stateImage.getAverageHeight())
                .build());
        return histComparison.compareAll(stateImage, overlappingGrids.getAllRegions(), sceneHSV);
    }

    /**
     * Performs histogram matching across multiple search regions.
     * <p>
     * This convenience method applies the single-region histogram search to
     * multiple regions, aggregating all matches found. This is useful when
     * searching in specific areas of interest rather than the entire scene.
     * <p>
     * Each region is processed independently using {@link #find}, and all
     * resulting matches are combined into a single list.
     *
     * @param regions List of search regions to examine. Each region will be adjusted to scene bounds.
     * @param stateImage The target image whose histogram is being matched
     * @param sceneHSV The scene image in HSV color space
     * @return Aggregated list of all Match objects found across all regions
     */
    public List<Match> findAll(List<Region> regions, StateImage stateImage, Mat sceneHSV) {
        List<Match> allMatches = new ArrayList<>();
        regions.forEach(reg -> allMatches.addAll(find(reg, stateImage, sceneHSV)));
        return allMatches;
    }

}
