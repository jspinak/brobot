package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.database.primitives.image.Image;
import io.github.jspinak.brobot.database.primitives.region.Region;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Creates Match objects when probability is being used instead of Snapshots.
 * This class uses a lot of random generation to make Match objects. Snapshots
 * make use of a history of saved Match objects and thus do not need random numbers.
 */
@Component
public class MatchMaker {

    public static class Builder {
        private int x = -1;
        private int y = -1;
        private int w = 200;
        private int h = 100;
        private Region searchRegion = new Region();

        public Builder setSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        public Builder setImage(Image image) {
            if (image == null) return this;
            org.sikuli.script.Image img = new Pattern(image.getFilenames().get(0)).getImage();
            this.w = img.w;
            this.h = img.h;
            return this;
        }

        public Builder setImageWH(int w, int h) {
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder setImageXYWH(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder setImageXYWH(Region region) {
            this.x = region.x;
            this.y = region.y;
            this.w = region.w;
            this.h = region.h;
            return this;
        }

        public Match build() {
            if (x >= 0 && y >= 0) return buildWithXYWH();
            return buildInSearchRegion();
        }

        private Match buildWithXYWH() {
            return new Match(x, y, w, h, .99, new Screen());
        }

        /**
         * Builds a Match with the dimensions x,y,w,h somewhere in the searchRegion.
         * @return a new Match
         */
        private Match buildInSearchRegion() {
            x = searchRegion.x + new Random().nextInt(Math.max(1, searchRegion.w - w));
            y = searchRegion.y + new Random().nextInt(Math.max(1, searchRegion.h - h));
            return new Match(x, y, w, h, .99, new Screen());
        }
    }

}

// MatchMaker, MatchMaker, make me a match
