package io.github.jspinak.brobot.action.composite.repeat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClickUntilOptionsTest {
    
    private ClickUntilOptions.Builder builder;
    
    @BeforeEach
    void setUp() {
        builder = new ClickUntilOptions.Builder();
    }
    
    @Test
    void testDefaultValues() {
        // Execute
        ClickUntilOptions options = builder.build();
        
        // Verify
        assertEquals(ClickUntilOptions.Condition.OBJECTS_APPEAR, options.getCondition());
    }
    
    @Test
    void testSetCondition_ObjectsAppear() {
        // Execute
        ClickUntilOptions options = builder
            .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
            .build();
        
        // Verify
        assertEquals(ClickUntilOptions.Condition.OBJECTS_APPEAR, options.getCondition());
    }
    
    @Test
    void testSetCondition_ObjectsVanish() {
        // Execute
        ClickUntilOptions options = builder
            .setCondition(ClickUntilOptions.Condition.OBJECTS_VANISH)
            .build();
        
        // Verify
        assertEquals(ClickUntilOptions.Condition.OBJECTS_VANISH, options.getCondition());
    }
    
    @Test
    void testCopyConstructor() {
        // Setup
        ClickUntilOptions original = new ClickUntilOptions.Builder()
            .setCondition(ClickUntilOptions.Condition.OBJECTS_VANISH)
            .build();
        
        // Execute
        ClickUntilOptions copy = new ClickUntilOptions.Builder(original).build();
        
        // Verify
        assertEquals(original.getCondition(), copy.getCondition());
        assertNotSame(original, copy); // Should be different objects
    }
    
    @Test
    void testModifyCopy() {
        // Setup
        ClickUntilOptions original = new ClickUntilOptions.Builder()
            .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
            .build();
        
        // Execute
        ClickUntilOptions modified = new ClickUntilOptions.Builder(original)
            .setCondition(ClickUntilOptions.Condition.OBJECTS_VANISH)
            .build();
        
        // Verify
        assertEquals(ClickUntilOptions.Condition.OBJECTS_APPEAR, original.getCondition());
        assertEquals(ClickUntilOptions.Condition.OBJECTS_VANISH, modified.getCondition());
    }
    
    @Test
    void testInheritanceFromActionConfig() {
        // Verify that ClickUntilOptions extends ActionConfig
        ClickUntilOptions options = builder.build();
        assertTrue(options instanceof io.github.jspinak.brobot.action.ActionConfig);
    }
    
    @Test
    void testBuilderSelfMethod() {
        // Test that the self() method returns the correct type
        ClickUntilOptions.Builder returnedBuilder = builder.setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR);
        assertSame(builder, returnedBuilder);
    }
    
    @Test
    void testImmutability() {
        // Create an options object
        ClickUntilOptions options = builder
            .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
            .build();
        
        // Modify the builder after building
        builder.setCondition(ClickUntilOptions.Condition.OBJECTS_VANISH);
        
        // Verify the options object wasn't affected
        assertEquals(ClickUntilOptions.Condition.OBJECTS_APPEAR, options.getCondition());
    }
    
    @Test
    void testConditionEnum() {
        // Verify all enum values are accessible
        ClickUntilOptions.Condition[] conditions = ClickUntilOptions.Condition.values();
        assertEquals(2, conditions.length);
        
        // Verify specific enum values exist
        assertEquals("OBJECTS_APPEAR", ClickUntilOptions.Condition.OBJECTS_APPEAR.name());
        assertEquals("OBJECTS_VANISH", ClickUntilOptions.Condition.OBJECTS_VANISH.name());
    }
    
    @Test
    void testDeprecatedAnnotation() {
        // Verify the class is marked as deprecated
        assertTrue(ClickUntilOptions.class.isAnnotationPresent(Deprecated.class));
    }
    
    @Test
    void testBuilderChaining() {
        // Test fluent interface
        ClickUntilOptions options = new ClickUntilOptions.Builder()
            .setCondition(ClickUntilOptions.Condition.OBJECTS_VANISH)
            .build();
        
        assertNotNull(options);
        assertEquals(ClickUntilOptions.Condition.OBJECTS_VANISH, options.getCondition());
    }
}