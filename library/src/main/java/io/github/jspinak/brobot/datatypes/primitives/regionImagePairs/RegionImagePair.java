package io.github.jspinak.brobot.datatypes.primitives.regionImagePairs;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Data;

/**
 * An Image with an associated Region. Used for Images with fixed locations
 * (the Image should always appear in the same location). When the Image is found,
 * the associated Region is defined by the Image Match. Further searches will look
 * only in the defined Region.
 */
@Data
public class RegionImagePair {

    private Region region = new Region();
    private Image image;

    public boolean defined() {
        return region.defined();
    }
}
