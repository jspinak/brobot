package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.grid.OverlappingGrids;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class FindAllHistograms {

    private HistComparison histComparison;

    public FindAllHistograms(HistComparison histComparison) {
        this.histComparison = histComparison;
    }

    /**
     * Looks for areas in the region that have a similar histogram to the given Image.
     * Divides region into smaller areas using two overlapping grids; searches each cell.
     * Returns a sorted Map with the regions from best match to worst match.
     * The Map's key is the Region and the value is the score as a Double.
     * TODO: Train a neural net using this method to find these areas much more quickly (YOLO).
     *
     * @param region the overall region in which to search
     * @param image the image to use
     * @return a sortable list of regions contained in the overall region and similarity scores
     */
    public LinkedHashMap<Region, Double> find(Region region, Image image) {
        OverlappingGrids overlappingGrids = new OverlappingGrids(new Grid.Builder()
                .setRegion(region)
                .setCellWidth(image.getWidth(0))
                .setCellHeight(image.getHeight(0))
                .build());
        return histComparison.compareAll(image, overlappingGrids.getAllRegions());
    }

}
