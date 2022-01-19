package database.primitives.regionImagePairs;

import com.brobot.multimodule.database.primitives.image.Image;
import com.brobot.multimodule.database.primitives.region.Region;
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
