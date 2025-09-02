package io.github.jspinak.brobot.annotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnnotationProcessor - processes @State and @Transition annotations.
 * Verifies automatic state registration and transition setup with full Spring context.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AnnotationProcessor Integration Tests")
public class AnnotationProcessorIT {
    
    @Autowired
    private AnnotationProcessor processor;
    
    @Autowired
    private StateRegistrationService registrationService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Nested
    @DisplayName("State Annotation Processing")
    class StateAnnotationProcessing {
        
        @Test
        @DisplayName("Process class with @State annotation")
        public void testProcessStateAnnotation() {
            // Create a test state class
            @State(name = "TestState")
            class TestStateClass {
                @StateImage
                private String image = "test.png";
                
                @StateRegion
                private Region region = new Region(0, 0, 100, 100);
                
                @StateLocation
                private Location location = new Location(50, 50);
            }
            
            TestStateClass stateClass = new TestStateClass();
            
            // Process the annotations
            processor.processStateAnnotations(stateClass);
            
            // Verify state was registered
            assertTrue(registrationService.getRegisteredStates().stream()
                .anyMatch(state -> "TestState".equals(state.getName())));
        }
        
        @Test
        @DisplayName("Extract state name from annotation")
        public void testExtractStateName() {
            @State(name = "LoginState")
            class NamedState {}
            
            NamedState instance = new NamedState();
            String name = processor.extractStateName(instance.getClass());
            
            assertEquals("LoginState", name);
        }
        
        @Test
        @DisplayName("Use class name when state name not specified")
        public void testDefaultStateName() {
            @State
            class UnnamedState {}
            
            UnnamedState instance = new UnnamedState();
            String name = processor.extractStateName(instance.getClass());
            
            assertEquals("UnnamedState", name);
        }
        
        @Test
        @DisplayName("Process state with images")
        public void testProcessStateWithImages() {
            @State(name = "ImageState", images = {"button.png", "form.png"})
            class ImageState {}
            
            ImageState instance = new ImageState();
            State state = processor.buildStateFromAnnotation(instance);
            
            assertNotNull(state);
            assertEquals("ImageState", state.getName());
            assertEquals(2, state.getStateImages().size());
        }
        
        @Test
        @DisplayName("Process state with properties")
        public void testProcessStateProperties() {
            @State(
                name = "ConfiguredState",
                canHide = true,
                canTransitionToSelf = false,
                description = "Test state"
            )
            class ConfiguredState {}
            
            ConfiguredState instance = new ConfiguredState();
            State state = processor.buildStateFromAnnotation(instance);
            
            assertTrue(state.canHide());
            assertFalse(state.canTransitionToSelf());
            assertEquals("Test state", state.getDescription());
        }
    }
    
    @Nested
    @DisplayName("Transition Annotation Processing")
    class TransitionAnnotationProcessing {
        
        @Test
        @DisplayName("Process @Transition annotation")
        public void testProcessTransitionAnnotation() {
            @State(name = "SourceState")
            class SourceState {
                @Transition(to = "TargetState", trigger = "button.png")
                public void goToTarget() {}
            }
            
            SourceState instance = new SourceState();
            processor.processTransitionAnnotations(instance);
            
            // Verify transition was registered
            Map<String, List<TransitionInfo>> transitions = 
                registrationService.getRegisteredTransitions();
            assertTrue(transitions.containsKey("SourceState"));
            
            List<TransitionInfo> sourceTransitions = transitions.get("SourceState");
            assertTrue(sourceTransitions.stream()
                .anyMatch(t -> "TargetState".equals(t.getTargetState())));
        }
        
        @Test
        @DisplayName("Process multiple transitions")
        public void testMultipleTransitions() {
            @State(name = "MultiTransitionState")
            class MultiTransitionState {
                @Transition(to = "StateA", trigger = "buttonA.png")
                public void goToA() {}
                
                @Transition(to = "StateB", trigger = "buttonB.png")
                public void goToB() {}
                
                @Transition(to = "StateC", trigger = "buttonC.png")
                public void goToC() {}
            }
            
            MultiTransitionState instance = new MultiTransitionState();
            processor.processTransitionAnnotations(instance);
            
            Map<String, List<TransitionInfo>> transitions = 
                registrationService.getRegisteredTransitions();
            List<TransitionInfo> stateTransitions = transitions.get("MultiTransitionState");
            
            assertNotNull(stateTransitions);
            assertEquals(3, stateTransitions.size());
        }
        
        @Test
        @DisplayName("Process conditional transition")
        public void testConditionalTransition() {
            @State(name = "ConditionalState")
            class ConditionalState {
                @Transition(
                    to = "SuccessState",
                    trigger = "submit.png",
                    condition = "isFormValid"
                )
                public void submit() {}
                
                public boolean isFormValid() {
                    return true;
                }
            }
            
            ConditionalState instance = new ConditionalState();
            processor.processTransitionAnnotations(instance);
            
            Map<String, List<TransitionInfo>> transitions = 
                registrationService.getRegisteredTransitions();
            List<TransitionInfo> stateTransitions = transitions.get("ConditionalState");
            
            assertNotNull(stateTransitions);
            TransitionInfo transition = stateTransitions.get(0);
            assertEquals("isFormValid", transition.getCondition());
        }
    }
    
    @Nested
    @DisplayName("Field Annotation Processing")
    class FieldAnnotationProcessing {
        
        @Test
        @DisplayName("Process @StateImage fields")
        public void testProcessStateImageFields() throws Exception {
            @State(name = "ImageFieldState")
            class ImageFieldState {
                @StateImage(name = "logo")
                private String logoImage = "logo.png";
                
                @StateImage
                private String button = "button.png";
            }
            
            ImageFieldState instance = new ImageFieldState();
            State state = processor.buildStateFromAnnotation(instance);
            
            List<StateImage> images = state.getStateImages();
            assertEquals(2, images.size());
            
            assertTrue(images.stream().anyMatch(img -> "logo".equals(img.getName())));
            assertTrue(images.stream().anyMatch(img -> img.getImageNames().contains("button.png")));
        }
        
        @Test
        @DisplayName("Process @StateRegion fields")
        public void testProcessStateRegionFields() throws Exception {
            @State(name = "RegionFieldState")
            class RegionFieldState {
                @StateRegion(name = "searchArea")
                private Region searchRegion = new Region(10, 10, 200, 200);
                
                @StateRegion
                private Region clickArea = new Region(100, 100, 50, 50);
            }
            
            RegionFieldState instance = new RegionFieldState();
            State state = processor.buildStateFromAnnotation(instance);
            
            List<StateRegion> regions = state.getStateRegions();
            assertEquals(2, regions.size());
            
            assertTrue(regions.stream().anyMatch(r -> "searchArea".equals(r.getName())));
        }
        
        @Test
        @DisplayName("Process @StateLocation fields")
        public void testProcessStateLocationFields() throws Exception {
            @State(name = "LocationFieldState")
            class LocationFieldState {
                @StateLocation(name = "clickPoint")
                private Location clickLocation = new Location(100, 100);
                
                @StateLocation
                private Location hoverPoint = new Location(200, 200);
            }
            
            LocationFieldState instance = new LocationFieldState();
            State state = processor.buildStateFromAnnotation(instance);
            
            List<StateLocation> locations = state.getStateLocations();
            assertEquals(2, locations.size());
            
            assertTrue(locations.stream().anyMatch(l -> "clickPoint".equals(l.getName())));
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Process inheritance hierarchy")
        public void testInheritanceProcessing() {
            @State(name = "BaseState")
            class BaseState {
                @StateImage
                protected String baseImage = "base.png";
            }
            
            @State(name = "DerivedState")
            class DerivedState extends BaseState {
                @StateImage
                private String derivedImage = "derived.png";
            }
            
            DerivedState instance = new DerivedState();
            State state = processor.buildStateFromAnnotation(instance);
            
            // Should include both base and derived images
            List<StateImage> images = state.getStateImages();
            assertEquals(2, images.size());
        }
        
        @Test
        @DisplayName("Process state with all annotation types")
        public void testCompleteStateProcessing() {
            @State(
                name = "CompleteState",
                images = {"header.png", "footer.png"},
                canHide = true,
                description = "A complete state with all features"
            )
            class CompleteState {
                @StateImage(name = "logo")
                private String logo = "logo.png";
                
                @StateRegion(name = "content")
                private Region contentArea = new Region(0, 100, 800, 600);
                
                @StateLocation(name = "submitButton")
                private Location submitLocation = new Location(400, 500);
                
                @Transition(to = "NextState", trigger = "next.png")
                public void goNext() {}
                
                @Transition(to = "PreviousState", trigger = "back.png")
                public void goBack() {}
            }
            
            CompleteState instance = new CompleteState();
            
            // Process all annotations
            processor.processStateAnnotations(instance);
            processor.processTransitionAnnotations(instance);
            
            // Verify complete processing
            State state = registrationService.getRegisteredStates().stream()
                .filter(s -> "CompleteState".equals(s.getName()))
                .findFirst()
                .orElse(null);
            
            assertNotNull(state);
            assertEquals("CompleteState", state.getName());
            assertTrue(state.canHide());
            assertEquals("A complete state with all features", state.getDescription());
            
            // Check images (2 from annotation + 1 from field)
            assertTrue(state.getStateImages().size() >= 3);
            
            // Check regions and locations
            assertFalse(state.getStateRegions().isEmpty());
            assertFalse(state.getStateLocations().isEmpty());
            
            // Check transitions
            Map<String, List<TransitionInfo>> transitions = 
                registrationService.getRegisteredTransitions();
            List<TransitionInfo> stateTransitions = transitions.get("CompleteState");
            assertNotNull(stateTransitions);
            assertEquals(2, stateTransitions.size());
        }
    }
}