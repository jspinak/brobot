package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.analysis.scene.SceneCombinationGenerator;
import io.github.jspinak.brobot.analysis.scene.SceneCombinationPopulator;
import io.github.jspinak.brobot.analysis.scene.SceneCombinationStore;
import io.github.jspinak.brobot.analysis.state.discovery.ProvisionalStateBuilder;
import io.github.jspinak.brobot.analysis.state.discovery.ProvisionalStateStore;
import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Finds and creates states dynamically based on scene analysis and image combinations.
 * <p>
 * This component implements a sophisticated state discovery mechanism that analyzes
 * multiple scenes to identify persistent UI elements and their relationships. It
 * automatically groups images that appear together across scenes into logical states,
 * enabling dynamic state structure creation without predefined state definitions.
 * 
 * <p>The state finding process involves:
 * <ol>
 *   <li>Analyzing scene combinations to identify which images appear together</li>
 *   <li>Populating scene combinations with images based on their co-occurrence</li>
 *   <li>Creating temporary states based on image groupings</li>
 *   <li>Converting state images to matches for the action result</li>
 * </ol>
 * 
 * <p>This approach is particularly useful for:
 * <ul>
 *   <li>Discovering application states without manual definition</li>
 *   <li>Building state models from screenshot collections</li>
 *   <li>Identifying stable UI element groups across different screens</li>
 *   <li>Creating adaptive automation that learns from observations</li>
 * </ul>
 * 
 * @see SceneCombination
 * @see ProvisionalStateStore
 * @see SceneCombinationGenerator
 * @see ProvisionalStateBuilder
 */
@Component
public class FindState {

    private final SceneCombinationGenerator getSceneCombinations;
    private final SceneCombinationStore sceneCombinations;
    private final ProvisionalStateStore tempStateRepo;
    private final ProvisionalStateBuilder populateTempStateRepo;
    private final SceneCombinationPopulator populateSceneCombinations;

    /**
     * Creates a new FindState instance with required dependencies.
     * 
     * @param getSceneCombinations Service for analyzing and extracting scene combinations
     * @param sceneCombinations Repository for storing and managing scene combinations
     * @param tempStateRepo Repository for temporary states created during analysis
     * @param populateTempStateRepo Service for creating states from scene analysis
     * @param populateSceneCombinations Service for populating combinations with images
     */
    public FindState(SceneCombinationGenerator getSceneCombinations, SceneCombinationStore sceneCombinations,
                      ProvisionalStateStore tempStateRepo, ProvisionalStateBuilder populateTempStateRepo,
                      SceneCombinationPopulator populateSceneCombinations) {
        this.getSceneCombinations = getSceneCombinations;
        this.sceneCombinations = sceneCombinations;
        this.tempStateRepo = tempStateRepo;
        this.populateTempStateRepo = populateTempStateRepo;
        this.populateSceneCombinations = populateSceneCombinations;
    }

    /**
     * Discovers states by analyzing scene combinations and image co-occurrence patterns.
     * <p>
     * This method performs comprehensive state discovery by analyzing how images appear
     * together across multiple scenes. Each ObjectCollection represents a screenshot with
     * its associated state images and potential transitions. The analysis creates temporary
     * states based on image groupings and converts them to matches.
     * 
     * <p>The process flow:
     * <ol>
     *   <li>Extracts all possible scene combinations from the collections</li>
     *   <li>Populates combinations with images that appear in both scenes</li>
     *   <li>Stores the populated combinations for future reference</li>
     *   <li>Creates temporary states based on image groupings</li>
     *   <li>Converts each state image to a match with high confidence (0.99)</li>
     *   <li>Filters matches based on minimum area requirements</li>
     * </ol>
     * 
     * <p><b>Note:</b> The method assumes each state image has at least one pattern and
     * uses the first pattern's image for the match. Matches are created with a fixed
     * similarity score of 0.99, indicating high confidence in state identification.</p>
     * 
     * @param matches The ActionResult to populate with discovered state matches. This object
     *                is modified to include all qualifying matches. Must contain valid
     *                ActionOptions with minimum area settings.
     * @param objectCollections List of ObjectCollections, each representing a screenshot
     *                         with its state images and scene data. Must not be null or empty.
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        List<SceneCombination> sceneCombinationList = getSceneCombinations.getAllSceneCombinations(objectCollections);
        populateSceneCombinations.populateSceneCombinationsWithImages(
                sceneCombinationList, objectCollections, matches.getActionConfig());
        sceneCombinations.addSceneCombinations(sceneCombinationList);
        populateTempStateRepo.createAndAddStatesForSceneToStateRepo(objectCollections);
        tempStateRepo.getAllStateImages().forEach(stateImage -> {
            Match matchFromPattern = new Match.Builder()
                .setRegion(stateImage.getLargestDefinedFixedRegionOrNewRegion())
                .setBufferedImage(stateImage.getPatterns().get(0).getBImage()) // must be an exact match
                .setStateObjectData(stateImage)
                .setSearchImage(stateImage.getPatterns().get(0).getBImage())
                .setName(stateImage.getName())
                .setSimScore(.99)
                .build();
            // Since ActionConfig doesn't have getMinArea(), we'll use a default
            // or check if it's a specific type that has area filtering
            int minSize = 1; // Default minimum area
            if (matches.getActionConfig() instanceof BaseFindOptions) {
                // For now, use default since BaseFindOptions doesn't have minArea
                minSize = 1;
            }
            if (minSize <= matchFromPattern.size()) matches.add(matchFromPattern);
        });
    }

}
