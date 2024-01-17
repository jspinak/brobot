package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.ImageOps;
import io.github.jspinak.brobot.imageUtils.MatOps;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.data.annotation.Id;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Pattern and StateImage_ (to replace StateImage) are for a code restructuring that gets rid of RegionImagePairs.
 * This can also replace DynamicImage, which currently only uses KmeansProfiles for one of its Image objects.
 * StateImage, which can hold multiple Pattern objects, is currently used for classification. Pattern could also be used
 * but not all Pattern objects are associated with StateImage objects and some are generic (without an image).
 *
 * TODO: If Pattern is never used for classification, you can remove the KMeansProfiles.
 *
 * <p>Database: Pattern is an @Entity since a single Pattern may be in multiple StateImage objects. All fields in
 * Pattern should be @Embedded. This is the lowest level of object that should be stored in the database. </p>
 */
@Entity
@Getter
@Setter
public class Pattern extends org.sikuli.script.Pattern {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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
    For this reason, I've created a Brobot Image object and use this object in Match. The Brobot Image object extends
    the SikuliX Image object.
     */
    private Mat mat;
    /*
    Since Pattern extends SikuliX Pattern, it contains a SikuliX Image object and a BufferedImage. There is no need for
    a Brobot Image, which extends SikuliX Image and would provide a duplicate BufferedImage. ImageDTO is used to store the
    BufferedImage in the database as a byte array. Any changes to the BufferedImage should be saved in the byte array before
    saving the Pattern to the database.
     */
    @Embedded
    private ImageDTO imageDTO;
    /*
    An image that should always appear in the same location has fixed==true.
     */
    private boolean fixed = false;
    @Embedded
    private SearchRegions searchRegions = new SearchRegions();
    private boolean setKmeansColorProfiles = false; // this is an expensive operation and should be done only when needed
    @Embedded
    private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    @Embedded
    private ColorCluster colorCluster = new ColorCluster();
    @Embedded
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
    @Embedded
    private Anchors anchors = new Anchors(); // for defining regions using this object as input

    public Pattern(String string) {
        super(string);
        setNameFromFilenameIfEmpty(string);
        setMatWithBufferedImage();
    }

    public Pattern(URL url) {
        super(url);
        setMatWithBufferedImage();
    }

    public Pattern(BufferedImage bimg) {
        super(bimg);
        setMatWithBufferedImage();
    }

    public Pattern(Mat mat) {
        super(BufferedImageOps.fromMat(mat));
    }

    /**
     * Converts the BufferedImage in Image to a BGR JavaCV Mat.
     * @return a BGR Mat.
     */
    public Mat getMat() {
        return ImageOps.getMatBGR(getImage());
    }

    /**
     * Setting the Mat involves converting the BGR Mat parameter to a BufferedImage and saving it in the SukiliX Image.
     * Mat objects are not stored explicitly in Pattern but converted to and from BufferedImage objects.
     * @param matBGR a JavaCV Mat in BGR format.
     */
    public void setMat(Mat matBGR) {
        setBImage(BufferedImageOps.fromMat(matBGR));
    }

    /**
     * Converts the BufferedImage in Image to an HSV JavaCV Mat.
     * @return an HSV Mat.
     */
    public Mat getMatHSV() {
        return ImageOps.getMatHSV(getImage());
    }

    /**
     * This is called before the Pattern is saved to the database. It saves the BufferedImage as a byte array in
     * the imageDTO object.
     */
    public void setBytes() {
        imageDTO.setBytesWithImage(getImage());
    }

    /**
     * This is called after the Pattern is retrieved from the database. The byte array in imageDTO is converted
     * to a BufferedImage and saved in the Image object of the Pattern.
     */
    public void setBufferedImage() {
        setBImage(imageDTO.getBufferedImage());
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

    private void setMatWithBufferedImage() {
        BufferedImage bImage = getBImage();
        Optional<Mat> optionalMat = MatOps.bufferedImageToMat(bImage);
        optionalMat.ifPresent(this::setMat);
    }

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
        return getImage() == null;
    }

    public boolean equals(Pattern pattern) {
        boolean sameFilename = getFilename().equals(pattern.getFilename());
        boolean bothFixedOrBothNot = fixed == pattern.isFixed();
        boolean sameSearchRegions = searchRegions.equals(pattern.getSearchRegions());
        boolean sameMatchHistory = matchHistory.equals(pattern.getMatchHistory());
        boolean bothDynamicOrBothNot = dynamic == pattern.isDynamic();
        boolean samePosition = position.equals(pattern.getPosition());
        boolean sameAnchors = anchors.equals(pattern.getAnchors());
        if (!sameFilename) return false;
        if (!bothFixedOrBothNot) return false;
        if (!sameSearchRegions) return false;
        if (!sameMatchHistory) return false;
        if (!bothDynamicOrBothNot) return false;
        if (!samePosition) return false;
        if (!sameAnchors) return false;
        return true;
    }

    public static class Builder {

        private String name;
        private Mat mat;
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

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMat(Mat mat) {
            this.mat = mat;
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
            // new Pattern(filename) should set the mat and name, only overwrite if explicitly set
            if (mat != null) {
                pattern.mat = mat;
                BufferedImage bufferedImage = BufferedImageOps.fromMat(mat);
                pattern.setBImage(bufferedImage); // this also sets SikuliX.Image
            }
            if (name != null) pattern.name = name;
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
