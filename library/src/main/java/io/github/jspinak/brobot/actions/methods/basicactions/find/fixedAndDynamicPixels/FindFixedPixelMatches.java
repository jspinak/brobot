package io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels;

import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.MatchProofer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindDynamicPixels;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FindFixedPixelMatches {

    private final FindDynamicPixels findDynamicPixels;
    private final MatchProofer matchProofer;

    public FindFixedPixelMatches(FindDynamicPixels findDynamicPixels, MatchProofer matchProofer) {
        this.findDynamicPixels = findDynamicPixels;
        this.matchProofer = matchProofer;
    }

    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        List<Pattern> allPatterns = getAllPatterns(objectCollections);
        MatVector matVector = new MatVector();
        allPatterns.forEach(pattern -> matVector.push_back(pattern.getMat()));
        if (matVector.size() < 2) return; // nothing to compare
        Mat fixedPixelMask = findDynamicPixels.getFixedPixelMask(matVector);
        matches.setMask(fixedPixelMask);
        // since all Patterns have the same fixed pixels, we can use any Pattern for the Match objects
        setMatches(fixedPixelMask, matches, allPatterns.get(0));
    }

    public List<Pattern> getAllPatterns(List<ObjectCollection> objectCollections) {
        List<Pattern> allPatterns = new ArrayList<>();
        // add all StateImages and Scenes
        objectCollections.forEach(objColl -> {
            objColl.getStateImages().forEach(stateImage -> {
                allPatterns.addAll(stateImage.getPatterns());
            });
            allPatterns.addAll(objColl.getScenes());
        });
        return allPatterns;
    }

    public void setMatches(Mat mask, Matches matches, Pattern pattern) {
        Contours contours = new Contours.Builder()
                .setBgrFromClassification2d(mask)
                .setMinArea(matches.getActionOptions().getMinArea())
                .setMaxArea(matches.getActionOptions().getMaxArea())
                .build();
        for (Match match : contours.getMatchList()) {
            if (matchProofer.isInSearchRegions(match, matches.getActionOptions(), pattern)) {
                match.setSearchImage(new Image(pattern));
                matches.add(match);
            }
        }
    }
}
