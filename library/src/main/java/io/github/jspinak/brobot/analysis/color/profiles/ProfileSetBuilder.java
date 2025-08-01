package io.github.jspinak.brobot.analysis.color.profiles;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.analysis.color.ColorClusterFactory;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.stereotype.Component;

/**
 * Orchestrates the complete color profile initialization process for state images.
 * 
 * <p>This class serves as a high-level coordinator that manages the entire workflow of
 * color profile generation for {@link StateImage} objects in the Brobot framework. It
 * combines the functionality of multiple specialized components to create comprehensive
 * color profiles that are used for color-based image matching and state recognition.</p>
 * 
 * <p>The color profiling process involves:
 * <ol>
 *   <li>Initializing one-column color matrices from the state image</li>
 *   <li>Generating color clusters using statistical analysis</li>
 *   <li>Creating visualization matrices for the color profiles</li>
 * </ol>
 * </p>
 * 
 * <p>Color profiles are essential for:
 * <ul>
 *   <li>Dynamic image matching based on color similarity</li>
 *   <li>State identification using color signatures</li>
 *   <li>Color-based filtering in find operations</li>
 *   <li>Visualization of image color characteristics</li>
 * </ul>
 * </p>
 * 
 * @see StateImage
 * @see ColorClusterFactory
 * @see ProfileMatrixBuilder
 * @see ProfileMatrixInitializer
 * @see ColorCluster
 */
@Component
public class ProfileSetBuilder {

    private final ColorClusterFactory setColorCluster;
    private final ProfileMatrixBuilder setProfileMats;
    private final ProfileMatrixInitializer initProfileMats;

    /**
     * Constructs a ProfileSetBuilder instance with required dependencies.
     * 
     * @param setColorCluster Component for generating color clusters from images
     * @param setProfileMats Component for creating visualization matrices
     * @param initProfileMats Component for initializing one-column color matrices
     */
    public ProfileSetBuilder(ColorClusterFactory setColorCluster, ProfileMatrixBuilder setProfileMats,
                          ProfileMatrixInitializer initProfileMats) {
        this.setColorCluster = setColorCluster;
        this.setProfileMats = setProfileMats;
        this.initProfileMats = initProfileMats;
    }

    /**
     * Performs complete color profile initialization for a state image.
     * 
     * <p>This method executes the full color profiling workflow:
     * <ol>
     *   <li>Initializes one-column BGR and HSV matrices from the state image</li>
     *   <li>Generates and sets the color cluster profile</li>
     *   <li>Creates visualization matrices for the color profile</li>
     * </ol>
     * </p>
     * 
     * <p>After this method completes, the state image will have:
     * <ul>
     *   <li>One-column color matrices for efficient color analysis</li>
     *   <li>A complete {@link ColorCluster} with statistical color information</li>
     *   <li>Visualization matrices for UI display or debugging</li>
     * </ul>
     * </p>
     * 
     * @param stateImage The state image to process. This object is modified with
     *                   color profile data including one-column matrices and color clusters.
     */
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
