package io.github.jspinak.brobot.datatypes.primitives.regionImagePairs;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RegionImagePairs contain Images that should appear in fixed locations.
 * RegionImagePairs hold a RegionImagePair for each Image.
 * When an Image is found, its corresponding Region is defined.
 * Further searches will use the defined Region to look for an Image.
 */
@Getter
@Setter
public class RegionImagePairs {

    private Set<RegionImagePair> pairs = new HashSet<>();
    private RegionImagePair lastPairFound;

    public RegionImagePairs() {}

    public RegionImagePairs(String... images) {
        for (String image : images) {
            RegionImagePair newRegImgPair = new RegionImagePair();
            newRegImgPair.setImage(new Image(image));
            pairs.add(newRegImgPair);
        }
    }

    public void addImage(String imageName) {
        RegionImagePair newRegImgPair = new RegionImagePair();
        newRegImgPair.setImage(new Image(imageName));
        pairs.add(newRegImgPair);
    }

    public void addImages(String... imageNames) {
        List.of(imageNames).forEach(this::addImage);
    }

    public boolean defined() {
        return lastPairFound != null;
    }

    public Region getLastRegionFound() {
        return lastPairFound.getRegion();
    }

    public Image getFirstImage() {
        if (pairs.isEmpty()) return null;
        return pairs.iterator().next().getImage();
    }

    public Set<String> getImageNames() {
        Set<String> names = new HashSet<>();
        pairs.forEach(pair -> names.addAll(pair.getImage().getImageNames()));
        return names;
    }

    public boolean contains(Image image) {
        for (RegionImagePair rip : pairs) if (rip.getImage().equals(image)) return true;
        return false;
    }

    public boolean equals(RegionImagePairs regionImagePairs) {
        for (RegionImagePair rip : pairs) {
            if (!regionImagePairs.contains(rip.getImage())) return false;
        }
        return true;
    }

}
