package io.github.jspinak.brobot.test.mock;

import static org.mockito.Mockito.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for Screen in headless test environment. Provides a mock Screen bean for tests
 * running in mock mode.
 */
@TestConfiguration
public class MockScreenConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "brobot.mock", havingValue = "true", matchIfMissing = true)
    public Screen mockScreen() {
        Screen mockScreen = mock(Screen.class);

        // Set up basic screen properties using reflection since w, h, x, y are public fields
        mockScreen.w = 1920;
        mockScreen.h = 1080;
        mockScreen.x = 0;
        mockScreen.y = 0;

        // Mock the getID() method
        when(mockScreen.getID()).thenReturn(0);

        // Mock getBounds()
        when(mockScreen.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));

        // Mock getRect()
        when(mockScreen.getRect()).thenReturn(new Rectangle(0, 0, 1920, 1080));

        // Mock capture methods to return a dummy image
        BufferedImage dummyImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        ScreenImage mockScreenImage = mock(ScreenImage.class);
        when(mockScreenImage.getImage()).thenReturn(dummyImage);

        when(mockScreen.capture()).thenReturn(mockScreenImage);
        when(mockScreen.capture(any(Region.class))).thenReturn(mockScreenImage);
        when(mockScreen.capture(any(Rectangle.class))).thenReturn(mockScreenImage);
        when(mockScreen.capture(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(mockScreenImage);

        // Mock getCenter()
        when(mockScreen.getCenter()).thenReturn(new Location(960, 540));

        // Mock newRegion()
        Region mockRegion = mock(Region.class);
        when(mockRegion.getScreen()).thenReturn(mockScreen);
        when(mockScreen.newRegion(any(Region.class))).thenReturn(mockRegion);
        when(mockScreen.newRegion(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(mockRegion);

        return mockScreen;
    }
}
