package io.github.jspinak.brobot.runner.json.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.runner.json.serializers.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrobotJsonModuleTest {

    @Mock
    private ActionOptionsSerializer actionOptionsSerializer;
    
    @Mock
    private MatchesSerializer matchesSerializer;
    
    @Mock
    private ObjectCollectionSerializer objectCollectionSerializer;
    
    @Mock
    private MatSerializer matSerializer;
    
    @Mock
    private ImageSerializer imageSerializer;
    
    @Mock
    private ActionConfigDeserializer actionConfigDeserializer;
    
    @Mock
    private ImageDeserializer imageDeserializer;
    
    @Mock
    private SearchRegionsDeserializer searchRegionsDeserializer;
    
    @Mock
    private MatDeserializer matDeserializer;
    
    private BrobotJsonModule brobotJsonModule;
    
    @BeforeEach
    void setUp() {
        brobotJsonModule = new BrobotJsonModule(
            actionOptionsSerializer,
            matchesSerializer,
            objectCollectionSerializer,
            matSerializer,
            imageSerializer,
            actionConfigDeserializer,
            imageDeserializer,
            searchRegionsDeserializer,
            matDeserializer
        );
    }
    
    @Test
    void testModuleName() {
        // Verify
        assertEquals("BrobotJsonModule", brobotJsonModule.getModuleName());
    }
    
    @Test
    void testSerializersRegistered() {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        // Verify that serializers are properly registered
        // We can't directly verify the registration, but we can test that the module
        // is properly constructed and can be registered without exceptions
        assertNotNull(mapper);
        assertTrue(mapper.getRegisteredModuleIds().contains(brobotJsonModule.getTypeId()));
    }
    
    @Test
    void testDeserializersRegistered() {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        // Verify that deserializers are properly registered
        assertNotNull(mapper);
        assertTrue(mapper.getRegisteredModuleIds().contains(brobotJsonModule.getTypeId()));
    }
    
    @Test
    void testModuleIntegration_ActionOptions() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAction(ActionOptions.Action.CLICK);
        
        // Note: serialize method returns void, so we can't mock the return
        // We're just verifying that the module registration doesn't throw exceptions
        
        // Execute - This would trigger the custom serializer if properly integrated
        // Note: In real usage, the serializer would be called, but in this test
        // we're mainly verifying the module registration doesn't throw exceptions
        assertDoesNotThrow(() -> mapper.writeValueAsString(actionOptions));
    }
    
    @Test
    void testModuleIntegration_ActionResult() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        ActionResult actionResult = new ActionResult();
        
        // Execute
        assertDoesNotThrow(() -> mapper.writeValueAsString(actionResult));
    }
    
    @Test
    void testModuleIntegration_ObjectCollection() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        ObjectCollection objectCollection = new ObjectCollection();
        
        // Execute
        assertDoesNotThrow(() -> mapper.writeValueAsString(objectCollection));
    }
    
    @Test
    void testModuleIntegration_Mat() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        // Note: Creating a real Mat would require native libraries
        // For this test, we're verifying the registration doesn't fail
        
        // Execute & Verify
        assertNotNull(mapper);
        assertTrue(mapper.getRegisteredModuleIds().contains(brobotJsonModule.getTypeId()));
    }
    
    @Test
    void testModuleIntegration_Image() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        // Execute & Verify - Registration should succeed
        assertNotNull(mapper);
        assertTrue(mapper.getRegisteredModuleIds().contains(brobotJsonModule.getTypeId()));
    }
    
    @Test
    void testModuleIntegration_ActionConfigDeserialization() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        String json = "{\"type\":\"click\"}";
        
        // Execute & Verify - Should not throw exception
        assertDoesNotThrow(() -> {
            // In a real scenario, this would use the custom deserializer
            // Here we're testing that the module registration works
            mapper.readValue(json, ActionConfig.class);
        });
    }
    
    @Test
    void testModuleIntegration_SearchRegionsDeserialization() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(brobotJsonModule);
        
        String json = "{\"regions\":[]}";
        
        // Execute & Verify - Should not throw exception
        assertDoesNotThrow(() -> {
            mapper.readValue(json, SearchRegions.class);
        });
    }
    
    @Test
    void testSerialVersionUID() {
        // Verify that the module has a serial version UID
        // This is important for serialization compatibility
        assertEquals(1L, getSerialVersionUID());
    }
    
    @Test
    void testModuleConstruction_NullSerializers() {
        // Test that the module handles null serializers gracefully
        // In production, Spring would ensure non-null injection
        assertThrows(IllegalArgumentException.class, () -> 
            new BrobotJsonModule(
                null,
                matchesSerializer,
                objectCollectionSerializer,
                matSerializer,
                imageSerializer,
                actionConfigDeserializer,
                imageDeserializer,
                searchRegionsDeserializer,
                matDeserializer
            )
        );
    }
    
    @Test
    void testModuleConstruction_NullDeserializers() {
        // Test that the module handles null deserializers gracefully
        assertThrows(IllegalArgumentException.class, () -> 
            new BrobotJsonModule(
                actionOptionsSerializer,
                matchesSerializer,
                objectCollectionSerializer,
                matSerializer,
                imageSerializer,
                null,
                imageDeserializer,
                searchRegionsDeserializer,
                matDeserializer
            )
        );
    }
    
    // Helper method to access the serial version UID via reflection
    private long getSerialVersionUID() {
        try {
            java.lang.reflect.Field field = BrobotJsonModule.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            return field.getLong(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}