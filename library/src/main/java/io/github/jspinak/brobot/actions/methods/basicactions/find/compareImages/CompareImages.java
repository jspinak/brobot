package io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class CompareImages {

    private final MockOrLive mockOrLive;

    public CompareImages(MockOrLive mockOrLive) {
        this.mockOrLive = mockOrLive;
    }

    public Match compare(List<StateImage> imgs, StateImage img2) {
        Match bestScoringMatch = new Match(new org.sikuli.script.Match(new Region(), 0));
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
        Match bestScoringMatch = new Match(new org.sikuli.script.Match(new Region(), 0));
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
     * @return a Match of p2 with the similarity score
     */
    public Match compare(Pattern p1, Pattern p2) {
        Match match = new Match.Builder()
                .setMatch(new Region(0,0,1,1)) // the region is not important here
                .setPattern(p2)
                .build();
        Pattern biggestPattern = p1;
        Pattern smallesPattern = p2;
        BufferedImage bi1 = p1.getBImage();
        BufferedImage bi2 = p2.getBImage();
        if (bi1 == null || bi2 == null) return match;
        if ((bi1.getHeight()*bi1.getWidth()) < (bi2.getHeight()*bi2.getWidth())) {
            biggestPattern = p2;
            smallesPattern = p1;
        }
        Scene scene = new Scene.Builder()
                    .setBufferedImageBGR(biggestPattern.getBImage())
                    .build();

        List<Match> matchList = mockOrLive.findAll(smallesPattern, scene);
        Match bestMatch = Collections.max(matchList, Comparator.comparingDouble(org.sikuli.script.Match::getScore));
        if (bestMatch == null) return match;
        bestMatch.setPattern(p2);
        return bestMatch;
    }
}
