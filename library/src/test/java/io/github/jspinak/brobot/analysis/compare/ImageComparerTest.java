package io.github.jspinak.brobot.analysis.compare;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.match.EmptyMatch;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.jspinak.brobot.model.element.Image;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageComparerTest {
    
    @Mock
    private ExecutionModeController mockOrLive;
    
    @Mock
    private SizeComparator compareSize;
    
    @Mock
    private StateImage mockStateImage1;
    
    @Mock
    private StateImage mockStateImage2;
    
    @Mock
    private Pattern mockPattern1;
    
    @Mock
    private Pattern mockPattern2;
    
    @Mock
    private BufferedImage mockBufferedImage1;
    
    @Mock
    private BufferedImage mockBufferedImage2;
    
    @Mock
    private Image mockImage1;
    
    @Mock
    private Image mockImage2;
    
    @Mock
    private Match mockMatch;
    
    private ImageComparer imageComparer;
    
    @BeforeEach
    void setUp() {
        imageComparer = new ImageComparer(mockOrLive, compareSize);
    }
    
    @Test
    void testCompare_ListOfStateImages_ReturnsHighestScoringMatch() {
        // Setup
        StateImage stateImage3 = mock(StateImage.class);
        List<StateImage> images = Arrays.asList(mockStateImage1, stateImage3);
        
        Match match1 = mock(Match.class);
        Match match2 = mock(Match.class);
        when(match1.getScore()).thenReturn(0.7);
        when(match2.getScore()).thenReturn(0.9);
        
        // Mock Pattern setup
        when(mockStateImage1.getPatterns()).thenReturn(Collections.singletonList(mockPattern1));
        when(mockStateImage2.getPatterns()).thenReturn(Collections.singletonList(mockPattern2));
        when(stateImage3.getPatterns()).thenReturn(Collections.singletonList(mockPattern1));
        
        when(mockPattern1.getBImage()).thenReturn(mockBufferedImage1);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        when(mockPattern1.getImage()).thenReturn(mockImage1);
        when(mockPattern2.getImage()).thenReturn(mockImage2);
        
        when(compareSize.getEnvelopedFirstOrNone(any(), any()))
            .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
        
        when(mockOrLive.findAll(any(), any()))
            .thenReturn(Collections.singletonList(match1))
            .thenReturn(Collections.singletonList(match2));
        
        // Execute
        Match result = imageComparer.compare(images, mockStateImage2);
        
        // Verify
        assertEquals(match2, result);
        verify(mockOrLive, times(2)).findAll(any(), any());
    }
    
    @Test
    void testCompare_ListOfStateImages_EmptyList_ReturnsEmptyMatch() {
        // Setup
        List<StateImage> emptyList = Collections.emptyList();
        
        // Execute
        Match result = imageComparer.compare(emptyList, mockStateImage2);
        
        // Verify
        assertTrue(result instanceof EmptyMatch);
        assertEquals(0.0, result.getScore());
    }
    
    @Test
    void testCompare_TwoStateImages_MultiplePatterns() {
        // Setup
        Pattern pattern3 = mock(Pattern.class);
        Pattern pattern4 = mock(Pattern.class);
        
        when(mockStateImage1.getPatterns()).thenReturn(Arrays.asList(mockPattern1, pattern3));
        when(mockStateImage2.getPatterns()).thenReturn(Arrays.asList(mockPattern2, pattern4));
        
        when(mockPattern1.getBImage()).thenReturn(mockBufferedImage1);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        when(pattern3.getBImage()).thenReturn(mockBufferedImage1);
        when(pattern4.getBImage()).thenReturn(mockBufferedImage2);
        when(mockPattern1.getImage()).thenReturn(mockImage1);
        when(mockPattern2.getImage()).thenReturn(mockImage2);
        when(pattern3.getImage()).thenReturn(mockImage1);
        when(pattern4.getImage()).thenReturn(mockImage2);
        
        when(compareSize.getEnvelopedFirstOrNone(any(), any()))
            .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
        
        Match bestMatch = mock(Match.class);
        when(bestMatch.getScore()).thenReturn(0.95);
        
        when(mockOrLive.findAll(any(), any()))
            .thenReturn(Collections.singletonList(mockMatch))
            .thenReturn(Collections.singletonList(mockMatch))
            .thenReturn(Collections.singletonList(bestMatch))
            .thenReturn(Collections.singletonList(mockMatch));
        
        when(mockMatch.getScore()).thenReturn(0.8);
        
        // Execute
        Match result = imageComparer.compare(mockStateImage1, mockStateImage2);
        
        // Verify
        assertEquals(bestMatch, result);
        verify(mockOrLive, times(4)).findAll(any(), any()); // 2x2 pattern combinations
    }
    
    @Test
    void testCompare_TwoStateImages_EmptyPatterns() {
        // Setup
        when(mockStateImage1.getPatterns()).thenReturn(Collections.emptyList());
        when(mockStateImage2.getPatterns()).thenReturn(Collections.emptyList());
        
        // Execute
        Match result = imageComparer.compare(mockStateImage1, mockStateImage2);
        
        // Verify
        assertTrue(result instanceof EmptyMatch);
        verify(mockOrLive, never()).findAll(any(), any());
    }
    
    @Test
    void testCompare_TwoPatterns_Success() {
        // Setup
        when(mockPattern1.getBImage()).thenReturn(mockBufferedImage1);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        when(mockPattern1.getName()).thenReturn("pattern1");
        when(mockPattern2.getName()).thenReturn("pattern2");
        when(mockPattern1.getImage()).thenReturn(mockImage1);
        when(mockPattern2.getImage()).thenReturn(mockImage2);
        
        when(compareSize.getEnvelopedFirstOrNone(mockPattern1, mockPattern2))
            .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
        
        when(mockOrLive.findAll(eq(mockPattern1), any(Scene.class)))
            .thenReturn(Collections.singletonList(mockMatch));
        
        when(mockMatch.getScore()).thenReturn(0.85);
        
        // Execute
        Match result = imageComparer.compare(mockPattern1, mockPattern2);
        
        // Verify
        assertEquals(mockMatch, result);
        verify(mockOrLive).findAll(eq(mockPattern1), any(Scene.class));
    }
    
    @Test
    void testCompare_TwoPatterns_NullPattern1() {
        // Execute
        Match result = imageComparer.compare(null, mockPattern2);
        
        // Verify
        assertTrue(result instanceof EmptyMatch);
        verify(mockOrLive, never()).findAll(any(), any());
    }
    
    @Test
    void testCompare_TwoPatterns_NullPattern2() {
        // Execute
        Match result = imageComparer.compare(mockPattern1, null);
        
        // Verify
        assertTrue(result instanceof EmptyMatch);
        verify(mockOrLive, never()).findAll(any(), any());
    }
    
    @Test
    void testCompare_TwoPatterns_NullBufferedImage() {
        // Setup
        when(mockPattern1.getBImage()).thenReturn(null);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        
        // Execute
        Match result = imageComparer.compare(mockPattern1, mockPattern2);
        
        // Verify
        assertTrue(result instanceof EmptyMatch);
        verify(compareSize, never()).getEnvelopedFirstOrNone(any(), any());
    }
    
    @Test
    void testCompare_TwoPatterns_NoEnvelopedPatterns() {
        // Setup
        when(mockPattern1.getBImage()).thenReturn(mockBufferedImage1);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        
        when(compareSize.getEnvelopedFirstOrNone(mockPattern1, mockPattern2))
            .thenReturn(Collections.emptyList());
        
        // Execute
        Match result = imageComparer.compare(mockPattern1, mockPattern2);
        
        // Verify
        assertTrue(result instanceof EmptyMatch);
        verify(mockOrLive, never()).findAll(any(), any());
    }
    
    @Test
    void testCompare_TwoPatterns_NoMatchesFound() {
        // Setup
        when(mockPattern1.getBImage()).thenReturn(mockBufferedImage1);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        when(mockPattern1.getName()).thenReturn("small");
        when(mockPattern2.getName()).thenReturn("large");
        
        // Mock the image objects
        when(mockPattern1.getImage()).thenReturn(mockImage1);
        when(mockPattern2.getImage()).thenReturn(mockImage2);
        
        when(compareSize.getEnvelopedFirstOrNone(mockPattern1, mockPattern2))
            .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
        
        when(mockOrLive.findAll(eq(mockPattern1), any(Scene.class)))
            .thenReturn(Collections.emptyList());
        
        // Execute
        Match result = imageComparer.compare(mockPattern1, mockPattern2);
        
        // Verify - EmptyMatch.Builder returns a Match with score 0
        assertNotNull(result);
        assertEquals(0.0, result.getScore());
        assertEquals("small found in large", result.getName());
        assertNotNull(result.getScene());
    }
    
    @Test
    void testCompare_TwoPatterns_MultipleMatches_ReturnsHighestScore() {
        // Setup
        when(mockPattern1.getBImage()).thenReturn(mockBufferedImage1);
        when(mockPattern2.getBImage()).thenReturn(mockBufferedImage2);
        when(mockPattern1.getName()).thenReturn("pattern1");
        when(mockPattern2.getName()).thenReturn("pattern2");
        when(mockPattern1.getImage()).thenReturn(mockImage1);
        when(mockPattern2.getImage()).thenReturn(mockImage2);
        
        when(compareSize.getEnvelopedFirstOrNone(mockPattern1, mockPattern2))
            .thenReturn(Arrays.asList(mockPattern1, mockPattern2));
        
        Match match1 = mock(Match.class);
        Match match2 = mock(Match.class);
        Match match3 = mock(Match.class);
        when(match1.getScore()).thenReturn(0.7);
        when(match2.getScore()).thenReturn(0.95);
        when(match3.getScore()).thenReturn(0.8);
        
        when(mockOrLive.findAll(eq(mockPattern1), any(Scene.class)))
            .thenReturn(Arrays.asList(match1, match2, match3));
        
        // Execute
        Match result = imageComparer.compare(mockPattern1, mockPattern2);
        
        // Verify
        assertEquals(match2, result);
    }
}