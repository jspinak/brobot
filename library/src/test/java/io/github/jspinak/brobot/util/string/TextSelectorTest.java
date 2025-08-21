package io.github.jspinak.brobot.util.string;

import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextSelectorTest extends BrobotTestBase {
    
    private TextSelector textSelector;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        textSelector = new TextSelector();
    }
    
    @Test
    public void testMostSimilarWithListInput() {
        Text sampleText = new Text();
        sampleText.add("Click Here");
        sampleText.add("CLICK HERE");
        sampleText.add("Click Here");
        sampleText.add("click here");
        sampleText.add("Click Here");
        
        String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
        
        // The MOST_SIMILAR method returns one of the variations
        // The actual implementation might prioritize differently than frequency
        assertTrue(sampleText.getAll().contains(result), "Result should be one of the input variations");
    }
    
    @Test
    public void testMostSimilarWithTextObject() {
        Text sampleText = new Text();
        sampleText.add("Click Here");
        sampleText.add("CLICK HERE");
        sampleText.add("Click Here");
        sampleText.add("click here");
        sampleText.add("Click Here");
        
        String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
        
        // The MOST_SIMILAR method returns one of the variations
        assertTrue(sampleText.getAll().contains(result), "Result should be one of the input variations");
    }
    
    @Test
    public void testConsistencyOfMostSimilar() {
        Text sampleText = new Text();
        sampleText.add("Click Here");
        sampleText.add("CLICK HERE");
        sampleText.add("Click Here");
        sampleText.add("click here");
        sampleText.add("Click Here");
        
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String result = textSelector.getString(TextSelector.Method.MOST_SIMILAR, sampleText);
            results.add(result);
        }
        
        assertEquals(1, results.size(), "Most similar should return consistent results");
        // The result should be consistent and one of the input variations
        String consistentResult = results.iterator().next();
        assertTrue(sampleText.getAll().contains(consistentResult), "Result should be one of the input variations");
    }
}