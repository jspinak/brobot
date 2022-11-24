package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

@Component
public class SetAllProfiles {

    private final SetColorCluster setColorCluster;
    private final SetProfileMats setProfileMats;
    private final InitProfileMats initProfileMats;

    public SetAllProfiles(SetColorCluster setColorCluster, SetProfileMats setProfileMats,
                          InitProfileMats initProfileMats) {
        this.setColorCluster = setColorCluster;
        this.setProfileMats = setProfileMats;
        this.initProfileMats = initProfileMats;
    }

    public void setMatsAndColorProfiles(StateImageObject stateImageObject) {
            initProfileMats.setOneColumnMats(stateImageObject);
            setColorProfile(stateImageObject);
    }

    /**
     * Sets the average color profile for the StateImageObject.
     * All images are processed, regardless of whether they are dynamic or not.
     * Non-dynamic images can also be used with color searches and thus need an average color profile.
     * @param stateImageObject StateImageObject to be processed.
     */
    public void setColorProfile(StateImageObject stateImageObject) {
        ColorCluster colorCluster = setColorCluster.getColorProfile(stateImageObject.getDynamicImage().getOneColumnBGRMat());
        stateImageObject.getDynamicImage().setInsideColorCluster(colorCluster);
        setProfileMats.setMats(stateImageObject);
    }

}
