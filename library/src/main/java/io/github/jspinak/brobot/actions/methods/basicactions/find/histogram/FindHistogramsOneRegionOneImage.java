package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.grid.OverlappingGrids;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FindHistogramsOneRegionOneImage {

    private final HistComparison histComparison;

    public FindHistogramsOneRegionOneImage(HistComparison histComparison) {
        this.histComparison = histComparison;
    }

    /**
     * Looks for areas in the region that have a similar histogram to the given Image.
     * Divides region into smaller areas using two overlapping grids; searches each cell.
     * Returns a sorted Map with the regions from best match to worst match.
     * The Map's key is the Region and the value is the score as a Double.
     *
     * @param region the overall region in which to search
     * @param stateImage the image to use
     * @param sceneHSV the scene in HSV
     * @return a sortable list of regions contained in the overall region and similarity scores
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

    public List<Match> findAll(List<Region> regions, StateImage stateImage, Mat sceneHSV) {
        List<Match> allMatches = new ArrayList<>();
        regions.forEach(reg -> allMatches.addAll(find(reg, stateImage, sceneHSV)));
        return allMatches;
    }

}
