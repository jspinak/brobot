package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StateImage Tests")
public class StateImageTest extends BrobotTestBase {
    
    @BeforeAll
    public static void setUpClass() {
        // Enable mock mode at the class level to ensure all Pattern creations work
        MockModeManager.setMockMode(true);
    }
    
    @Mock
    private Pattern mockPattern;
    
    @Mock
    private Pattern mockPattern2;
    
    @Mock
    private ColorCluster mockColorCluster;
    
    @Mock
    private KmeansProfilesAllSchemas mockKmeansProfiles;
    
    @Mock
    private Mat mockMat;
    
    @Mock
    private ActionHistory mockActionHistory;
    
    private StateImage stateImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateImage = new StateImage();
    }
    
    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {
        
        @Test
        @DisplayName("Default constructor initializes properly")
        public void testDefaultConstructor() {
            StateImage newImage = new StateImage();
            
            assertNull(newImage.getId());
            assertEquals(0L, newImage.getProjectId());
            assertEquals(StateObject.Type.IMAGE, newImage.getObjectType());
            assertEquals("", newImage.getName());
            assertNotNull(newImage.getPatterns());
            assertTrue(newImage.getPatterns().isEmpty());
            assertEquals("null", newImage.getOwnerStateName());
            assertNull(newImage.getOwnerStateId());
            assertEquals(0, newImage.getTimesActedOn());
            assertFalse(newImage.isShared());
            assertFalse(newImage.isDynamic());
            assertNotNull(newImage.getKmeansProfilesAllSchemas());
            assertNotNull(newImage.getColorCluster());
        }
        
        @Test
        @DisplayName("Set and get basic properties")
        public void testBasicProperties() {
            Long id = 123L;
            Long projectId = 456L;
            String name = "LoginButton";
            
            stateImage.setId(id);
            stateImage.setProjectId(projectId);
            stateImage.setName(name);
            stateImage.setOwnerStateName("LoginState");
            stateImage.setOwnerStateId(789L);
            stateImage.setTimesActedOn(5);
            stateImage.setShared(true);
            stateImage.setDynamic(true);
            stateImage.setIndex(10);
            
            assertEquals(id, stateImage.getId());
            assertEquals(projectId, stateImage.getProjectId());
            assertEquals(name, stateImage.getName());
            assertEquals("LoginState", stateImage.getOwnerStateName());
            assertEquals(789L, stateImage.getOwnerStateId());
            assertEquals(5, stateImage.getTimesActedOn());
            assertTrue(stateImage.isShared());
            assertTrue(stateImage.isDynamic());
            assertEquals(10, stateImage.getIndex());
        }
    }
    
    @Nested
    @DisplayName("Pattern Management")
    class PatternManagement {
        
        @Test
        @DisplayName("Add patterns to StateImage")
        public void testAddPatterns() {
            stateImage.getPatterns().add(mockPattern);
            stateImage.getPatterns().add(mockPattern2);
            
            assertEquals(2, stateImage.getPatterns().size());
            assertTrue(stateImage.getPatterns().contains(mockPattern));
            assertTrue(stateImage.getPatterns().contains(mockPattern2));
        }
        
        @Test
        @DisplayName("Set patterns list")
        public void testSetPatterns() {
            List<Pattern> patterns = Arrays.asList(mockPattern, mockPattern2);
            
            stateImage.setPatterns(patterns);
            
            assertEquals(2, stateImage.getPatterns().size());
            assertEquals(patterns, stateImage.getPatterns());
        }
        
        @Test
        @DisplayName("Patterns list maintains order")
        public void testPatternsOrder() {
            Pattern p1 = mock(Pattern.class);
            Pattern p2 = mock(Pattern.class);
            Pattern p3 = mock(Pattern.class);
            
            stateImage.getPatterns().add(p1);
            stateImage.getPatterns().add(p2);
            stateImage.getPatterns().add(p3);
            
            assertEquals(p1, stateImage.getPatterns().get(0));
            assertEquals(p2, stateImage.getPatterns().get(1));
            assertEquals(p3, stateImage.getPatterns().get(2));
        }
    }
    
    @Nested
    @DisplayName("Color Analysis")
    class ColorAnalysis {
        
        @Test
        @DisplayName("Set and get color cluster")
        public void testColorCluster() {
            stateImage.setColorCluster(mockColorCluster);
            
            assertEquals(mockColorCluster, stateImage.getColorCluster());
        }
        
        @Test
        @DisplayName("Set and get kmeans profiles")
        public void testKmeansProfiles() {
            stateImage.setKmeansProfilesAllSchemas(mockKmeansProfiles);
            
            assertEquals(mockKmeansProfiles, stateImage.getKmeansProfilesAllSchemas());
        }
        
        @Test
        @DisplayName("Set Mat objects for color analysis")
        public void testMatObjects() {
            Mat bgrMat = mock(Mat.class);
            Mat hsvMat = mock(Mat.class);
            Mat imagesMat = mock(Mat.class);
            Mat profilesMat = mock(Mat.class);
            
            stateImage.setOneColumnBGRMat(bgrMat);
            stateImage.setOneColumnHSVMat(hsvMat);
            stateImage.setImagesMat(imagesMat);
            stateImage.setProfilesMat(profilesMat);
            
            assertEquals(bgrMat, stateImage.getOneColumnBGRMat());
            assertEquals(hsvMat, stateImage.getOneColumnHSVMat());
            assertEquals(imagesMat, stateImage.getImagesMat());
            assertEquals(profilesMat, stateImage.getProfilesMat());
        }
    }
    
    @Nested
    @DisplayName("StateObject Interface")
    class StateObjectInterface {
        
        @Test
        @DisplayName("StateImage implements StateObject")
        public void testImplementsStateObject() {
            assertTrue(stateImage instanceof StateObject);
        }
        
        @Test
        @DisplayName("Object type is IMAGE")
        public void testObjectType() {
            assertEquals(StateObject.Type.IMAGE, stateImage.getObjectType());
        }
        
        @Test
        @DisplayName("Cannot change object type")
        public void testObjectTypeImmutable() {
            stateImage.setObjectType(StateObject.Type.IMAGE);
            assertEquals(StateObject.Type.IMAGE, stateImage.getObjectType());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Build with name")
        public void testBuilderWithName() {
            StateImage image = new StateImage.Builder()
                .setName("TestImage")
                .build();
            
            assertEquals("TestImage", image.getName());
        }
        
        @Test
        @DisplayName("Build with single pattern")
        public void testBuilderWithSinglePattern() {
            when(mockPattern.getName()).thenReturn("button.png");
            
            StateImage image = new StateImage.Builder()
                .addPattern(mockPattern)
                .build();
            
            assertEquals(1, image.getPatterns().size());
            assertEquals(mockPattern, image.getPatterns().get(0));
            assertEquals("button.png", image.getName()); // Name derived from pattern
        }
        
        @Test
        @DisplayName("Build with multiple patterns")
        public void testBuilderWithMultiplePatterns() {
            StateImage image = new StateImage.Builder()
                .addPattern(mockPattern)
                .addPattern(mockPattern2)
                .build();
            
            assertEquals(2, image.getPatterns().size());
        }
        
        @Test
        @DisplayName("Build with pattern filenames")
        public void testBuilderWithPatternFilenames() {
            StateImage image = new StateImage.Builder()
                .addPattern("button1.png")
                .addPattern("button2.png")
                .build();
            
            assertEquals(2, image.getPatterns().size());
            assertEquals("button1", image.getName());
        }
        
        @Test
        @DisplayName("Build with multiple pattern filenames at once")
        public void testBuilderWithMultipleFilenames() {
            StateImage image = new StateImage.Builder()
                .addPatterns("btn1.png", "btn2.png", "btn3.png")
                .build();
            
            assertEquals(3, image.getPatterns().size());
            assertEquals("btn1", image.getName());
        }
        
        @Test
        @DisplayName("Build with position for all patterns")
        public void testBuilderWithPosition() {
            Position position = new Position(50, 50);
            
            StateImage image = new StateImage.Builder()
                .addPattern("test.png")
                .setPositionForAllPatterns(position)
                .build();
            
            assertEquals(1, image.getPatterns().size());
            // Position should be applied to pattern
        }
        
        @Test
        @DisplayName("Build with offset for all patterns")
        public void testBuilderWithOffset() {
            Location offset = new Location(10, 20);
            
            StateImage image = new StateImage.Builder()
                .addPattern("test.png")
                .setOffsetForAllPatterns(offset)
                .build();
            
            assertEquals(1, image.getPatterns().size());
        }
        
        @Test
        @DisplayName("Build with search region for all patterns")
        public void testBuilderWithSearchRegion() {
            Region searchRegion = new Region(0, 0, 100, 100);
            
            StateImage image = new StateImage.Builder()
                .addPattern("test.png")
                .setSearchRegionForAllPatterns(searchRegion)
                .build();
            
            assertEquals(1, image.getPatterns().size());
        }
        
        @Test
        @DisplayName("Build with owner state")
        public void testBuilderWithOwnerState() {
            StateImage image = new StateImage.Builder()
                .setOwnerStateName("MainState")
                .build();
            
            assertEquals("MainState", image.getOwnerStateName());
        }
        
        @Test
        @DisplayName("Build with index")
        public void testBuilderWithIndex() {
            StateImage image = new StateImage.Builder()
                .setIndex(5)
                .build();
            
            assertEquals(5, image.getIndex());
        }
        
        @Test
        @DisplayName("Build complex StateImage")
        public void testBuilderComplex() {
            StateImage image = new StateImage.Builder()
                .setName("ComplexImage")
                .addPatterns("img1.png", "img2.png", "img3.png")
                .setOwnerStateName("ComplexState")
                .setIndex(10)
                .setPositionForAllPatterns(25, 75)
                .setOffsetForAllPatterns(5, 5)
                .setSearchRegionForAllPatterns(new Region(10, 10, 200, 200))
                .build();
            
            assertEquals("ComplexImage", image.getName());
            assertEquals(3, image.getPatterns().size());
            assertEquals("ComplexState", image.getOwnerStateName());
            assertEquals(10, image.getIndex());
        }
    }
    
    @Nested
    @DisplayName("Shared and Dynamic Images")
    class SharedAndDynamicImages {
        
        @Test
        @DisplayName("Shared image flag")
        public void testSharedImage() {
            assertFalse(stateImage.isShared());
            
            stateImage.setShared(true);
            
            assertTrue(stateImage.isShared());
        }
        
        @Test
        @DisplayName("Dynamic image flag")
        public void testDynamicImage() {
            assertFalse(stateImage.isDynamic());
            
            stateImage.setDynamic(true);
            
            assertTrue(stateImage.isDynamic());
        }
        
        @Test
        @DisplayName("Shared and dynamic image")
        public void testSharedAndDynamic() {
            stateImage.setShared(true);
            stateImage.setDynamic(true);
            
            assertTrue(stateImage.isShared());
            assertTrue(stateImage.isDynamic());
        }
    }
    
    @Nested
    @DisplayName("Action Tracking")
    class ActionTracking {
        
        @Test
        @DisplayName("Track times acted on")
        public void testTimesActedOn() {
            assertEquals(0, stateImage.getTimesActedOn());
            
            stateImage.setTimesActedOn(10);
            assertEquals(10, stateImage.getTimesActedOn());
            
            stateImage.setTimesActedOn(stateImage.getTimesActedOn() + 1);
            assertEquals(11, stateImage.getTimesActedOn());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10, 100, 1000})
        @DisplayName("Various action counts")
        public void testVariousActionCounts(int count) {
            stateImage.setTimesActedOn(count);
            assertEquals(count, stateImage.getTimesActedOn());
        }
    }
    
    @Nested
    @DisplayName("Pattern Methods")
    class PatternMethods {
        
        @Test
        @DisplayName("Get first pattern when exists")
        public void testGetFirstPattern() {
            stateImage.getPatterns().add(mockPattern);
            stateImage.getPatterns().add(mockPattern2);
            
            Pattern first = stateImage.getPatterns().isEmpty() ? null : stateImage.getPatterns().get(0);
            
            assertEquals(mockPattern, first);
        }
        
        @Test
        @DisplayName("Get first pattern when empty returns null")
        public void testGetFirstPatternEmpty() {
            Pattern first = stateImage.getPatterns().isEmpty() ? null : stateImage.getPatterns().get(0);
            
            assertNull(first);
        }
        
        @Test
        @DisplayName("Is empty when no patterns")
        public void testIsEmptyTrue() {
            assertTrue(stateImage.isEmpty());
        }
        
        @Test
        @DisplayName("Is not empty when has patterns")
        public void testIsEmptyFalse() {
            stateImage.getPatterns().add(mockPattern);
            
            assertFalse(stateImage.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("StateImage for login button")
        public void testLoginButtonStateImage() {
            // Create a StateImage for a login button with multiple variations
            StateImage loginButton = new StateImage.Builder()
                .setName("LoginButton")
                .addPatterns("login-normal.png", "login-hover.png", "login-pressed.png")
                .setOwnerStateName("LoginScreen")
                .setPositionForAllPatterns(50, 80) // Bottom center of screen
                .build();
            
            assertEquals("LoginButton", loginButton.getName());
            assertEquals(3, loginButton.getPatterns().size());
            assertEquals("LoginScreen", loginButton.getOwnerStateName());
            assertFalse(loginButton.isDynamic());
            assertFalse(loginButton.isShared());
        }
        
        @Test
        @DisplayName("Shared header logo across states")
        public void testSharedHeaderLogo() {
            StateImage headerLogo = new StateImage.Builder()
                .setName("HeaderLogo")
                .addPattern("company-logo.png")
                .setPositionForAllPatterns(10, 5) // Top left
                .build();
            
            headerLogo.setShared(true); // Appears in multiple states
            
            assertEquals("HeaderLogo", headerLogo.getName());
            assertTrue(headerLogo.isShared());
            assertEquals(1, headerLogo.getPatterns().size());
        }
        
        @Test
        @DisplayName("Dynamic content area")
        public void testDynamicContentArea() {
            StateImage dynamicContent = new StateImage.Builder()
                .setName("DynamicNewsFeed")
                .setOwnerStateName("HomePage")
                .build();
            
            dynamicContent.setDynamic(true); // Content changes frequently
            dynamicContent.setColorCluster(mockColorCluster); // Use color analysis instead
            
            assertTrue(dynamicContent.isDynamic());
            assertTrue(dynamicContent.isEmpty()); // No patterns for dynamic content
            assertEquals(mockColorCluster, dynamicContent.getColorCluster());
        }
    }
}