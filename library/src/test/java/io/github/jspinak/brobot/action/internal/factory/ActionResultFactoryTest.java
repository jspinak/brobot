package io.github.jspinak.brobot.action.internal.factory;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionResultFactoryTest {

    @Mock
    private SceneAnalysisCollectionBuilder sceneAnalysisCollectionBuilder;
    
    @Mock
    private TimeProvider timeProvider;
    
    private ActionResultFactory factory;
    private LocalDateTime testTime;
    
    @BeforeEach
    void setUp() {
        factory = new ActionResultFactory(sceneAnalysisCollectionBuilder, timeProvider);
        testTime = LocalDateTime.now();
        when(timeProvider.now()).thenReturn(testTime);
    }
    
    @Test
    void testInit_WithActionOptions_BasicAction() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAction(ActionOptions.Action.CLICK);
        actionOptions.setMaxWait(5.0);
        
        String description = "Test click action";
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = factory.init(actionOptions, description, collection);
        
        // Verify
        assertNotNull(result);
        assertNotNull(result.getActionLifecycle());
        assertEquals(testTime, result.getActionLifecycle().getStartTime());
        assertEquals(description, result.getActionDescription());
        assertNotNull(result.getSceneAnalysisCollection());
        
        // Verify scene analysis not populated for non-COLOR actions
        verify(sceneAnalysisCollectionBuilder, never()).get(any(), anyInt(), anyInt(), any());
    }
    
    @Test
    void testInit_WithActionOptions_ColorFind() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAction(ActionOptions.Action.FIND);
        actionOptions.setFind(ActionOptions.Find.COLOR);
        actionOptions.setMaxWait(10.0);
        
        ObjectCollection collection1 = new ObjectCollection.Builder().build();
        ObjectCollection collection2 = new ObjectCollection.Builder().build();
        
        SceneAnalyses mockAnalyses = new SceneAnalyses();
        when(sceneAnalysisCollectionBuilder.get(anyList(), eq(1), eq(0.0), eq(actionOptions)))
                .thenReturn(mockAnalyses);
        
        // Execute
        ActionResult result = factory.init(actionOptions, "Color find", collection1, collection2);
        
        // Verify
        assertNotNull(result);
        assertEquals(mockAnalyses, result.getSceneAnalysisCollection());
        
        // Verify scene analysis was created for COLOR find
        verify(sceneAnalysisCollectionBuilder).get(
                argThat(list -> list.size() == 2),
                eq(1),
                eq(0.0),
                eq(actionOptions)
        );
    }
    
    @Test
    void testInit_WithActionConfig() {
        // Setup
        ActionConfig actionConfig = new ClickOptions.Builder().build();
        String description = "Test with ActionConfig";
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = factory.init(actionConfig, description, collection);
        
        // Verify
        assertNotNull(result);
        assertNotNull(result.getActionLifecycle());
        assertEquals(testTime, result.getActionLifecycle().getStartTime());
        assertEquals(description, result.getActionDescription());
        assertEquals(actionConfig, result.getActionConfig());
        assertNotNull(result.getSceneAnalysisCollection());
    }
    
    @Test
    void testInit_WithActionOptions_NoDescription() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setMaxWait(3.0);
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = factory.init(actionOptions, collection);
        
        // Verify
        assertNotNull(result);
        assertEquals("", result.getActionDescription());
    }
    
    @Test
    void testInit_WithActionOptions_ListOfCollections() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setMaxWait(7.0);
        
        List<ObjectCollection> collections = Arrays.asList(
                new ObjectCollection.Builder().build(),
                new ObjectCollection.Builder().build(),
                new ObjectCollection.Builder().build()
        );
        
        // Execute
        ActionResult result = factory.init(actionOptions, collections);
        
        // Verify
        assertNotNull(result);
        assertEquals("", result.getActionDescription());
        assertNotNull(result.getActionLifecycle());
    }
    
    @Test
    void testInit_WithActionOptions_MultipleCollections() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setMaxWait(15.0);
        
        ObjectCollection collection1 = new ObjectCollection.Builder().build();
        ObjectCollection collection2 = new ObjectCollection.Builder().build();
        ObjectCollection collection3 = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = factory.init(actionOptions, "Multi-collection action", 
                collection1, collection2, collection3);
        
        // Verify
        assertNotNull(result);
        assertEquals("Multi-collection action", result.getActionDescription());
    }
    
    @Test
    void testInit_WithActionOptions_EmptyCollections() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setMaxWait(2.0);
        
        // Execute - no collections
        ActionResult result = factory.init(actionOptions, "Empty action");
        
        // Verify
        assertNotNull(result);
        assertEquals("Empty action", result.getActionDescription());
        assertNotNull(result.getActionLifecycle());
        assertNotNull(result.getSceneAnalysisCollection());
    }
    
    @Test
    void testInit_ActionLifecycleMaxWait() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        double maxWait = 25.5;
        actionOptions.setMaxWait(maxWait);
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = factory.init(actionOptions, collection);
        
        // Verify - check that max wait is properly set in lifecycle
        assertNotNull(result.getActionLifecycle());
        LocalDateTime expectedEndTime = testTime.plusNanos((long)(maxWait * 1_000_000_000));
        assertEquals(expectedEndTime, result.getActionLifecycle().getAllowedEndTime());
    }
    
    @Test
    void testInit_ColorFind_VerifySceneAnalysisArguments() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAction(ActionOptions.Action.FIND);
        actionOptions.setFind(ActionOptions.Find.COLOR);
        
        ObjectCollection collection1 = new ObjectCollection.Builder().build();
        ObjectCollection collection2 = new ObjectCollection.Builder().build();
        
        SceneAnalyses mockAnalyses = new SceneAnalyses();
        when(sceneAnalysisCollectionBuilder.get(anyList(), anyInt(), anyDouble(), any()))
                .thenReturn(mockAnalyses);
        
        // Execute
        factory.init(actionOptions, collection1, collection2);
        
        // Verify the exact arguments passed to scene analysis builder
        verify(sceneAnalysisCollectionBuilder).get(
                argThat(list -> {
                    return list.size() == 2 &&
                           list.get(0) == collection1 &&
                           list.get(1) == collection2;
                }),
                eq(1),    // rows
                eq(0.0),  // secondsBetweenCaptures
                eq(actionOptions)
        );
    }
    
    @Test
    void testInit_WithActionConfig_DefaultMaxWait() {
        // Setup
        ActionConfig actionConfig = new ClickOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = factory.init(actionConfig, "Test", collection);
        
        // Verify - should use default max wait of 10.0
        assertNotNull(result.getActionLifecycle());
        LocalDateTime expectedEndTime = testTime.plusSeconds(10);
        assertEquals(expectedEndTime, result.getActionLifecycle().getAllowedEndTime());
    }
}