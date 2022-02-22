package io.github.jspinak.brobot.database.state;

import io.github.jspinak.brobot.database.primitives.image.Image;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.state.State;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import lombok.Data;
import org.sikuli.script.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.jspinak.brobot.database.state.NullState.Enum.NULL;

/**
 * This class holds all the objects that can be passed to an Action.
 */
@Data
public class ObjectCollection {

    private List<StateLocation> stateLocations = new ArrayList<>();
    private List<StateImageObject> stateImages = new ArrayList<>();
    private List<StateRegion> stateRegions = new ArrayList<>();
    private List<StateString> stateStrings = new ArrayList<>();
    private List<Matches> matches = new ArrayList<>();

    private ObjectCollection() {
    }

    public boolean empty() {
        return stateLocations.isEmpty()
                && stateImages.isEmpty()
                && stateRegions.isEmpty()
                && stateStrings.isEmpty()
                && matches.isEmpty();
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

    public static class Builder {
        private List<StateLocation> stateLocations = new ArrayList<>();
        private List<StateImageObject> stateImages = new ArrayList<>();
        private List<StateRegion> stateRegions = new ArrayList<>();
        private List<StateString> stateStrings = new ArrayList<>();
        private List<Matches> matches = new ArrayList<>();

        public Builder withLocations(Location... locations) {
            for (Location location : locations) this.stateLocations.add(location.inNullState());
            return this;
        }

        public Builder withLocations(StateLocation... locations) {
            Collections.addAll(this.stateLocations, locations);
            return this;
        }

        public Builder withImages(Image... images) {
            for (Image image : images) this.stateImages.add(image.inNullState());
            return this;
        }

        public Builder withImages(List<StateImageObject> images) {
            this.stateImages.addAll(images);
            return this;
        }

        public Builder withImages(StateImageObject... images) {
            Collections.addAll(this.stateImages, images);
            return this;
        }

        public Builder withAllStateImages(State state) {
            if (state == null) Report.print("null state passed| ");
            stateImages.addAll(state.getStateImages());
            return this;
        }

        public Builder withNonSharedImages(State state) {
            if (state == null) Report.print("null state passed| ");
            for (StateImageObject stateImageObject : state.getStateImages()) {
                if (!stateImageObject.isShared()) stateImages.add(stateImageObject);
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
                        .inState(NULL)
                        .build());
            }
            return this;
        }

        public ObjectCollection build() {
            ObjectCollection objectCollection = new ObjectCollection();
            objectCollection.stateLocations = stateLocations;
            objectCollection.stateImages = stateImages;
            objectCollection.stateRegions = stateRegions;
            objectCollection.stateStrings = stateStrings;
            objectCollection.matches = matches;
            return objectCollection;
        }
    }
}
