package io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeData;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.ImageAttributes;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.TransitionImage;
import io.github.jspinak.brobot.datatypes.primitives.dynamicImage.DynamicImage;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.regionImagePairs.RegionImagePairs;
import io.github.jspinak.brobot.datatypes.state.NullState;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * StateImageObject represents an Image that exists in the owner State. Unless
 * it has a shared Image, this StateImageObject is representative of the State and will be
 * used to find it in case Brobot is lost. All StateImageObjects, including those with shared
 * Images, are used to find a State during StateTransitions and with Find Actions.
 *
 * StateImageObjects can have either an Image or a RegionImagePairs. RegionImagePairs are for
 * Images with fixed locations and are specified by the 'isFixed' option in the Builder.
 */
@Getter
@Setter
public class StateImageObject implements StateObject {

    // common StateObject fields (too few fields and too few StateObject classes for composition or hierarchy)
    // hierarchy would make builders much more complex
    private String name;
    // the index is a unique identifier used for classification matrices
    private int index;
    // ownerStateName is set by the State when the object is added
    private String ownerStateName;
    //private int staysVisibleAfterClicked = 100; //probability
    private int timesActedOn = 0;
    private MatchHistory matchHistory = new MatchHistory();

    // unique fields
    private SearchRegions searchRegionsObject = new SearchRegions();
    private boolean fixed = false; // RIPs are fixed. the image always appears in the same spot.
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching
    private Image image = new Image();
    public Image getImage() {
        if (dynamic) {
            return dynamicImage.getInside();
        } else {
            return image;
        }
    }
    private RegionImagePairs regionImagePairs = new RegionImagePairs();
    private DynamicImage dynamicImage = new DynamicImage();
    private int baseProbabilityExists = 100;
    private int probabilityExists = 100; // probability that the image exists given that the state exists.
    private Position position = new Position(.5,.5); // use to convert a match to a Location
    private boolean shared = false; // also found in other states
    private Anchors anchors = new Anchors(); // for defining regions using this object as input
    /*
    Attributes learned from the image filename and initial screenshots of the environment.
    These values determine MatchHistories, SearchRegions, and other variables when
    building the initial State structure (States and Transitions) from image filenames and screenshots.
     */
    private ImageAttributes attributes = new ImageAttributes();

    // primarily used when creating state structures without names
    private TransitionImage transitionImage;
    private Set<String> statesToEnter = new HashSet<>();
    private Set<String> statesToExit = new HashSet<>();

    private StateImageObject() {}

    public Region getSearchRegion() {
        return searchRegionsObject.getSearchRegion();
    }

    public void setSearchRegion(Region region) {
        searchRegionsObject.setSearchRegion(region);
    }

    public List<Region> getAllSearchRegions() {
        return searchRegionsObject.getAllRegions();
    }

    public void setSearchRegionsObject(List<Region> regions) {
        searchRegionsObject.setSearchRegions(regions);
    }

    public void addSearchRegions(List<Region> regions) {
        searchRegionsObject.addSearchRegions(regions);
    }

    // when using RIPs we assume it is defined when found, when using var.loc.images we define the searchRegion explicitly.
    // to see if a searchRegion for a RIP is defined, use <varName>.getSearchRegion().defined()
    public boolean defined() {
        if (isFixed() && !regionImagePairs.defined()) return false;
        return searchRegionsObject.defined();
    }

    // for RIPs the search region may not be defined.
    // we want to be able to use the RIP's defined region for other operations.
    // this method will give us what we want regardless of whether the image is fixed or not
    // we want to know the region that has been defined for it. for variable location images, this is the searchRegion,
    // and for RIPs it is the region of the first image found.
    // we ignore the searchRegion for RIPs
    public Optional<Region> getDefinedRegion() {
        if (regionImagePairs.defined()) return Optional.of(regionImagePairs.getLastRegionFound());
        if (!isFixed() && searchRegionsObject.defined()) return Optional.of(searchRegionsObject.getSearchRegion());
        return Optional.empty();
    }

    public void setProbabilityToBaseProbability() {
        probabilityExists = baseProbabilityExists;
    }

    public void addAnchor(Position.Name definedRegionBorder, Position positionInMatch) {
        anchors.add(new Anchor(definedRegionBorder, positionInMatch));
    }

    public void addAnchor(Position.Name definedRegionBorder, Position.Name positionInMatch) {
        anchors.add(new Anchor(definedRegionBorder, new Position(positionInMatch)));
    }

    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withImages(this)
                .build();
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public void addSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public AttributeData getAttributeData(AttributeTypes.Attribute attribute) {
        return attributes.getScreenshots().get(attribute);
    }

    public void addImage(String filename) {
        regionImagePairs.addImage(filename);
        image.addImage(filename);
    }

    public ColorCluster getColorCluster() {
        return getDynamicImage().getInsideColorCluster();
    }

    /**
     * adds the Images, Snapshots, and Attributes of the parameter to this StateImageObject
     * @param img the StateImageObject with the values to add
     */
    public void merge(StateImageObject img) {
        // add images
        img.attributes.getFilenames().forEach(this::addImage);
        // add snapshots
        img.matchHistory.getSnapshots().forEach(snp -> matchHistory.addSnapshot(snp));
        // add attributes
        attributes.merge(img.attributes);
        if (!img.fixed) fixed = false;
    }

    public boolean equals(StateImageObject stateImageObject) {
        return image.equals(stateImageObject.getImage()) &&
                regionImagePairs.equals(stateImageObject.getRegionImagePairs());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("StateImageObject:");
        stringBuilder.append(" name=").append(name);
        stringBuilder.append(" ownerState=").append(ownerStateName);
        stringBuilder.append(" searchRegions=");
        searchRegionsObject.getAllRegions().forEach(stringBuilder::append);
        stringBuilder.append(" snapshotRegions=");
        matchHistory.getSnapshots().forEach(snapshot ->
                snapshot.getMatchList().forEach(stringBuilder::append));
        return stringBuilder.toString();
    }

    public static class Builder {
        private String name = "";
        private int index;
        private String ownerStateName;
        private MatchHistory matchHistory = new MatchHistory();
        private SearchRegions searchRegions = new SearchRegions();
        private boolean fixed = true; // set to true as of 1.0.2
        private boolean dynamic = false;
        private Image image = new Image();
        private RegionImagePairs regionImagePairs = new RegionImagePairs();
        private DynamicImage dynamicImage = new DynamicImage();
        private int baseProbabilityExists = 100;
        private int probabilityExists = 100;
        private Position position = new Position(.5, .5);
        private boolean shared = false;
        private Anchors anchors = new Anchors();
        private TransitionImage transitionImage;

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder withSearchRegion(Region searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion);
            return this;
        }

        public Builder withSearchRegion(int x, int y, int w, int h) {
            this.searchRegions.addSearchRegions(new Region(x, y, w, h));
            return this;
        }

        public Builder inState(String stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder isFixed(boolean fixed) {
            this.fixed = fixed;
            return this;
        }

        public Builder setDynamicImage(DynamicImage dynamicImage) {
            this.dynamicImage = dynamicImage;
            this.dynamic = true;
            return this;
        }

        public Builder isDynamic() {
            this.dynamic = true;
            return this;
        }

        public Builder withImage(String... imageNames) {
            this.image.addImages(imageNames);
            this.regionImagePairs.addImages(imageNames);
            if (this.name.equals("")) {
                int iend = imageNames[0].indexOf('_');
                if (iend == -1) this.name = imageNames[0];
                else this.name = imageNames[0].substring(0, iend);
            }
            return this;
        }

        public Builder withImages(String... imageNames) {
            return withImage(imageNames);
        }

        public Builder withImage(Image... images) {
            List<String> names = new ArrayList<>();
            for (Image image : images) names.addAll(image.getImageNames());
            return withImage(names.toArray(new String[0]));
        }

        public Builder withImages(Image... images) {
            return withImage(images);
        }

        public Builder withImages(List<Image> images) {
            return withImage(images.toArray(new Image[0]));
        }

        public Builder setBaseProbabilityExists(int prob) {
            baseProbabilityExists = prob;
            return this;
        }

        public Builder setProbabilityExists(int prob) {
            probabilityExists = prob;
            return this;
        }

        public Builder withPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder isShared(boolean shared) {
            this.shared = shared;
            return this;
        }

        public Builder addAnchor(Position.Name borderOfRegionToDefine, Position positionInMatch) {
            anchors.add(new Anchor(borderOfRegionToDefine, positionInMatch));
            return this;
        }

        public Builder addAnchor(Position.Name borderOfRegionToDefine, Position.Name positionInMatch) {
            anchors.add(new Anchor(borderOfRegionToDefine, new Position(positionInMatch)));
            return this;
        }

        public Builder addSnapshot(MatchSnapshot matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        // new in version 1.1.0
        public Builder addSnapshot(int x, int y, int w, int h) {
            this.matchHistory.addSnapshot(new MatchSnapshot(x, y, w, h));
            return this;
        }

        public Builder withTransitionImage(TransitionImage transitionImage) {
            this.transitionImage = transitionImage;
            return this;
        }

        /*
        When no dynamic inside image exists, set the inside image of the dynamic image to the static image provided.
        This makes it easier to initialize images, as a dynamic image is not required. Instead, the user can just
        toggle a boolean stating that the image is dynamic.
         */
        private void setDynamicInsideImage(StateImageObject stateImageObject) {
            if (stateImageObject.getDynamicImage().getInside().isEmpty()) {
                //Report.println("Setting dynamic inside image to static image for " + stateImageObject.getName());
                this.dynamicImage.setInside(stateImageObject.image);
            }
        }

        public StateImageObject build() {
            StateImageObject stateImageObject = new StateImageObject();
            stateImageObject.name = name;
            stateImageObject.index = index;
            stateImageObject.matchHistory = matchHistory;
            stateImageObject.searchRegionsObject = searchRegions;
            stateImageObject.ownerStateName = ownerStateName;
            stateImageObject.fixed = fixed;
            stateImageObject.dynamic = dynamic;
            stateImageObject.image = image;
            stateImageObject.regionImagePairs = regionImagePairs;
            stateImageObject.dynamicImage = dynamicImage;
            setDynamicInsideImage(stateImageObject);
            stateImageObject.baseProbabilityExists = baseProbabilityExists;
            stateImageObject.probabilityExists = probabilityExists;
            stateImageObject.position = position;
            stateImageObject.shared = shared;
            stateImageObject.anchors = anchors;
            stateImageObject.transitionImage = transitionImage;
            return stateImageObject;
        }

        public StateImageObject generic() {
            StateImageObject stateImageObject = new StateImageObject();
            stateImageObject.name = "generic";
            stateImageObject.ownerStateName = NullState.Name.NULL.toString();
            return stateImageObject;
        }

    }

}
