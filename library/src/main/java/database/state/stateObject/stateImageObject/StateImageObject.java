package database.state.stateObject.stateImageObject;

import com.brobot.multimodule.database.primitives.image.Image;
import com.brobot.multimodule.database.primitives.location.Anchor;
import com.brobot.multimodule.database.primitives.location.Anchors;
import com.brobot.multimodule.database.primitives.location.Position;
import com.brobot.multimodule.database.primitives.match.MatchHistory;
import com.brobot.multimodule.database.primitives.match.MatchSnapshot;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.primitives.regionImagePairs.RegionImagePairs;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.StateObject;
import com.brobot.multimodule.primatives.enums.StateEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * StateImageObject represents an Image that exists in the owner State. Unless
 * it has a shared Image, this StateImageObject is representative of the State and will be
 * used to find it in case Brobot is lost. All StateImageObjects, including those with shared
 * Images, are used to find a State during StateTransitions and with Find Actions.
 *
 * StateImageObjects can have either an Image or a RegionImagePairs. RegionImagePairs are for
 * Images with fixed locations and are specified by the 'isFixed' option in the Builder.
 */
@Data
public class StateImageObject implements StateObject {

    // common StateObject fields (too few fields and too few StateObject classes for composition or hierarchy)
    // hierarchy would make builders much more complex
    private String name;
    // ownerStateName is set by the State when the object is added
    private StateEnum ownerStateName;
    //private int staysVisibleAfterClicked = 100; //probability
    private int timesActedOn = 0;
    private MatchHistory matchHistory = new MatchHistory();

    // unique fields
    private SearchRegions searchRegionsObject = new SearchRegions();
    private boolean fixed = false; // RIPs are fixed. the image always appears in the same spot.
    private Image image = new Image();
    private RegionImagePairs regionImagePairs = new RegionImagePairs();
    private int baseProbabilityExists = 100;
    private int probabilityExists = 100; // probability that the image exists given that the state exists.
    private Position position = new Position(50,50); // use to convert a match to a Location
    private boolean shared = false; // also found in other states
    private Anchors anchors = new Anchors(); // for defining regions using this object as input

    private StateImageObject() {}

    public Region getSearchRegion() {
        return searchRegionsObject.getSearchRegion();
    }

    public void setSearchRegion(Region region) {
        setSearchRegionsObject(Collections.singletonList(region));
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

    public static class Builder {
        private String name;
        private StateEnum ownerStateName;
        private MatchHistory matchHistory = new MatchHistory();
        private SearchRegions searchRegions = new SearchRegions();
        private boolean fixed = false;
        private Image image = new Image();
        private RegionImagePairs regionImagePairs = new RegionImagePairs();
        private int baseProbabilityExists = 100;
        private int probabilityExists = 100;
        private Position position = new Position(50, 50);
        private boolean shared = false;
        private Anchors anchors = new Anchors();

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder withSearchRegion(Region searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion);
            return this;
        }

        public Builder inState(StateEnum stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder isFixed() {
            this.fixed = true;
            return this;
        }

        public Builder withImage(String... imageNames) {
            this.image = new Image(imageNames);
            this.regionImagePairs = new RegionImagePairs(imageNames);
            this.name = imageNames[0];
            return this;
        }

        public Builder withImages(String... imageNames) {
            for (String str : imageNames) {
                withImage(str);
            }
            return this;
        }

        public Builder withImage(Image... images) {
            List<String> names = new ArrayList<>();
            for (Image image : images) names.addAll(image.getImageNames());
            this.image = new Image(names.toArray(new String[0]));
            this.regionImagePairs = new RegionImagePairs(names.toArray(new String[0]));
            this.name = images[0].getImageNames().toString().substring(0,20);
            return this;
        }

        public Builder withImages(Image... images) {
            for (Image image : images) withImage(image);
            return this;
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

        public Builder isShared() {
            this.shared = true;
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

        public StateImageObject build() {
            StateImageObject stateImageObject = new StateImageObject();
            stateImageObject.name = name;
            stateImageObject.matchHistory = matchHistory;
            stateImageObject.searchRegionsObject = searchRegions;
            stateImageObject.ownerStateName = ownerStateName;
            stateImageObject.fixed = fixed;
            stateImageObject.image = image;
            stateImageObject.regionImagePairs = regionImagePairs;
            stateImageObject.baseProbabilityExists = baseProbabilityExists;
            stateImageObject.probabilityExists = probabilityExists;
            stateImageObject.position = position;
            stateImageObject.shared = shared;
            stateImageObject.anchors = anchors;
            return stateImageObject;
        }

    }

}
