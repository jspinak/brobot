package io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages;

import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.NoMatch;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class CompareImages {

    private final MockOrLive mockOrLive;
    private final CompareSize compareSize;

    public CompareImages(MockOrLive mockOrLive, CompareSize compareSize) {
        this.mockOrLive = mockOrLive;
        this.compareSize = compareSize;
    }

    /**
     * Compare all StateImage objects in the 1st parameter with the StateImage in the 2nd parameter.
     * The Match returned is the best match found, or no match when there are none.
     * @param imgs a list of images
     * @param img2 the image to compare
     * @return the best match
     */
    public Match compare(List<StateImage> imgs, StateImage img2) {
        Match bestScoringMatch = new NoMatch();
        for (StateImage img1 : imgs) {
            Match newMatch = compare(img1, img2);
            if (newMatch.getScore() > bestScoringMatch.getScore()) bestScoringMatch = newMatch;
        }
        return bestScoringMatch;
    }

    /**
     * The largest of img1 and img2 will become the scene.
     * We want to know how img2 compares with img1, so the Match returned will have details for img2 and the
     * match score.
     * @param img1 the base image
     * @param img2 the comparing image
     * @return a Match of img2 with the similarity score
     */
    public Match compare(StateImage img1, StateImage img2) {
        Match bestScoringMatch = new NoMatch();
        for (Pattern p1 : img1.getPatterns()) {
            for (Pattern p2 : img2.getPatterns()) {
                Match newMatch = compare(p1, p2);
                if (newMatch.getScore() > bestScoringMatch.getScore()) bestScoringMatch = newMatch;
            }
        }
        return bestScoringMatch;
    }

    /**
     * Comparing images is subjective. This will need various options in the ActionOptions to take care of the
     * different ways to compare. For example, is an image contained multiple times in another larger image more or
     * less similar than an image of the same size but with no direct matches? This base case is built for simplicity
     * and will return the best match of one image in another.
     * @param p1 the base image
     * @param p2 the image to compare
     * @return a Match with the smaller image as the searchImage and the larger image as the Scene.
     */
    public Match compare(Pattern p1, Pattern p2) {
        if (p1 == null || p2 == null || p1.getBImage() == null || p2.getBImage() == null) return new NoMatch();
        List<Pattern> sortedPatterns = compareSize.getEnvelopedFirstOrNone(p1, p2);
        if (sortedPatterns.isEmpty()) return new NoMatch();
        Pattern biggestPattern = sortedPatterns.get(1);
        Pattern smallestPattern = sortedPatterns.get(0);
        String name = smallestPattern.getName() + " found in " + biggestPattern.getName();
        Image scene = biggestPattern.getImage();
        List<Match> matchList = mockOrLive.findAll(smallestPattern, scene);
        if (matchList.isEmpty()) {
            return new NoMatch.Builder()
                    .setName(name)
                    .setSearchImage(smallestPattern.getImage())
                    .setScene(biggestPattern.getImage())
                    .build();
        }
        return Collections.max(matchList, Comparator.comparingDouble(Match::getScore));
    }
}
