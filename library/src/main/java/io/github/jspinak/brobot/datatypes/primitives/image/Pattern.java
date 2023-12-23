package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.SearchRegions;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

/**
 * Pattern and StateImage_ (to replace StateImage) are for a code restructuring that gets rid of RegionImagePairs.
 * This can also replace DynamicImage, which currently only uses KmeansProfiles for one of its Image objects.
 * StateImage, which can hold multiple Pattern objects, is currently used for classification. Pattern could also be used
 * but not all Pattern objects are associated with StateImage objects and some are generic (without an image). If
 * Pattern is never used for classification, you can remove the KMeansProfiles.
 */
@Getter
@Setter
public class Pattern extends org.sikuli.script.Pattern {

    private boolean fixed = false;
    private SearchRegions searchRegions = new SearchRegions();
    private boolean setKmeansColorProfiles = false; // this is an expensive operation and should be done only when needed
    private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    private MatchHistory matchHistory = new MatchHistory();
    private int index; // a unique identifier used for classification matrices
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching
    /**
     * Position is used to convert a Match to a Location. The super class has offset.x and offset.y, which
     * point to a location as an offset from the match's center. This is ok when you know the size of the
     * image but less convenient for working with general areas of the image (i.e. top left, bottom right).
     */
    private Position position = new Position(.5,.5); // use to convert a match to a location
    private Anchors anchors = new Anchors(); // for defining regions using this object as input

    public Pattern(String string) {
        super(string);
    }

    public Pattern(URL url) {
        super(url);
    }

    public Pattern(BufferedImage bimg) {
        super(bimg);
    }

    /**
     * Creates a generic Pattern without an associated image. Useful for creating MatchObject(s)
     * for operations not requiring a Pattern.
     * @return a generic Pattern
     */
    public Pattern() {}

    /**
     * The initial x will be 0. If a sub image is created, x will represent its position in the super image.
     * @return the x-position in the super image.
     */
    public int x() {
        return getImage().x;
    }

    public int y() {
        return getImage().y;
    }

    public int w() {
        return getImage().w;
    }

    public int h() {
        return getImage().h;
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found is returned.
     * If there are multiple regions, this returns a random selection.
     * @return a region
     */
    public Region getRegion() {
        return searchRegions.getRegion(fixed);
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found is returned.
     * Otherwise, all regions are returned.
     * @return all usable regions
     */
    public List<Region> getRegions() {
        return searchRegions.getRegions(fixed);
    }

    public void addSearchRegion(Region region) {
        searchRegions.addSearchRegions(region);
    }

    public void resetFixedSearchRegion() {
        searchRegions.resetFixedRegion();
    }

    public void setSearchRegions(Region... regions) {
        searchRegions.setSearchRegions(List.of(regions));
    }

    public void addMatchSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public static class Builder {

        private String filename;
        private boolean fixed = false;
        private SearchRegions searchRegions = new SearchRegions();
        private boolean setKmeansColorProfiles = false;
        private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        private MatchHistory matchHistory = new MatchHistory();
        private int index;
        private boolean dynamic = false;
        private Position position = new Position(.5,.5);
        private Anchors anchors = new Anchors();

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setFixed(boolean isFixed) {
            this.fixed = isFixed;
            return this;
        }

        public Builder setSearchRegions(SearchRegions searchRegions) {
            this.searchRegions = searchRegions;
            return this;
        }

        public Builder addSearchRegion(Region searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion);
            return this;
        }

        public Builder setSetKmeansColorProfiles(boolean setKmeansColorProfiles) {
            this.setKmeansColorProfiles = setKmeansColorProfiles;
            return this;
        }

        public Builder setKmeansProfilesAllSchemas(KmeansProfilesAllSchemas kmeansProfilesAllSchemas) {
            this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            return this;
        }

        public Builder setMatchHistory(MatchHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public Builder addMatchSnapshot(MatchSnapshot matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder setDynamic(boolean isDynamic) {
            this.dynamic = isDynamic;
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder addAnchor(Anchor anchor) {
            this.anchors.add(anchor);
            return this;
        }

        public Builder addAnchor(Position.Name inRegionToDefine, Position.Name inMatch) {
            this.anchors.add(new Anchor(inRegionToDefine, new Position(inMatch)));
            return this;
        }

        public Pattern build() {
            Pattern pattern = new Pattern(filename);
            pattern.fixed = fixed;
            pattern.searchRegions = searchRegions;
            pattern.setKmeansColorProfiles = setKmeansColorProfiles;
            pattern.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            pattern.matchHistory = matchHistory;
            pattern.index = index;
            pattern.dynamic = dynamic;
            pattern.position = position;
            pattern.anchors = anchors;
            return pattern;
        }
    }

}
