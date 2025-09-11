package io.github.jspinak.brobot.analysis.color.profiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.analysis.color.ColorClusterFactory;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileSetBuilder Tests")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class ProfileSetBuilderTest extends BrobotTestBase {

    @Mock private ColorClusterFactory colorClusterFactory;

    @Mock private ProfileMatrixBuilder profileMatrixBuilder;

    @Mock private ProfileMatrixInitializer profileMatrixInitializer;

    @Mock private StateImage stateImage;

    @Mock private Mat mockMat;

    @Mock private ColorCluster mockColorCluster;

    private ProfileSetBuilder profileSetBuilder;

    @BeforeEach
    public void setUp() {
        super.setupTest();
        profileSetBuilder =
                new ProfileSetBuilder(
                        colorClusterFactory, profileMatrixBuilder, profileMatrixInitializer);
    }

    @Test
    @DisplayName("Constructor properly initializes dependencies")
    public void testConstructor() {
        // Execute
        ProfileSetBuilder builder =
                new ProfileSetBuilder(
                        colorClusterFactory, profileMatrixBuilder, profileMatrixInitializer);

        // Verify
        assertNotNull(builder);
    }

    @Test
    @DisplayName("setMatsAndColorProfiles executes complete workflow")
    public void testSetMatsAndColorProfiles() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(mockColorCluster);

        // Execute
        profileSetBuilder.setMatsAndColorProfiles(stateImage);

        // Verify workflow order
        InOrder inOrder =
                inOrder(
                        profileMatrixInitializer,
                        colorClusterFactory,
                        stateImage,
                        profileMatrixBuilder);

        // Step 1: Initialize one-column matrices
        inOrder.verify(profileMatrixInitializer).setOneColumnMats(stateImage);

        // Step 2: Get one-column BGR mat
        inOrder.verify(stateImage).getOneColumnBGRMat();

        // Step 3: Generate color cluster
        inOrder.verify(colorClusterFactory).getColorProfile(mockMat);

        // Step 4: Set color cluster on state image
        inOrder.verify(stateImage).setColorCluster(mockColorCluster);

        // Step 5: Create visualization matrices
        inOrder.verify(profileMatrixBuilder).setMats(stateImage);
    }

    @Test
    @DisplayName("setColorProfile sets color cluster correctly")
    public void testSetColorProfile() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(mockColorCluster);

        // Execute
        profileSetBuilder.setColorProfile(stateImage);

        // Verify
        verify(stateImage).getOneColumnBGRMat();
        verify(colorClusterFactory).getColorProfile(mockMat);
        verify(stateImage).setColorCluster(mockColorCluster);
        verify(profileMatrixBuilder).setMats(stateImage);
    }

    @Test
    @DisplayName("setColorProfile with null Mat")
    public void testSetColorProfile_NullMat() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(null);
        when(colorClusterFactory.getColorProfile(null)).thenReturn(mockColorCluster);

        // Execute
        profileSetBuilder.setColorProfile(stateImage);

        // Verify - should still process
        verify(colorClusterFactory).getColorProfile(null);
        verify(stateImage).setColorCluster(mockColorCluster);
        verify(profileMatrixBuilder).setMats(stateImage);
    }

    @Test
    @DisplayName("setColorProfile with null ColorCluster result")
    public void testSetColorProfile_NullColorCluster() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(null);

        // Execute
        profileSetBuilder.setColorProfile(stateImage);

        // Verify - should set null cluster
        verify(stateImage).setColorCluster(null);
        verify(profileMatrixBuilder).setMats(stateImage);
    }

    @Test
    @DisplayName("setMatsAndColorProfiles with multiple state images")
    public void testSetMatsAndColorProfiles_MultipleImages() {
        // Setup
        StateImage image1 = mock(StateImage.class);
        StateImage image2 = mock(StateImage.class);
        StateImage image3 = mock(StateImage.class);

        Mat mat1 = mock(Mat.class);
        Mat mat2 = mock(Mat.class);
        Mat mat3 = mock(Mat.class);

        ColorCluster cluster1 = mock(ColorCluster.class);
        ColorCluster cluster2 = mock(ColorCluster.class);
        ColorCluster cluster3 = mock(ColorCluster.class);

        when(image1.getOneColumnBGRMat()).thenReturn(mat1);
        when(image2.getOneColumnBGRMat()).thenReturn(mat2);
        when(image3.getOneColumnBGRMat()).thenReturn(mat3);

        when(colorClusterFactory.getColorProfile(mat1)).thenReturn(cluster1);
        when(colorClusterFactory.getColorProfile(mat2)).thenReturn(cluster2);
        when(colorClusterFactory.getColorProfile(mat3)).thenReturn(cluster3);

        // Execute
        profileSetBuilder.setMatsAndColorProfiles(image1);
        profileSetBuilder.setMatsAndColorProfiles(image2);
        profileSetBuilder.setMatsAndColorProfiles(image3);

        // Verify each image was processed
        verify(image1).setColorCluster(cluster1);
        verify(image2).setColorCluster(cluster2);
        verify(image3).setColorCluster(cluster3);

        verify(profileMatrixBuilder, times(3)).setMats(any(StateImage.class));
    }

    @Test
    @DisplayName("Verify color cluster is set before profile matrices")
    public void testColorClusterSetBeforeProfileMatrices() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(mockColorCluster);

        // Capture the state when setMats is called
        doAnswer(
                        invocation -> {
                            // At this point, setColorCluster should have been called
                            verify(stateImage).setColorCluster(mockColorCluster);
                            return null;
                        })
                .when(profileMatrixBuilder)
                .setMats(stateImage);

        // Execute
        profileSetBuilder.setColorProfile(stateImage);

        // Verify
        verify(profileMatrixBuilder).setMats(stateImage);
    }

    @Test
    @DisplayName("Exception in colorClusterFactory propagates")
    public void testSetColorProfile_ExceptionInColorClusterFactory() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat))
                .thenThrow(new RuntimeException("Color analysis failed"));

        // Execute & Verify
        assertThrows(
                RuntimeException.class,
                () -> {
                    profileSetBuilder.setColorProfile(stateImage);
                });

        // Verify partial execution
        verify(stateImage).getOneColumnBGRMat();
        verify(stateImage, never()).setColorCluster(any());
        verify(profileMatrixBuilder, never()).setMats(any());
    }

    @Test
    @DisplayName("Exception in profileMatrixInitializer propagates")
    public void testSetMatsAndColorProfiles_ExceptionInInitializer() {
        // Setup
        doThrow(new RuntimeException("Initialization failed"))
                .when(profileMatrixInitializer)
                .setOneColumnMats(stateImage);

        // Execute & Verify
        assertThrows(
                RuntimeException.class,
                () -> {
                    profileSetBuilder.setMatsAndColorProfiles(stateImage);
                });

        // Verify no further processing
        verify(stateImage, never()).getOneColumnBGRMat();
        verify(colorClusterFactory, never()).getColorProfile(any());
    }

    @Test
    @DisplayName("Exception in profileMatrixBuilder propagates")
    public void testSetColorProfile_ExceptionInMatrixBuilder() {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(mockColorCluster);
        doThrow(new RuntimeException("Matrix building failed"))
                .when(profileMatrixBuilder)
                .setMats(stateImage);

        // Execute & Verify
        assertThrows(
                RuntimeException.class,
                () -> {
                    profileSetBuilder.setColorProfile(stateImage);
                });

        // Verify partial execution
        verify(stateImage).setColorCluster(mockColorCluster);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("setColorProfile with null state image")
    public void testSetColorProfile_NullStateImage(StateImage nullImage) {
        // Execute & Verify - should throw NPE
        assertThrows(
                NullPointerException.class,
                () -> {
                    profileSetBuilder.setColorProfile(nullImage);
                });
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("setMatsAndColorProfiles with null state image")
    public void testSetMatsAndColorProfiles_NullStateImage(StateImage nullImage) {
        // Execute & Verify - should throw NPE
        assertThrows(
                NullPointerException.class,
                () -> {
                    profileSetBuilder.setMatsAndColorProfiles(nullImage);
                });
    }

    @Test
    @DisplayName("Verify argument passed to colorClusterFactory")
    public void testColorClusterFactory_RecievesCorrectArgument() {
        // Setup
        Mat expectedMat = mock(Mat.class);
        when(stateImage.getOneColumnBGRMat()).thenReturn(expectedMat);
        when(colorClusterFactory.getColorProfile(any())).thenReturn(mockColorCluster);

        ArgumentCaptor<Mat> matCaptor = ArgumentCaptor.forClass(Mat.class);

        // Execute
        profileSetBuilder.setColorProfile(stateImage);

        // Verify
        verify(colorClusterFactory).getColorProfile(matCaptor.capture());
        assertEquals(expectedMat, matCaptor.getValue());
    }

    @Test
    @DisplayName("Verify argument passed to setColorCluster")
    public void testSetColorCluster_RecievesCorrectArgument() {
        // Setup
        ColorCluster expectedCluster = mock(ColorCluster.class);
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(expectedCluster);

        ArgumentCaptor<ColorCluster> clusterCaptor = ArgumentCaptor.forClass(ColorCluster.class);

        // Execute
        profileSetBuilder.setColorProfile(stateImage);

        // Verify
        verify(stateImage).setColorCluster(clusterCaptor.capture());
        assertEquals(expectedCluster, clusterCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 100})
    @DisplayName("Performance test with multiple iterations")
    public void testSetMatsAndColorProfiles_Performance(int iterations) {
        // Setup
        when(stateImage.getOneColumnBGRMat()).thenReturn(mockMat);
        when(colorClusterFactory.getColorProfile(mockMat)).thenReturn(mockColorCluster);

        // Execute
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            profileSetBuilder.setMatsAndColorProfiles(stateImage);
        }
        long endTime = System.currentTimeMillis();

        // Verify
        verify(profileMatrixInitializer, times(iterations)).setOneColumnMats(stateImage);
        verify(colorClusterFactory, times(iterations)).getColorProfile(mockMat);
        verify(stateImage, times(iterations)).setColorCluster(mockColorCluster);
        verify(profileMatrixBuilder, times(iterations)).setMats(stateImage);

        // Performance should be reasonable
        long duration = endTime - startTime;
        assertTrue(
                duration < 1000,
                "Processing " + iterations + " iterations took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Thread safety - concurrent processing")
    public void testConcurrentProcessing() throws InterruptedException {
        // Setup
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        StateImage[] images = new StateImage[threadCount];

        for (int i = 0; i < threadCount; i++) {
            images[i] = mock(StateImage.class);
            Mat mat = mock(Mat.class);
            ColorCluster cluster = mock(ColorCluster.class);

            when(images[i].getOneColumnBGRMat()).thenReturn(mat);
            when(colorClusterFactory.getColorProfile(mat)).thenReturn(cluster);
        }

        // Execute concurrent processing
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] =
                    new Thread(
                            () -> {
                                profileSetBuilder.setMatsAndColorProfiles(images[index]);
                            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify each image was processed
        for (StateImage image : images) {
            verify(profileMatrixInitializer).setOneColumnMats(image);
            verify(image).setColorCluster(any(ColorCluster.class));
            verify(profileMatrixBuilder).setMats(image);
        }
    }
}
