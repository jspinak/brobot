package io.github.jspinak.brobot.action.composite.multiple.finds;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("NestedFinds Tests")
public class NestedFindsTest extends BrobotTestBase {
    
    private NestedFinds nestedFinds;
    
    @Mock
    private ActionChainExecutor mockChainExecutor;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private StateImage mockWindowImage;
    
    @Mock
    private StateImage mockDialogImage;
    
    @Mock
    private StateImage mockButtonImage;
    
    @Mock
    private Region mockRegion;
    
    @Mock
    private Match mockMatch;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        nestedFinds = new NestedFinds(mockChainExecutor);
        
        // Setup default mock behaviors
        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockWindowImage.getName()).thenReturn("Window");
        when(mockDialogImage.getName()).thenReturn("Dialog");
        when(mockButtonImage.getName()).thenReturn("Button");
        when(mockMatch.getRegion()).thenReturn(mockRegion);
    }
    
    @Nested
    @DisplayName("Basic Nested Find Operations")
    class BasicNestedFindOperations {
        
        @Test
        @DisplayName("Should perform simple nested find")
        public void testSimpleNestedFind() {
            ActionResult result = new ActionResult();
            ObjectCollection windowCollection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            ObjectCollection dialogCollection = new ObjectCollection.Builder()
                .withImages(mockDialogImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, windowCollection, dialogCollection);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle three-level nested find")
        public void testThreeLevelNestedFind() {
            ActionResult result = new ActionResult();
            ObjectCollection windowCollection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            ObjectCollection dialogCollection = new ObjectCollection.Builder()
                .withImages(mockDialogImage)
                .build();
            ObjectCollection buttonCollection = new ObjectCollection.Builder()
                .withImages(mockButtonImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().setSimilarity(0.9).build())
                .addFindStep(new PatternFindOptions.Builder().setSimilarity(0.95).build())
                .addFindStep(new PatternFindOptions.Builder().setSimilarity(0.98).build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, windowCollection, dialogCollection, buttonCollection);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle empty object collections")
        public void testEmptyObjectCollections() {
            ActionResult result = new ActionResult();
            
            nestedFinds.perform(result);
            
            assertFalse(result.isSuccess());
            verify(mockChainExecutor, never()).executeChain(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle single collection")
        public void testSingleCollection() {
            ActionResult result = new ActionResult();
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collection);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Configuration Options")
    class ConfigurationOptions {
        
        @Test
        @DisplayName("Should use default options when not provided")
        public void testDefaultOptions() {
            ActionResult result = new ActionResult();
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            
            // No options set - should create default
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collection);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle mismatched steps and collections")
        public void testMismatchedStepsAndCollections() {
            ActionResult result = new ActionResult();
            
            // 2 steps but 3 collections
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            ObjectCollection collection1 = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            ObjectCollection collection2 = new ObjectCollection.Builder()
                .withImages(mockDialogImage)
                .build();
            ObjectCollection collection3 = new ObjectCollection.Builder()
                .withImages(mockButtonImage)
                .build();
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collection1, collection2, collection3);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.7, 0.9, 0.95, 0.99})
        @DisplayName("Should handle various similarity thresholds")
        public void testVariousSimilarityThresholds(double similarity) {
            ActionResult result = new ActionResult();
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder()
                    .setSimilarity(similarity)
                    .build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collection);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Action Type")
    class ActionType {
        
        @Test
        @DisplayName("Should return FIND action type")
        public void testActionType() {
            assertEquals(NestedFinds.Type.FIND, nestedFinds.getActionType());
        }
    }
    
    @Nested
    @DisplayName("Chaining Behavior")
    class ChainingBehavior {
        
        @Test
        @DisplayName("Should build nested chain with correct strategy")
        public void testNestedChainStrategy() {
            ActionResult result = new ActionResult();
            ObjectCollection collection1 = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            ObjectCollection collection2 = new ObjectCollection.Builder()
                .withImages(mockDialogImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            ArgumentCaptor<ActionChainOptions> chainOptionsCaptor = 
                ArgumentCaptor.forClass(ActionChainOptions.class);
            
            when(mockChainExecutor.executeChain(chainOptionsCaptor.capture(), any(), any()))
                .thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collection1, collection2);
            
            ActionChainOptions capturedOptions = chainOptionsCaptor.getValue();
            assertNotNull(capturedOptions);
            assertEquals(ActionChainOptions.ChainingStrategy.NESTED, capturedOptions.getStrategy());
        }
        
        @Test
        @DisplayName("Should pass collections to chain executor")
        public void testCollectionsPassedToExecutor() {
            ActionResult result = new ActionResult();
            ObjectCollection collection1 = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            ObjectCollection collection2 = new ObjectCollection.Builder()
                .withImages(mockDialogImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            ArgumentCaptor<ObjectCollection[]> collectionsCaptor = 
                ArgumentCaptor.forClass(ObjectCollection[].class);
            
            when(mockChainExecutor.executeChain(any(), any(), collectionsCaptor.capture()))
                .thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collection1, collection2);
            
            ObjectCollection[] capturedCollections = collectionsCaptor.getValue();
            assertEquals(2, capturedCollections.length);
            assertEquals(collection1, capturedCollections[0]);
            assertEquals(collection2, capturedCollections[1]);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null result")
        public void testNullResult() {
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            
            assertDoesNotThrow(() -> nestedFinds.perform(null, collection));
        }
        
        @Test
        @DisplayName("Should handle null collections array")
        public void testNullCollectionsArray() {
            ActionResult result = new ActionResult();
            
            assertDoesNotThrow(() -> nestedFinds.perform(result, (ObjectCollection[]) null));
        }
        
        @Test
        @DisplayName("Should handle null individual collections")
        public void testNullIndividualCollections() {
            ActionResult result = new ActionResult();
            ObjectCollection validCollection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, validCollection, null);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle executor failure")
        public void testExecutorFailure() {
            ActionResult result = new ActionResult();
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any()))
                .thenThrow(new RuntimeException("Executor failure"));
            
            assertDoesNotThrow(() -> nestedFinds.perform(result, collection));
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle deep nesting (5 levels)")
        public void testDeepNesting() {
            ActionResult result = new ActionResult();
            List<ObjectCollection> collections = new ArrayList<>();
            NestedFindsOptions.Builder optionsBuilder = new NestedFindsOptions.Builder();
            
            for (int i = 0; i < 5; i++) {
                collections.add(new ObjectCollection.Builder()
                    .withImages(mockWindowImage)
                    .build());
                optionsBuilder.addFindStep(new PatternFindOptions.Builder()
                    .setSimilarity(0.9 + (i * 0.01))
                    .build());
            }
            
            result.setActionConfig(optionsBuilder.build());
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, collections.toArray(new ObjectCollection[0]));
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle mixed object types in collections")
        public void testMixedObjectTypes() {
            ActionResult result = new ActionResult();
            
            ObjectCollection mixedCollection1 = new ObjectCollection.Builder()
                .withImages(mockWindowImage)
                .withRegions(mockRegion)
                .build();
            
            ObjectCollection mixedCollection2 = new ObjectCollection.Builder()
                .withImages(mockDialogImage)
                .withRegions(mockRegion)
                .build();
            
            NestedFindsOptions options = new NestedFindsOptions.Builder()
                .addFindStep(new PatternFindOptions.Builder().build())
                .addFindStep(new PatternFindOptions.Builder().build())
                .build();
            
            result.setActionConfig(options);
            
            when(mockChainExecutor.executeChain(any(), any(), any())).thenReturn(mockActionResult);
            
            nestedFinds.perform(result, mixedCollection1, mixedCollection2);
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any(), any());
        }
    }
}