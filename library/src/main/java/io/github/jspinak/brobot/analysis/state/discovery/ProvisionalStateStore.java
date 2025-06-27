package io.github.jspinak.brobot.analysis.state.discovery;

import io.github.jspinak.brobot.model.analysis.state.discovery.ImageSceneMap;
import io.github.jspinak.brobot.model.analysis.state.discovery.ProvisionalState;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Repository for temporary states created during scene analysis.
 * <p>
 * This component manages a collection of temporary states that are dynamically created
 * based on the scene membership patterns of images. Each state represents a unique
 * combination of scenes, and images are assigned to states based on which scenes
 * they appear in. This helps in understanding the logical grouping of UI elements
 * across different application states.
 * 
 * <p>The repository automatically creates new states as needed when images with
 * previously unseen scene combinations are added.</p>
 * 
 * @see ProvisionalState
 * @see ImageSceneMap
 * @see ProvisionalStateBuilder
 */
@Component
@Getter
public class ProvisionalStateStore {

    private final List<ProvisionalState> states = new ArrayList<>();

    /**
     * Adds an image to the appropriate temporary state based on its scene membership.
     * <p>
     * This method finds or creates a temporary state that matches the provided scene
     * combination and adds the image to it. The image's owner state name is updated
     * to reflect its assignment to the temporary state.
     * 
     * @param stateImage The image to add to the repository. Its owner state name will
     *                   be modified to match the assigned temporary state.
     * @param imageInTheseScenes The set of scene indices where this image appears
     */
    public void addImage(StateImage stateImage, Set<Integer> imageInTheseScenes) {
        ProvisionalState state = getState(imageInTheseScenes);
        stateImage.setOwnerStateName(state.getName());
        state.addImage(stateImage);
    }

    /**
     * Convenience method to add an image using a ScenesPerImage object.
     * <p>
     * This method extracts the image and its scene information from the provided
     * object and delegates to {@link #addImage(StateImage, Set)}.
     * 
     * @param scenesPerImage Container holding both the image and its scene membership
     */
    public void addImage(ImageSceneMap scenesPerImage) {
        addImage(scenesPerImage.getStateImage(), scenesPerImage.getScenes());
    }

    /**
     * Retrieves or creates a temporary state for the given scene combination.
     * <p>
     * If a state already exists with the exact same scene combination, it is returned.
     * Otherwise, a new state is created with a name derived from the scene indices
     * (e.g., "1-2-3-" for scenes 1, 2, and 3) and added to the repository.
     * 
     * @param scenes The set of scene indices that define this state
     * @return The existing or newly created temporary state
     */
    private ProvisionalState getState(Set<Integer> scenes) {
        // if the state exists, return the state
        for (ProvisionalState state : states) {
            if (state.hasEqualSceneSets(scenes)) return state;
        }
        // otherwise, create the state and add it to the repo
        StringBuilder name = new StringBuilder();
        scenes.forEach(scene -> name.append(scene).append("-"));
        ProvisionalState state = new ProvisionalState(name.toString());
        states.add(state);
        return state;
    }

    /**
     * Retrieves all images from all temporary states in the repository.
     * <p>
     * This method aggregates images from all states into a single list, useful
     * for operations that need to process all images regardless of their state
     * assignment.
     * 
     * @return A list containing all StateImages from all temporary states
     */
    public List<StateImage> getAllStateImages() {
        List<StateImage> allImages = new ArrayList<>();
        states.forEach(state -> allImages.addAll(state.getImages()));
        return allImages;
    }


}
