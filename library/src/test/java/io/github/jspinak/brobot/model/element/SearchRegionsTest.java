package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the SearchRegions class which manages multiple search areas 
 * for pattern matching in the Brobot framework.
 */
@DisplayName("SearchRegions Model Tests")
public class SearchRegionsTest extends BrobotTestBase {

    private SearchRegions searchRegions;
    private Region testRegion1;
    private Region testRegion2;
    private Region fixedRegion;
    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        searchRegions = new SearchRegions();
        testRegion1 = new Region(0, 0, 100, 100);
        testRegion2 = new Region(100, 100, 200, 200);
        fixedRegion = new Region(500, 500, 100, 100);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create empty SearchRegions")
    void testDefaultConstructor() {
        // When
        SearchRegions regions = new SearchRegions();
        
        // Then
        assertNotNull(regions);
        assertNotNull(regions.getRegions());
        assertTrue(regions.getRegions().isEmpty());
        assertNull(regions.getFixedRegion());
        assertFalse(regions.isFixedRegionSet());
    }

    @Test
    @DisplayName("Should add single search region")
    void testAddSingleSearchRegion() {
        // When
        searchRegions.addSearchRegions(testRegion1);
        
        // Then
        assertFalse(searchRegions.getRegions(false).isEmpty());
        assertTrue(searchRegions.getRegions(false).contains(testRegion1));
    }

    @Test
    @DisplayName("Should add multiple search regions")
    void testAddMultipleSearchRegions() {
        // When
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        
        // Then
        List<Region> regions = searchRegions.getRegions(false);
        assertFalse(regions.isEmpty());
        // Regions might be merged if adjacent
        assertTrue(regions.size() >= 1);
    }

    @Test
    @DisplayName("Should add search regions from list")
    void testAddSearchRegionsFromList() {
        // Given
        List<Region> regionList = List.of(testRegion1, testRegion2);
        
        // When
        searchRegions.addSearchRegions(regionList);
        
        // Then
        assertFalse(searchRegions.getRegions(false).isEmpty());
    }

    @Test
    @DisplayName("Should set and check fixed region")
    void testFixedRegion() {
        // When
        searchRegions.setFixedRegion(fixedRegion);
        
        // Then
        assertTrue(searchRegions.isFixedRegionSet());
        assertEquals(fixedRegion, searchRegions.getFixedRegion());
    }

    @Test
    @DisplayName("Should reset fixed region")
    void testResetFixedRegion() {
        // Given
        searchRegions.setFixedRegion(fixedRegion);
        assertTrue(searchRegions.isFixedRegionSet());
        
        // When
        searchRegions.resetFixedRegion();
        
        // Then
        assertFalse(searchRegions.isFixedRegionSet());
        assertNull(searchRegions.getFixedRegion());
    }

    @Test
    @DisplayName("Should get regions based on fixed flag")
    void testGetRegionsWithFixedFlag() {
        // Given
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        searchRegions.setFixedRegion(fixedRegion);
        
        // When - fixed = true
        List<Region> fixedRegions = searchRegions.getRegions(true);
        
        // Then
        assertEquals(1, fixedRegions.size());
        assertEquals(fixedRegion, fixedRegions.get(0));
        
        // When - fixed = false
        List<Region> normalRegions = searchRegions.getRegions(false);
        
        // Then
        assertFalse(normalRegions.isEmpty());
        assertFalse(normalRegions.contains(fixedRegion));
    }

    @Test
    @DisplayName("Should get one region")
    void testGetOneRegion() {
        // When - No regions
        Region emptyRegion = searchRegions.getOneRegion();
        assertNotNull(emptyRegion);
        
        // When - With regions
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        Region oneRegion = searchRegions.getOneRegion();
        
        // Then
        assertNotNull(oneRegion);
        assertTrue(oneRegion.isDefined());
    }

    @Test
    @DisplayName("Should get fixed if defined or random region")
    void testGetFixedIfDefinedOrRandomRegion() {
        // Given
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        
        // When - fixed = false
        Region randomRegion = searchRegions.getFixedIfDefinedOrRandomRegion(false);
        
        // Then
        assertNotNull(randomRegion);
        
        // When - fixed = true with fixed region set
        searchRegions.setFixedRegion(fixedRegion);
        Region fixedResult = searchRegions.getFixedIfDefinedOrRandomRegion(true);
        
        // Then
        assertEquals(fixedRegion, fixedResult);
    }

    @Test
    @DisplayName("Should set search region (replacing all)")
    void testSetSearchRegion() {
        // Given
        searchRegions.addSearchRegions(testRegion1);
        
        // When
        searchRegions.setSearchRegion(testRegion2);
        
        // Then
        List<Region> regions = searchRegions.getRegions(false);
        assertFalse(regions.contains(testRegion1));
        assertTrue(regions.contains(testRegion2));
    }

    @Test
    @DisplayName("Should get all regions without defaults")
    void testGetAllRegions() {
        // When - Empty
        List<Region> emptyRegions = searchRegions.getAllRegions();
        assertTrue(emptyRegions.isEmpty());
        
        // When - With regions
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        List<Region> allRegions = searchRegions.getAllRegions();
        
        // Then
        assertFalse(allRegions.isEmpty());
    }

    @Test
    @DisplayName("Should get regions for search with default")
    void testGetRegionsForSearch() {
        // When - No regions configured
        List<Region> defaultRegions = searchRegions.getRegionsForSearch();
        
        // Then - Should return full screen default
        assertEquals(1, defaultRegions.size());
        assertNotNull(defaultRegions.get(0));
        
        // When - With regions configured
        searchRegions.addSearchRegions(testRegion1);
        List<Region> configuredRegions = searchRegions.getRegionsForSearch();
        
        // Then
        assertFalse(configuredRegions.isEmpty());
        
        // When - With fixed region
        searchRegions.setFixedRegion(fixedRegion);
        List<Region> fixedSearchRegions = searchRegions.getRegionsForSearch();
        
        // Then
        assertEquals(1, fixedSearchRegions.size());
        assertEquals(fixedRegion, fixedSearchRegions.get(0));
    }

    @Test
    @DisplayName("Should check if any region is defined")
    void testIsAnyRegionDefined() {
        // When - No regions
        assertFalse(searchRegions.isAnyRegionDefined());
        
        // When - With normal region
        searchRegions.addSearchRegions(testRegion1);
        assertTrue(searchRegions.isAnyRegionDefined());
        
        // When - With fixed region only
        SearchRegions fixedOnly = new SearchRegions();
        fixedOnly.setFixedRegion(fixedRegion);
        assertTrue(fixedOnly.isAnyRegionDefined());
    }

    @Test
    @DisplayName("Should check if defined based on fixed flag")
    void testIsDefined() {
        // When - No regions, not fixed
        assertFalse(searchRegions.isDefined(false));
        
        // When - With regions, not fixed
        searchRegions.addSearchRegions(testRegion1);
        assertTrue(searchRegions.isDefined(false));
        
        // When - Fixed but no fixed region
        assertFalse(searchRegions.isDefined(true));
        
        // When - Fixed with fixed region
        searchRegions.setFixedRegion(fixedRegion);
        assertTrue(searchRegions.isDefined(true));
    }

    @Test
    @DisplayName("Should handle null regions in add")
    void testAddNullRegions() {
        // When
        searchRegions.addSearchRegions((Region) null);
        
        // Then - Should handle gracefully
        assertTrue(searchRegions.getRegions(false).isEmpty());
        
        // When - Mix of null and valid
        searchRegions.addSearchRegions(testRegion1, null, testRegion2);
        
        // Then
        assertFalse(searchRegions.getRegions(false).isEmpty());
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON")
    void testJacksonSerialization() throws JsonProcessingException {
        // Given
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        searchRegions.setFixedRegion(fixedRegion);
        
        // When - Serialize
        String json = objectMapper.writeValueAsString(searchRegions);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("regions"));
        assertTrue(json.contains("fixedRegion"));
        
        // When - Deserialize
        SearchRegions deserialized = objectMapper.readValue(json, SearchRegions.class);
        
        // Then
        assertNotNull(deserialized);
        assertNotNull(deserialized.getRegions());
        assertEquals(fixedRegion, deserialized.getFixedRegion());
    }

    @TestFactory
    @DisplayName("Fixed region behavior tests")
    Stream<DynamicTest> testFixedRegionBehavior() {
        return Stream.of(
            dynamicTest("Fixed region overrides normal regions when fixed=true", () -> {
                SearchRegions sr = new SearchRegions();
                sr.addSearchRegions(testRegion1, testRegion2);
                sr.setFixedRegion(fixedRegion);
                
                List<Region> regions = sr.getRegions(true);
                assertEquals(1, regions.size());
                assertEquals(fixedRegion, regions.get(0));
            }),
            
            dynamicTest("Normal regions returned when fixed=false", () -> {
                SearchRegions sr = new SearchRegions();
                sr.addSearchRegions(testRegion1, testRegion2);
                sr.setFixedRegion(fixedRegion);
                
                List<Region> regions = sr.getRegions(false);
                assertFalse(regions.contains(fixedRegion));
            }),
            
            dynamicTest("Default region is considered set", () -> {
                SearchRegions sr = new SearchRegions();
                Region defaultRegion = new Region();
                sr.setFixedRegion(defaultRegion);
                // Default Region() creates full-screen region which is defined
                assertTrue(sr.isFixedRegionSet() == defaultRegion.isDefined());
            }),
            
            dynamicTest("Null fixed region not considered set", () -> {
                SearchRegions sr = new SearchRegions();
                sr.setFixedRegion(null);
                assertFalse(sr.isFixedRegionSet());
            })
        );
    }

    @ParameterizedTest
    @CsvSource({
        "true,true",    // fixed=true, has fixed region
        "true,false",   // fixed=true, no fixed region
        "false,true",   // fixed=false, has fixed region
        "false,false"   // fixed=false, no fixed region
    })
    @DisplayName("Should handle various fixed configurations")
    void testVariousFixedConfigurations(boolean fixed, boolean hasFixedRegion) {
        // Given
        searchRegions.addSearchRegions(testRegion1);
        if (hasFixedRegion) {
            searchRegions.setFixedRegion(fixedRegion);
        }
        
        // When
        List<Region> regions = searchRegions.getRegions(fixed);
        
        // Then
        assertNotNull(regions);
        if (fixed && hasFixedRegion) {
            assertEquals(1, regions.size());
            assertEquals(fixedRegion, regions.get(0));
        } else {
            assertFalse(regions.contains(fixedRegion));
        }
    }

    @Test
    @DisplayName("Should handle equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        SearchRegions sr1 = new SearchRegions();
        sr1.addSearchRegions(testRegion1);
        sr1.setFixedRegion(fixedRegion);
        
        SearchRegions sr2 = new SearchRegions();
        sr2.addSearchRegions(testRegion1);
        sr2.setFixedRegion(fixedRegion);
        
        SearchRegions sr3 = new SearchRegions();
        sr3.addSearchRegions(testRegion2);
        
        // Then - Reflexive
        assertEquals(sr1, sr1);
        assertEquals(sr1.hashCode(), sr1.hashCode());
        
        // Symmetric
        assertEquals(sr1, sr2);
        assertEquals(sr2, sr1);
        assertEquals(sr1.hashCode(), sr2.hashCode());
        
        // Different regions
        assertNotEquals(sr1, sr3);
        
        // Null safety
        assertNotEquals(sr1, null);
        assertNotEquals(sr1, "not search regions");
    }

    @Test
    @DisplayName("Should get mutable regions copy")
    void testGetRegionsMutable() {
        // Given
        searchRegions.addSearchRegions(testRegion1, testRegion2);
        
        // When
        List<Region> mutable = searchRegions.getRegionsMutable();
        mutable.clear();
        
        // Then - Original should not be affected (if it's a copy)
        assertFalse(searchRegions.getRegions(false).isEmpty());
    }

    @Test
    @DisplayName("Should handle set regions directly")
    void testSetRegions() {
        // Given
        List<Region> newRegions = List.of(testRegion1, testRegion2);
        
        // When
        searchRegions.setRegions(newRegions);
        
        // Then
        assertEquals(newRegions, searchRegions.getRegions());
    }
}