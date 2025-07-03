package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.classify.Classify;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.highlight.Highlight;
import io.github.jspinak.brobot.action.basic.mouse.MouseDown;
import io.github.jspinak.brobot.action.basic.mouse.MouseUp;
import io.github.jspinak.brobot.action.basic.mouse.MoveMouse;
import io.github.jspinak.brobot.action.basic.mouse.ScrollMouseWheel;
import io.github.jspinak.brobot.action.basic.region.DefineRegion;
import io.github.jspinak.brobot.action.basic.type.KeyDown;
import io.github.jspinak.brobot.action.basic.type.KeyUp;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.basic.wait.WaitVanish;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static io.github.jspinak.brobot.action.ActionOptions.Action.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BasicActionRegistryTest {

    @Mock
    private Find find;
    @Mock
    private Click click;
    @Mock
    private MouseDown mouseDown;
    @Mock
    private MouseUp mouseUp;
    @Mock
    private DefineRegion defineRegion;
    @Mock
    private TypeText typeText;
    @Mock
    private MoveMouse moveMouse;
    @Mock
    private WaitVanish waitVanish;
    @Mock
    private Highlight highlight;
    @Mock
    private ScrollMouseWheel scrollMouseWheel;
    @Mock
    private KeyDown keyDown;
    @Mock
    private KeyUp keyUp;
    @Mock
    private Classify classify;
    
    private BasicActionRegistry basicActionRegistry;
    
    @BeforeEach
    void setUp() {
        basicActionRegistry = new BasicActionRegistry(
                find, click, mouseDown, mouseUp, defineRegion, typeText,
                moveMouse, waitVanish, highlight, scrollMouseWheel,
                keyDown, keyUp, classify
        );
    }
    
    @Test
    void testGetAction_Find() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(FIND);
        
        assertTrue(action.isPresent());
        assertSame(find, action.get());
    }
    
    @Test
    void testGetAction_Click() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(CLICK);
        
        assertTrue(action.isPresent());
        assertSame(click, action.get());
    }
    
    @Test
    void testGetAction_MouseDown() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(MOUSE_DOWN);
        
        assertTrue(action.isPresent());
        assertSame(mouseDown, action.get());
    }
    
    @Test
    void testGetAction_MouseUp() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(MOUSE_UP);
        
        assertTrue(action.isPresent());
        assertSame(mouseUp, action.get());
    }
    
    @Test
    void testGetAction_Define() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(DEFINE);
        
        assertTrue(action.isPresent());
        assertSame(defineRegion, action.get());
    }
    
    @Test
    void testGetAction_Type() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(TYPE);
        
        assertTrue(action.isPresent());
        assertSame(typeText, action.get());
    }
    
    @Test
    void testGetAction_Move() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(MOVE);
        
        assertTrue(action.isPresent());
        assertSame(moveMouse, action.get());
    }
    
    @Test
    void testGetAction_Vanish() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(VANISH);
        
        assertTrue(action.isPresent());
        assertSame(waitVanish, action.get());
    }
    
    @Test
    void testGetAction_Highlight() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(HIGHLIGHT);
        
        assertTrue(action.isPresent());
        assertSame(highlight, action.get());
    }
    
    @Test
    void testGetAction_ScrollMouseWheel() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(SCROLL_MOUSE_WHEEL);
        
        assertTrue(action.isPresent());
        assertSame(scrollMouseWheel, action.get());
    }
    
    @Test
    void testGetAction_KeyDown() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(KEY_DOWN);
        
        assertTrue(action.isPresent());
        assertSame(keyDown, action.get());
    }
    
    @Test
    void testGetAction_KeyUp() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(KEY_UP);
        
        assertTrue(action.isPresent());
        assertSame(keyUp, action.get());
    }
    
    @Test
    void testGetAction_Classify() {
        Optional<ActionInterface> action = basicActionRegistry.getAction(CLASSIFY);
        
        assertTrue(action.isPresent());
        assertSame(classify, action.get());
    }
    
    @Test
    void testGetAction_NonExistentAction() {
        // This test relies on there being at least one ActionOptions.Action enum value
        // that is not registered in BasicActionRegistry. If all are registered,
        // this test would need to be modified.
        Optional<ActionInterface> action = basicActionRegistry.getAction(null);
        
        assertFalse(action.isPresent());
    }
}