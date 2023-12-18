package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Setter
public class GetTransitionImages {

    private final GetImageJavaCV getImage;

    private int minWidthBetweenImages = 6; // when images are closer together, they get merged into one image

    public GetTransitionImages(GetImageJavaCV getImage) {
        this.getImage = getImage;
    }

    /**
     * This method searches on screen for potential links that, when clicked, will take us to other pages.
     * It looks for words and symbols and finds the regions of these words and symbols.
     * Once it has the regions, it attempts to group symbols and regions that are close together. It does this
     * because there are often multiple words in a link. Then, it takes the union of the images in a group to get
     * a region that encompasses all images in the group. It then captures the pixels and stores it in a Mat object
     * in a TransitionImage object. TransitionImage objects, once created, are stored in the TransitionImage
     * repository.
     *
     * @param usableArea images are only used if they are within this region
     * @return a list of TransitionImage objects
     */
    public List<TransitionImage> findAndCapturePotentialLinks(Region usableArea, List<Region> dynamicRegions) {
        List<TransitionImage> transitionImages = new ArrayList<>();
        List<Match> potentialLinks = new Screen().findWords();
        List<Match> usableLinks = getUsableLinks(potentialLinks, usableArea, dynamicRegions);
        for (int i=0; i<usableLinks.size(); i++) {
            TransitionImageHelper transitionImageHelper = createTransitionImage(i, usableLinks);
            TransitionImage transitionImage = transitionImageHelper.getTransitionImage();
            Mat image = getImage.getMatFromScreen(transitionImage.getRegion());
            transitionImage.setImage(image);
            transitionImages.add(transitionImage);
            i = transitionImageHelper.getLastIndex();
        }
        return transitionImages;
    }

    private List<Match> getUsableLinks(List<Match> allMatches, Region usableArea, List<Region> dynamicRegions) {
        List<Match> fixedAndUsableLinks = new ArrayList<>();
        List<Match> usableLinks = allMatches.stream().filter(usableArea::contains).toList();
        for (Match link : usableLinks) {
            if (isLinkInFixedRegion(dynamicRegions, link)) fixedAndUsableLinks.add(link);
        }
        return fixedAndUsableLinks;
    }

    private boolean isLinkInFixedRegion(List<Region> dynamicRegions, Match link) {
        for (Region dynamicRegion : dynamicRegions) {
            if (dynamicRegion.contains(link.getCenter())) return false;
        }
        return true;
    }

    /*
    Returns the index of the last image included in the TransitionImage.
     */
    TransitionImageHelper createTransitionImage(int startIndex, List<Match> links) {
        TransitionImageHelper helper = new TransitionImageHelper(startIndex, links.get(startIndex));
        for (int i=startIndex+1; i<links.size(); i++) {
            if (!helper.getTransitionImage().isSameWordGroup(links.get(i), minWidthBetweenImages)) return helper;
            helper.addPotentialLink(i, links.get(i));
        }
        return helper;
    }

}
