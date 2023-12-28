package io.github.jspinak.brobot.datatypes.state;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;

import java.util.*;

import static io.github.jspinak.brobot.datatypes.primitives.location.Position.Name.TOPLEFT;
import static io.github.jspinak.brobot.datatypes.state.NullState.Name.NULL;

/**
 * This class holds all the objects that can be passed to an Action, which include:
 * - StateLocation
 * - StateImage
 * - StateRegion
 * - StateString
 * - Matches
 * - Image: these are scenes, or screenshots on file, that are used for the operation instead of the active environment
 *   (what is currently on the monitor).
 */
@Getter
@Setter
public class ObjectCollection {

    private List<StateLocation> stateLocations = new ArrayList<>();
    private List<StateImage> stateImages = new ArrayList<>();
    private List<StateRegion> stateRegions = new ArrayList<>();
    private List<StateString> stateStrings = new ArrayList<>();
    private List<Matches> matches = new ArrayList<>();
    private List<Pattern> scenes = new ArrayList<>();

    private ObjectCollection() {}

    public boolean isEmpty() {
        return stateLocations.isEmpty()
                && stateImages.isEmpty()
                && stateRegions.isEmpty()
                && stateStrings.isEmpty()
                && matches.isEmpty()
                && scenes.isEmpty();
    }

    /**
     * Sets the timesActedOn variable to 0 for all objects, including those
     * found in the Matches variable. Knowing how many times an object Match
     * was acted on is valuable for understanding the actual automation as
     * well as for performing mocks.
     */
    public void resetTimesActedOn() {
        stateImages.forEach(sio -> sio.setTimesActedOn(0));
        stateLocations.forEach(sio -> sio.setTimesActedOn(0));
        stateRegions.forEach(sio -> sio.setTimesActedOn(0));
        stateStrings.forEach(sio -> sio.setTimesActedOn(0));
        matches.forEach(m -> m.setTimesActedOn(0));
    }

    public String getFirstObjectName() {
        if (!stateImages.isEmpty()) {
            if (!stateImages.get(0).getName().isEmpty()) return stateImages.get(0).getName();
            else if (!stateImages.get(0).getPatterns().get(0).getFilename().isEmpty())
                return stateImages.get(0).getPatterns().get(0).getFilename();
        }
        if (!stateLocations.isEmpty() && !stateLocations.get(0).getName().isEmpty())
            return stateLocations.get(0).getName();
        if (!stateRegions.isEmpty() && !stateRegions.get(0).getName().isEmpty())
            return stateRegions.get(0).getName();
        if (!stateStrings.isEmpty()) return stateStrings.get(0).getString();
        return "";
    }

    public boolean contains(StateImage stateImage) {
        for (StateImage si : stateImages) {
            if (stateImage.equals(si)) return true;
        }
        return false;
    }

    public boolean contains(StateRegion stateRegion) {
        return stateRegions.contains(stateRegion);
    }

    public boolean contains(StateLocation stateLocation) {
        return stateLocations.contains(stateLocation);
    }

    public boolean contains(StateString stateString) {
        return stateStrings.contains(stateString);
    }

    public boolean contains(Matches m) {
        return matches.contains(m);
    }

    public boolean contains(Pattern sc) { return scenes.contains(sc); }

    public boolean equals(ObjectCollection objectCollection) {
        for (StateImage si : stateImages) {
            if (!objectCollection.contains(si)) {
                //Report.println(si.getName()+" is not in the objectCollection. ");
                return false;
            }
        }
        for (StateRegion sr : stateRegions) {
            if (!objectCollection.contains(sr)) {
                //Report.println(" regions different ");
                return false;
            }
        }
        for (StateLocation sl : stateLocations) {
            if (!objectCollection.contains(sl)) {
                //Report.println(" locations different ");
                return false;
            }
        }
        for (StateString ss : stateStrings) {
            if (!objectCollection.contains(ss)) {
                //Report.println(" strings different ");
                return false;
            }
        }
        for (Matches m : matches) {
            if (!objectCollection.contains(m)) {
                //Report.println(" matches different ");
                return false;
            }
        }
        for (Pattern sc : scenes) {
            if (!objectCollection.contains(sc)) {
                //Report.println(" matches different ");
                return false;
            }
        }
        return true;
    }

    public Set<String> getAllImageFilenames() {
        Set<String> filenames = new HashSet<>();
        stateImages.forEach(sI -> sI.getPatterns().forEach(p -> filenames.add(p.getFilename())));
        return filenames;
    }

    public Set<String> getAllOwnerStates() {
        Set<String> states = new HashSet<>();
        stateImages.forEach(si -> states.add(si.getOwnerStateName()));
        stateLocations.forEach(si -> states.add(si.getOwnerStateName()));
        stateRegions.forEach(si -> states.add(si.getOwnerStateName()));
        stateStrings.forEach(si -> states.add(si.getOwnerStateName()));
        return states;
    }

    public static class Builder {
        private List<StateLocation> stateLocations = new ArrayList<>();
        private List<StateImage> stateImage_s = new ArrayList<>();
        private List<StateRegion> stateRegions = new ArrayList<>();
        private List<StateString> stateStrings = new ArrayList<>();
        private List<Matches> matches = new ArrayList<>();
        private List<Pattern> scene_s = new ArrayList<>();

        public Builder withLocations(Location... locations) {
            for (Location location : locations) {
                StateLocation stateLocation = location.asStateLocationInNullState();
                stateLocation.setPosition(TOPLEFT);
                this.stateLocations.add(stateLocation);
            }
            return this;
        }

        public Builder withLocations(StateLocation... locations) {
            Collections.addAll(this.stateLocations, locations);
            return this;
        }

        public Builder withImage_s(StateImage... stateImages) {
            this.stateImage_s.addAll(Arrays.asList(stateImages));
            return this;
        }

        public Builder withImage_s(List<StateImage> stateImages) {
            this.stateImage_s.addAll(stateImages);
            return this;
        }

        public Builder withPatterns(Pattern... patterns) {
            for (Pattern pattern : patterns) this.stateImage_s.add(pattern.inNullState());
            return this;
        }

        public Builder withAllStateImages(State state) {
            if (state == null) {
                Report.print("null state passed| ");
                return this;
            } else stateImage_s.addAll(state.getStateImages());
            return this;
        }

        public Builder withNonSharedImages(State state) {
            if (state == null) {
                Report.print("null state passed| ");
                return this;
            }
            for (StateImage stateImage : state.getStateImages()) {
                if (!stateImage.isShared()) stateImage_s.add(stateImage);
            }
            return this;
        }

        public Builder withRegions(Region... regions) {
            for (Region region : regions) this.stateRegions.add(region.inNullState());
            return this;
        }

        public Builder withRegions(StateRegion... regions) {
            Collections.addAll(this.stateRegions, regions);
            return this;
        }

        public Builder withGridSubregions(int rows, int columns, Region... regions) {
            for (Region region : regions) {
                for (Region gridRegion : region.getGridRegions(rows, columns))
                    this.stateRegions.add(gridRegion.inNullState());
            }
            return this;
        }

        // should the State info be kept if it's a subregion? this is not clear, so we're sticking with NullState for now
        public Builder withGridSubregions(int rows, int columns, StateRegion... regions) {
            for (StateRegion region : regions) {
                for (Region gridRegion : region.getSearchRegion().getGridRegions(rows, columns))
                    this.stateRegions.add(gridRegion.inNullState());
            }
            return this;
        }

        public Builder withStrings(String... strings) {
            for (String string : strings) this.stateStrings.add(new StateString.InNullState().withString(string));
            return this;
        }

        public Builder withStrings(StateString... strings) {
            Collections.addAll(this.stateStrings, strings);
            return this;
        }

        public Builder withMatches(Matches... matches) {
            Collections.addAll(this.matches, matches);
            return this;
        }

        public Builder withMatches(Match... matches) {
            for (Match match : matches) {
                this.stateRegions.add(new StateRegion.Builder()
                        .withSearchRegion(new Region(match))
                        .inState(NULL.toString())
                        .build());
            }
            return this;
        }

        public Builder withScene_s(String... strings) {
            for (String string : strings) this.scene_s.add(new Pattern(string));
            return this;
        }

        public Builder withScene_s(Pattern... patterns) {
            this.scene_s.addAll(List.of(patterns));
            return this;
        }

        public ObjectCollection build() {
            ObjectCollection objectCollection = new ObjectCollection();
            objectCollection.stateImages = stateImage_s;
            objectCollection.stateLocations = stateLocations;
            objectCollection.stateRegions = stateRegions;
            objectCollection.stateStrings = stateStrings;
            objectCollection.matches = matches;
            objectCollection.scenes = scene_s;
            return objectCollection;
        }
    }
}
