package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;

/**
 * A class representing no match.
 */
public class NoMatch extends Match {

    public NoMatch() {
        setName("no match");
        setRegion(new Region(0,0,0,0));
        setScore(0);
    }

    public static class Builder {
        private String name = "no match";
        private Region region = new Region(0,0,0,0);
        private Image searchImage;
        private Scene scene;
        private StateObjectData stateObjectData;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setSearchImage(Image searchImage) {
            this.searchImage = searchImage;
            return this;
        }

        public Builder setScene(Scene scene) {
            this.scene = scene;
            return this;
        }

        public Builder setStateObjectData(StateObjectData stateObjectData) {
            this.stateObjectData = stateObjectData;
            return this;
        }

        public Match build() {
            Match match = new Match();
            match.setName(name);
            match.setRegion(region);
            match.setSearchImage(searchImage);
            match.setScene(scene);
            match.setStateObjectData(stateObjectData);
            match.setScore(0);
            return match;
        }
    }
}
