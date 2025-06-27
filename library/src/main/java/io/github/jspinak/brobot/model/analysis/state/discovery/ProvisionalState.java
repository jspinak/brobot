package io.github.jspinak.brobot.model.analysis.state.discovery;

import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Temporary container for building states with associated images and scenes.
 * <p>
 * This class is used during the state creation process to collect images and scenes
 * that will eventually form a complete state. It provides logic to prevent adding
 * nested images (images whose regions are completely contained within other images)
 * to maintain a clean set of non-overlapping visual elements.
 * <p>
 * The "temporary" nature indicates this is an intermediate data structure used
 * during state construction, before the final {@link io.github.jspinak.brobot.model.state.State}
 * objects are created.
 *
 * @see StateImage
 */
@Getter
public class ProvisionalState {

    /** The name identifier for this temporary state. */
    private final String name;
    
    /** Set of scene indices associated with this temporary state. */
    private final Set<Integer> scenes = new HashSet<>();
    
    /** List of images belonging to this temporary state, maintained without nested images. */
    private final List<StateImage> images = new ArrayList<>();

    /**
     * Creates a new temporary state with the specified name.
     *
     * @param name The identifier for this temporary state. Must not be null.
     */
    public ProvisionalState(String name) {
        this.name = name;
    }

    /**
     * Adds an image to this temporary state if it's not nested within existing images.
     * <p>
     * This method checks whether the provided image's region is completely contained
     * within any existing image's region. If it is nested, the image is not added
     * to prevent redundant or overlapping visual elements. This helps maintain a
     * cleaner set of images where each represents a distinct visual area.
     *
     * @param stateImage The {@link StateImage} to add. Must not be null.
     *                  The image will only be added if its region is not contained
     *                  within any existing image's region.
     */
    public void addImage(StateImage stateImage) {
        if (isImageNested(stateImage)) return; // don't add if another image's region contains its region
        images.add(stateImage);
    }

    /**
     * Checks if this temporary state contains a specific scene index.
     *
     * @param scene The scene index to check for.
     * @return true if this temporary state contains the specified scene index,
     *         false otherwise.
     */
    public boolean contains(int scene) {
        return scenes.contains(scene);
    }

    /**
     * Checks if this temporary state has exactly the same set of scenes as provided.
     * <p>
     * This method performs a complete equality check between the internal scene set
     * and the provided set. Both sets must contain exactly the same scene indices
     * for this method to return true.
     *
     * @param scenes The set of scene indices to compare against. Must not be null.
     * @return true if the scene sets are equal (contain exactly the same indices),
     *         false otherwise.
     */
    public boolean hasEqualSceneSets(Set<Integer> scenes) {
        return this.scenes.equals(scenes);
    }

    /**
     * Determines if an image is nested within any existing image in this temporary state.
     * <p>
     * An image is considered nested if its region is completely contained within
     * another image's region. This check is performed by comparing the largest
     * defined fixed region of each image. This method is used internally to
     * prevent adding redundant images that are already visually covered by
     * existing images.
     *
     * @param stateImage The {@link StateImage} to check for nesting. Must not be null.
     * @return true if the image's region is completely contained within any existing
     *         image's region, false otherwise.
     */
    private boolean isImageNested(StateImage stateImage) {
        for (StateImage image : images) {
            if (image.getLargestDefinedFixedRegionOrNewRegion().contains(stateImage.getLargestDefinedFixedRegionOrNewRegion()))
                return true;
        }
        return false;
    }
}
