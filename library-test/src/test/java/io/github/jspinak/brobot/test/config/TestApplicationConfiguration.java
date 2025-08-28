package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.core.services.SikuliScreenCapture;
import io.github.jspinak.brobot.model.element.Region;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import java.awt.image.BufferedImage;

import static org.mockito.Mockito.*;

/**
 * Test configuration that excludes problematic beans from component scanning
 * and provides mock replacements for headless testing.
 */
@TestConfiguration
@ComponentScan(
    basePackages = "io.github.jspinak.brobot",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE, 
            classes = {SikuliScreenCapture.class}
        )
    }
)
public class TestApplicationConfiguration {
    
    /**
     * Mock bean for SikuliScreenCapture to prevent real initialization.
     */
    @Bean(name = "sikuliScreenCapture")
    @Primary
    public SikuliScreenCapture mockSikuliScreenCapture() {
        SikuliScreenCapture mock = mock(SikuliScreenCapture.class);
        
        // Mock capture methods
        BufferedImage dummyImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        
        when(mock.captureScreen()).thenReturn(dummyImage);
        when(mock.captureRegion(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(dummyImage);
        when(mock.captureRegion(any(Region.class))).thenReturn(dummyImage);
        when(mock.captureActiveScreen()).thenReturn(dummyImage);
        when(mock.captureMonitor(anyInt())).thenReturn(dummyImage);
        
        // Mock monitor information
        when(mock.getMonitorCount()).thenReturn(1);
        when(mock.getMonitorBounds(anyInt())).thenReturn(new Region(0, 0, 1920, 1080));
        when(mock.getVirtualDesktopBounds()).thenReturn(new Region(0, 0, 1920, 1080));
        
        // Mock availability
        when(mock.isAvailable()).thenReturn(true);
        when(mock.getImplementationName()).thenReturn("MockSikuli");
        
        return mock;
    }
    
    /**
     * Mock bean for ScreenCaptureService interface.
     */
    @Bean(name = "screenCaptureService")  
    public ScreenCaptureService mockScreenCaptureService() {
        ScreenCaptureService mockService = mock(ScreenCaptureService.class);
        
        // Mock capture methods to return dummy images
        BufferedImage dummyImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        
        when(mockService.captureScreen()).thenReturn(dummyImage);
        when(mockService.captureRegion(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(dummyImage);
        when(mockService.captureRegion(any(Region.class))).thenReturn(dummyImage);
        when(mockService.captureActiveScreen()).thenReturn(dummyImage);
        when(mockService.captureMonitor(anyInt())).thenReturn(dummyImage);
        
        // Mock monitor information
        when(mockService.getMonitorCount()).thenReturn(1);
        when(mockService.getMonitorBounds(anyInt())).thenReturn(new Region(0, 0, 1920, 1080));
        when(mockService.getVirtualDesktopBounds()).thenReturn(new Region(0, 0, 1920, 1080));
        
        // Mock availability
        when(mockService.isAvailable()).thenReturn(true);
        when(mockService.getImplementationName()).thenReturn("Mock");
        
        return mockService;
    }
}