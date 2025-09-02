package io.github.jspinak.brobot.runner.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.StringWriter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ActionConfigSerializer polymorphic serialization.
 * Tests cover:
 * - Type discriminator field addition (@type)
 * - Mapping between 20+ ActionConfig subclasses
 * - Support for full names and shortened aliases
 * - Unknown type handling
 * - Serialization of all ActionConfig implementations
 */
@DisplayName("ActionConfigSerializer - Polymorphic Serialization")
public class ActionConfigSerializerTest extends BrobotTestBase {

    private ActionConfigSerializer serializer;
    private ObjectMapper objectMapper;
    
    @Mock
    private JsonGenerator jsonGenerator;
    
    @Mock
    private SerializerProvider serializerProvider;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        serializer = new ActionConfigSerializer();
        objectMapper = new ObjectMapper();
        
        // Register the serializer
        SimpleModule module = new SimpleModule();
        module.addSerializer(ActionConfig.class, serializer);
        objectMapper.registerModule(module);
    }

    @Nested
    @DisplayName("Type Discriminator Tests")
    class TypeDiscriminatorTests {

        @Test
        @DisplayName("Should add @type field for PatternFindOptions")
        void shouldAddTypeFieldForPatternFindOptions() throws Exception {
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("\"FIND\"") || json.contains("\"PatternFindOptions\""));
        }

        @Test
        @DisplayName("Should add @type field for ClickOptions")
        void shouldAddTypeFieldForClickOptions() throws Exception {
            ClickOptions options = new ClickOptions.Builder().build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("\"CLICK\"") || json.contains("\"ClickOptions\""));
        }

        @Test
        @DisplayName("Should add @type field for TypeOptions")
        void shouldAddTypeFieldForTypeOptions() throws Exception {
            TypeOptions options = new TypeOptions.Builder()
                    .build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("\"TYPE\"") || json.contains("\"TypeOptions\""));
        }
    }

    @Nested
    @DisplayName("All ActionConfig Subclasses Tests")
    class AllActionConfigSubclassesTests {

        @ParameterizedTest
        @MethodSource("actionConfigProvider")
        @DisplayName("Should serialize all ActionConfig implementations")
        void shouldSerializeAllActionConfigImplementations(ActionConfig config, String expectedType) throws Exception {
            String json = objectMapper.writeValueAsString(config);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains(expectedType), 
                "Expected type '" + expectedType + "' not found in JSON: " + json);
        }

        private static Stream<Arguments> actionConfigProvider() {
            return Stream.of(
                Arguments.of(new PatternFindOptions.Builder().build(), "FIND"),
                Arguments.of(new ClickOptions.Builder().build(), "CLICK"),
                Arguments.of(new TypeOptions.Builder().build(), "TYPE"),
                Arguments.of(new DragOptions.Builder().build(), "DRAG"),
                Arguments.of(new ScrollOptions.Builder().build(), "SCROLL"),
                Arguments.of(new MouseMoveOptions.Builder().build(), "MOVE")
            );
        }
    }

    @Nested
    @DisplayName("Field Serialization Tests")
    class FieldSerializationTests {

        @Test
        @DisplayName("Should serialize PatternFindOptions fields")
        void shouldSerializePatternFindOptionsFields() throws Exception {
            PatternFindOptions options = new PatternFindOptions.Builder()
                    .setDoOnEach(PatternFindOptions.DoOnEach.FIRST)
                    .setStrategy(PatternFindOptions.Strategy.FIRST)
                    .setSimilarity(0.95)
                    .build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"doOnEach\":\"FIRST\""));
            assertTrue(json.contains("\"strategy\":\"FIRST\""));
            assertTrue(json.contains("0.95"));
        }

        @Test
        @DisplayName("Should serialize ClickOptions fields")
        void shouldSerializeClickOptionsFields() throws Exception {
            ClickOptions options = new ClickOptions.Builder()
                    .setNumberOfClicks(2)
                    .build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("true") || json.contains("false"));
            assertTrue(json.contains("2"));
        }

        @Test
        @DisplayName("Should serialize TypeOptions fields")
        void shouldSerializeTypeOptionsFields() throws Exception {
            TypeOptions options = new TypeOptions.Builder()
                    .setTypeDelay(0.5)
                    .setModifiers("CTRL")
                    .build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"Hello World\""));
            assertTrue(json.contains("true"));
        }
    }

    @Nested
    @DisplayName("Null and Empty Handling Tests")
    class NullAndEmptyHandlingTests {

        @Test
        @DisplayName("Should handle null ActionConfig")
        void shouldHandleNullActionConfig() throws Exception {
            ActionConfig nullConfig = null;
            
            String json = objectMapper.writeValueAsString(nullConfig);
            
            assertEquals("null", json);
        }

        @Test
        @DisplayName("Should serialize empty PatternFindOptions")
        void shouldSerializeEmptyPatternFindOptions() throws Exception {
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            // Should still have the type even if other fields are default/null
        }

        @Test
        @DisplayName("Should handle ActionConfig with null fields")
        void shouldHandleActionConfigWithNullFields() throws Exception {
            TypeOptions options = new TypeOptions.Builder()
                    .setTypeDelay(0.0) // Default delay
                    .build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            // Null text should be handled gracefully
        }
    }

    @Nested
    @DisplayName("Custom ActionConfig Implementation Tests")
    class CustomActionConfigTests {

        @Test
        @DisplayName("Should handle custom ActionConfig implementation")
        void shouldHandleCustomActionConfigImplementation() throws Exception {
            ActionConfig customConfig = new CustomActionConfig.Builder()
                    .setCustomField("testValue")
                    .build();
            
            String json = objectMapper.writeValueAsString(customConfig);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            // Should handle unknown implementation gracefully
        }

        // Custom implementation for testing
        private static class CustomActionConfig extends ActionConfig {
            private final String customField;
            
            private CustomActionConfig(Builder builder) {
                super(builder);
                this.customField = builder.customField;
            }
            
            public String getCustomField() {
                return customField;
            }
            
            public static class Builder extends ActionConfig.Builder<Builder> {
                private String customField = "customValue";
                
                public Builder setCustomField(String customField) {
                    this.customField = customField;
                    return this;
                }
                
                @Override
                protected Builder self() {
                    return this;
                }
                
                public CustomActionConfig build() {
                    return new CustomActionConfig(this);
                }
            }
        }
    }

    @Nested
    @DisplayName("Nested Object Serialization Tests")
    class NestedObjectSerializationTests {

        @Test
        @DisplayName("Should serialize nested ActionConfig objects")
        void shouldSerializeNestedActionConfigObjects() throws Exception {
            // Create a complex object containing ActionConfig
            ComplexObject complex = new ComplexObject();
            complex.setOptions(new PatternFindOptions.Builder()
                    .setSimilarity(0.85)
                    .build());
            complex.setName("test");
            
            String json = objectMapper.writeValueAsString(complex);
            
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("\"test\""));
            assertTrue(json.contains("0.85"));
        }

        // Helper class for testing
        private static class ComplexObject {
            private ActionConfig options;
            private String name;
            
            public ActionConfig getOptions() { return options; }
            public void setOptions(ActionConfig options) { this.options = options; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
        }
    }

    @Nested
    @DisplayName("Serialization Performance Tests")
    class SerializationPerformanceTests {

        @Test
        @DisplayName("Should serialize large number of ActionConfigs efficiently")
        void shouldSerializeLargeNumberOfActionConfigsEfficiently() throws Exception {
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 1000; i++) {
                PatternFindOptions options = new PatternFindOptions.Builder()
                        .setSimilarity(0.5 + (i * 0.0001))
                        .build();
                
                String json = objectMapper.writeValueAsString(options);
                assertNotNull(json);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Should complete in reasonable time (less than 5 seconds)
            assertTrue(duration < 5000, "Serialization took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle concurrent serialization")
        void shouldHandleConcurrentSerialization() throws Exception {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            final boolean[] success = new boolean[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        ActionConfig config = new PatternFindOptions.Builder()
                                .setSimilarity(0.9)
                                .build();
                        
                        String json = objectMapper.writeValueAsString(config);
                        success[index] = json != null && json.contains("@type");
                    } catch (Exception e) {
                        success[index] = false;
                    }
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            for (boolean s : success) {
                assertTrue(s);
            }
        }
    }

    @Test
    @DisplayName("Should maintain field order in serialization")
    void shouldMaintainFieldOrderInSerialization() throws Exception {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.75)
                .build();
        
        String json = objectMapper.writeValueAsString(options);
        
        // @type should come first
        int typeIndex = json.indexOf("@type");
        int doOnEachIndex = json.indexOf("doOnEach");
        int strategyIndex = json.indexOf("strategy");
        
        assertTrue(typeIndex >= 0);
        assertTrue(typeIndex < doOnEachIndex || doOnEachIndex == -1);
        assertTrue(typeIndex < strategyIndex || strategyIndex == -1);
    }
}