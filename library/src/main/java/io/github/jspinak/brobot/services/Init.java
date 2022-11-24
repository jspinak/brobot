package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

@Component
public class Init {

    private final StateService stateService;
    private final SetAllProfiles setAllProfiles;
    private SetKMeansProfiles setKMeansProfiles;

    private int lastImageIndex = 1; // 0 should correspond to "no class" since matrices are typically initialized with 0s

    public Init(StateService stateService, SetAllProfiles setAllProfiles, SetKMeansProfiles setKMeansProfiles) {
        this.stateService = stateService;
        this.setAllProfiles = setAllProfiles;
        this.setKMeansProfiles = setKMeansProfiles;
    }

    /**
     * This method is called from the client app after all beans have been initialized.
     * @param path Path to the directory containing the images.
     */
    public void setBundlePathAndPreProcessImages(String path) {
        org.sikuli.script.ImagePath.setBundlePath(path);
        stateService.findAllStates().forEach(this::preProcessImages);
    }

    private void preProcessImages(State state) {
        for (StateImageObject stateImageObject : state.getStateImages()) {
            System.out.println("saving index " + lastImageIndex + " to " + stateImageObject.getName());
            stateImageObject.setIndex(lastImageIndex);
            setAllProfiles.setMatsAndColorProfiles(stateImageObject);
            lastImageIndex++;
            if (BrobotSettings.initProfilesForDynamicImages && stateImageObject.isDynamic() ||
                    (BrobotSettings.initProfilesForStaticfImages && !stateImageObject.isDynamic())) {
                setKMeansProfiles.setProfiles(stateImageObject);
            }
        }
    }

    public void add(String path) {
        org.sikuli.script.ImagePath.add(path);
    }
}
