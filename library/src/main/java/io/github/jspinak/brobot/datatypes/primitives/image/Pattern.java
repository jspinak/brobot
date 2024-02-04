package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
//import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Pattern and StateImage_ (to replace StateImage) are for a code restructuring that gets rid of RegionImagePairs.
 * This can also replace DynamicImage, which currently only uses KmeansProfiles for one of its Image objects.
 * StateImage, which can hold multiple Pattern objects, is currently used for classification. Pattern could also be used
 * but not all Pattern objects are associated with StateImage objects and some are generic (without an image).
 *
 * TODO: If Pattern is never used for classification, you can remove the KMeansProfiles.
 *
 * <p>Database: Pattern is an @Entity since a single Pattern may be in multiple StateImage objects. All fields in
 * Pattern should be @Embedded. This is the lowest level of object that should be stored in the database.
 * I removed the SikuliX Pattern since it is a non-JPA entity and caused problems with persistence. </p>
 */
@Entity
@Getter
@Setter
public class Pattern {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // fields from SikuliX Pattern
    private String url; // originally type URL, which requires conversion for use with JPA
    private String imgpath;

    private String name;
    /*
    The SikuliX Image object is part of Pattern. It loads an image from file or the web into memory
    and provides a BufferedImage object. There are, however, occasions requiring an image to be
    captured only in memory. For example, when Brobot creates a model of the environment, the images
    captured can be saved in a database and do not need to be on file. This could be achieved with a
    SikuliX Image by setting the BufferedImage directly `super(bimg)`.

    It would be possible to use the SikuliX Image within Pattern for working with the physical representation of
    an image, but this raises architecture concerns in Brobot. Most importantly, Match needs a representation of the
    image, and having Pattern within Match would create a circular class chain. Pattern also needs a MatchHistory to
    perform mock runs and MatchHistory contains a list of MatchSnapshot, which itself contains a list of Match objects.
    For this reason, I've created a Brobot Image object and use this object in Match and in Pattern. ImageDTO is used to store the
    BufferedImage in the database as a byte array. The most recent BufferedImage is saved in the byte array before
    the Pattern is sent to the database.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;
    /*
    An image that should always appear in the same location has fixed==true.
     */
    private boolean fixed = false;
    @OneToOne(cascade = CascadeType.ALL)
    private SearchRegions searchRegions = new SearchRegions();
    private boolean setKmeansColorProfiles = false; // this is an expensive operation and should be done only when needed
    //@Embedded
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    //@Embedded
    //private ColorCluster colorCluster = new ColorCluster();
    @OneToOne(cascade = CascadeType.ALL)
    private MatchHistory matchHistory = new MatchHistory();
    private int index; // a unique identifier used for classification matrices
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching
    /**
     * Position is used to convert a Match to a Location. The super class has offset.x and offset.y, which
     * point to a location as an offset from the match's center. This is ok when you know the size of the
     * image but less convenient for working with general areas of the image (i.e. top left, bottom right).
     */
    @Embedded
    private Position position = new Position(.5,.5); // use to convert a match to a location
    @OneToOne(cascade = CascadeType.ALL)
    private Anchors anchors = new Anchors(); // for defining regions using this object as input

    public Pattern(String imgPath) {
        this.imgpath = imgPath;
        setNameFromFilenameIfEmpty(imgPath);
        // using SikuliX to get the BufferedImage would make it OS-independent
        this.image = new Image(BufferedImageOps.getBuffImgFromFile(imgPath));
    }

    public Pattern(BufferedImage bimg) {
        image = new Image(bimg);
    }

    public Pattern(Mat mat) {
        image = new Image(mat);
    }

    /**
     * Converts the BufferedImage in Image to a BGR JavaCV Mat.
     * @return a BGR Mat.
     */
    public Mat getMat() {
        return image.getMatBGR();
    }

    /**
     * Setting the Mat involves converting the BGR Mat parameter to a BufferedImage and saving it in the SukiliX Image.
     * Mat objects are not stored explicitly in Pattern but converted to and from BufferedImage objects.
     * @param matBGR a JavaCV Mat in BGR format.
     */
    public void setMat(Mat matBGR) {
        image.setBufferedImage(BufferedImageOps.fromMat(matBGR));
    }

    /**
     * Converts the BufferedImage in Image to an HSV JavaCV Mat.
     * @return an HSV Mat.
     */
    public Mat getMatHSV() {
        return image.getMatHSV();
    }

    /**
     * This is called before the Pattern is saved to the database. It saves the BufferedImage as a byte array in
     * the imageDTO object.
     */
    public void setBytes() {
        image.setBytesForPersistence();
    }

    /**
     * This is called after the Pattern is retrieved from the database. The byte array in imageDTO is converted
     * to a BufferedImage and saved in the Image object of the Pattern.
     */
    public void setBufferedImage() {
        image.setBufferedImageFromBytes();
    }

    /**
     * Creates a generic Pattern without an associated image. Useful for creating Match objects
     * for operations not requiring a Pattern.
     */
    public Pattern() {}

    private void setNameFromFilenameIfEmpty(String filename) {
        if (filename == null) return;
        if (name == null || name.isEmpty()) {
            File file = new File(filename); // Create a File object from the image path
            name = file.getName().replaceFirst("[.][^.]+$", ""); // the file name without extension
        }
    }

    public int w() {
        return image.w();
    }

    public int h() {
        return image.h();
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found is returned.
     * If there are multiple regions, this returns a random selection.
     * @return a region
     */
    public Region getRegion() {
        return searchRegions.getFixedIfDefinedOrRandomRegion(fixed);
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

    public void setSearchRegionsTo(Region... regions) {
        searchRegions.setRegions(List.of(regions));
    }

    public int size() {
        return w() * h();
    }

    public void addMatchSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public void addMatchSnapshot(int x, int y, int w, int h) {
        MatchSnapshot matchSnapshot = new MatchSnapshot(x, y, w, h);
        addMatchSnapshot(matchSnapshot);
    }

    public StateImage inNullState() {
        return new StateImage.Builder()
                .addPattern(this)
                .setOwnerStateName("null")
                .build();
    }

    public boolean isDefined() {
        return getSearchRegions().isDefined(fixed);
    }

    public boolean isEmpty() {
        return image.isEmpty();
    }

    public boolean equals(Pattern comparePattern) {
        boolean sameFilename = imgpath.equals(comparePattern.getImgpath());
        boolean bothFixedOrBothNot = fixed == comparePattern.isFixed();
        boolean sameSearchRegions = searchRegions.equals(comparePattern.getSearchRegions());
        boolean sameMatchHistory = matchHistory.equals(comparePattern.getMatchHistory());
        boolean bothDynamicOrBothNot = dynamic == comparePattern.isDynamic();
        boolean samePosition = position.equals(comparePattern.getPosition());
        boolean sameAnchors = anchors.equals(comparePattern.getAnchors());
        if (!sameFilename) return false;
        if (!bothFixedOrBothNot) return false;
        if (!sameSearchRegions) return false;
        if (!sameMatchHistory) return false;
        if (!bothDynamicOrBothNot) return false;
        if (!samePosition) return false;
        if (!sameAnchors) return false;
        return true;
    }

    // __convenience functions for the SikuliX Pattern object__
    /**
     * Another way to get the SikuliX object.
     * @return the SikuliX Pattern object.
     */
    public org.sikuli.script.Pattern sikuli() {
        return new org.sikuli.script.Pattern(image.sikuli());
    }

    public BufferedImage getBImage() {
        return image.getBufferedImage();
    }

    public static class Builder {

        private String name;
        private BufferedImage bufferedImage;
        private String filename;
        private boolean fixed = false;
        private SearchRegions searchRegions = new SearchRegions();
        private boolean setKmeansColorProfiles = false;
        //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        private MatchHistory matchHistory = new MatchHistory();
        private int index;
        private boolean dynamic = false;
        private Position position = new Position(.5,.5);
        private Anchors anchors = new Anchors();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMat(Mat mat) {
            this.bufferedImage = BufferedImageOps.fromMat(mat);
            return this;
        }

        public Builder setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setFixed(boolean isFixed) {
            this.fixed = isFixed;
            return this;
        }

        public Builder setFixedRegion(Region fixedRegion) {
            this.searchRegions.setFixedRegion(fixedRegion);
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

        //public Builder setKmeansProfilesAllSchemas(KmeansProfilesAllSchemas kmeansProfilesAllSchemas) {
        //    this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
        //    return this;
        //}

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

        public Builder addAnchor(Positions.Name inRegionToDefine, Positions.Name inMatch) {
            this.anchors.add(new Anchor(inRegionToDefine, new Position(inMatch)));
            return this;
        }

        private Pattern makeNewPattern() {
            if (filename != null) return new Pattern(filename);
            if (bufferedImage != null) return new Pattern(bufferedImage);
            return new Pattern();
        }

        private void setBImageIfAvailable(Pattern pattern) {
            if (bufferedImage == null) return;
            if (pattern.getImage() == null) pattern.setImage(new Image(bufferedImage));
            else if (pattern.getBImage() == null) pattern.getImage().setBufferedImage(bufferedImage);
        }

        public Pattern build() {
            Pattern pattern = makeNewPattern();
            if (name != null) pattern.name = name;
            setBImageIfAvailable(pattern);
            pattern.fixed = fixed;
            pattern.searchRegions = searchRegions;
            pattern.setKmeansColorProfiles = setKmeansColorProfiles;
            //pattern.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            pattern.matchHistory = matchHistory;
            pattern.index = index;
            pattern.dynamic = dynamic;
            pattern.position = position;
            pattern.anchors = anchors;
            return pattern;
        }
    }

}
