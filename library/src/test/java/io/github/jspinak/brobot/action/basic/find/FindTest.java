package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter;
import io.github.jspinak.brobot.action.internal.find.OffsetLocationManagerV2;
import io.github.jspinak.brobot.action.internal.find.match.MatchAdjusterV2;
import io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder;
import io.github.jspinak.brobot.analysis.match.MatchFusion;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.util.string.TextSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindTest {

    @Mock private FindStrategyRegistryV2 findStrategyRegistry;
    @Mock private StateMemory stateMemory;
    @Mock private NonImageObjectConverter nonImageObjectConverter;
    @Mock private MatchAdjusterV2 matchAdjuster;
    @Mock private ProfileSetBuilder profileSetBuilder;
    @Mock private OffsetLocationManagerV2 offsetLocationManager;
    @Mock private MatchFusion matchFusion;
    @Mock private MatchContentExtractor matchContentExtractor;
    @Mock private TextSelector textSelector;
    @Mock private FindStrategy findStrategy;

    private Find find;
    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;
    private StateImage stateImage;
    private Pattern pattern;

    @BeforeEach
    void setUp() {
        find = new Find(findStrategyRegistry, stateMemory, nonImageObjectConverter,
                matchAdjuster, profileSetBuilder, offsetLocationManager, matchFusion,
                matchContentExtractor, textSelector);

        actionOptions = new ActionOptions.Builder()
                .setFind(ActionOptions.Find.FIRST)
                .setMaxMatchesToActOn(5)
                .setAction(ActionOptions.Action.FIND)
                .build();

        pattern = new Pattern.Builder()
                .setName("testPattern")
                .setFilename("test.png")
                .build();

        stateImage = new StateImage.Builder()
                .setName("testStateImage")
                .addPattern(pattern)
                .build();

        objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
    }

    @Test
    void testPerform_WithValidStateImage_FindsMatches() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        Match match = new Match.Builder()
                .setRegion(new Region(10, 20, 30, 40))
                .setSimScore(0.95)
                .setSearchImage(stateImage)
                .build();

        ActionResult strategyResult = new ActionResult();
        strategyResult.setSuccess(true);
        strategyResult.addMatchObject(match);

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.singletonList(findStrategy));
        when(findStrategy.find(any(), any())).thenReturn(strategyResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(strategyResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(strategyResult);

        // Act
        find.perform(actionResult, objectCollection);

        // Assert
        verify(findStrategy).find(any(), any());
        verify(matchAdjuster).adjust(any(), any());
        verify(offsetLocationManager).offsetActionResult(any(), any());
    }

    @Test
    void testPerform_WithStateRegion_HandlesNonImageObjects() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        Region region = new Region(50, 60, 70, 80);
        StateRegion stateRegion = new StateRegion.Builder()
                .setName("testRegion")
                .withSearchRegion(region)
                .build();

        ObjectCollection regionCollection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .build();

        ActionResult convertedResult = new ActionResult();
        convertedResult.setSuccess(true);
        convertedResult.addMatchObject(new Match.Builder()
                .setRegion(region)
                .build());

        when(nonImageObjectConverter.convert(any())).thenReturn(convertedResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(convertedResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(convertedResult);

        // Act
        find.perform(actionResult, regionCollection);

        // Assert
        verify(nonImageObjectConverter).convert(any());
    }

    @Test
    void testPerform_WithStateLocation_HandlesNonImageObjects() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        Location location = new Location(100, 200);
        StateLocation stateLocation = new StateLocation.Builder()
                .setName("testLocation")
                .setLocation(location)
                .build();

        ObjectCollection locationCollection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();

        ActionResult convertedResult = new ActionResult();
        convertedResult.setSuccess(true);
        convertedResult.addMatchObject(new Match.Builder()
                .setRegion(new Region(location.getX(), location.getY(), 1, 1))
                .build());

        when(nonImageObjectConverter.convert(any())).thenReturn(convertedResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(convertedResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(convertedResult);

        // Act
        find.perform(actionResult, locationCollection);

        // Assert
        verify(nonImageObjectConverter).convert(any());
    }

    @Test
    void testPerform_WithColorStrategy_CreatesColorProfiles() {
        // Arrange
        ActionOptions colorOptions = new ActionOptions.Builder()
                .setFind(ActionOptions.Find.COLOR)
                .setAction(ActionOptions.Action.FIND)
                .build();

        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(colorOptions);

        ActionResult strategyResult = new ActionResult();
        strategyResult.setSuccess(true);

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.singletonList(findStrategy));
        when(findStrategy.find(any(), any())).thenReturn(strategyResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(strategyResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(strategyResult);

        // Act
        find.perform(actionResult, objectCollection);

        // Assert
        verify(profileSetBuilder).setColorProfiles(any(), any());
    }

    @Test
    void testPerform_ExtractsTextFromMatches() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        ActionResult strategyResult = new ActionResult();
        strategyResult.setSuccess(true);
        strategyResult.setText("Found text");

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.singletonList(findStrategy));
        when(findStrategy.find(any(), any())).thenReturn(strategyResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(strategyResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(strategyResult);
        when(matchContentExtractor.extractTextFromAllMatches(any())).thenReturn("Extracted text");
        when(textSelector.selectString(any(), any())).thenReturn("Final text");

        // Act
        find.perform(actionResult, objectCollection);

        // Assert
        verify(matchContentExtractor).extractTextFromAllMatches(any());
        verify(textSelector).selectString(any(), any());
    }

    @Test
    void testPerform_WithMultipleObjectCollections_ProcessesAll() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        ObjectCollection collection1 = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();

        StateImage stateImage2 = new StateImage.Builder()
                .setName("testStateImage2")
                .addPattern(pattern)
                .build();

        ObjectCollection collection2 = new ObjectCollection.Builder()
                .withImages(stateImage2)
                .build();

        ActionResult strategyResult = new ActionResult();
        strategyResult.setSuccess(true);

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.singletonList(findStrategy));
        when(findStrategy.find(any(), any())).thenReturn(strategyResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(strategyResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(strategyResult);

        // Act
        find.perform(actionResult, collection1, collection2);

        // Assert
        verify(findStrategy, times(2)).find(any(), any());
    }

    @Test
    void testPerform_WithFusionEnabled_FusesMatches() {
        // Arrange
        ActionOptions fusionOptions = new ActionOptions.Builder()
                .setFind(ActionOptions.Find.ALL)
                .setDoOnEach(ActionOptions.DoOnEach.FUSION)
                .setAction(ActionOptions.Action.FIND)
                .build();

        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(fusionOptions);

        ActionResult strategyResult = new ActionResult();
        strategyResult.setSuccess(true);
        strategyResult.addMatchObject(new Match.Builder()
                .setRegion(new Region(0, 0, 10, 10))
                .build());

        ActionResult fusedResult = new ActionResult();
        fusedResult.setSuccess(true);

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.singletonList(findStrategy));
        when(findStrategy.find(any(), any())).thenReturn(strategyResult);
        when(matchFusion.fuseMatches(any(), any())).thenReturn(fusedResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(fusedResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(fusedResult);

        // Act
        find.perform(actionResult, objectCollection);

        // Assert
        verify(matchFusion).fuseMatches(any(), any());
    }

    @Test
    void testPerform_CapturesSnapshots() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        Match match = new Match.Builder()
                .setRegion(new Region(10, 20, 30, 40))
                .setSimScore(0.95)
                .build();

        ActionResult strategyResult = new ActionResult();
        strategyResult.setSuccess(true);
        strategyResult.addMatchObject(match);

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.singletonList(findStrategy));
        when(findStrategy.find(any(), any())).thenReturn(strategyResult);
        when(matchAdjuster.adjust(any(), any())).thenReturn(strategyResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(strategyResult);

        // Act
        find.perform(actionResult, objectCollection);

        // Assert
        verify(stateMemory).captureSnapshot(any(), any());
    }

    @Test
    void testPerform_WithNoStrategies_ReturnsEmptyResult() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);

        when(findStrategyRegistry.getStrategies()).thenReturn(Collections.emptyList());

        ActionResult emptyResult = new ActionResult();
        when(matchAdjuster.adjust(any(), any())).thenReturn(emptyResult);
        when(offsetLocationManager.offsetActionResult(any(), any())).thenReturn(emptyResult);

        // Act
        find.perform(actionResult, objectCollection);

        // Assert
        verify(findStrategy, never()).find(any(), any());
    }

    @Test
    void testGetActionType_ReturnsFind() {
        assertEquals(ActionInterface.Type.FIND, find.getActionType());
    }
}