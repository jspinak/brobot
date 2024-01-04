package io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The image to compare is in ObjectCollections #1 (0), in the list of StateImage objects.
 * The images to compare with are in ObjectCollections #2 (1), in the list of StateImage objects.
 */
@Component
public class FindSimilarImages {

    private final CompareImages compareImages;

    public FindSimilarImages(CompareImages compareImages) {
        this.compareImages = compareImages;
    }

    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        if (objectCollections.size() < 2) return;
        if (objectCollections.get(0).getStateImages().isEmpty() || objectCollections.get(1).getStateImages().isEmpty()) return;
        /*
         There are images in the 1st and 2nd ObjectCollection.
         Compare all images in the 1st ObjectCollection with each image in the 2nd ObjectCollection. There will be
         a Match object for each image in the 2nd collection.
         */
        List<Match> matchList = new ArrayList<>();
        List<StateImage> baseImages = objectCollections.get(0).getStateImages();
        for (StateImage image : objectCollections.get(1).getStateImages()) {
            matchList.add(compareImages.compare(baseImages, image));
        }
        matches.setMatchList(matchList);
    }
}
