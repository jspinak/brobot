package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateObjectMetadata class.
 * Tests metadata creation, property management, and reference tracking.
 */
@DisplayName("StateObjectMetadata Tests")
public class StateObjectMetadataTest extends BrobotTestBase {

    private StateObjectMetadata metadata;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        metadata = new StateObjectMetadata();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor initializes with default values")
        void defaultConstructorInitializesWithDefaults() {
            // Given/When
            StateObjectMetadata defaultMetadata = new StateObjectMetadata();

            // Then
            assertEquals("", defaultMetadata.getStateObjectId());
            assertEquals(StateObject.Type.IMAGE, defaultMetadata.getObjectType());
            assertEquals("", defaultMetadata.getStateObjectName());
            assertEquals("", defaultMetadata.getOwnerStateName());
            assertNull(defaultMetadata.getOwnerStateId());
        }

        @Test
        @DisplayName("Constructor from StateObject copies all properties")
        void constructorFromStateObjectCopiesProperties() {
            // Given
            StateObject mockStateObject = mock(StateObject.class);
            when(mockStateObject.getIdAsString()).thenReturn("obj-123");
            when(mockStateObject.getObjectType()).thenReturn(StateObject.Type.REGION);
            when(mockStateObject.getName()).thenReturn("TestRegion");
            when(mockStateObject.getOwnerStateName()).thenReturn("MainState");
            when(mockStateObject.getOwnerStateId()).thenReturn(42L);

            // When
            StateObjectMetadata fromObject = new StateObjectMetadata(mockStateObject);

            // Then
            assertEquals("obj-123", fromObject.getStateObjectId());
            assertEquals(StateObject.Type.REGION, fromObject.getObjectType());
            assertEquals("TestRegion", fromObject.getStateObjectName());
            assertEquals("MainState", fromObject.getOwnerStateName());
            assertEquals(42L, fromObject.getOwnerStateId());
        }

        @Test
        @DisplayName("Constructor handles StateObject with null values")
        void constructorHandlesStateObjectWithNullValues() {
            // Given
            StateObject mockStateObject = mock(StateObject.class);
            when(mockStateObject.getIdAsString()).thenReturn(null);
            when(mockStateObject.getObjectType()).thenReturn(null);
            when(mockStateObject.getName()).thenReturn(null);
            when(mockStateObject.getOwnerStateName()).thenReturn(null);
            when(mockStateObject.getOwnerStateId()).thenReturn(null);

            // When
            StateObjectMetadata fromObject = new StateObjectMetadata(mockStateObject);

            // Then
            assertNull(fromObject.getStateObjectId());
            assertNull(fromObject.getObjectType());
            assertNull(fromObject.getStateObjectName());
            assertNull(fromObject.getOwnerStateName());
            assertNull(fromObject.getOwnerStateId());
        }
    }

    @Nested
    @DisplayName("Property Management")
    class PropertyManagement {

        @Test
        @DisplayName("Set and get state object ID")
        void setAndGetStateObjectId() {
            // Given
            String objectId = "unique-id-456";

            // When
            metadata.setStateObjectId(objectId);

            // Then
            assertEquals(objectId, metadata.getStateObjectId());
        }

        @ParameterizedTest
        @DisplayName("Set and get various object types")
        @EnumSource(StateObject.Type.class)
        void setAndGetObjectType(StateObject.Type type) {
            // When
            metadata.setObjectType(type);

            // Then
            assertEquals(type, metadata.getObjectType());
        }

        @Test
        @DisplayName("Set and get state object name")
        void setAndGetStateObjectName() {
            // Given
            String objectName = "LoginButton";

            // When
            metadata.setStateObjectName(objectName);

            // Then
            assertEquals(objectName, metadata.getStateObjectName());
        }

        @Test
        @DisplayName("Set and get owner state name")
        void setAndGetOwnerStateName() {
            // Given
            String stateName = "LoginPage";

            // When
            metadata.setOwnerStateName(stateName);

            // Then
            assertEquals(stateName, metadata.getOwnerStateName());
        }

        @ParameterizedTest
        @DisplayName("Set and get various owner state IDs")
        @ValueSource(longs = {0L, 1L, 100L, 999999L, Long.MAX_VALUE})
        void setAndGetOwnerStateId(long stateId) {
            // When
            metadata.setOwnerStateId(stateId);

            // Then
            assertEquals(stateId, metadata.getOwnerStateId());
        }

        @Test
        @DisplayName("Handle null owner state ID")
        void handleNullOwnerStateId() {
            // When
            metadata.setOwnerStateId(null);

            // Then
            assertNull(metadata.getOwnerStateId());
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("ToString includes all properties")
        void toStringIncludesAllProperties() {
            // Given
            metadata.setStateObjectId("id-789");
            metadata.setObjectType(StateObject.Type.LOCATION);
            metadata.setStateObjectName("SubmitLocation");
            metadata.setOwnerStateName("FormPage");
            metadata.setOwnerStateId(15L);

            // When
            String result = metadata.toString();

            // Then
            assertTrue(result.contains("SubmitLocation"));
            assertTrue(result.contains("LOCATION"));
            assertTrue(result.contains("FormPage"));
            assertTrue(result.contains("id-789"));
            assertTrue(result.contains("15"));
        }

        @Test
        @DisplayName("ToString handles null values gracefully")
        void toStringHandlesNullValues() {
            // Given
            metadata.setStateObjectId(null);
            metadata.setObjectType(null);
            metadata.setStateObjectName(null);
            metadata.setOwnerStateName(null);
            metadata.setOwnerStateId(null);

            // When
            String result = metadata.toString();

            // Then
            assertNotNull(result);
            assertTrue(result.contains("null"));
        }

        @Test
        @DisplayName("ToString with default values")
        void toStringWithDefaultValues() {
            // Given - default metadata

            // When
            String result = metadata.toString();

            // Then
            assertNotNull(result);
            assertTrue(result.contains("IMAGE"));
            // Empty strings might not be visible but shouldn't cause errors
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @ParameterizedTest
        @DisplayName("Handle various string values for ID")
        @ValueSource(strings = {"", " ", "  ", "\t", "\n", "very-long-id-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
        @NullSource
        void handleVariousStringValuesForId(String id) {
            // When
            metadata.setStateObjectId(id);

            // Then
            assertEquals(id, metadata.getStateObjectId());
        }

        @ParameterizedTest
        @DisplayName("Handle special characters in names")
        @ValueSource(strings = {"name with spaces", "name-with-dashes", "name_with_underscores", 
                               "name.with.dots", "name@with#special$chars", "名前", "😀"})
        void handleSpecialCharactersInNames(String name) {
            // When
            metadata.setStateObjectName(name);
            metadata.setOwnerStateName(name);

            // Then
            assertEquals(name, metadata.getStateObjectName());
            assertEquals(name, metadata.getOwnerStateName());
        }

        @Test
        @DisplayName("Handle negative owner state ID")
        void handleNegativeOwnerStateId() {
            // Given
            Long negativeId = -999L;

            // When
            metadata.setOwnerStateId(negativeId);

            // Then
            assertEquals(negativeId, metadata.getOwnerStateId());
        }
    }

    @Nested
    @DisplayName("Use Cases")
    class UseCases {

        @Test
        @DisplayName("Create metadata for image state object")
        void createMetadataForImageStateObject() {
            // Given
            StateObject mockImage = mock(StateObject.class);
            when(mockImage.getIdAsString()).thenReturn("img-001");
            when(mockImage.getObjectType()).thenReturn(StateObject.Type.IMAGE);
            when(mockImage.getName()).thenReturn("logo.png");
            when(mockImage.getOwnerStateName()).thenReturn("HomePage");
            when(mockImage.getOwnerStateId()).thenReturn(1L);

            // When
            StateObjectMetadata imageMetadata = new StateObjectMetadata(mockImage);

            // Then
            assertEquals("img-001", imageMetadata.getStateObjectId());
            assertEquals(StateObject.Type.IMAGE, imageMetadata.getObjectType());
            assertEquals("logo.png", imageMetadata.getStateObjectName());
            assertEquals("HomePage", imageMetadata.getOwnerStateName());
            assertEquals(1L, imageMetadata.getOwnerStateId());
        }

        @Test
        @DisplayName("Create metadata for region state object")
        void createMetadataForRegionStateObject() {
            // Given
            StateObject mockRegion = mock(StateObject.class);
            when(mockRegion.getIdAsString()).thenReturn("reg-002");
            when(mockRegion.getObjectType()).thenReturn(StateObject.Type.REGION);
            when(mockRegion.getName()).thenReturn("SearchArea");
            when(mockRegion.getOwnerStateName()).thenReturn("SearchPage");
            when(mockRegion.getOwnerStateId()).thenReturn(2L);

            // When
            StateObjectMetadata regionMetadata = new StateObjectMetadata(mockRegion);

            // Then
            assertEquals("reg-002", regionMetadata.getStateObjectId());
            assertEquals(StateObject.Type.REGION, regionMetadata.getObjectType());
            assertEquals("SearchArea", regionMetadata.getStateObjectName());
            assertEquals("SearchPage", regionMetadata.getOwnerStateName());
            assertEquals(2L, regionMetadata.getOwnerStateId());
        }

        @Test
        @DisplayName("Create metadata for location state object")
        void createMetadataForLocationStateObject() {
            // Given
            StateObject mockLocation = mock(StateObject.class);
            when(mockLocation.getIdAsString()).thenReturn("loc-003");
            when(mockLocation.getObjectType()).thenReturn(StateObject.Type.LOCATION);
            when(mockLocation.getName()).thenReturn("ClickPoint");
            when(mockLocation.getOwnerStateName()).thenReturn("FormPage");
            when(mockLocation.getOwnerStateId()).thenReturn(3L);

            // When
            StateObjectMetadata locationMetadata = new StateObjectMetadata(mockLocation);

            // Then
            assertEquals("loc-003", locationMetadata.getStateObjectId());
            assertEquals(StateObject.Type.LOCATION, locationMetadata.getObjectType());
            assertEquals("ClickPoint", locationMetadata.getStateObjectName());
            assertEquals("FormPage", locationMetadata.getOwnerStateName());
            assertEquals(3L, locationMetadata.getOwnerStateId());
        }

        @Test
        @DisplayName("Update metadata properties after creation")
        void updateMetadataPropertiesAfterCreation() {
            // Given
            StateObjectMetadata mutableMetadata = new StateObjectMetadata();

            // When - simulate updates over time
            mutableMetadata.setStateObjectId("initial-id");
            mutableMetadata.setObjectType(StateObject.Type.IMAGE);
            
            // Later update
            mutableMetadata.setStateObjectName("UpdatedName");
            mutableMetadata.setOwnerStateName("NewOwner");
            mutableMetadata.setOwnerStateId(99L);

            // Then
            assertEquals("initial-id", mutableMetadata.getStateObjectId());
            assertEquals(StateObject.Type.IMAGE, mutableMetadata.getObjectType());
            assertEquals("UpdatedName", mutableMetadata.getStateObjectName());
            assertEquals("NewOwner", mutableMetadata.getOwnerStateName());
            assertEquals(99L, mutableMetadata.getOwnerStateId());
        }
    }

    @Nested
    @DisplayName("Data Class Behavior")
    class DataClassBehavior {

        @Test
        @DisplayName("Equals and hashCode work correctly")
        void equalsAndHashCodeWorkCorrectly() {
            // Given
            StateObjectMetadata metadata1 = new StateObjectMetadata();
            metadata1.setStateObjectId("same-id");
            metadata1.setObjectType(StateObject.Type.REGION);
            metadata1.setStateObjectName("SameName");
            metadata1.setOwnerStateName("SameOwner");
            metadata1.setOwnerStateId(100L);

            StateObjectMetadata metadata2 = new StateObjectMetadata();
            metadata2.setStateObjectId("same-id");
            metadata2.setObjectType(StateObject.Type.REGION);
            metadata2.setStateObjectName("SameName");
            metadata2.setOwnerStateName("SameOwner");
            metadata2.setOwnerStateId(100L);

            StateObjectMetadata metadata3 = new StateObjectMetadata();
            metadata3.setStateObjectId("different-id");
            metadata3.setObjectType(StateObject.Type.REGION);
            metadata3.setStateObjectName("SameName");
            metadata3.setOwnerStateName("SameOwner");
            metadata3.setOwnerStateId(100L);

            // Then
            assertEquals(metadata1, metadata2);
            assertEquals(metadata1.hashCode(), metadata2.hashCode());
            assertNotEquals(metadata1, metadata3);
            assertNotEquals(metadata1.hashCode(), metadata3.hashCode());
        }

        @Test
        @DisplayName("Not equal to null")
        void notEqualToNull() {
            // Given
            StateObjectMetadata metadata = new StateObjectMetadata();

            // Then
            assertNotEquals(null, metadata);
        }

        @Test
        @DisplayName("Not equal to different type")
        void notEqualToDifferentType() {
            // Given
            StateObjectMetadata metadata = new StateObjectMetadata();
            String differentType = "not metadata";

            // Then
            assertNotEquals(metadata, differentType);
        }
    }
}