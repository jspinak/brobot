package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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

    public void setMatsAndColorProfiles(StateImage stateImage) {
            initProfileMats.setOneColumnMats(stateImage);
            setColorProfile(stateImage);
    }

    /**
     * Sets the average color profile for the StateImage.
     * All images are processed, regardless of whether they are dynamic or not.
     * Non-dynamic images can also be used with color searches and thus need an average color profile.
     * @param stateImage StateImage to be processed.
     */
    public void setColorProfile(StateImage stateImage) {
        ColorCluster colorCluster = setColorCluster.getColorProfile(stateImage.getOneColumnBGRMat());
        stateImage.setColorCluster(colorCluster);
        setProfileMats.setMats(stateImage);
    }

}
