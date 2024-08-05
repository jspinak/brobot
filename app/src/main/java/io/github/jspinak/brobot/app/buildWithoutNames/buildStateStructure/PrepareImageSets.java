package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.preliminaryStates.ImageSetsAndAssociatedScreens;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImageOps;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Setter
public class PrepareImageSets {

    private final StatelessImageOps statelessImageOps;

    public PrepareImageSets(StatelessImageOps statelessImageOps) {
        this.statelessImageOps = statelessImageOps;
    }

    /**
     * Once all scraping is done, states can be created from the TransitionImageRepo.
     * To do this, we need to figure out the minimum combinations of images. For example, screens
     * a(1,2,3,4,5,6), b(1,2,3,4,6), c(1,2,3), d(4,5,6) would produce states i(1,2,3), ii(4,6), and iii(5).
     * Images can be classified by identifying them in screens: 1(a,b,c), 2(a,b,c), 3(a,b,c), 4(a,b,d), 5(a,d), 6(a,b,d).
     * When set of screens match, the corresponding images belong together in a state.
     * States = i(a,b,c;1,2,3), ii(a,b,d;4,6), iii(a,d; 5).
     */
    public List<ImageSetsAndAssociatedScreens> defineStatesWithImages(List<StatelessImage> statelessImages) {
        List<ImageSetsAndAssociatedScreens> imagesScreens = new ArrayList<>();
        statelessImages.forEach(img -> addToImageSet(imagesScreens, img));
        return imagesScreens;
    }

    private void addToImageSet(List<ImageSetsAndAssociatedScreens> imagesScreens,
                               StatelessImage statelessImage) {
        for (ImageSetsAndAssociatedScreens imgScr : imagesScreens) {
            if (imgScr.ifSameScreensAddImage(statelessImage)) {
                // ImageSetsAndAssociatedScreens are a preliminary representation of states, and the created state's name will be the imgScr's index
                statelessImage.setOwnerState(imagesScreens.indexOf(imgScr));
                return;
            }
        }
        imagesScreens.add(new ImageSetsAndAssociatedScreens(statelessImage));
        statelessImage.setOwnerState(imagesScreens.size()-1); // the state name is the last index added
    }


}
