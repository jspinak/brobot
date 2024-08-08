package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RegionEmbeddableMapper {

    public RegionEmbeddable map(Region region) {
        RegionEmbeddable regionEmbeddable = new RegionEmbeddable();
        regionEmbeddable.setX(region.getX());
        regionEmbeddable.setY(region.getY());
        regionEmbeddable.setW(region.getW());
        regionEmbeddable.setH(region.getH());
        return regionEmbeddable;
    }

    public Region map(RegionEmbeddable regionEmbeddable) {
        Region region = new Region();
        region.setX(regionEmbeddable.getX());
        region.setY(regionEmbeddable.getY());
        region.setW(regionEmbeddable.getW());
        region.setH(regionEmbeddable.getH());
        return region;
    }

    public List<RegionEmbeddable> mapToRegionEmbeddableList(List<Region> regions) {
        List<RegionEmbeddable> regionEmbeddableList = new ArrayList<>();
        regions.forEach(region -> regionEmbeddableList.add(map(region)));
        return regionEmbeddableList;
    }

    public List<Region> mapToRegionList(List<RegionEmbeddable> regionEmbeddableList) {
        List<Region> regions = new ArrayList<>();
        regionEmbeddableList.forEach(regionEmbeddable -> regions.add(map(regionEmbeddable)));
        return regions;
    }
}
