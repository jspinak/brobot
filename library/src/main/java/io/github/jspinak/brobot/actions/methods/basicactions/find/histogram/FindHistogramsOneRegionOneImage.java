package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.grid.OverlappingGrids;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindHistogramsOneRegionOneImage {

    private HistComparison histComparison;

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
     * @param stateImageObject the image to use
     * @param sceneHSV the scene in HSV
     * @return a sortable list of regions contained in the overall region and similarity scores
     */
    public List<MatchObject> find(Region region, StateImageObject stateImageObject, Mat sceneHSV, int actionId) {
        Image image = stateImageObject.getImage();
        region.setW(Math.min(region.w + region.x, sceneHSV.cols()) - region.x);
        region.setH(Math.min(region.h + region.y, sceneHSV.rows()) - region.y);
        OverlappingGrids overlappingGrids = new OverlappingGrids(new Grid.Builder()
                .setRegion(region)
                .setCellWidth(image.getAverageWidth())
                .setCellHeight(image.getAverageHeight())
                .build());
        return histComparison.compareAll(stateImageObject, overlappingGrids.getAllRegions(), sceneHSV, actionId);
    }

}
