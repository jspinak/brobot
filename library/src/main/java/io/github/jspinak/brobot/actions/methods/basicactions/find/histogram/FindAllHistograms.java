package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.grid.OverlappingGrids;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetBufferedImage;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FindAllHistograms {

    private Histogram histogram;
    private CompareHistogram compareHistogram;
    private GetBufferedImage getBufferedImage;

    public FindAllHistograms(Histogram histogram, CompareHistogram compareHistogram,
                             GetBufferedImage getBufferedImage) {
        this.histogram = histogram;
        this.compareHistogram = compareHistogram;
        this.getBufferedImage = getBufferedImage;
    }

    /**
     * Looks for areas in the region that have a similar histogram to the given Image.
     * Divides region into smaller areas using two overlapping grids; searches each cell.
     * Returns a sorted Map with the regions from best match to worst match.
     * The Map's key is the Region and the value is the score as a Double.
     * TODO: Train a neural net using this method to find these areas much more quickly (YOLO).
     */
    public LinkedHashMap<Region, Double> find(Region region, Image image) {
        Map<Region, Double> allRegionScores = new HashMap<>();
        for (int i=0; i<image.getFilenames().size(); i++) {
            LinkedHashMap<Region, Double> regScores =
                    find(region, image.getWidth(i), image.getHeight(i), image.getFilenames().get(i));
            for (Map.Entry<Region, Double> entry : regScores.entrySet()) {
                allRegionScores.computeIfPresent(entry.getKey(), (k,v) -> Math.max(v, entry.getValue()));
                allRegionScores.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return allRegionScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    public LinkedHashMap<Region, Double> find(Region region, int width, int height, String filename) {
        Report.println("get hist for filename "+filename);
        OverlappingGrids overlappingGrids = new OverlappingGrids(new Grid.Builder()
                .setRegion(region)
                .setCellWidth(width)
                .setCellHeight(height)
                .build());
        return find(overlappingGrids, filename);
    }

    public LinkedHashMap<Region, Double> find(OverlappingGrids grids, String filename) {
        try {
            String pathName = ImagePath.getBundlePath()+"/"+filename;
            BufferedImage bufferedImage = getBufferedImage.fromFile(pathName);
            System.out.println(pathName);
            Map<Region, Double> regionScores = new HashMap<>();
            grids.getAllRegions().forEach(reg ->
                regionScores.put(reg, compareHistogram.compareHist(
                        bufferedImage, getBufferedImage.fromScreen(reg))));
            return regionScores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));
        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

}
