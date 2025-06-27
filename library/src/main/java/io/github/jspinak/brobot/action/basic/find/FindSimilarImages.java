package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.analysis.compare.ImageComparer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The image to compare is in ObjectCollections #1 (0), in the list of StateImage objects.
 * The images to compare with are in ObjectCollections #2 (1), in the list of StateImage objects.
 */
@Component
public class FindSimilarImages {

    private final ImageComparer imageComparer;

    public FindSimilarImages(ImageComparer imageComparer) {
        this.imageComparer = imageComparer;
    }

    public void find(ActionResult actionResult, List<ObjectCollection> objectCollections) {
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
            Match comparisonMatch = imageComparer.compare(baseImages, image);
            matchList.add(comparisonMatch);
        }
        actionResult.setMatchList(matchList);
    }
}