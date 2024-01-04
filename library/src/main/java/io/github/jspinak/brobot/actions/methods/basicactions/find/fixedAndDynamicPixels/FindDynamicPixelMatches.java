package io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels;

import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindDynamicPixels;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindDynamicPixelMatches {

    private final FindDynamicPixels findDynamicPixels;
    private final FindFixedPixelMatches findFixedPixelMatches;

    public FindDynamicPixelMatches(FindDynamicPixels findDynamicPixels, FindFixedPixelMatches findFixedPixelMatches) {
        this.findDynamicPixels = findDynamicPixels;
        this.findFixedPixelMatches = findFixedPixelMatches;
    }

    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        List<Pattern> allPatterns = findFixedPixelMatches.getAllPatterns(objectCollections);
        MatVector matVector = new MatVector();
        allPatterns.forEach(pattern -> matVector.push_back(pattern.getMat()));
        if (matVector.size() < 2) return; // nothing to compare
        Mat fixedPixelMask = findDynamicPixels.getDynamicPixelMask(matVector);
        matches.setMask(fixedPixelMask);
        // since all Patterns have the same fixed pixels, we can use any Pattern for the Match objects
        findFixedPixelMatches.setMatches(fixedPixelMask, matches, allPatterns.get(0));
    }
}
