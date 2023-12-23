package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.ImageAttributes;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.TransitionImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of Pattern objects, meant to replace StateImage.
 * It can belong to a state.
 *
 *
 * Some variables names appear both in Pattern and Image. For example,
 * - index values are used for individual Pattern objects and a different set of index values are used for Image
 *   objects.
 * - KmeansProfiles for Image objects use the pixels of all Pattern objects in the Image to determine the ColorProfiles.
 */
@Getter
public class StateImage_ { //implements StateObject {

    private String name = "";
    private List<Pattern> patterns = new ArrayList<>();
    private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    private int index; // a unique identifier used for classification matrices
    private String ownerStateName = "null"; // ownerStateName is set by the State when the object is added
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

    /**
     * Sets the Position for each Pattern in the Image.
     * @param position the Position to use for each Pattern.
     */
    public void setPositions(Position position) {
        patterns.forEach(pattern -> pattern.setPosition(position));
    }

    /**
     * Sets the Anchor objects for each Pattern in the Image. Existing Anchor objects will be deleted.
     * @param anchors the Anchor objects to use for each Pattern.
     */
    public void setAnchors(Anchor... anchors) {
        patterns.forEach(pattern -> pattern.getAnchors().setAnchors(List.of(anchors)));
    }

    /**
     * Sets the search regions for each Pattern in the Image. Existing search regions will be deleted.
     * @param regions the regions to set for each Pattern.
     */
    public void setSearchRegions(Region... regions) {
        patterns.forEach(pattern -> pattern.setSearchRegions(regions));
    }

    public static class Builder {
        private String name = "";
        private List<Pattern> patterns = new ArrayList<>();
        private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        private int index;
        private String ownerStateName = "null";
        private ImageAttributes attributes = new ImageAttributes();
        private TransitionImage transitionImage;
        private Set<String> statesToEnter = new HashSet<>();
        private Set<String> statesToExit = new HashSet<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPatterns(List<Pattern> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder addPattern(Pattern pattern) {
            this.patterns.add(pattern);
            return this;
        }

        public Builder setKmeansProfilesAllSchemas(KmeansProfilesAllSchemas kmeansProfilesAllSchemas) {
            this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            return this;
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder setOwnerStateName(String ownerStateName) {
            this.ownerStateName = ownerStateName;
            return this;
        }

        public Builder setAttributes(ImageAttributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder setTransitionImage(TransitionImage transitionImage) {
            this.transitionImage = transitionImage;
            return this;
        }

        public Builder setStatesToEnter(Set<String> statesToEnter) {
            this.statesToEnter = statesToEnter;
            return this;
        }

        public Builder setStatesToExit(Set<String> statesToExit) {
            this.statesToExit = statesToExit;
            return this;
        }

        public StateImage_ build() {
            StateImage_ stateImage = new StateImage_();
            stateImage.name = name;
            stateImage.patterns = patterns;
            stateImage.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
            stateImage.index = index;
            stateImage.ownerStateName = ownerStateName;
            stateImage.attributes = attributes;
            stateImage.transitionImage = transitionImage;
            stateImage.statesToEnter = statesToEnter;
            stateImage.statesToExit = statesToExit;
            return stateImage;
        }
    }
}
