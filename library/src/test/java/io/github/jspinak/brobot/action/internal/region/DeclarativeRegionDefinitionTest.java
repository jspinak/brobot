package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the declarative region definition feature introduced in Brobot 1.1.0.
 * Tests SearchRegionOnObject configuration and basic functionality.
 */
@DisplayName("Declarative Region Definition Tests")
class DeclarativeRegionDefinitionTest extends BrobotTestBase {

    private StateImage baseImage;
    private StateImage dependentImage;
    private State testState;

    @BeforeEach
    void setUp() {
        super.setupTest();
        
        // Create a base state image
        baseImage = new StateImage.Builder()
            .setName("BaseImage")
            .addPattern(new Pattern.Builder()
                .setFilename("base.png")
                .build())
            .build();
    }

    @Nested
    @DisplayName("Basic Declarative Region Tests")
    class BasicDeclarativeRegionTests {

        @Test
        @DisplayName("Should create SearchRegionOnObject with basic configuration")
        void testBasicSearchRegionOnObject() {
            // Create SearchRegionOnObject configuration
            SearchRegionOnObject searchRegionConfig = SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("TestState")
                .setTargetObjectName("BaseImage")
                .build();
            
            assertNotNull(searchRegionConfig);
            assertEquals(StateObject.Type.IMAGE, searchRegionConfig.getTargetType());
            assertEquals("TestState", searchRegionConfig.getTargetStateName());
            assertEquals("BaseImage", searchRegionConfig.getTargetObjectName());
            assertNull(searchRegionConfig.getAdjustments());
        }

        @Test
        @DisplayName("Should create SearchRegionOnObject with adjustments")
        void testSearchRegionOnObjectWithAdjustments() {
            // Create adjustments
            MatchAdjustmentOptions adjustments = MatchAdjustmentOptions.builder()
                .setAddX(10)
                .setAddY(-5)
                .setAddW(50)
                .setAddH(20)
                .build();
            
            // Create SearchRegionOnObject with adjustments
            SearchRegionOnObject searchRegionConfig = SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("TestState")
                .setTargetObjectName("BaseImage")
                .setAdjustments(adjustments)
                .build();
            
            assertNotNull(searchRegionConfig.getAdjustments());
            assertEquals(10, searchRegionConfig.getAdjustments().getAddX());
            assertEquals(-5, searchRegionConfig.getAdjustments().getAddY());
            assertEquals(50, searchRegionConfig.getAdjustments().getAddW());
            assertEquals(20, searchRegionConfig.getAdjustments().getAddH());
        }

        @Test
        @DisplayName("Should create SearchRegionOnObject with fixed dimensions")
        void testSearchRegionOnObjectWithFixedDimensions() {
            // Create adjustments with fixed dimensions
            MatchAdjustmentOptions adjustments = MatchAdjustmentOptions.builder()
                .setAddY(100)
                .setAbsoluteW(200)
                .setAbsoluteH(50)
                .build();
            
            // Create SearchRegionOnObject with fixed dimensions
            SearchRegionOnObject searchRegionConfig = SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("TestState")
                .setTargetObjectName("BaseImage")
                .setAdjustments(adjustments)
                .build();
            
            assertNotNull(searchRegionConfig.getAdjustments());
            assertEquals(100, searchRegionConfig.getAdjustments().getAddY());
            assertEquals(200, searchRegionConfig.getAdjustments().getAbsoluteW());
            assertEquals(50, searchRegionConfig.getAdjustments().getAbsoluteH());
        }
    }

    @Nested
    @DisplayName("StateImage Configuration Tests")
    class StateImageConfigurationTests {

        @Test
        @DisplayName("Should attach SearchRegionOnObject to StateImage")
        void testStateImageWithSearchRegionOnObject() {
            // Create dependent image with SearchRegionOnObject
            dependentImage = new StateImage.Builder()
                .setName("DependentImage")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("TestState")
                    .setTargetObjectName("BaseImage")
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("dependent.png")
                    .build())
                .build();
            
            // Verify SearchRegionOnObject is set
            assertNotNull(dependentImage.getSearchRegionOnObject());
            assertEquals("BaseImage", dependentImage.getSearchRegionOnObject().getTargetObjectName());
            assertEquals(StateObject.Type.IMAGE, dependentImage.getSearchRegionOnObject().getTargetType());
        }

        @Test
        @DisplayName("Should configure dependent image with adjustments")
        void testDependentImageWithAdjustments() {
            // Create dependent image
            dependentImage = new StateImage.Builder()
                .setName("DependentImage")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("TestState")
                    .setTargetObjectName("BaseImage")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(10)
                        .setAddY(20)
                        .build())
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("dependent.png")
                    .build())
                .build();
            
            // Verify configuration is correct
            assertNotNull(dependentImage.getSearchRegionOnObject());
            assertNotNull(dependentImage.getSearchRegionOnObject().getAdjustments());
            assertEquals(10, dependentImage.getSearchRegionOnObject().getAdjustments().getAddX());
            assertEquals(20, dependentImage.getSearchRegionOnObject().getAdjustments().getAddY());
        }
    }

    @Nested
    @DisplayName("Cross-State Dependency Tests")
    class CrossStateDependencyTests {

        @Test
        @DisplayName("Should support cross-state dependencies")
        void testCrossStateDependencies() {
            // Create dependent image referencing different state
            StateImage crossStateDependentImage = new StateImage.Builder()
                .setName("CrossStateDependentImage")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("AnotherState")
                    .setTargetObjectName("AnotherImage")
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("cross-dependent.png")
                    .build())
                .build();
            
            // Verify cross-state configuration
            assertNotNull(crossStateDependentImage.getSearchRegionOnObject());
            assertEquals("AnotherState", crossStateDependentImage.getSearchRegionOnObject().getTargetStateName());
            assertEquals("AnotherImage", crossStateDependentImage.getSearchRegionOnObject().getTargetObjectName());
        }
    }

    @Nested
    @DisplayName("Complex Dependency Chain Tests")
    class ComplexDependencyChainTests {

        @Test
        @DisplayName("Should handle complex adjustment chains")
        void testComplexAdjustmentChains() {
            // Create a chain of dependent images
            StateImage firstDependent = new StateImage.Builder()
                .setName("FirstDependent")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("TestState")
                    .setTargetObjectName("BaseImage")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(10)
                        .setAddY(10)
                        .build())
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("first-dependent.png")
                    .build())
                .build();
            
            StateImage secondDependent = new StateImage.Builder()
                .setName("SecondDependent")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("TestState")
                    .setTargetObjectName("FirstDependent")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(20)
                        .setAddY(20)
                        .build())
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("second-dependent.png")
                    .build())
                .build();
            
            // Verify chain is set up correctly
            assertNotNull(firstDependent.getSearchRegionOnObject());
            assertEquals("BaseImage", firstDependent.getSearchRegionOnObject().getTargetObjectName());
            
            assertNotNull(secondDependent.getSearchRegionOnObject());
            assertEquals("FirstDependent", secondDependent.getSearchRegionOnObject().getTargetObjectName());
        }

        @Test
        @DisplayName("Should handle multiple dependents on same target")
        void testMultipleDependentsOnSameTarget() {
            // Create multiple images depending on the same base
            List<StateImage> dependents = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                StateImage dependent = new StateImage.Builder()
                    .setName("Dependent" + i)
                    .setSearchRegionOnObject(SearchRegionOnObject.builder()
                        .setTargetType(StateObject.Type.IMAGE)
                        .setTargetStateName("TestState")
                        .setTargetObjectName("BaseImage")
                        .setAdjustments(MatchAdjustmentOptions.builder()
                            .setAddX(i * 10)
                            .setAddY(i * 10)
                            .build())
                        .build())
                    .addPattern(new Pattern.Builder()
                        .setFilename("dependent" + i + ".png")
                        .build())
                    .build();
                dependents.add(dependent);
            }
            
            // Verify all dependents are configured correctly
            for (int i = 0; i < dependents.size(); i++) {
                StateImage dependent = dependents.get(i);
                assertNotNull(dependent.getSearchRegionOnObject());
                assertEquals("BaseImage", dependent.getSearchRegionOnObject().getTargetObjectName());
                assertEquals(i * 10, dependent.getSearchRegionOnObject().getAdjustments().getAddX());
                assertEquals(i * 10, dependent.getSearchRegionOnObject().getAdjustments().getAddY());
            }
        }
    }

    @Nested
    @DisplayName("State Integration Tests")
    class StateIntegrationTests {

        @Test
        @DisplayName("Should add declarative regions to state")
        void testDeclarativeRegionsInState() {
            // Create dependent image
            dependentImage = new StateImage.Builder()
                .setName("DependentImage")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("TestState")
                    .setTargetObjectName("BaseImage")
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("dependent.png")
                    .build())
                .build();
            
            // Add to state
            testState = new State.Builder("TestState")
                .withImages(baseImage, dependentImage)
                .build();
            
            // Verify state contains both images
            assertNotNull(testState);
            assertEquals("TestState", testState.getName());
            assertEquals(2, testState.getStateImages().size());
            assertTrue(testState.getStateImages().contains(baseImage));
            assertTrue(testState.getStateImages().contains(dependentImage));
        }

        @Test
        @DisplayName("Should support multiple states with dependencies")
        void testMultipleStatesWithDependencies() {
            // Create states with cross-dependencies
            State state1 = new State.Builder("State1")
                .withImages(new StateImage.Builder()
                    .setName("Image1")
                    .addPattern(new Pattern.Builder()
                        .setFilename("image1.png")
                        .build())
                    .build())
                .build();
            
            StateImage dependentInState2 = new StateImage.Builder()
                .setName("DependentImage2")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("State1")
                    .setTargetObjectName("Image1")
                    .build())
                .addPattern(new Pattern.Builder()
                    .setFilename("dependent2.png")
                    .build())
                .build();
            
            State state2 = new State.Builder("State2")
                .withImages(dependentInState2)
                .build();
            
            // Verify cross-state dependency is configured
            StateImage depImage = state2.getStateImages().iterator().next();
            assertNotNull(depImage.getSearchRegionOnObject());
            assertEquals("State1", depImage.getSearchRegionOnObject().getTargetStateName());
            assertEquals("Image1", depImage.getSearchRegionOnObject().getTargetObjectName());
        }
    }
}