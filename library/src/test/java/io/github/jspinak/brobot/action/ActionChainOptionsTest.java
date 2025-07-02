package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionChainOptions configuration class.
 */
class ActionChainOptionsTest {
    
    @Test
    void builder_shouldCreateChainWithSingleAction() {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(clickOptions).build();
        
        assertEquals(clickOptions, chain.getInitialAction());
        assertTrue(chain.getChainedActions().isEmpty());
        assertEquals(ActionChainOptions.ChainingStrategy.NESTED, chain.getStrategy());
    }
    
    @Test
    void builder_shouldChainMultipleActions() {
        ClickOptions click = new ClickOptions.Builder().build();
        TypeOptions type = new TypeOptions.Builder().build();
        PatternFindOptions find = new PatternFindOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(click)
            .then(type)
            .then(find)
            .build();
        
        assertEquals(click, chain.getInitialAction());
        List<ActionConfig> chainedActions = chain.getChainedActions();
        assertEquals(2, chainedActions.size());
        assertEquals(type, chainedActions.get(0));
        assertEquals(find, chainedActions.get(1));
    }
    
    @Test
    void builder_shouldSetChainingStrategy() {
        ClickOptions click = new ClickOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(click)
            .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
            .build();
        
        assertEquals(ActionChainOptions.ChainingStrategy.CONFIRM, chain.getStrategy());
    }
    
    @Test
    void builder_shouldDefaultToNestedStrategy() {
        ClickOptions click = new ClickOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(click).build();
        
        assertEquals(ActionChainOptions.ChainingStrategy.NESTED, chain.getStrategy());
    }
    
    @Test
    void builder_shouldInheritActionConfigSettings() {
        ClickOptions click = new ClickOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(click)
            .setPauseBeforeBegin(1.0)
            .setPauseAfterEnd(2.0)
            .setIllustrate(ActionChainOptions.Illustrate.YES)
            .build();
        
        assertEquals(1.0, chain.getPauseBeforeBegin());
        assertEquals(2.0, chain.getPauseAfterEnd());
        assertEquals(ActionChainOptions.Illustrate.YES, chain.getIllustrate());
    }
    
    @Test
    void chainingStrategyEnum_shouldHaveCorrectValues() {
        ActionChainOptions.ChainingStrategy[] values = 
            ActionChainOptions.ChainingStrategy.values();
        assertEquals(2, values.length);
        
        assertNotNull(ActionChainOptions.ChainingStrategy.valueOf("NESTED"));
        assertNotNull(ActionChainOptions.ChainingStrategy.valueOf("CONFIRM"));
    }
    
    @Test
    void builder_shouldAllowComplexChains() {
        // Build a complex chain: find -> click -> type -> find (verify)
        PatternFindOptions findButton = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.9)
            .build();
            
        ClickOptions clickButton = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .setPauseAfterEnd(0.5)
            .build();
            
        TypeOptions typeText = new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .build();
            
        PatternFindOptions verifyResult = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .setPauseBeforeBegin(1.0)
            .build();
        
        ActionChainOptions complexChain = new ActionChainOptions.Builder(findButton)
            .then(clickButton)
            .then(typeText)
            .then(verifyResult)
            .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
            .build();
        
        assertEquals(findButton, complexChain.getInitialAction());
        assertEquals(3, complexChain.getChainedActions().size());
        assertTrue(complexChain.getChainedActions().get(0) instanceof ClickOptions);
        assertTrue(complexChain.getChainedActions().get(1) instanceof TypeOptions);
        assertTrue(complexChain.getChainedActions().get(2) instanceof PatternFindOptions);
    }
    
    @Test
    void builder_shouldCreateImmutableChainedActionsList() {
        ClickOptions click = new ClickOptions.Builder().build();
        TypeOptions type = new TypeOptions.Builder().build();
        
        ActionChainOptions chain = new ActionChainOptions.Builder(click)
            .then(type)
            .build();
        
        List<ActionConfig> chainedActions = chain.getChainedActions();
        
        // Try to modify the returned list - should throw exception
        assertThrows(UnsupportedOperationException.class, () -> {
            chainedActions.add(new ClickOptions.Builder().build());
        });
    }
}