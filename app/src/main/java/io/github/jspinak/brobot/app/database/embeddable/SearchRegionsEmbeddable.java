package io.github.jspinak.brobot.app.database.embeddable;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Data
public class SearchRegionsEmbeddable {

    @ElementCollection
    @CollectionTable(name = "regions", joinColumns = @JoinColumn(name = "searchRegions_id"))
    private List<RegionEmbeddable> regions = new ArrayList<>();
    @Embedded
    private RegionEmbeddable fixedRegion = new RegionEmbeddable();
}
