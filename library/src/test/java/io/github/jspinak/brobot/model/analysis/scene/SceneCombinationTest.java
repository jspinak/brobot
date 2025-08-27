package io.github.jspinak.brobot.model.analysis.scene;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for SceneCombination.
 * Tests scene combination management, image associations, and dynamic pixel tracking.
 */
@DisplayName("SceneCombination Tests")
public class SceneCombinationTest extends BrobotTestBase {
    
    private SceneCombination sceneCombination;
    private Mat dynamicPixelsMat;
    private StateImage mockStateImage1;
    private StateImage mockStateImage2;
    private StateImage mockStateImage3;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create test Mat for dynamic pixels
        dynamicPixelsMat = new Mat(100, 100, CV_8UC1);
        
        // Create mock StateImages
        mockStateImage1 = mock(StateImage.class);
        when(mockStateImage1.getName()).thenReturn("image1");
        when(mockStateImage1.toString()).thenReturn("StateImage[image1]");
        
        mockStateImage2 = mock(StateImage.class);
        when(mockStateImage2.getName()).thenReturn("image2");
        when(mockStateImage2.toString()).thenReturn("StateImage[image2]");
        
        mockStateImage3 = mock(StateImage.class);
        when(mockStateImage3.getName()).thenReturn("image3");
        when(mockStateImage3.toString()).thenReturn("StateImage[image3]");
        
        // Create SceneCombination
        sceneCombination = new SceneCombination(dynamicPixelsMat, 0, 1);
    }
    
    @AfterEach
    void tearDown() {
        if (dynamicPixelsMat != null && !dynamicPixelsMat.isNull()) {
            dynamicPixelsMat.release();
        }
    }
    
    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create SceneCombination with provided parameters")
        void shouldCreateWithParameters() {
            assertNotNull(sceneCombination);
            assertEquals(dynamicPixelsMat, sceneCombination.getDynamicPixels());
            assertEquals(0, sceneCombination.getScene1());
            assertEquals(1, sceneCombination.getScene2());
            assertNotNull(sceneCombination.getImages());
            assertTrue(sceneCombination.getImages().isEmpty());
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 0",
            "1, 2",
            "5, 10",
            "100, 200"
        })
        @DisplayName("Should handle various scene indices")
        void shouldHandleVariousSceneIndices(int scene1, int scene2) {
            SceneCombination combination = new SceneCombination(dynamicPixelsMat, scene1, scene2);
            
            assertEquals(scene1, combination.getScene1());
            assertEquals(scene2, combination.getScene2());
        }
        
        @Test
        @DisplayName("Should handle null dynamic pixels Mat")
        void shouldHandleNullDynamicPixels() {
            SceneCombination combination = new SceneCombination(null, 2, 3);
            
            assertNull(combination.getDynamicPixels());
            assertEquals(2, combination.getScene1());
            assertEquals(3, combination.getScene2());
        }
    }
    
    @Nested
    @DisplayName("Image Management")
    class ImageManagement {
        
        @Test
        @DisplayName("Should add single image to combination")
        void shouldAddSingleImage() {
            sceneCombination.addImage(mockStateImage1);
            
            assertEquals(1, sceneCombination.getImages().size());
            assertTrue(sceneCombination.getImages().contains(mockStateImage1));
        }
        
        @Test
        @DisplayName("Should add multiple images to combination")
        void shouldAddMultipleImages() {
            sceneCombination.addImage(mockStateImage1);
            sceneCombination.addImage(mockStateImage2);
            sceneCombination.addImage(mockStateImage3);
            
            assertEquals(3, sceneCombination.getImages().size());
            assertTrue(sceneCombination.getImages().contains(mockStateImage1));
            assertTrue(sceneCombination.getImages().contains(mockStateImage2));
            assertTrue(sceneCombination.getImages().contains(mockStateImage3));
        }
        
        @Test
        @DisplayName("Should allow duplicate images")
        void shouldAllowDuplicateImages() {
            sceneCombination.addImage(mockStateImage1);
            sceneCombination.addImage(mockStateImage1);
            sceneCombination.addImage(mockStateImage1);
            
            assertEquals(3, sceneCombination.getImages().size());
        }
        
        @Test
        @DisplayName("Should maintain insertion order")
        void shouldMaintainInsertionOrder() {
            sceneCombination.addImage(mockStateImage3);
            sceneCombination.addImage(mockStateImage1);
            sceneCombination.addImage(mockStateImage2);
            
            assertEquals(mockStateImage3, sceneCombination.getImages().get(0));
            assertEquals(mockStateImage1, sceneCombination.getImages().get(1));
            assertEquals(mockStateImage2, sceneCombination.getImages().get(2));
        }
    }
    
    @Nested
    @DisplayName("Contains Operations")
    class ContainsOperations {
        
        @Test
        @DisplayName("Should correctly identify contained images")
        void shouldIdentifyContainedImages() {
            sceneCombination.addImage(mockStateImage1);
            sceneCombination.addImage(mockStateImage2);
            
            assertTrue(sceneCombination.contains(mockStateImage1));
            assertTrue(sceneCombination.contains(mockStateImage2));
            assertFalse(sceneCombination.contains(mockStateImage3));
        }
        
        @Test
        @DisplayName("Should return false for null image")
        void shouldReturnFalseForNullImage() {
            sceneCombination.addImage(mockStateImage1);
            
            assertFalse(sceneCombination.contains((StateImage) null));
        }
        
        @Test
        @DisplayName("Should correctly identify scene1")
        void shouldIdentifyScene1() {
            assertTrue(sceneCombination.contains(0));
            assertFalse(sceneCombination.contains(2));
        }
        
        @Test
        @DisplayName("Should correctly identify scene2")
        void shouldIdentifyScene2() {
            assertTrue(sceneCombination.contains(1));
            assertFalse(sceneCombination.contains(3));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {-1, 2, 5, 100, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("Should return false for scenes not in combination")
        void shouldReturnFalseForOtherScenes(int scene) {
            assertFalse(sceneCombination.contains(scene));
        }
        
        @Test
        @DisplayName("Should handle same scene in both positions")
        void shouldHandleSameSceneInBothPositions() {
            SceneCombination sameScenesCombo = new SceneCombination(dynamicPixelsMat, 5, 5);
            
            assertTrue(sameScenesCombo.contains(5));
            assertFalse(sameScenesCombo.contains(4));
            assertFalse(sameScenesCombo.contains(6));
        }
    }
    
    @Nested
    @DisplayName("Setters and Getters")
    class SettersAndGetters {
        
        @Test
        @DisplayName("Should update dynamic pixels Mat")
        void shouldUpdateDynamicPixelsMat() {
            Mat newMat = new Mat(50, 50, CV_8UC1);
            try {
                sceneCombination.setDynamicPixels(newMat);
                
                assertEquals(newMat, sceneCombination.getDynamicPixels());
                assertNotEquals(dynamicPixelsMat, sceneCombination.getDynamicPixels());
            } finally {
                newMat.release();
            }
        }
        
        @Test
        @DisplayName("Should update scene indices")
        void shouldUpdateSceneIndices() {
            sceneCombination.setScene1(10);
            sceneCombination.setScene2(20);
            
            assertEquals(10, sceneCombination.getScene1());
            assertEquals(20, sceneCombination.getScene2());
        }
        
        @Test
        @DisplayName("Should replace entire image list")
        void shouldReplaceImageList() {
            sceneCombination.addImage(mockStateImage1);
            
            List<StateImage> newImageList = new ArrayList<>();
            newImageList.add(mockStateImage2);
            newImageList.add(mockStateImage3);
            
            sceneCombination.setImages(newImageList);
            
            assertEquals(2, sceneCombination.getImages().size());
            assertFalse(sceneCombination.contains(mockStateImage1));
            assertTrue(sceneCombination.contains(mockStateImage2));
            assertTrue(sceneCombination.contains(mockStateImage3));
        }
    }
    
    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {
        
        @Test
        @DisplayName("Should generate string with no images")
        void shouldGenerateStringWithNoImages() {
            String result = sceneCombination.toString();
            
            assertNotNull(result);
            assertTrue(result.contains("Scenes: 0 1"));
            assertTrue(result.contains("Images:"));
        }
        
        @Test
        @DisplayName("Should generate string with single image")
        void shouldGenerateStringWithSingleImage() {
            sceneCombination.addImage(mockStateImage1);
            
            String result = sceneCombination.toString();
            
            assertTrue(result.contains("Scenes: 0 1"));
            assertTrue(result.contains("StateImage[image1]"));
        }
        
        @Test
        @DisplayName("Should generate string with multiple images")
        void shouldGenerateStringWithMultipleImages() {
            sceneCombination.addImage(mockStateImage1);
            sceneCombination.addImage(mockStateImage2);
            sceneCombination.addImage(mockStateImage3);
            
            String result = sceneCombination.toString();
            
            assertTrue(result.contains("Scenes: 0 1"));
            assertTrue(result.contains("StateImage[image1]"));
            assertTrue(result.contains("StateImage[image2]"));
            assertTrue(result.contains("StateImage[image3]"));
        }
        
        @Test
        @DisplayName("Should handle large scene indices in string")
        void shouldHandleLargeSceneIndicesInString() {
            SceneCombination largeCombination = new SceneCombination(null, 999, 1000);
            
            String result = largeCombination.toString();
            
            assertTrue(result.contains("Scenes: 999 1000"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle negative scene indices")
        void shouldHandleNegativeSceneIndices() {
            SceneCombination negativeCombination = new SceneCombination(dynamicPixelsMat, -1, -2);
            
            assertEquals(-1, negativeCombination.getScene1());
            assertEquals(-2, negativeCombination.getScene2());
            assertTrue(negativeCombination.contains(-1));
            assertTrue(negativeCombination.contains(-2));
            assertFalse(negativeCombination.contains(0));
        }
        
        @Test
        @DisplayName("Should handle very large image collections")
        void shouldHandleLargeImageCollections() {
            for (int i = 0; i < 1000; i++) {
                StateImage mockImage = mock(StateImage.class);
                when(mockImage.getName()).thenReturn("image" + i);
                sceneCombination.addImage(mockImage);
            }
            
            assertEquals(1000, sceneCombination.getImages().size());
        }
        
        @Test
        @DisplayName("Should handle empty dynamic pixels Mat")
        void shouldHandleEmptyDynamicPixelsMat() {
            Mat emptyMat = new Mat();
            try {
                SceneCombination combination = new SceneCombination(emptyMat, 0, 1);
                
                assertNotNull(combination.getDynamicPixels());
                assertTrue(combination.getDynamicPixels().empty());
            } finally {
                emptyMat.release();
            }
        }
        
        @Test
        @DisplayName("Should maintain JsonIgnore annotation for dynamic pixels")
        void shouldMaintainJsonIgnoreAnnotation() throws Exception {
            // Verify the JsonIgnore annotation is present
            var field = SceneCombination.class.getDeclaredField("dynamicPixels");
            assertNotNull(field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class));
        }
    }
}